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

/**
 * Controlador para la gestión de productos.
 * Solo accesible por usuarios con rol ADMIN.
 * Permite consultar, crear, actualizar y eliminar productos.
 * Soporta asociación de productos a categorías.
 */
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

    /**
     * Obtiene todos los productos registrados en el sistema.
     * Solo ADMIN.
     * @return Lista de productos.
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * Obtiene un producto por su ID.
     * Solo ADMIN.
     * @param id ID del producto.
     * @return Producto encontrado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Obtiene todos los productos asociados a una categoría.
     * Solo ADMIN.
     * @param categoryId ID de la categoría.
     * @return Lista de productos de la categoría.
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    /**
     * Crea un nuevo producto.
     * Solo ADMIN.
     * @param request Datos para crear el producto.
     * @return Producto creado.
     */
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

    /**
     * Actualiza un producto existente.
     * Solo ADMIN.
     * @param id ID del producto.
     * @param request Datos actualizados.
     * @return Producto actualizado.
     */
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

    /**
     * Elimina un producto por su ID.
     * Solo ADMIN.
     * @param id ID del producto.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}