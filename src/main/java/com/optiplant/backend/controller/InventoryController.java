package com.optiplant.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.optiplant.backend.dto.CreateInventoryRequest;
import com.optiplant.backend.dto.ProductAvailabilityResponse;
import com.optiplant.backend.dto.TransferStockRequest;
import com.optiplant.backend.dto.UpdateInventoryRequest;
import com.optiplant.backend.entity.Inventory;
import com.optiplant.backend.repository.InventoryRepository;
import com.optiplant.backend.service.InventoryService;

/**
 * Controlador para la gestión de inventarios por sucursal.
 * Solo accesible por usuarios con rol ADMIN, excepto algunos endpoints para SUCURSAL.
 * Permite consultar, crear, actualizar, eliminar inventarios y transferir stock entre sucursales.
 * También permite consultar disponibilidad de productos y el inventario propio de la sucursal.
 */
@RestController
@RequestMapping("/inventories")
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryRepository inventoryRepository;

    public InventoryController(InventoryService inventoryService, InventoryRepository inventoryRepository) {
        this.inventoryService = inventoryService;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Obtiene todos los inventarios registrados en el sistema.
     * Solo ADMIN.
     * @return Lista de inventarios.
     */
    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventories() {
        return ResponseEntity.ok(inventoryService.getAllInventories());
    }

    /**
     * Obtiene el inventario de una sucursal específica.
     * Solo ADMIN.
     * @param branchId ID de la sucursal.
     * @return Lista de inventarios de la sucursal.
     */
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<Inventory>> getInventoriesByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(inventoryService.getInventoriesByBranch(branchId));
    }

    /**
     * Obtiene un inventario por su ID.
     * Solo ADMIN.
     * @param id ID del inventario.
     * @return Inventario encontrado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }

    /**
     * Crea un nuevo registro de inventario para una sucursal y producto.
     * Solo ADMIN.
     * @param request Datos para crear el inventario.
     * @return Inventario creado.
     */
    @PostMapping
    public ResponseEntity<Inventory> createInventory(@RequestBody CreateInventoryRequest request) {
        Inventory inventory = inventoryService.createInventory(request.branchId(), request.productId(), request.quantity());
        return ResponseEntity.ok(inventory);
    }

    /**
     * Actualiza la cantidad de un inventario existente.
     * Solo ADMIN.
     * @param id ID del inventario.
     * @param request Nueva cantidad.
     * @return Inventario actualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable Long id, @RequestBody UpdateInventoryRequest request) {
        Inventory updated = inventoryService.updateInventory(id, request.quantity());
        return ResponseEntity.ok(updated);
    }

    /**
     * Elimina un inventario por su ID.
     * Solo ADMIN.
     * @param id ID del inventario.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Transfiere stock de un producto entre sucursales.
     * Solo ADMIN.
     * @param request Datos de la transferencia.
     */
    @PostMapping("/transfer")
    public ResponseEntity<Void> transferStock(@RequestBody TransferStockRequest request) {
        inventoryService.transferStock(request.sourceBranchId(), request.destBranchId(), request.productId(), request.quantity());
        return ResponseEntity.ok().build();
    }

    /**
     * Consulta la disponibilidad de un producto en todas las sucursales.
     * Solo SUCURSAL.
     * @param productId ID del producto.
     * @return Lista de sucursales y cantidades disponibles.
     */
    @GetMapping("/product/{productId}/availability")
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<List<ProductAvailabilityResponse>> getProductAvailability(@PathVariable Long productId) {
        List<Inventory> inventories = inventoryRepository.findAll().stream()
                .filter(inv -> inv.getProduct().getId().equals(productId))
                .toList();
        List<ProductAvailabilityResponse> responses = inventories.stream()
                .map(inv -> new ProductAvailabilityResponse(inv.getBranch().getId(), inv.getBranch().getName(), inv.getQuantity()))
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Obtiene el inventario de la sucursal asociada al usuario autenticado.
     * Solo SUCURSAL.
     * @param authentication Información de autenticación.
     * @return Lista de inventarios de la sucursal.
     */
    @GetMapping("/my-branch")
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<List<Inventory>> getMyBranchInventory(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(inventoryService.getInventoriesByUsername(username));
    }
}
