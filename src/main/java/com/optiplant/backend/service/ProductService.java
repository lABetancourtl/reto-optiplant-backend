package com.optiplant.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.optiplant.backend.entity.Category;
import com.optiplant.backend.entity.Product;
import com.optiplant.backend.repository.CategoryRepository;
import com.optiplant.backend.repository.ProductRepository;

/**
 * Servicio para la gestión de productos.
 * Permite consultar, crear, actualizar, eliminar productos y filtrar por categoría.
 * Utilizado por los controladores para operar sobre la entidad Product.
 *
 * <p>
 * Principales métodos:
 * <ul>
 *   <li>getAllProducts(): Obtiene todos los productos.</li>
 *   <li>getProductById(Long id): Obtiene un producto por su ID.</li>
 *   <li>createProduct(Product product): Crea un nuevo producto.</li>
 *   <li>updateProduct(Long id, Product productDetails): Actualiza un producto existente.</li>
 *   <li>deleteProduct(Long id): Elimina un producto por su ID.</li>
 *   <li>getProductsByCategory(Long categoryId): Obtiene productos por categoría.</li>
 * </ul>
 * </p>
 *
 * @author Optiplant Backend
 * @since 2024
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Obtiene todos los productos existentes en el sistema.
     *
     * @return Lista de productos.
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Obtiene un producto por su identificador único.
     *
     * @param id Identificador del producto.
     * @return Producto encontrado.
     * @throws RuntimeException si no se encuentra el producto.
     */
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    /**
     * Crea un nuevo producto en el sistema.
     *
     * @param product Producto a crear.
     * @return Producto creado.
     */
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    /**
     * Actualiza un producto existente por su identificador.
     *
     * @param id Identificador del producto a actualizar.
     * @param productDetails Detalles actualizados del producto.
     * @return Producto actualizado.
     * @throws RuntimeException si no se encuentra el producto.
     */
    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setCategory(productDetails.getCategory());
        return productRepository.save(product);
    }

    /**
     * Elimina un producto por su identificador.
     *
     * @param id Identificador del producto a eliminar.
     * @throws RuntimeException si no se encuentra el producto.
     */
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    /**
     * Obtiene todos los productos asociados a una categoría.
     *
     * @param categoryId Identificador de la categoría.
     * @return Lista de productos de la categoría.
     */
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
}
