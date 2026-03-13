package com.optiplant.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.optiplant.backend.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
