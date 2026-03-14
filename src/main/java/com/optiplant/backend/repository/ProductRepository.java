package com.optiplant.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.optiplant.backend.entity.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);
}
