package com.optiplant.backend.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record CreateInboundTransferRequest(
        @NotNull(message = "El ID de producto es obligatorio")
        Long productId,

        @NotNull(message = "La cantidad es obligatoria")
        Integer quantity,

        List<Long> destinationBranchIds,

        Boolean allBranches
) {
}

