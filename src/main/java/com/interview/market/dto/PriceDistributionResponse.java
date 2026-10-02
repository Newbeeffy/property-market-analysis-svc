package com.interview.market.dto;

import java.util.List;

/**
 * Price histogram: the dataset's price range split into equally-sized buckets,
 * each with a count of properties whose price falls in that range.
 */
public record PriceDistributionResponse(
        int divisions,
        double minPrice,
        double maxPrice,
        List<Bucket> buckets) {

    public record Bucket(double lowerBound, double upperBound, long count) {
    }
}
