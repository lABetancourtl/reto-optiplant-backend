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

@RestController
@RequestMapping("/branch-operations")
@PreAuthorize("hasRole('SUCURSAL')")
public class BranchOperationsController {

    private final BranchOperationsService branchOperationsService;

    public BranchOperationsController(BranchOperationsService branchOperationsService) {
        this.branchOperationsService = branchOperationsService;
    }

    @PostMapping("/sales")
    public ResponseEntity<Sale> createSale(@Valid @RequestBody CreateSaleRequest request, Principal principal) {
        return ResponseEntity.ok(branchOperationsService.createSale(principal.getName(), request));
    }

    @GetMapping("/sales")
    public ResponseEntity<List<Sale>> getMyBranchSales(Principal principal) {
        return ResponseEntity.ok(branchOperationsService.getMyBranchSales(principal.getName()));
    }

    @GetMapping("/sales/{saleId}/items")
    public ResponseEntity<List<SaleItem>> getSaleItems(@PathVariable Long saleId, Principal principal) {
        return ResponseEntity.ok(branchOperationsService.getSaleItemsBySaleIdForMyBranch(principal.getName(), saleId));
    }

    @PostMapping("/returns")
    public ResponseEntity<ProductReturn> createReturn(@Valid @RequestBody CreateProductReturnRequest request,
                                                      Principal principal) {
        return ResponseEntity.ok(branchOperationsService.createReturn(principal.getName(), request));
    }

    @PostMapping("/exchanges")
    public ResponseEntity<ProductExchange> createExchange(@Valid @RequestBody CreateProductExchangeRequest request,
                                                          Principal principal) {
        return ResponseEntity.ok(branchOperationsService.createExchange(principal.getName(), request));
    }
}

