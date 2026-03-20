package com.optiplant.backend.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optiplant.backend.dto.TransferEventDTO;
import com.optiplant.backend.entity.Branch;
import com.optiplant.backend.entity.Product;
import com.optiplant.backend.entity.Transfer;
import com.optiplant.backend.entity.TransferStatus;
import com.optiplant.backend.entity.User;
import com.optiplant.backend.repository.BranchRepository;
import com.optiplant.backend.repository.ProductRepository;
import com.optiplant.backend.repository.TransferRepository;
import com.optiplant.backend.repository.UserRepository;

/**
 * Servicio para gestionar transferencias de productos entre sucursales y abastecimiento.
 * Incluye lógica para solicitudes, aprobaciones, confirmaciones, notificaciones y actualización de inventario.
 */
@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final SimpMessagingTemplate messagingTemplate;

    public TransferService(TransferRepository transferRepository,
                           BranchRepository branchRepository,
                           ProductRepository productRepository,
                           UserRepository userRepository,
                           InventoryService inventoryService,
                           SimpMessagingTemplate messagingTemplate) {
        this.transferRepository = transferRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.inventoryService = inventoryService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Obtiene todas las transferencias registradas.
     * @return Lista de transferencias
     */
    public List<Transfer> getAllTransfers() {
        return transferRepository.findAll();
    }

    /**
     * Obtiene una transferencia por su ID.
     * @param id ID de la transferencia
     * @return Transferencia encontrada
     */
    public Transfer getTransferById(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));
    }

    /**
     * Crea una solicitud de transferencia entre sucursales.
     * Notifica a la sucursal origen y al admin.
     * @param sourceBranchId ID de sucursal origen
     * @param destBranchId ID de sucursal destino
     * @param productId ID de producto
     * @param quantity Cantidad solicitada
     * @param requestedById ID del usuario solicitante
     * @return Transferencia creada
     */
    @Transactional
    public Transfer createTransferRequest(Long sourceBranchId, Long destBranchId, Long productId, Integer quantity, Long requestedById) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("La cantidad debe ser positiva");
        }

        Branch sourceBranch = branchRepository.findById(sourceBranchId).orElseThrow(() -> new RuntimeException("Source branch not found"));
        Branch destBranch = branchRepository.findById(destBranchId).orElseThrow(() -> new RuntimeException("Destination branch not found"));
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        User requestedBy = userRepository.findById(requestedById).orElseThrow(() -> new RuntimeException("User not found"));

        if (sourceBranchId.equals(destBranchId)) {
            throw new RuntimeException("Las sucursales de origen y de destino no pueden ser las mismas");
        }

        Transfer transfer = new Transfer();
        transfer.setStatus(TransferStatus.REQUESTED);
        transfer.setSourceBranch(sourceBranch);
        transfer.setDestBranch(destBranch);
        transfer.setProduct(product);
        transfer.setQuantity(quantity);
        transfer.setRequestedBy(requestedBy);

        Transfer saved = transferRepository.save(transfer);

        // Notificar a sucursal origen que le hicieron una solicitud
        messagingTemplate.convertAndSend(
                "/topic/transfers/branch/" + sourceBranchId,
                buildEvent(saved, "REQUESTED")
        );
        // Notificar al admin
        messagingTemplate.convertAndSend("/topic/transfers/admin", buildEvent(saved, "REQUESTED"));

        return saved;
    }

    /**
     * Crea transferencias de ingreso de productos (abastecimiento) a una o varias sucursales.
     * Notifica a cada sucursal destino y al admin.
     * @param productId ID de producto
     * @param quantity Cantidad a ingresar
     * @param destinationBranchIds Lista de sucursales destino
     * @param allBranches Si es true, envía a todas las sucursales
     * @param requestedById ID del usuario admin
     * @return Lista de transferencias creadas
     */
    @Transactional
    public List<Transfer> createInboundTransfers(Long productId,
                                                 Integer quantity,
                                                 List<Long> destinationBranchIds,
                                                 Boolean allBranches,
                                                 Long requestedById) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("La cantidad debe ser positiva.");
        }

        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        User requestedBy = userRepository.findById(requestedById).orElseThrow(() -> new RuntimeException("User not found"));

        boolean sendToAll = Boolean.TRUE.equals(allBranches);
        Set<Long> destinationIds;
        if (sendToAll) {
            destinationIds = branchRepository.findAll().stream()
                    .map(Branch::getId)
                    .collect(Collectors.toSet());
        } else {
            if (destinationBranchIds == null || destinationBranchIds.isEmpty()) {
                throw new RuntimeException("Debe indicar al menos una sucursal destino");
            }
            destinationIds = destinationBranchIds.stream().collect(Collectors.toSet());
        }

        if (destinationIds.isEmpty()) {
            throw new RuntimeException("No se encontraron sucursales destino para el ingreso");
        }

        List<Branch> destinationBranches = branchRepository.findAllById(destinationIds);
        if (destinationBranches.size() != destinationIds.size()) {
            throw new RuntimeException("Una o varias sucursales destino no existen");
        }

        List<Transfer> savedTransfers = destinationBranches.stream().map(destBranch -> {
            Transfer transfer = new Transfer();
            transfer.setStatus(TransferStatus.APPROVED);
            transfer.setSourceBranch(null);
            transfer.setDestBranch(destBranch);
            transfer.setProduct(product);
            transfer.setQuantity(quantity);
            transfer.setRequestedBy(requestedBy);
            transfer.setTrackingCode(UUID.randomUUID().toString());

            return transferRepository.save(transfer);
        }).toList();

        for (Transfer transfer : savedTransfers) {
            messagingTemplate.convertAndSend(
                    "/topic/transfers/branch/" + transfer.getDestBranch().getId(),
                    buildEvent(transfer, "INBOUND_CREATED")
            );
            messagingTemplate.convertAndSend("/topic/transfers/admin", buildEvent(transfer, "INBOUND_CREATED"));
        }

        return savedTransfers;
    }

    /**
     * Aprueba o rechaza una transferencia entre sucursales.
     * Solo la sucursal origen puede aprobar/rechazar.
     * Notifica a la sucursal destino y al admin.
     * @param id ID de transferencia
     * @param status Estado nuevo (APPROVED/REJECTED)
     * @param justification Justificación en caso de rechazo
     * @param userId ID del usuario que aprueba/rechaza
     * @return Transferencia actualizada
     */
    @Transactional
    public Transfer approveOrRejectTransfer(Long id, TransferStatus status,
                                            String justification, Long userId) {
        Transfer transfer = getTransferById(id);

        if (transfer.getSourceBranch() == null) {
            throw new RuntimeException("Este ingreso no requiere aprobacion o rechazo de sucursal origen");
        }

        if (transfer.getStatus() != TransferStatus.REQUESTED) {
            throw new RuntimeException("Transfer is not in REQUESTED status");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

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
            transfer.setTrackingCode(UUID.randomUUID().toString());
        }

        Transfer updated = transferRepository.save(transfer);
        String eventType = status == TransferStatus.APPROVED ? "APPROVED" : "REJECTED";

        // Notificar a sucursal destino (A) con el resultado
        messagingTemplate.convertAndSend(
                "/topic/transfers/branch/" + transfer.getDestBranch().getId(),
                buildEvent(updated, eventType)
        );
        // Notificar al admin
        messagingTemplate.convertAndSend("/topic/transfers/admin", buildEvent(updated, eventType));

        return updated;
    }

    /**
     * Confirma la recepción de productos de una transferencia.
     * Solo la sucursal destino puede confirmar.
     * Actualiza inventario y notifica a sucursales y admin.
     * @param trackingCode Código de seguimiento
     * @param receivedQuantity Cantidad recibida
     * @param userId ID del usuario que confirma
     * @return Transferencia actualizada
     */
    @Transactional
    public Transfer confirmReceipt(String trackingCode, Integer receivedQuantity, Long userId) {
        Transfer transfer = transferRepository.findByTrackingCode(trackingCode)
                .orElseThrow(() -> new RuntimeException("Transfer with tracking code not found"));

        if (transfer.getStatus() != TransferStatus.APPROVED && transfer.getStatus() != TransferStatus.SENT) {
            throw new RuntimeException("Transfer is not in a receivable status");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

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

        // Mover inventario según el tipo de operación.
        if (transfer.getSourceBranch() != null) {
            inventoryService.transferStock(
                    transfer.getSourceBranch().getId(),
                    transfer.getDestBranch().getId(),
                    transfer.getProduct().getId(),
                    receivedQuantity
            );
        } else {
            inventoryService.increaseStock(
                    transfer.getDestBranch().getId(),
                    transfer.getProduct().getId(),
                    receivedQuantity
            );
        }

        Transfer updated = transferRepository.save(transfer);

        // Notificar a ambas sucursales y al admin que el traslado fue completado
        messagingTemplate.convertAndSend(
                "/topic/transfers/branch/" + transfer.getDestBranch().getId(),
                buildEvent(updated, "RECEIVED")
        );
        if (transfer.getSourceBranch() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/transfers/branch/" + transfer.getSourceBranch().getId(),
                    buildEvent(updated, "RECEIVED")
            );
        }
        messagingTemplate.convertAndSend(
                "/topic/transfers/admin",
                buildEvent(updated, "RECEIVED")
        );

        return updated;
    }

    /**
     * Actualiza el estado de una transferencia.
     * Solo ADMIN puede cambiar el estado.
     * @param id ID de transferencia
     * @param newStatus Estado nuevo
     * @return Transferencia actualizada
     */
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

    /**
     * Obtiene transferencias donde la sucursal es origen.
     * @param branchId ID de sucursal
     * @return Lista de transferencias
     */
    public List<Transfer> getTransfersBySourceBranch(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        return transferRepository.findBySourceBranch(branch);
    }

    /**
     * Obtiene transferencias donde la sucursal es destino.
     * @param branchId ID de sucursal
     * @return Lista de transferencias
     */
    public List<Transfer> getTransfersByDestBranch(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        return transferRepository.findByDestBranch(branch);
    }

    /**
     * Obtiene transferencias por estado.
     * @param status Estado de transferencia
     * @return Lista de transferencias
     */
    public List<Transfer> getTransfersByStatus(TransferStatus status) {
        return transferRepository.findByStatus(status);
    }

    /**
     * Obtiene transferencias solicitadas por un usuario.
     * @param userId ID de usuario
     * @return Lista de transferencias
     */
    public List<Transfer> getTransfersByUser(Long userId) {
        return transferRepository.findByRequestedById(userId);
    }

    /**
     * Construye un evento de transferencia para notificaciones por WebSocket.
     * @param t Transferencia
     * @param type Tipo de evento
     * @return DTO de evento
     */
    private TransferEventDTO buildEvent(Transfer t, String type) {
        return new TransferEventDTO(
                t.getId(),
                type,
                t.getSourceBranch() != null ? t.getSourceBranch().getId() : null,
                t.getSourceBranch() != null ? t.getSourceBranch().getName() : null,
                t.getDestBranch().getId(),
                t.getDestBranch().getName(),
                t.getProduct().getName(),
                t.getQuantity(),
                t.getTrackingCode(),
                t.getJustification()
        );
    }
}

