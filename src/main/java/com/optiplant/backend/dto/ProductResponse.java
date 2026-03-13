package com.optiplant.backend.dto;

public record ProductResponse(
        Long id,
        String name,
        String description,
        Double price,
        String category
) {
}
