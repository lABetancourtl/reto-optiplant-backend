package com.optiplant.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.optiplant.backend.entity.Category;
import com.optiplant.backend.repository.CategoryRepository;

/**
 * Servicio para la gestión de categorías de productos.
 * Proporciona métodos para obtener, crear, actualizar y eliminar categorías.
 * Utilizado por los controladores para operar sobre la entidad Category.
 *
 * <p>
 * Métodos:
 * <ul>
 *   <li>getAllCategories(): Obtiene todas las categorías.</li>
 *   <li>getCategoryById(Long id): Obtiene una categoría por su ID.</li>
 *   <li>createCategory(Category category): Crea una nueva categoría.</li>
 *   <li>updateCategory(Long id, Category categoryDetails): Actualiza una categoría existente.</li>
 *   <li>deleteCategory(Long id): Elimina una categoría por su ID.</li>
 * </ul>
 * </p>
 *
 * @author Optiplant Backend
 * @since 2024
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Obtiene todas las categorías existentes en el sistema.
     *
     * @return Lista de categorías.
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Obtiene una categoría por su identificador único.
     *
     * @param id Identificador de la categoría.
     * @return Categoría encontrada.
     * @throws RuntimeException si no se encuentra la categoría.
     */
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
    }

    /**
     * Crea una nueva categoría en el sistema.
     *
     * @param category Categoría a crear.
     * @return Categoría creada.
     */
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * Actualiza una categoría existente por su identificador.
     *
     * @param id Identificador de la categoría a actualizar.
     * @param categoryDetails Detalles actualizados de la categoría.
     * @return Categoría actualizada.
     * @throws RuntimeException si no se encuentra la categoría.
     */
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = getCategoryById(id);
        category.setName(categoryDetails.getName());
        return categoryRepository.save(category);
    }

    /**
     * Elimina una categoría por su identificador.
     *
     * @param id Identificador de la categoría a eliminar.
     * @throws RuntimeException si no se encuentra la categoría.
     */
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }
}
