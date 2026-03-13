package com.optiplant.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio")
        String userName,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
