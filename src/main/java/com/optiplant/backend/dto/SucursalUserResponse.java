package com.optiplant.backend.dto;

public record SucursalUserResponse(
        Long id,
        String userName,
        String name,
        String role,
        Long branchId,
        String branchName
) {
}
