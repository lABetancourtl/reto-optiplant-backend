package com.optiplant.backend.dto;

public record TopBranchForProductResponse(
        Long productId,
        String productName,
        Long branchId,
        String branchName,
        Long unitsSold,
        Double totalAmount
) {
}

