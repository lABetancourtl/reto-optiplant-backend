package com.optiplant.backend.repository;

import com.optiplant.backend.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByBranchIdOrderByCreatedAtDesc(Long branchId);
}

