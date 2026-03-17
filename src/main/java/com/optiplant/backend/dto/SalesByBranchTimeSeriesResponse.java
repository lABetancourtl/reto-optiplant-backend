package com.optiplant.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SalesByBranchTimeSeriesResponse(
		TimeGranularity granularity,
		LocalDateTime fromDate,
		LocalDateTime toDate,
		List<BranchSalesTimeSeriesResponse> branches
) {
}

