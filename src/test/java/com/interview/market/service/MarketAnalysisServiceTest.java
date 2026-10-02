package com.interview.market.service;

import com.interview.market.data.PropertyRepository;
import com.interview.market.dto.PriceDistributionResponse;
import com.interview.market.dto.SegmentsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class MarketAnalysisServiceTest {

    private MarketAnalysisService service;

    @BeforeEach
    void setUp() {
        PropertyRepository repository =
                new PropertyRepository(new ClassPathResource("data/house-price-dataset.csv"));
        service = new MarketAnalysisService(repository);
    }

    @Test
    void segmentsGroupsByBedrooms() {
        SegmentsResponse response = service.segments("bedrooms");

        assertThat(response.groupBy()).isEqualTo("bedrooms");
        assertThat(response.segments())
                .extracting(SegmentsResponse.Segment::key)
                .containsExactly("2", "3", "4");

        long total = response.segments().stream()
                .mapToLong(SegmentsResponse.Segment::count)
                .sum();
        assertThat(total).isEqualTo(50);
    }

    @Test
    void priceDistributionSumsToTotal() {
        PriceDistributionResponse response = service.priceDistribution(5);

        assertThat(response.divisions()).isEqualTo(5);
        assertThat(response.maxPrice()).isGreaterThan(response.minPrice());

        long total = response.buckets().stream()
                .mapToLong(PriceDistributionResponse.Bucket::count)
                .sum();
        assertThat(total).isEqualTo(50);
    }
}
