package com.optiplant.backend.dto;

public record BranchTopProductResponse(
        Long productId,
        String productName,
        Long unitsSold,
        Double totalAmount
) {
}

