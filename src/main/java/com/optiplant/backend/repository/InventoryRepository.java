package com.optiplant.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.optiplant.backend.entity.Branch;
import com.optiplant.backend.entity.Inventory;
import com.optiplant.backend.entity.Product;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByBranch(Branch branch);

    Optional<Inventory> findByBranchAndProduct(Branch branch, Product product);
}
