package com.optiplant.backend.dto;

public record BranchSalesSummaryResponse(
        Long branchId,
        String branchName,
        Double totalAmount,
        Long totalSales
) {
}

