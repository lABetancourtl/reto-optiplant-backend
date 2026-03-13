package com.optiplant.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optiplant.backend.entity.Branch;
import com.optiplant.backend.entity.Inventory;
import com.optiplant.backend.entity.Product;
import com.optiplant.backend.repository.BranchRepository;
import com.optiplant.backend.repository.InventoryRepository;
import com.optiplant.backend.repository.ProductRepository;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public InventoryService(InventoryRepository inventoryRepository, BranchRepository branchRepository, ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
    }

    public List<Inventory> getAllInventories() {
        return inventoryRepository.findAll();
    }

    public List<Inventory> getInventoriesByBranch(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        return inventoryRepository.findByBranch(branch);
    }

    public Inventory getInventoryById(Long id) {
        return inventoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Inventory not found"));
    }

    public Inventory createInventory(Long branchId, Long productId, Integer quantity) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Inventory existing = inventoryRepository.findByBranchAndProduct(branch, product).orElse(null);
        if (existing != null) {
            throw new RuntimeException("Inventory already exists for this branch and product");
        }

        Inventory inventory = new Inventory();
        inventory.setBranch(branch);
        inventory.setProduct(product);
        inventory.setQuantity(quantity);
        return inventoryRepository.save(inventory);
    }

    public Inventory updateInventory(Long id, Integer quantity) {
        Inventory inventory = getInventoryById(id);
        inventory.setQuantity(quantity);
        return inventoryRepository.save(inventory);
    }

    public void deleteInventory(Long id) {
        Inventory inventory = getInventoryById(id);
        inventoryRepository.delete(inventory);
    }

    @Transactional
    public void transferStock(Long sourceBranchId, Long destBranchId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be positive");
        }

        Branch sourceBranch = branchRepository.findById(sourceBranchId)
                .orElseThrow(() -> new RuntimeException("Source branch not found"));
        Branch destBranch = branchRepository.findById(destBranchId)
                .orElseThrow(() -> new RuntimeException("Destination branch not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Inventory sourceInventory = inventoryRepository.findByBranchAndProduct(sourceBranch, product)
                .orElseThrow(() -> new RuntimeException("Source inventory not found"));
        if (sourceInventory.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock in source branch");
        }

        Inventory destInventory = inventoryRepository.findByBranchAndProduct(destBranch, product)
                .orElseGet(() -> {
                    Inventory newInv = new Inventory();
                    newInv.setBranch(destBranch);
                    newInv.setProduct(product);
                    newInv.setQuantity(0);
                    return inventoryRepository.save(newInv);
                });

        sourceInventory.setQuantity(sourceInventory.getQuantity() - quantity);
        destInventory.setQuantity(destInventory.getQuantity() + quantity);

        inventoryRepository.save(sourceInventory);
        inventoryRepository.save(destInventory);
    }
}
