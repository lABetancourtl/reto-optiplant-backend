package com.optiplant.backend.repository;

import com.optiplant.backend.dto.BranchTopProductResponse;
import com.optiplant.backend.dto.ProductBranchSalesResponse;
import com.optiplant.backend.entity.SaleItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
    List<SaleItem> findBySaleId(Long saleId);

    @Query("""
            select new com.optiplant.backend.dto.ProductBranchSalesResponse(
                si.sale.branch.id,
                si.sale.branch.name,
                coalesce(sum(si.quantity), 0),
                coalesce(sum(si.subtotal), 0)
            )
            from SaleItem si
            where si.product.id = :productId
            group by si.sale.branch.id, si.sale.branch.name
            order by coalesce(sum(si.quantity), 0) desc, coalesce(sum(si.subtotal), 0) desc
            """)
    List<ProductBranchSalesResponse> getSalesByBranchForProduct(Long productId);

    @Query("""
            select new com.optiplant.backend.dto.BranchTopProductResponse(
                si.product.id,
                si.product.name,
                coalesce(sum(si.quantity), 0),
                coalesce(sum(si.subtotal), 0)
            )
            from SaleItem si
            where si.sale.branch.id = :branchId
            group by si.product.id, si.product.name
            order by coalesce(sum(si.quantity), 0) desc, coalesce(sum(si.subtotal), 0) desc
            """)
    List<BranchTopProductResponse> getTopProductsByBranch(Long branchId, Pageable pageable);
}
