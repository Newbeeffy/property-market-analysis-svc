package com.interview.market.dto;

import java.util.List;

/**
 * Marginal effect of each feature on price, proxied from the model service's
 * {@code /model-info} endpoint. A coefficient is the price change per one unit
 * of that feature, all else equal.
 */
public record CoefficientsResponse(
        double intercept,
        List<Coefficient> coefficients) {

    public record Coefficient(String feature, double coefficient) {
    }
}
