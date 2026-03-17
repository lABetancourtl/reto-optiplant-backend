package com.optiplant.backend.dto;

import java.time.LocalDateTime;

public record BranchSalesTimePointResponse(
        LocalDateTime bucketStart,
        Double totalAmount,
        Long totalSales
) {
}

