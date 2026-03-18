package com.optiplant.backend.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateBulkTransferRequest(
        @NotNull(message = "El ID de sucursal origen es obligatorio")
        Long sourceBranchId,

        @NotEmpty(message = "Debe indicar al menos una sucursal destino")
        List<Long> destinationBranchIds,

        @NotNull(message = "El ID de producto es obligatorio")
        Long productId,

        @NotNull(message = "La cantidad es obligatoria")
        Integer quantity
) {
}

