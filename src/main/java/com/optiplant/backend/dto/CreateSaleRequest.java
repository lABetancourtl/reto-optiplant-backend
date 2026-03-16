package com.optiplant.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateSaleRequest(
        @NotEmpty(message = "La venta debe incluir al menos un producto")
        List<@Valid CreateSaleItemRequest> items
) {
}

