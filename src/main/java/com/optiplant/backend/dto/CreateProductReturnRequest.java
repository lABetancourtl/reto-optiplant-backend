package com.optiplant.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateProductReturnRequest(
        @NotNull(message = "El ID del detalle de venta es obligatorio")
        Long saleItemId,

        @NotNull(message = "La cantidad a devolver es obligatoria")
        @Min(value = 1, message = "La cantidad a devolver debe ser mayor o igual a 1")
        Integer quantity,

        String reason
) {
}

