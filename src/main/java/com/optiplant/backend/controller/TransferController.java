package com.optiplant.backend.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.optiplant.backend.dto.ApproveRejectTransferRequest;
import com.optiplant.backend.dto.ConfirmReceiptRequest;
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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Transfer>> getAllTransfers() {
        return ResponseEntity.ok(transferService.getAllTransfers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUCURSAL')")
    public ResponseEntity<Transfer> getTransferById(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.getTransferById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<Transfer> createTransferRequest(@RequestBody CreateTransferRequest request, Principal principal) {
        Long userId = userRepository.findByUsername(principal.getName()).get().getId();
        Transfer transfer = transferService.createTransferRequest(request.sourceBranchId(), request.destBranchId(),
                                                                  request.productId(), request.quantity(), userId);
        return ResponseEntity.ok(transfer);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Transfer> updateTransferStatus(@PathVariable Long id, @RequestBody UpdateTransferStatusRequest request) {
        Transfer updated = transferService.updateTransferStatus(id, request.status());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Transfer>> getTransfersByStatus(@PathVariable TransferStatus status) {
        return ResponseEntity.ok(transferService.getTransfersByStatus(status));
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<List<Transfer>> getTransfersByUser(Principal principal) {
        Long userId = userRepository.findByUsername(principal.getName()).get().getId();
        return ResponseEntity.ok(transferService.getTransfersByUser(userId));
    }

    @PutMapping("/{id}/approve-reject")
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<Transfer> approveOrRejectTransfer(@PathVariable Long id, @RequestBody ApproveRejectTransferRequest request, Principal principal) {
        Long userId = userRepository.findByUsername(principal.getName()).get().getId();
        Transfer updated = transferService.approveOrRejectTransfer(id, request.status(), request.justification(), userId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/confirm-receipt")
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<Transfer> confirmReceipt(@RequestBody ConfirmReceiptRequest request, Principal principal) {
        Long userId = userRepository.findByUsername(principal.getName()).get().getId();
        Transfer updated = transferService.confirmReceipt(request.trackingCode(), request.receivedQuantity(), userId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/dest-branch")
    @PreAuthorize("hasRole('SUCURSAL')")
    public ResponseEntity<List<Transfer>> getTransfersByDestBranch(Principal principal) {
        Long userId = userRepository.findByUsername(principal.getName()).get().getId();
        User user = userRepository.findById(userId).get();
        Long branchId = user.getBranch().getId();
        return ResponseEntity.ok(transferService.getTransfersByDestBranch(branchId));
    }
}
