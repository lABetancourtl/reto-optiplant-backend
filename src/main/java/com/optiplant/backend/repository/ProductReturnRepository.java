package com.optiplant.backend.repository;

import com.optiplant.backend.entity.ProductReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductReturnRepository extends JpaRepository<ProductReturn, Long> {

    @Query("select coalesce(sum(r.quantity), 0) from ProductReturn r where r.saleItem.id = :saleItemId")
    Integer sumReturnedQuantityBySaleItemId(@Param("saleItemId") Long saleItemId);
}

