package com.optiplant.backend.configuration;

import com.optiplant.backend.entity.Branch;
import com.optiplant.backend.entity.Product;
import com.optiplant.backend.entity.Sale;
import com.optiplant.backend.entity.SaleItem;
import com.optiplant.backend.entity.User;
import com.optiplant.backend.repository.BranchRepository;
import com.optiplant.backend.repository.ProductRepository;
import com.optiplant.backend.repository.SaleItemRepository;
import com.optiplant.backend.repository.SaleRepository;
import com.optiplant.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
@ConditionalOnProperty(name = "app.seed.sales-history.enabled", havingValue = "false")
public class SalesHistorySeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SalesHistorySeeder.class);

    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;

    private final int monthsBack;
    private final int minSalesPerMonth;
    private final int maxSalesPerMonth;
    private final int minItemsPerSale;
    private final int maxItemsPerSale;
    private final int minQuantityPerItem;
    private final int maxQuantityPerItem;
    private final boolean onlyIfNoSales;

    public SalesHistorySeeder(BranchRepository branchRepository,
                              ProductRepository productRepository,
                              UserRepository userRepository,
                              SaleRepository saleRepository,
                              SaleItemRepository saleItemRepository,
                              @Value("${app.seed.sales-history.months-back:8}") int monthsBack,
                              @Value("${app.seed.sales-history.min-sales-per-month:8}") int minSalesPerMonth,
                              @Value("${app.seed.sales-history.max-sales-per-month:20}") int maxSalesPerMonth,
                              @Value("${app.seed.sales-history.min-items-per-sale:1}") int minItemsPerSale,
                              @Value("${app.seed.sales-history.max-items-per-sale:4}") int maxItemsPerSale,
                              @Value("${app.seed.sales-history.min-quantity-per-item:1}") int minQuantityPerItem,
                              @Value("${app.seed.sales-history.max-quantity-per-item:8}") int maxQuantityPerItem,
                              @Value("${app.seed.sales-history.only-if-no-sales:true}") boolean onlyIfNoSales) {
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.monthsBack = monthsBack;
        this.minSalesPerMonth = minSalesPerMonth;
        this.maxSalesPerMonth = maxSalesPerMonth;
        this.minItemsPerSale = minItemsPerSale;
        this.maxItemsPerSale = maxItemsPerSale;
        this.minQuantityPerItem = minQuantityPerItem;
        this.maxQuantityPerItem = maxQuantityPerItem;
        this.onlyIfNoSales = onlyIfNoSales;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (onlyIfNoSales && saleRepository.count() > 0) {
            log.info("SalesHistorySeeder omitido: ya existen ventas y only-if-no-sales=true");
            return;
        }

        List<Branch> branches = branchRepository.findAll();
        List<Product> products = productRepository.findAll();
        if (branches.isEmpty() || products.isEmpty()) {
            log.info("SalesHistorySeeder omitido: no hay sucursales o productos");
            return;
        }

        int seededSales = 0;
        int seededItems = 0;

        int safeMonthsBack = Math.max(1, monthsBack);
        int safeMinSales = Math.max(1, minSalesPerMonth);
        int safeMaxSales = Math.max(safeMinSales, maxSalesPerMonth);
        int safeMinItems = Math.max(1, minItemsPerSale);
        int safeMaxItems = Math.max(safeMinItems, maxItemsPerSale);
        int safeMinQuantity = Math.max(1, minQuantityPerItem);
        int safeMaxQuantity = Math.max(safeMinQuantity, maxQuantityPerItem);

        for (Branch branch : branches) {
            List<User> branchUsers = userRepository.findByRoleAndBranchId("SUCURSAL", branch.getId());
            if (branchUsers.isEmpty()) {
                continue;
            }

            User soldBy = branchUsers.get(0);

            for (int monthOffset = safeMonthsBack - 1; monthOffset >= 0; monthOffset--) {
                YearMonth targetMonth = YearMonth.now().minusMonths(monthOffset);
                Random random = new Random((branch.getId() * 97L) + (targetMonth.getYear() * 31L) + targetMonth.getMonthValue());

                int salesInMonth = nextInRange(random, safeMinSales, safeMaxSales);
                for (int i = 0; i < salesInMonth; i++) {
                    Sale sale = new Sale();
                    sale.setBranch(branch);
                    sale.setSoldBy(soldBy);
                    sale.setCreatedAt(randomDateTimeInMonth(targetMonth, random));
                    sale.setTotalAmount(0.0);
                    sale = saleRepository.save(sale);

                    int maxAvailableItems = Math.min(safeMaxItems, products.size());
                    int itemsInSale = nextInRange(random, safeMinItems, maxAvailableItems);

                    List<Product> shuffledProducts = new ArrayList<>(products);
                    Collections.shuffle(shuffledProducts, random);

                    double total = 0.0;
                    for (int itemIndex = 0; itemIndex < itemsInSale; itemIndex++) {
                        Product product = shuffledProducts.get(itemIndex);
                        int quantity = nextInRange(random, safeMinQuantity, safeMaxQuantity);

                        SaleItem item = new SaleItem();
                        item.setSale(sale);
                        item.setProduct(product);
                        item.setQuantity(quantity);
                        item.setUnitPrice(product.getPrice());
                        item.setSubtotal(product.getPrice() * quantity);
                        saleItemRepository.save(item);
                        seededItems++;

                        total += item.getSubtotal();
                    }

                    sale.setTotalAmount(total);
                    saleRepository.save(sale);
                    seededSales++;
                }
            }
        }

        log.info("SalesHistorySeeder finalizado: {} ventas y {} items creados", seededSales, seededItems);
    }

    private int nextInRange(Random random, int min, int max) {
        if (max <= min) {
            return min;
        }
        return random.nextInt((max - min) + 1) + min;
    }

    private LocalDateTime randomDateTimeInMonth(YearMonth yearMonth, Random random) {
        int maxDay = yearMonth.lengthOfMonth();
        int day = nextInRange(random, 1, maxDay);
        int hour = nextInRange(random, 8, 20);
        int minute = nextInRange(random, 0, 59);
        return yearMonth.atDay(day).atTime(hour, minute);
    }
}

