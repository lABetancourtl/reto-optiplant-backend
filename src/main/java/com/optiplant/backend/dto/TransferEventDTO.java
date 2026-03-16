package com.optiplant.backend.dto;

public record TransferEventDTO(
        Long transferId,
        String type,          // REQUESTED, APPROVED, REJECTED, RECEIVED
        Long sourceBranchId,
        String sourceBranchName,
        Long destBranchId,
        String destBranchName,
        String productName,
        Integer quantity,
        String trackingCode,
        String justification
) {}