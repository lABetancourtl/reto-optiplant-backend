package com.optiplant.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.optiplant.backend.dto.CreateCategoryRequest;
import com.optiplant.backend.dto.UpdateCategoryRequest;
import com.optiplant.backend.entity.Category;
import com.optiplant.backend.service.CategoryService;

/**
 * Controlador REST para la gestión de categorías de productos.
 * Permite al ADMIN crear, consultar, actualizar y eliminar categorías.
 * Seguridad: Solo usuarios con rol ADMIN pueden acceder a estos endpoints.
 * Las categorías se usan para clasificar productos y facilitar búsquedas y reportes.
 */
@RestController
@RequestMapping("/categories")
@PreAuthorize("hasRole('ADMIN')")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * GET /categories
     * Devuelve la lista de todas las categorías.
     */
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * GET /categories/{id}
     * Devuelve los datos de una categoría por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    /**
     * POST /categories
     * Crea una nueva categoría.
     * Body: CreateCategoryRequest (name)
     * Retorna la categoría creada.
     */
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody CreateCategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        Category saved = categoryService.createCategory(category);
        return ResponseEntity.ok(saved);
    }

    /**
     * PUT /categories/{id}
     * Actualiza el nombre de una categoría.
     * Body: UpdateCategoryRequest (name)
     * Retorna la categoría actualizada.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody UpdateCategoryRequest request) {
        Category categoryDetails = new Category();
        categoryDetails.setName(request.name());
        Category updated = categoryService.updateCategory(id, categoryDetails);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /categories/{id}
     * Elimina una categoría por su ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
