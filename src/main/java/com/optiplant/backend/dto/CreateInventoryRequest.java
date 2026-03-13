package com.optiplant.backend.dto;

import jakarta.validation.constraints.NotNull;

public record CreateInventoryRequest(
        @NotNull(message = "El ID de sucursal es obligatorio")
        Long branchId,

        @NotNull(message = "El ID de producto es obligatorio")
        Long productId,

        @NotNull(message = "La cantidad es obligatoria")
        Integer quantity
) {
}
