package com.optiplant.backend.dto;

public record InventoryEventDTO(
        Long inventoryId,
        Long branchId,
        String branchName,
        Long productId,
        String productName,
        Integer quantity,
        String type           // UPDATED, TRANSFER_OUT, TRANSFER_IN
) {}