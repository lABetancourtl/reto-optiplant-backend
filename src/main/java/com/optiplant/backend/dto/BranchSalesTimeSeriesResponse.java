package com.optiplant.backend.dto;

import java.util.List;

public record BranchSalesTimeSeriesResponse(
        Long branchId,
        String branchName,
        List<BranchSalesTimePointResponse> points
) {
}

