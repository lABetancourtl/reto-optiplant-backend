package com.optiplant.backend.service;

import com.optiplant.backend.dto.CreateProductExchangeRequest;
import com.optiplant.backend.dto.CreateProductReturnRequest;
import com.optiplant.backend.dto.CreateSaleItemRequest;
import com.optiplant.backend.dto.CreateSaleRequest;
import com.optiplant.backend.entity.*;
import com.optiplant.backend.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BranchOperationsService {

    private static final double EPSILON = 0.0001;

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductReturnRepository productReturnRepository;
    private final ProductExchangeRepository productExchangeRepository;
    private final long maxReturnHours;

    public BranchOperationsService(UserRepository userRepository,
                                   ProductRepository productRepository,
                                   InventoryRepository inventoryRepository,
                                   SaleRepository saleRepository,
                                   SaleItemRepository saleItemRepository,
                                   ProductReturnRepository productReturnRepository,
                                   ProductExchangeRepository productExchangeRepository,
                                   @Value("${app.returns.max-hours:72}") long maxReturnHours) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.productReturnRepository = productReturnRepository;
        this.productExchangeRepository = productExchangeRepository;
        this.maxReturnHours = maxReturnHours;
    }

    @Transactional
    public Sale createSale(String username, CreateSaleRequest request) {
        User user = getSucursalUser(username);

        Sale sale = new Sale();
        sale.setBranch(user.getBranch());
        sale.setSoldBy(user);
        sale = saleRepository.save(sale);

        double total = 0.0;
        for (CreateSaleItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            Inventory inventory = inventoryRepository.findByBranchAndProduct(user.getBranch(), product)
                    .orElseThrow(() -> new RuntimeException("No hay inventario para el producto en esta sucursal"));

            if (inventory.getQuantity() < itemRequest.quantity()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + product.getName());
            }

            inventory.setQuantity(inventory.getQuantity() - itemRequest.quantity());
            inventoryRepository.save(inventory);

            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProduct(product);
            saleItem.setQuantity(itemRequest.quantity());
            saleItem.setUnitPrice(product.getPrice());
            saleItem.setSubtotal(product.getPrice() * itemRequest.quantity());
            saleItemRepository.save(saleItem);

            total += saleItem.getSubtotal();
        }

        sale.setTotalAmount(total);
        return saleRepository.save(sale);
    }

    @Transactional
    public ProductReturn createReturn(String username, CreateProductReturnRequest request) {
        User user = getSucursalUser(username);
        SaleItem saleItem = getValidSaleItemForBranch(request.saleItemId(), user.getBranch().getId());

        validateReturnWindow(saleItem.getSale().getCreatedAt());

        int availableQuantity = getAvailableQuantityForReturnOrExchange(saleItem);
        if (request.quantity() > availableQuantity) {
            throw new RuntimeException("La cantidad excede el saldo disponible para devolución/cambio");
        }

        Inventory inventory = getOrCreateInventory(user.getBranch(), saleItem.getProduct());
        inventory.setQuantity(inventory.getQuantity() + request.quantity());
        inventoryRepository.save(inventory);

        ProductReturn productReturn = new ProductReturn();
        productReturn.setSaleItem(saleItem);
        productReturn.setBranch(user.getBranch());
        productReturn.setQuantity(request.quantity());
        productReturn.setReason(request.reason());

        return productReturnRepository.save(productReturn);
    }

    @Transactional
    public ProductExchange createExchange(String username, CreateProductExchangeRequest request) {
        User user = getSucursalUser(username);
        SaleItem saleItem = getValidSaleItemForBranch(request.saleItemId(), user.getBranch().getId());

        validateReturnWindow(saleItem.getSale().getCreatedAt());

        int availableQuantity = getAvailableQuantityForReturnOrExchange(saleItem);
        if (request.quantity() > availableQuantity) {
            throw new RuntimeException("La cantidad excede el saldo disponible para devolución/cambio");
        }

        Product originalProduct = saleItem.getProduct();
        Product newProduct = productRepository.findById(request.newProductId())
                .orElseThrow(() -> new RuntimeException("Producto nuevo no encontrado"));

        if (originalProduct.getId().equals(newProduct.getId())) {
            throw new RuntimeException("El cambio debe hacerse por un producto diferente");
        }

        if (newProduct.getPrice() <= originalProduct.getPrice()) {
            throw new RuntimeException("Solo se permiten cambios por productos de mayor valor");
        }

        double expectedDifference = (newProduct.getPrice() - originalProduct.getPrice()) * request.quantity();
        if (Math.abs(request.paidAmount() - expectedDifference) > EPSILON) {
            throw new RuntimeException("El excedente pagado no coincide con la diferencia esperada");
        }

        Inventory inventoryOriginal = getOrCreateInventory(user.getBranch(), originalProduct);
        inventoryOriginal.setQuantity(inventoryOriginal.getQuantity() + request.quantity());
        inventoryRepository.save(inventoryOriginal);

        Inventory inventoryNew = inventoryRepository.findByBranchAndProduct(user.getBranch(), newProduct)
                .orElseThrow(() -> new RuntimeException("No hay inventario del producto nuevo en esta sucursal"));

        if (inventoryNew.getQuantity() < request.quantity()) {
            throw new RuntimeException("Stock insuficiente del producto nuevo para realizar el cambio");
        }

        inventoryNew.setQuantity(inventoryNew.getQuantity() - request.quantity());
        inventoryRepository.save(inventoryNew);

        ProductExchange exchange = new ProductExchange();
        exchange.setSaleItem(saleItem);
        exchange.setBranch(user.getBranch());
        exchange.setNewProduct(newProduct);
        exchange.setQuantity(request.quantity());
        exchange.setAmountDifference(expectedDifference);
        exchange.setPaidAmount(request.paidAmount());
        exchange.setNote(request.note());

        return productExchangeRepository.save(exchange);
    }

    public List<Sale> getMyBranchSales(String username) {
        User user = getSucursalUser(username);
        return saleRepository.findByBranchIdOrderByCreatedAtDesc(user.getBranch().getId());
    }

    public List<SaleItem> getSaleItemsBySaleIdForMyBranch(String username, Long saleId) {
        User user = getSucursalUser(username);
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        if (!sale.getBranch().getId().equals(user.getBranch().getId())) {
            throw new RuntimeException("No tienes permisos para consultar esta venta");
        }

        return saleItemRepository.findBySaleId(saleId);
    }

    private User getSucursalUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!"SUCURSAL".equals(user.getRole())) {
            throw new RuntimeException("Solo un usuario SUCURSAL puede ejecutar esta operación");
        }

        if (user.getBranch() == null) {
            throw new RuntimeException("El usuario de sucursal no tiene sucursal asignada");
        }

        return user;
    }

    private SaleItem getValidSaleItemForBranch(Long saleItemId, Long branchId) {
        SaleItem saleItem = saleItemRepository.findById(saleItemId)
                .orElseThrow(() -> new RuntimeException("Detalle de venta no encontrado"));

        if (!saleItem.getSale().getBranch().getId().equals(branchId)) {
            throw new RuntimeException("No puedes operar sobre ventas de otra sucursal");
        }

        return saleItem;
    }

    private void validateReturnWindow(LocalDateTime saleDate) {
        if (saleDate.plusHours(maxReturnHours).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El tiempo maximo para cambios/devoluciones ya expiro");
        }
    }

    private int getAvailableQuantityForReturnOrExchange(SaleItem saleItem) {
        int returned = productReturnRepository.sumReturnedQuantityBySaleItemId(saleItem.getId());
        int exchanged = productExchangeRepository.sumExchangedQuantityBySaleItemId(saleItem.getId());
        return saleItem.getQuantity() - returned - exchanged;
    }

    private Inventory getOrCreateInventory(Branch branch, Product product) {
        return inventoryRepository.findByBranchAndProduct(branch, product)
                .orElseGet(() -> {
                    Inventory inventory = new Inventory();
                    inventory.setBranch(branch);
                    inventory.setProduct(product);
                    inventory.setQuantity(0);
                    return inventoryRepository.save(inventory);
                });
    }
}

