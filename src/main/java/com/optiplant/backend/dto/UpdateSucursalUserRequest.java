package com.optiplant.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateSucursalUserRequest(
        @NotBlank(message = "El usuario es obligatorio")
        String userName,

        String name,

        String password,

        Long branchId
) {
}
