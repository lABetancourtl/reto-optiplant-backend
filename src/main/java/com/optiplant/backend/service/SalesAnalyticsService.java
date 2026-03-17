package com.optiplant.backend.service;

import com.optiplant.backend.dto.*;
import com.optiplant.backend.entity.Product;
import com.optiplant.backend.repository.BranchRepository;
import com.optiplant.backend.repository.ProductRepository;
import com.optiplant.backend.repository.SaleItemRepository;
import com.optiplant.backend.repository.SaleRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SalesAnalyticsService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;

    public SalesAnalyticsService(SaleRepository saleRepository,
                                 SaleItemRepository saleItemRepository,
                                 ProductRepository productRepository,
                                 BranchRepository branchRepository) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.productRepository = productRepository;
        this.branchRepository = branchRepository;
    }

    public GlobalSalesSummaryResponse getGlobalSalesSummary() {
        Double totalAmount = saleRepository.sumGlobalSalesAmount();
        Long totalSales = saleRepository.countGlobalSales();
        return new GlobalSalesSummaryResponse(totalAmount, totalSales);
    }

    public List<BranchSalesSummaryResponse> getSalesSummaryByBranch() {
        return saleRepository.getSalesSummaryByBranch();
    }

    public TopBranchForProductResponse getTopBranchForProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        List<ProductBranchSalesResponse> branchSales = saleItemRepository.getSalesByBranchForProduct(productId);
        if (branchSales.isEmpty()) {
            return new TopBranchForProductResponse(product.getId(), product.getName(), null, null, 0L, 0.0);
        }

        ProductBranchSalesResponse topBranch = branchSales.get(0);
        return new TopBranchForProductResponse(
                product.getId(),
                product.getName(),
                topBranch.branchId(),
                topBranch.branchName(),
                topBranch.unitsSold(),
                topBranch.totalAmount()
        );
    }

    public List<ProductBranchSalesResponse> getSalesByBranchForProduct(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        return saleItemRepository.getSalesByBranchForProduct(productId);
    }

    public List<BranchTopProductResponse> getTopProductsByBranch(Long branchId, Integer limit) {
        branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

        int resolvedLimit = (limit == null || limit <= 0) ? 10 : Math.min(limit, 100);
        return saleItemRepository.getTopProductsByBranch(branchId, PageRequest.of(0, resolvedLimit));
    }

    public SalesByBranchTimeSeriesResponse getSalesByBranchTimeSeries(TimeGranularity granularity,
                                                                       LocalDate fromDate,
                                                                       LocalDate toDate,
                                                                       List<Long> branchIds) {
        TimeGranularity resolvedGranularity = granularity == null ? TimeGranularity.MONTH : granularity;
        LocalDateTime resolvedToDate = (toDate == null ? LocalDate.now().plusDays(1) : toDate.plusDays(1)).atStartOfDay();

        LocalDate defaultFromDate = switch (resolvedGranularity) {
            case DAY -> resolvedToDate.toLocalDate().minusDays(30);
            case WEEK -> resolvedToDate.toLocalDate().minusWeeks(12);
            case MONTH -> resolvedToDate.toLocalDate().minusMonths(12);
            case YEAR -> resolvedToDate.toLocalDate().minusYears(5);
        };
        LocalDateTime resolvedFromDate = (fromDate == null ? defaultFromDate : fromDate).atStartOfDay();

        if (!resolvedFromDate.isBefore(resolvedToDate)) {
            throw new RuntimeException("El rango de fechas es invalido");
        }

        List<Long> safeBranchIds = branchIds == null
                ? List.of()
                : branchIds.stream().filter(id -> id != null && id > 0).distinct().toList();

        List<SaleRepository.BranchSalesTimeSeriesRow> rows = safeBranchIds.isEmpty()
                ? saleRepository.getSalesByBranchTimeSeries(resolvedFromDate, resolvedToDate, resolvedGranularity.toDateTruncUnit())
                : saleRepository.getSalesByBranchTimeSeriesForBranches(resolvedFromDate, resolvedToDate, resolvedGranularity.toDateTruncUnit(), safeBranchIds);

        Map<Long, BranchSalesTimeSeriesResponse> branchesMap = new LinkedHashMap<>();
        for (SaleRepository.BranchSalesTimeSeriesRow row : rows) {
            BranchSalesTimeSeriesResponse series = branchesMap.get(row.getBranchId());
            if (series == null) {
                series = new BranchSalesTimeSeriesResponse(row.getBranchId(), row.getBranchName(), new ArrayList<>());
                branchesMap.put(row.getBranchId(), series);
            }

            series.points().add(new BranchSalesTimePointResponse(
                    row.getBucketStart(),
                    row.getTotalAmount(),
                    row.getTotalSales()
            ));
        }

        return new SalesByBranchTimeSeriesResponse(
                resolvedGranularity,
                resolvedFromDate,
                resolvedToDate,
                new ArrayList<>(branchesMap.values())
        );
    }
}
