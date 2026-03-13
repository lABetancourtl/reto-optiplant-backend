package com.optiplant.backend.dto;

public record ProductAvailabilityResponse(
        Long branchId,
        String branchName,
        Integer quantity
) {
}
