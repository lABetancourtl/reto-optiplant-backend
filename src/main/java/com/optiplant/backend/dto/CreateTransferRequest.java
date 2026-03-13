package com.optiplant.backend.dto;

import jakarta.validation.constraints.NotNull;

public record CreateTransferRequest(
        @NotNull(message = "El ID de sucursal origen es obligatorio")
        Long sourceBranchId,

        @NotNull(message = "El ID de sucursal destino es obligatorio")
        Long destBranchId,

        @NotNull(message = "El ID de producto es obligatorio")
        Long productId,

        @NotNull(message = "La cantidad es obligatoria")
        Integer quantity
) {
}
