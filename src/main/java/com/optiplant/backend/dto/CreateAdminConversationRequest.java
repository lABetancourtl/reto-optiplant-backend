package com.optiplant.backend.dto;

import jakarta.validation.constraints.NotNull;

public record CreateAdminConversationRequest(
        @NotNull(message = "La sucursal es obligatoria")
        Long branchId
) {
}

