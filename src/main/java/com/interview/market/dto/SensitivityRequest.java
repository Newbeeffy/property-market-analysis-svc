package com.interview.market.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Request for a sensitivity scan: vary one feature over [min, max] while holding
 * all other features fixed at {@code baseline}.
 */
public record SensitivityRequest(
        @NotNull Map<String, Double> baseline,
        @NotBlank String variable,
        @NotNull Double min,
        @NotNull Double max,
        @Min(2) @Max(100) int steps) {
}
