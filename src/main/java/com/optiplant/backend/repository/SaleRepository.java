package com.optiplant.backend.repository;

import com.optiplant.backend.dto.BranchSalesSummaryResponse;
import com.optiplant.backend.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    interface BranchSalesTimeSeriesRow {
        LocalDateTime getBucketStart();

        Long getBranchId();

        String getBranchName();

        Double getTotalAmount();

        Long getTotalSales();
    }

    List<Sale> findByBranchIdOrderByCreatedAtDesc(Long branchId);

    @Query("select coalesce(sum(s.totalAmount), 0) from Sale s")
    Double sumGlobalSalesAmount();

    @Query("select count(s.id) from Sale s")
    Long countGlobalSales();

    @Query("""
            select new com.optiplant.backend.dto.BranchSalesSummaryResponse(
                s.branch.id,
                s.branch.name,
                coalesce(sum(s.totalAmount), 0),
                count(s.id)
            )
            from Sale s
            group by s.branch.id, s.branch.name
            order by coalesce(sum(s.totalAmount), 0) desc
            """)
    List<BranchSalesSummaryResponse> getSalesSummaryByBranch();

    @Query(value = """
            select
                date_trunc(:granularity, s.created_at) as bucketStart,
                s.branch_id as branchId,
                b.name as branchName,
                coalesce(sum(s.total_amount), 0) as totalAmount,
                count(s.id) as totalSales
            from sales s
            join branches b on b.id = s.branch_id
            where s.created_at >= :fromDate
              and s.created_at < :toDate
            group by bucketStart, s.branch_id, b.name
            order by bucketStart asc, b.name asc
            """, nativeQuery = true)
    List<BranchSalesTimeSeriesRow> getSalesByBranchTimeSeries(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("granularity") String granularity
    );

    @Query(value = """
            select
                date_trunc(:granularity, s.created_at) as bucketStart,
                s.branch_id as branchId,
                b.name as branchName,
                coalesce(sum(s.total_amount), 0) as totalAmount,
                count(s.id) as totalSales
            from sales s
            join branches b on b.id = s.branch_id
            where s.created_at >= :fromDate
              and s.created_at < :toDate
              and s.branch_id in (:branchIds)
            group by bucketStart, s.branch_id, b.name
            order by bucketStart asc, b.name asc
            """, nativeQuery = true)
    List<BranchSalesTimeSeriesRow> getSalesByBranchTimeSeriesForBranches(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("granularity") String granularity,
            @Param("branchIds") List<Long> branchIds
    );
}
