package com.optiplant.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.optiplant.backend.entity.Branch;
import com.optiplant.backend.repository.BranchRepository;
import com.optiplant.backend.dto.CreateBranchRequest;
import com.optiplant.backend.dto.UpdateBranchRequest;
import com.optiplant.backend.service.InventoryService;

/**
 * Controlador REST para la gestión de sucursales (branches).
 * Permite al ADMIN crear, consultar, actualizar y eliminar sucursales.
 * Al crear una sucursal, se inicializa el inventario de la sucursal con todos los productos en cero.
 * Seguridad: Solo usuarios con rol ADMIN pueden acceder a estos endpoints.
 */
@RestController
@RequestMapping("/branches")
@PreAuthorize("hasRole('ADMIN')")
public class BranchController {

    private final BranchRepository branchRepository;
    private final InventoryService inventoryService;

    public BranchController(BranchRepository branchRepository, InventoryService inventoryService) {
        this.branchRepository = branchRepository;
        this.inventoryService = inventoryService;
    }

    /**
     * GET /branches
     * Devuelve la lista de todas las sucursales.
     */
    @GetMapping
    public ResponseEntity<List<Branch>> getAllBranches() {
        return ResponseEntity.ok(branchRepository.findAll());
    }

    /**
     * GET /branches/{id}
     * Devuelve los datos de una sucursal por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Branch> getBranchById(@PathVariable Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        return ResponseEntity.ok(branch);
    }

    /**
     * POST /branches
     * Crea una nueva sucursal.
     * Body: CreateBranchRequest (name, address, phone)
     * Inicializa inventario en cero para todos los productos.
     */
    @PostMapping
    public ResponseEntity<Branch> createBranch(@RequestBody CreateBranchRequest request) {
        Branch branch = new Branch();
        branch.setName(request.name());
        branch.setAddress(request.address());
        branch.setPhone(request.phone());
        Branch saved = branchRepository.save(branch);
        inventoryService.initializeInventoryForBranchWithZeroStock(saved);
        return ResponseEntity.ok(saved);
    }

    /**
     * PUT /branches/{id}
     * Actualiza los datos de una sucursal.
     * Body: UpdateBranchRequest (name, address, phone)
     */
    @PutMapping("/{id}")
    public ResponseEntity<Branch> updateBranch(@PathVariable Long id, @RequestBody UpdateBranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        branch.setName(request.name());
        branch.setAddress(request.address());
        branch.setPhone(request.phone());
        Branch updated = branchRepository.save(branch);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /branches/{id}
     * Elimina una sucursal por su ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBranch(@PathVariable Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        branchRepository.delete(branch);
        return ResponseEntity.noContent().build();
    }
}
