package com.optiplant.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateProductExchangeRequest(
        @NotNull(message = "El ID del detalle de venta es obligatorio")
        Long saleItemId,

        @NotNull(message = "El ID del producto nuevo es obligatorio")
        Long newProductId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor o igual a 1")
        Integer quantity,

        @NotNull(message = "El monto pagado es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El monto pagado no puede ser negativo")
        Double paidAmount,

        String note
) {
}

