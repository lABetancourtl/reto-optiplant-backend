package com.optiplant.backend.controller;

import com.optiplant.backend.dto.*;
import com.optiplant.backend.service.SalesAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador para endpoints de análisis y métricas de ventas.
 * Solo accesible por usuarios con rol ADMIN.
 * Permite obtener resúmenes globales, por sucursal, por producto y series temporales de ventas.
 * Facilita la visualización de datos para dashboards y reportes.
 */
@RestController
@RequestMapping("/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class SalesAnalyticsController {

    private final SalesAnalyticsService salesAnalyticsService;

    public SalesAnalyticsController(SalesAnalyticsService salesAnalyticsService) {
        this.salesAnalyticsService = salesAnalyticsService;
    }

    /**
     * Obtiene el resumen global de ventas (total de ventas y monto).
     * @return Resumen global de ventas.
     */
    @GetMapping("/sales/summary")
    public ResponseEntity<GlobalSalesSummaryResponse> getGlobalSalesSummary() {
        return ResponseEntity.ok(salesAnalyticsService.getGlobalSalesSummary());
    }

    /**
     * Obtiene el resumen de ventas por sucursal (ordenado por monto descendente).
     * @return Lista de resúmenes por sucursal.
     */
    @GetMapping("/sales/by-branch")
    public ResponseEntity<List<BranchSalesSummaryResponse>> getSalesByBranch() {
        return ResponseEntity.ok(salesAnalyticsService.getSalesSummaryByBranch());
    }

    /**
     * Obtiene la sucursal donde más se vende un producto específico.
     * @param productId ID del producto.
     * @return Sucursal top para el producto.
     */
    @GetMapping("/products/{productId}/top-branch")
    public ResponseEntity<TopBranchForProductResponse> getTopBranchForProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(salesAnalyticsService.getTopBranchForProduct(productId));
    }

    /**
     * Obtiene el desglose de ventas de un producto por sucursal.
     * @param productId ID del producto.
     * @return Lista de ventas por sucursal para el producto.
     */
    @GetMapping("/products/{productId}/sales-by-branch")
    public ResponseEntity<List<ProductBranchSalesResponse>> getProductSalesByBranch(@PathVariable Long productId) {
        return ResponseEntity.ok(salesAnalyticsService.getSalesByBranchForProduct(productId));
    }

    /**
     * Obtiene los productos más vendidos de una sucursal.
     * @param branchId ID de la sucursal.
     * @param limit Límite de productos (opcional).
     * @return Lista de productos top de la sucursal.
     */
    @GetMapping("/branches/{branchId}/top-products")
    public ResponseEntity<List<BranchTopProductResponse>> getTopProductsByBranch(@PathVariable Long branchId,
                                                                                  @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(salesAnalyticsService.getTopProductsByBranch(branchId, limit));
    }

    /**
     * Obtiene la serie temporal de ventas por sucursal, agrupada por granularidad (año, mes, semana, día).
     * @param granularity Granularidad de tiempo.
     * @param fromDate Fecha inicial (opcional).
     * @param toDate Fecha final (opcional).
     * @param branchIds IDs de sucursales (opcional).
     * @return Serie temporal de ventas por sucursal.
     */
    @GetMapping("/sales/by-branch/time-series")
    public ResponseEntity<SalesByBranchTimeSeriesResponse> getSalesByBranchTimeSeries(
            @RequestParam(required = false) TimeGranularity granularity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) List<Long> branchIds) {
        return ResponseEntity.ok(salesAnalyticsService.getSalesByBranchTimeSeries(granularity, fromDate, toDate, branchIds));
    }
}
