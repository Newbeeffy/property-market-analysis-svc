package com.interview.market.service;

import com.interview.market.data.PropertyRepository;
import com.interview.market.dto.PriceDistributionResponse;
import com.interview.market.dto.SegmentsResponse;
import com.interview.market.model.Property;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Aggregate statistics over the in-memory dataset. */
@Service
public class MarketAnalysisService {

    private final PropertyRepository repository;

    public MarketAnalysisService(PropertyRepository repository) {
        this.repository = repository;
    }

    /** Group by one field (bedrooms, bathrooms, yearBuilt, schoolRating) and return per-group stats. */
    public SegmentsResponse segments(String groupBy) {
        // 1. Split all properties into groups keyed by the chosen field's value.
        Map<String, List<Property>> groups = new LinkedHashMap<>();
        for (Property p : repository.all()) {
            String key = groupKey(p, groupBy);
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
        }

        // 2. Compute stats for each group.
        List<SegmentsResponse.Segment> segments = new ArrayList<>();
        for (Map.Entry<String, List<Property>> entry : groups.entrySet()) {
            segments.add(toSegment(entry.getKey(), entry.getValue()));
        }

        // 3. Order groups by their numeric key (2 before 3 before 4).
        segments.sort(Comparator.comparingDouble(s -> Double.parseDouble(s.key())));

        return new SegmentsResponse(groupBy, segments);
    }

    /** Split the price range into divisions buckets and count properties per bucket. */
    public PriceDistributionResponse priceDistribution(int divisions) {
        List<Property> all = repository.all();
        double min = minPrice(all);
        double max = maxPrice(all);
        double width = (max - min) / divisions;

        long[] counts = new long[divisions];
        for (Property p : all) {
            int idx = (int) ((p.price() - min) / width);
            if (idx >= divisions) {
                idx = divisions - 1; // the max price lands in the last bucket
            }
            counts[idx]++;
        }

        List<PriceDistributionResponse.Bucket> buckets = new ArrayList<>();
        for (int i = 0; i < divisions; i++) {
            buckets.add(new PriceDistributionResponse.Bucket(
                    min + i * width, min + (i + 1) * width, counts[i]));
        }
        return new PriceDistributionResponse(divisions, min, max, buckets);
    }

    private static String groupKey(Property p, String groupBy) {
        return switch (groupBy) {
            case "bedrooms" -> String.valueOf(p.bedrooms());
            case "bathrooms" -> String.valueOf(p.bathrooms());
            case "yearBuilt" -> String.valueOf(p.yearBuilt());
            case "schoolRating" -> String.valueOf(p.schoolRating());
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Unsupported groupBy: " + groupBy);
        };
    }

    private static SegmentsResponse.Segment toSegment(String key, List<Property> group) {
        double avgPrice = averagePrice(group);
        double minPrice = minPrice(group);
        double maxPrice = maxPrice(group);
        return new SegmentsResponse.Segment(key, group.size(), avgPrice, minPrice, maxPrice);
    }

    private static double minPrice(List<Property> all) {
        double min = Double.MAX_VALUE;
        for (Property p : all) {
            min = Math.min(min, p.price());
        }
        return min;
    }

    private static double maxPrice(List<Property> all) {
        double max = -Double.MAX_VALUE;
        for (Property p : all) {
            max = Math.max(max, p.price());
        }
        return max;
    }

    private static double averagePrice(List<Property> all) {
        double sum = 0;
        for (Property p : all) {
            sum += p.price();
        }
        return all.isEmpty() ? 0 : sum / all.size();
    }
}
