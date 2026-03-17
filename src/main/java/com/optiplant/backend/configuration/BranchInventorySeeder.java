package com.optiplant.backend.configuration;

import com.optiplant.backend.service.InventoryService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.seed.inventory.enabled", havingValue = "true")
public class BranchInventorySeeder implements ApplicationRunner {

    private final InventoryService inventoryService;

    public BranchInventorySeeder(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public void run(ApplicationArguments args) {
        inventoryService.initializeInventoryForAllBranches();
    }
}

