package com.optiplant.backend.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateInventoryRequest(
        @NotNull(message = "La cantidad es obligatoria")
        Integer quantity
) {
}
