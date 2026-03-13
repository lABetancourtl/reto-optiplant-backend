package com.optiplant.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.optiplant.backend.dto.CreateProductRequest;
import com.optiplant.backend.dto.ProductResponse;
import com.optiplant.backend.dto.UpdateProductRequest;
import com.optiplant.backend.entity.Category;
import com.optiplant.backend.entity.Product;
import com.optiplant.backend.service.CategoryService;
import com.optiplant.backend.service.ProductService;

@RestController
@RequestMapping("/products")
@PreAuthorize("hasRole('ADMIN')")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        if (request.categoryId() != null) {
            Category category = categoryService.getCategoryById(request.categoryId());
            product.setCategory(category);
        }
        Product saved = productService.createProduct(product);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody UpdateProductRequest request) {
        Product productDetails = new Product();
        productDetails.setName(request.name());
        productDetails.setDescription(request.description());
        productDetails.setPrice(request.price());
        if (request.categoryId() != null) {
            Category category = categoryService.getCategoryById(request.categoryId());
            productDetails.setCategory(category);
        }
        Product updated = productService.updateProduct(id, productDetails);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
