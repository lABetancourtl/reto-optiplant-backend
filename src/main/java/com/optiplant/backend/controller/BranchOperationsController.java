package com.optiplant.backend.controller;

import com.optiplant.backend.dto.CreateProductExchangeRequest;
import com.optiplant.backend.dto.CreateProductReturnRequest;
import com.optiplant.backend.dto.CreateSaleRequest;
import com.optiplant.backend.entity.ProductExchange;
import com.optiplant.backend.entity.ProductReturn;
import com.optiplant.backend.entity.Sale;
import com.optiplant.backend.entity.SaleItem;
import com.optiplant.backend.service.BranchOperationsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Controlador REST para operaciones de sucursal (ventas, devoluciones, cambios de producto).
 * Permite a usuarios con rol SUCURSAL registrar ventas, devoluciones y cambios.
 * Seguridad: Solo usuarios SUCURSAL pueden acceder a estos endpoints.
 * Todas las operaciones afectan el inventario de la sucursal.
 */

@RestController
@RequestMapping("/branch-operations")
@PreAuthorize("hasRole('SUCURSAL')")
public class BranchOperationsController {

    private final BranchOperationsService branchOperationsService;

    public BranchOperationsController(BranchOperationsService branchOperationsService) {
        this.branchOperationsService = branchOperationsService;
    }

    /**
     * POST /branch-operations/sales
     * Registra una venta en la sucursal.
     * Body: CreateSaleRequest (productos, cantidades, datos del cliente, etc.)
     * Retorna la venta registrada.
     */
    @PostMapping("/sales")
    public ResponseEntity<Sale> createSale(@Valid @RequestBody CreateSaleRequest request, Principal principal) {
        return ResponseEntity.ok(branchOperationsService.createSale(principal.getName(), request));
    }

    /**
     * GET /branch-operations/sales
     * Devuelve el historial de ventas de la sucursal.
     */
    @GetMapping("/sales")
    public ResponseEntity<List<Sale>> getMyBranchSales(Principal principal) {
        return ResponseEntity.ok(branchOperationsService.getMyBranchSales(principal.getName()));
    }

    /**
     * GET /branch-operations/sales/{saleId}/items
     * Devuelve los productos vendidos en una venta específica.
     */
    @GetMapping("/sales/{saleId}/items")
    public ResponseEntity<List<SaleItem>> getSaleItems(@PathVariable Long saleId, Principal principal) {
        return ResponseEntity.ok(branchOperationsService.getSaleItemsBySaleIdForMyBranch(principal.getName(), saleId));
    }

    /**
     * POST /branch-operations/returns
     * Registra una devolución de producto.
     * Body: CreateProductReturnRequest (producto, cantidad, motivo, etc.)
     * Retorna la devolución registrada.
     */
    @PostMapping("/returns")
    public ResponseEntity<ProductReturn> createReturn(@Valid @RequestBody CreateProductReturnRequest request,
                                                      Principal principal) {
        return ResponseEntity.ok(branchOperationsService.createReturn(principal.getName(), request));
    }

    /**
     * POST /branch-operations/exchanges
     * Registra un cambio de producto.
     * Body: CreateProductExchangeRequest (producto original, producto nuevo, cantidades, motivo, etc.)
     * Retorna el cambio registrado.
     */
    @PostMapping("/exchanges")
    public ResponseEntity<ProductExchange> createExchange(@Valid @RequestBody CreateProductExchangeRequest request,
                                                          Principal principal) {
        return ResponseEntity.ok(branchOperationsService.createExchange(principal.getName(), request));
    }
}
