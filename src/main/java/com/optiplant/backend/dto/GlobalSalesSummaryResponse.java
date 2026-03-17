package com.optiplant.backend.dto;

public record GlobalSalesSummaryResponse(
        Double totalAmount,
        Long totalSales
) {
}

