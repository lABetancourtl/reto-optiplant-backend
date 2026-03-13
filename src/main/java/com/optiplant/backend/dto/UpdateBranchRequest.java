package com.optiplant.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateBranchRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        String address,

        String phone
) {
}
