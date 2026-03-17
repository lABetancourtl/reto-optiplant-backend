package com.optiplant.backend.dto;

public enum TimeGranularity {
    DAY,
    WEEK,
    MONTH,
    YEAR;

    public String toDateTruncUnit() {
        return name().toLowerCase();
    }
}

