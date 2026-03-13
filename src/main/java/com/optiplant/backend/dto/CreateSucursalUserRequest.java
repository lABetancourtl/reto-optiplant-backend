package com.optiplant.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSucursalUserRequest(
        @NotBlank(message = "El usuario es obligatorio")
        String userName,

        @NotBlank(message = "La contraseña es obligatoria")
        String password,

        Long branchId
) {
}
