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

/**
 * Servicio para la obtención de métricas y análisis de ventas.
 * Proporciona métodos para consultar resúmenes globales, por sucursal, productos destacados,
 * ventas por sucursal y series temporales de ventas.
 * Utilizado por los controladores para endpoints de analítica y reportes.
 *
 * <p>
 * Principales métodos:
 * <ul>
 *   <li>getGlobalSalesSummary(): Resumen global de ventas.</li>
 *   <li>getSalesSummaryByBranch(): Resumen de ventas por sucursal.</li>
 *   <li>getTopBranchForProduct(Long productId): Sucursal con más ventas de un producto.</li>
 *   <li>getSalesByBranchForProduct(Long productId): Ventas de un producto por sucursal.</li>
 *   <li>getTopProductsByBranch(Long branchId, Integer limit): Productos más vendidos por sucursal.</li>
 *   <li>getSalesByBranchTimeSeries(TimeGranularity granularity, LocalDate fromDate, LocalDate toDate, List<Long> branchIds): Serie temporal de ventas por sucursal.</li>
 * </ul>
 * </p>
 *
 * @author Optiplant Backend
 * @since 2024
 */
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

    /**
     * Obtiene el resumen global de ventas (total vendido y cantidad de ventas).
     *
     * @return Respuesta con monto total y cantidad de ventas.
     */
    public GlobalSalesSummaryResponse getGlobalSalesSummary() {
        Double totalAmount = saleRepository.sumGlobalSalesAmount();
        Long totalSales = saleRepository.countGlobalSales();
        return new GlobalSalesSummaryResponse(totalAmount, totalSales);
    }

    /**
     * Obtiene el resumen de ventas por sucursal.
     *
     * @return Lista de resúmenes por sucursal.
     */
    public List<BranchSalesSummaryResponse> getSalesSummaryByBranch() {
        return saleRepository.getSalesSummaryByBranch();
    }

    /**
     * Obtiene la sucursal con más ventas para un producto.
     *
     * @param productId Identificador del producto.
     * @return Respuesta con datos de la sucursal y ventas.
     * @throws RuntimeException si el producto no existe.
     */
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

    /**
     * Obtiene las ventas de un producto por sucursal.
     *
     * @param productId Identificador del producto.
     * @return Lista de ventas por sucursal.
     * @throws RuntimeException si el producto no existe.
     */
    public List<ProductBranchSalesResponse> getSalesByBranchForProduct(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        return saleItemRepository.getSalesByBranchForProduct(productId);
    }

    /**
     * Obtiene los productos más vendidos por sucursal.
     *
     * @param branchId Identificador de la sucursal.
     * @param limit Límite de productos a consultar.
     * @return Lista de productos más vendidos.
     * @throws RuntimeException si la sucursal no existe.
     */
    public List<BranchTopProductResponse> getTopProductsByBranch(Long branchId, Integer limit) {
        branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

        int resolvedLimit = (limit == null || limit <= 0) ? 10 : Math.min(limit, 100);
        return saleItemRepository.getTopProductsByBranch(branchId, PageRequest.of(0, resolvedLimit));
    }

    /**
     * Obtiene la serie temporal de ventas por sucursal.
     *
     * @param granularity Granularidad de tiempo (día, semana, mes, año).
     * @param fromDate Fecha de inicio.
     * @param toDate Fecha de fin.
     * @param branchIds Lista de sucursales a consultar.
     * @return Respuesta con series temporales de ventas.
     * @throws RuntimeException si el rango de fechas es inválido.
     */
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
