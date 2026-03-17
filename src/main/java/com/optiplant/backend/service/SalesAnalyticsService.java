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
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

        LocalDate resolvedFromDate = fromDate == null ? LocalDate.now().minusMonths(11).withDayOfMonth(1) : fromDate;
        LocalDate resolvedToDate = toDate == null ? LocalDate.now() : toDate;

        if (resolvedFromDate.isAfter(resolvedToDate)) {
            throw new RuntimeException("La fecha inicial no puede ser mayor que la fecha final");
        }

        LocalDateTime fromDateTime = resolvedFromDate.atStartOfDay();
        LocalDateTime toDateTime = resolvedToDate.plusDays(1).atStartOfDay();

        List<SaleRepository.BranchSalesTimeSeriesRow> rows;
        if (branchIds == null || branchIds.isEmpty()) {
            rows = saleRepository.getSalesByBranchTimeSeries(fromDateTime, toDateTime, resolvedGranularity.toDateTruncUnit());
        } else {
            rows = saleRepository.getSalesByBranchTimeSeriesForBranches(
                    fromDateTime,
                    toDateTime,
                    resolvedGranularity.toDateTruncUnit(),
                    branchIds
            );
        }

        Map<Long, List<BranchSalesTimePointResponse>> pointsByBranch = new LinkedHashMap<>();
        Map<Long, String> branchNames = new LinkedHashMap<>();

        for (SaleRepository.BranchSalesTimeSeriesRow row : rows) {
            branchNames.putIfAbsent(row.getBranchId(), row.getBranchName());
            pointsByBranch
                    .computeIfAbsent(row.getBranchId(), key -> new ArrayList<>())
                    .add(new BranchSalesTimePointResponse(row.getBucketStart(), row.getTotalAmount(), row.getTotalSales()));
        }

        List<BranchSalesTimeSeriesResponse> series = pointsByBranch.entrySet().stream()
                .map(entry -> new BranchSalesTimeSeriesResponse(entry.getKey(), branchNames.get(entry.getKey()), entry.getValue()))
                .toList();

        return new SalesByBranchTimeSeriesResponse(resolvedGranularity, fromDateTime, toDateTime, series);
    }
}

