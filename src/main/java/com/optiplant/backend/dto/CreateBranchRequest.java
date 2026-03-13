package com.optiplant.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBranchRequest(
        @NotBlank(message = "El nombre de la sucursal es obligatorio")
        String name,

        String address,

        String phone
) {
}
