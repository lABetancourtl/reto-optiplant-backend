package com.optiplant.backend.controller;

import com.optiplant.backend.dto.*;
import com.optiplant.backend.service.SalesAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class SalesAnalyticsController {

    private final SalesAnalyticsService salesAnalyticsService;

    public SalesAnalyticsController(SalesAnalyticsService salesAnalyticsService) {
        this.salesAnalyticsService = salesAnalyticsService;
    }

    @GetMapping("/sales/summary")
    public ResponseEntity<GlobalSalesSummaryResponse> getGlobalSalesSummary() {
        return ResponseEntity.ok(salesAnalyticsService.getGlobalSalesSummary());
    }

    @GetMapping("/sales/by-branch")
    public ResponseEntity<List<BranchSalesSummaryResponse>> getSalesByBranch() {
        return ResponseEntity.ok(salesAnalyticsService.getSalesSummaryByBranch());
    }

    @GetMapping("/products/{productId}/top-branch")
    public ResponseEntity<TopBranchForProductResponse> getTopBranchForProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(salesAnalyticsService.getTopBranchForProduct(productId));
    }

    @GetMapping("/products/{productId}/sales-by-branch")
    public ResponseEntity<List<ProductBranchSalesResponse>> getProductSalesByBranch(@PathVariable Long productId) {
        return ResponseEntity.ok(salesAnalyticsService.getSalesByBranchForProduct(productId));
    }

    @GetMapping("/branches/{branchId}/top-products")
    public ResponseEntity<List<BranchTopProductResponse>> getTopProductsByBranch(@PathVariable Long branchId,
                                                                                  @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(salesAnalyticsService.getTopProductsByBranch(branchId, limit));
    }

    @GetMapping("/sales/by-branch/time-series")
    public ResponseEntity<SalesByBranchTimeSeriesResponse> getSalesByBranchTimeSeries(
            @RequestParam(required = false) TimeGranularity granularity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) List<Long> branchIds) {
        return ResponseEntity.ok(salesAnalyticsService.getSalesByBranchTimeSeries(granularity, fromDate, toDate, branchIds));
    }
}

