package com.optiplant.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfirmReceiptRequest(
        @NotBlank(message = "El código de seguimiento es obligatorio")
        String trackingCode,

        @NotNull(message = "La cantidad recibida es obligatoria")
        Integer receivedQuantity
) {
}
