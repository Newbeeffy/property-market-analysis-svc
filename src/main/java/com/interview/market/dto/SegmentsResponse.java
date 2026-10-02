package com.interview.market.dto;

import java.util.List;

/**
 * Grouped statistics for one dimension (bedrooms, yearBuilt, ...), backing the
 * segment-comparison views.
 */
public record SegmentsResponse(
        String groupBy,
        List<Segment> segments) {

    public record Segment(
            String key,
            long count,
            double avgPrice,
            double minPrice,
            double maxPrice) {
    }
}
