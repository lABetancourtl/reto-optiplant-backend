package com.optiplant.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optiplant.backend.entity.Branch;
import com.optiplant.backend.entity.Product;
import com.optiplant.backend.entity.Transfer;
import com.optiplant.backend.entity.TransferStatus;
import com.optiplant.backend.entity.User;
import com.optiplant.backend.repository.BranchRepository;
import com.optiplant.backend.repository.ProductRepository;
import com.optiplant.backend.repository.TransferRepository;
import com.optiplant.backend.repository.UserRepository;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;

    public TransferService(TransferRepository transferRepository, BranchRepository branchRepository,
                           ProductRepository productRepository, UserRepository userRepository,
                           InventoryService inventoryService) {
        this.transferRepository = transferRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.inventoryService = inventoryService;
    }

    public List<Transfer> getAllTransfers() {
        return transferRepository.findAll();
    }

    public Transfer getTransferById(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));
    }

    public Transfer createTransferRequest(Long sourceBranchId, Long destBranchId, Long productId,
                                          Integer quantity, Long requestedById) {
        Branch sourceBranch = branchRepository.findById(sourceBranchId)
                .orElseThrow(() -> new RuntimeException("Source branch not found"));
        Branch destBranch = branchRepository.findById(destBranchId)
                .orElseThrow(() -> new RuntimeException("Destination branch not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        User requestedBy = userRepository.findById(requestedById)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (sourceBranchId.equals(destBranchId)) {
            throw new RuntimeException("Source and destination branches cannot be the same");
        }

        Transfer transfer = new Transfer();
        transfer.setStatus(TransferStatus.REQUESTED);
        transfer.setSourceBranch(sourceBranch);
        transfer.setDestBranch(destBranch);
        transfer.setProduct(product);
        transfer.setQuantity(quantity);
        transfer.setRequestedBy(requestedBy);

        return transferRepository.save(transfer);
    }

    @Transactional
    public Transfer updateTransferStatus(Long id, TransferStatus newStatus) {
        Transfer transfer = getTransferById(id);

        if (transfer.getStatus() == TransferStatus.RECEIVED) {
            throw new RuntimeException("Cannot update a received transfer");
        }

        transfer.setStatus(newStatus);

        if (newStatus == TransferStatus.APPROVED) {
            transfer.setTrackingCode(UUID.randomUUID().toString());
        }

        return transferRepository.save(transfer);
    }

    public List<Transfer> getTransfersBySourceBranch(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        return transferRepository.findBySourceBranch(branch);
    }

    public List<Transfer> getTransfersByDestBranch(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        return transferRepository.findByDestBranch(branch);
    }

    public List<Transfer> getTransfersByStatus(TransferStatus status) {
        return transferRepository.findByStatus(status);
    }

    public List<Transfer> getTransfersByUser(Long userId) {
        return transferRepository.findByRequestedById(userId);
    }

    @Transactional
    public Transfer approveOrRejectTransfer(Long id, TransferStatus status, String justification, Long userId) {
        Transfer transfer = getTransferById(id);

        if (transfer.getStatus() != TransferStatus.REQUESTED) {
            throw new RuntimeException("Transfer is not in REQUESTED status");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // La sucursal ORIGEN (B) es quien decide si acepta o rechaza enviar el producto
        if (!"SUCURSAL".equals(user.getRole()) ||
                !transfer.getSourceBranch().getId().equals(user.getBranch().getId())) {
            throw new RuntimeException("Only the source branch user can approve or reject this transfer");
        }

        transfer.setStatus(status);

        if (status == TransferStatus.REJECTED) {
            if (justification == null || justification.trim().isEmpty()) {
                throw new RuntimeException("Justification is required for rejection");
            }
            transfer.setJustification(justification);
        } else if (status == TransferStatus.APPROVED) {
            // Generar código guía de la transportadora
            transfer.setTrackingCode(UUID.randomUUID().toString());
        }

        return transferRepository.save(transfer);
    }

    @Transactional
    public Transfer confirmReceipt(String trackingCode, Integer receivedQuantity, Long userId) {
        Transfer transfer = transferRepository.findAll().stream()
                .filter(t -> trackingCode.equals(t.getTrackingCode()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Transfer with tracking code not found"));

        if (transfer.getStatus() != TransferStatus.APPROVED) {
            throw new RuntimeException("Transfer is not in APPROVED status");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // La sucursal DESTINO (A) es quien confirma que recibió la mercancía
        if (!"SUCURSAL".equals(user.getRole()) ||
                !transfer.getDestBranch().getId().equals(user.getBranch().getId())) {
            throw new RuntimeException("Only the destination branch user can confirm receipt");
        }

        if (receivedQuantity > transfer.getQuantity()) {
            throw new RuntimeException("Received quantity cannot exceed requested quantity");
        }

        if (receivedQuantity < 1) {
            throw new RuntimeException("Received quantity must be at least 1");
        }

        transfer.setStatus(TransferStatus.RECEIVED);

        // Mover inventario: descontar de sucursal B (origen) y agregar a sucursal A (destino)
        inventoryService.transferStock(
                transfer.getSourceBranch().getId(),
                transfer.getDestBranch().getId(),
                transfer.getProduct().getId(),
                receivedQuantity
        );

        return transferRepository.save(transfer);
    }
}