package com.optiplant.backend.controller;

import java.security.Principal;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.optiplant.backend.dto.ApproveRejectTransferRequest;
import com.optiplant.backend.dto.ConfirmReceiptRequest;
import com.optiplant.backend.dto.CreateInboundTransferRequest;
import com.optiplant.backend.dto.CreateTransferRequest;
import com.optiplant.backend.dto.UpdateTransferStatusRequest;
import com.optiplant.backend.entity.Transfer;
import com.optiplant.backend.entity.TransferStatus;
import com.optiplant.backend.entity.User;
import com.optiplant.backend.repository.UserRepository;
import com.optiplant.backend.service.TransferService;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final TransferService transferService;
    private final UserRepository userRepository;

    public TransferController(TransferService transferService, UserRepository userRepository) {
        this.transferService = transferService;
        this.userRepository = userRepository;
    }

    /**
     * Obtiene todas las transferencias.
     * Solo accesible por ADMIN.
     * Endpoint: GET /transfers
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Transfer>> getAllTransfers() {
        return ResponseEntity.ok(transferService.getAllTransfers());
    }

    /**
     * Obtiene una transferencia por su ID.
     * Accesible por ADMIN y SUCURSAL.
     * Endpoint: GET /transfers/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUCURSAL')")
    public ResponseEntity<Transfer> getTransferById(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.getTransferById(id));
    }

    /**
     * Crea una solicitud de transferencia entre sucursales.
     * Accesible por ADMIN y SUCURSAL.
     * Endpoint: POST /transfers
     * Body: CreateTransferRequest
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUCURSAL')")
    public ResponseEntity<Transfer> createTransferRequest(@Valid @RequestBody CreateTransferRequest request, Principal principal) {
        Long userId = userRepository.findByUsername(principal.getName()).get().getId();
        Transfer transfer = transferService.createTransferRequest(request.sourceBranchId(), request.destBranchId(),
                request.productId(), request.quantity(), userId);
        return ResponseEntity.ok(transfer);
    }

    /**
     * Crea transferencias de ingreso de productos (abastecimiento) a sucursales.
     * Solo accesible por ADMIN.
     * Endpoint: POST /transfers/inbound
     * Body: CreateInboundTransferRequest
     */
    @PostMapping("/inbound")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Transfer>> createInboundTransfers(@Valid @RequestBody CreateInboundTransferRequest request, Principal principal) {
        Long userId = userRepository.findByUsername(principal.getName()).orElseThrow().getId();
        List<Transfer> transfers = transferService.createInboundTransfers(
                request.productId(),
                request.quantity(),
                request.destinationBranchIds(),
                request.allBranches(),
                userId
        );
        return ResponseEntity.ok(transfers);
    }

    /**
     * Actualiza el estado de una transferencia.
     * Solo accesible por ADMIN.
     * Endpoint: PUT /transfers/{id}/status
     * Body: UpdateTransferStatusRequest
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Transfer> updateTransferStatus(@PathVariable Long id, @RequestBody UpdateTransferStatusRequest request) {
        Transfer updated = transferService.updateTransferStatus(id, request.status());
        return ResponseEntity.ok(updated);
    }

    /**
     * Obtiene transferencias por estado.
     * Solo accesible por ADMIN.
     * Endpoint: GET /transfers/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Transfer>> getTransfersByStatus(@PathVariable TransferStatus status) {
        return ResponseEntity.ok(transferService.getTransfersByStatus(status));
    }

    /**
     * Obtiene transferencias asociadas al usuario logueado (solo SUCURSAL).
     * Endpoint: GET /transfers/user
     */
    @GetMapping("/user")
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<List<Transfer>> getTransfersByUser(Principal principal) {
        Long userId = userRepository.findByUsername(principal.getName()).get().getId();
        return ResponseEntity.ok(transferService.getTransfersByUser(userId));
    }

    /**
     * Aprueba o rechaza una transferencia (solo SUCURSAL).
     * Endpoint: PUT /transfers/{id}/approve-reject
     * Body: ApproveRejectTransferRequest
     */
    @PutMapping("/{id}/approve-reject")
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<Transfer> approveOrRejectTransfer(@PathVariable Long id, @RequestBody ApproveRejectTransferRequest request, Principal principal) {
        Long userId = userRepository.findByUsername(principal.getName()).get().getId();
        Transfer updated = transferService.approveOrRejectTransfer(id, request.status(), request.justification(), userId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Confirma la recepción de productos de una transferencia (solo SUCURSAL).
     * Endpoint: POST /transfers/confirm-receipt
     * Body: ConfirmReceiptRequest
     */
    @PostMapping("/confirm-receipt")
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<Transfer> confirmReceipt(@RequestBody ConfirmReceiptRequest request, Principal principal) {
        Long userId = userRepository.findByUsername(principal.getName()).get().getId();
        Transfer updated = transferService.confirmReceipt(request.trackingCode(), request.receivedQuantity(), userId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Obtiene transferencias donde la sucursal logueada es destino.
     * Endpoint: GET /transfers/dest-branch
     */
    @GetMapping("/dest-branch")
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<List<Transfer>> getTransfersByDestBranch(Principal principal) {
        Long userId = userRepository.findByUsername(principal.getName()).get().getId();
        User user = userRepository.findById(userId).get();
        Long branchId = user.getBranch().getId();
        return ResponseEntity.ok(transferService.getTransfersByDestBranch(branchId));
    }

    /**
     * Obtiene transferencias donde la sucursal logueada es origen.
     * Endpoint: GET /transfers/source-branch
     */
    @GetMapping("/source-branch")
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<List<Transfer>> getTransfersBySourceBranch(Principal principal) {
        Long userId = userRepository.findByUsername(principal.getName()).get().getId();
        User user = userRepository.findById(userId).get();
        Long branchId = user.getBranch().getId();
        return ResponseEntity.ok(transferService.getTransfersBySourceBranch(branchId));
    }
}