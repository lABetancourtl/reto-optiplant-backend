package com.optiplant.backend.dto;

import jakarta.validation.constraints.NotNull;

public record CreateBranchConversationRequest(
        Long sourceBranchId,

        @NotNull(message = "La sucursal destino es obligatoria")
        Long destinationBranchId
) {
}

