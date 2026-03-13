package com.optiplant.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "El usuario es obligatorio")
        String userName,

        @NotBlank(message = "La contraseña es obligatoria")
        String password,

        String role,

        Long branchId
) {
}
