package com.optiplant.backend.dto;

public record ProductBranchSalesResponse(
        Long branchId,
        String branchName,
        Long unitsSold,
        Double totalAmount
) {
}

