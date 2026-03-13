package com.optiplant.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.optiplant.backend.dto.CreateInventoryRequest;
import com.optiplant.backend.dto.ProductAvailabilityResponse;
import com.optiplant.backend.dto.TransferStockRequest;
import com.optiplant.backend.dto.UpdateInventoryRequest;
import com.optiplant.backend.entity.Inventory;
import com.optiplant.backend.repository.InventoryRepository;
import com.optiplant.backend.service.InventoryService;

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

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventories() {
        return ResponseEntity.ok(inventoryService.getAllInventories());
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<Inventory>> getInventoriesByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(inventoryService.getInventoriesByBranch(branchId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }

    @PostMapping
    public ResponseEntity<Inventory> createInventory(@RequestBody CreateInventoryRequest request) {
        Inventory inventory = inventoryService.createInventory(request.branchId(), request.productId(), request.quantity());
        return ResponseEntity.ok(inventory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable Long id, @RequestBody UpdateInventoryRequest request) {
        Inventory updated = inventoryService.updateInventory(id, request.quantity());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transferStock(@RequestBody TransferStockRequest request) {
        inventoryService.transferStock(request.sourceBranchId(), request.destBranchId(), request.productId(), request.quantity());
        return ResponseEntity.ok().build();
    }

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
}
