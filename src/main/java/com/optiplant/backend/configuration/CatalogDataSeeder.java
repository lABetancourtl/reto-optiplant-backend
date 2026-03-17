package com.optiplant.backend.configuration;

import com.optiplant.backend.entity.Category;
import com.optiplant.backend.entity.Product;
import com.optiplant.backend.repository.CategoryRepository;
import com.optiplant.backend.repository.ProductRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.seed.catalog.enabled", havingValue = "true")
public class CatalogDataSeeder implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CatalogDataSeeder(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, Category> categoriesByName = ensureCategories();
        seedProducts(categoriesByName);
    }

    private Map<String, Category> ensureCategories() {
        List<String> categoryNames = List.of(
                "Herramientas manuales",
                "Herramientas electricas",
                "Pinturas y acabados",
                "Ferreteria general",
                "Seguridad industrial",
                "Plomeria",
                "Construccion"
        );

        Map<String, Category> categories = new LinkedHashMap<>();
        for (String name : categoryNames) {
            Category category = categoryRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> {
                        Category newCategory = new Category();
                        newCategory.setName(name);
                        return categoryRepository.save(newCategory);
                    });
            categories.put(name, category);
        }
        return categories;
    }

    private void seedProducts(Map<String, Category> categoriesByName) {
        List<SeedProduct> products = List.of(
                new SeedProduct("Martillo de una pieza 16oz", "Martillo de acero con mango antideslizante", 42000.0, "Herramientas manuales"),
                new SeedProduct("Juego destornilladores x6", "Planos y estrella en varios tamanos", 38000.0, "Herramientas manuales"),
                new SeedProduct("Llave inglesa 10 pulgadas", "Apertura ajustable para tuercas", 36000.0, "Herramientas manuales"),
                new SeedProduct("Taladro percutor 1/2", "650W con velocidad variable", 289000.0, "Herramientas electricas"),
                new SeedProduct("Pulidora angular 4 1/2", "Pulidora de 900W para corte y desbaste", 245000.0, "Herramientas electricas"),
                new SeedProduct("Sierra caladora", "Sierra electrica para madera y metal delgado", 198000.0, "Herramientas electricas"),
                new SeedProduct("Pintura vinilo blanco 1 galon", "Cobertura interior, acabado mate", 56000.0, "Pinturas y acabados"),
                new SeedProduct("Esmalte sintetico negro 1/4", "Proteccion para metal y madera", 18000.0, "Pinturas y acabados"),
                new SeedProduct("Rodillo felpa 9 pulgadas", "Aplicacion uniforme para muros", 14500.0, "Pinturas y acabados"),
                new SeedProduct("Caja tornillo drywall 6x1 x100", "Tornillo para lamina y perfileria", 22000.0, "Ferreteria general"),
                new SeedProduct("Cinta teflon 1/2", "Sellado de conexiones roscadas", 2500.0, "Ferreteria general"),
                new SeedProduct("Silicona multiuso 280ml", "Sellante transparente para juntas", 16500.0, "Ferreteria general"),
                new SeedProduct("Guante nitrilo reforzado", "Proteccion para trabajo pesado", 12000.0, "Seguridad industrial"),
                new SeedProduct("Gafas de seguridad claras", "Lente anti impacto", 9000.0, "Seguridad industrial"),
                new SeedProduct("Casco de seguridad tipo I", "Suspension de 4 puntos", 28000.0, "Seguridad industrial"),
                new SeedProduct("Tubo PVC presion 1/2 x 3m", "Conduccion de agua a presion", 24000.0, "Plomeria"),
                new SeedProduct("Codo PVC 1/2", "Accesorio para giro de tuberia", 1200.0, "Plomeria"),
                new SeedProduct("Llave terminal lavamanos", "Valvula de paso para punto sanitario", 18500.0, "Plomeria"),
                new SeedProduct("Cemento gris bolsa 50kg", "Uso general para obra", 36000.0, "Construccion"),
                new SeedProduct("Arena de pega bulto", "Arena seleccionada para mezcla", 9000.0, "Construccion"),
                new SeedProduct("Ladrillo estructural unidad", "Bloque de arcilla para muros", 1200.0, "Construccion")
        );

        for (SeedProduct seedProduct : products) {
            Category category = categoriesByName.get(seedProduct.categoryName());
            if (category == null) {
                continue;
            }

            boolean exists = productRepository.existsByNameIgnoreCaseAndCategoryId(
                    seedProduct.name(),
                    category.getId()
            );
            if (exists) {
                continue;
            }

            Product product = new Product();
            product.setName(seedProduct.name());
            product.setDescription(seedProduct.description());
            product.setPrice(seedProduct.price());
            product.setCategory(category);
            productRepository.save(product);
        }
    }

    private record SeedProduct(String name, String description, Double price, String categoryName) {
    }
}

