package com.optiplant.backend.repository;

import com.optiplant.backend.entity.ProductExchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductExchangeRepository extends JpaRepository<ProductExchange, Long> {

    @Query("select coalesce(sum(e.quantity), 0) from ProductExchange e where e.saleItem.id = :saleItemId")
    Integer sumExchangedQuantityBySaleItemId(@Param("saleItemId") Long saleItemId);
}

