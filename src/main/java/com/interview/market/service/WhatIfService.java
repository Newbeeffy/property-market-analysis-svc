package com.interview.market.service;

import com.interview.market.dto.CoefficientsResponse;
import com.interview.market.dto.SensitivityRequest;
import com.interview.market.dto.SensitivityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Delegates to the downstream model service (Task 1) for "what-if" analysis.
 *
 */
@Service
public class WhatIfService {

    private final RestClient restClient;

    private static final Logger logger = LoggerFactory.getLogger(WhatIfService.class);

    public WhatIfService(RestClient restClient) {
        this.restClient = restClient;
    }

    @Cacheable("coefficients")
    public CoefficientsResponse coefficients() {
        Map<String, Object> info;
        try {
            info = restClient.get()
                    .uri("/model-info")
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
        } catch (RestClientException e) {
            logDownstreamError(e);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Model service unavailable", e);
        }

        double intercept = ((Number) info.get("intercept")).doubleValue();
        List<CoefficientsResponse.Coefficient> coefficients = coefficientsFrom(info);
        return new CoefficientsResponse(intercept, coefficients);
    }

    @Cacheable(value = "sensitivity", key = "#req")
    public SensitivityResponse sensitivity(SensitivityRequest req) {
        List<Map<String, Double>> batch = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (int i = 0; i < req.steps(); i++) {
            //To compute for each step from min to max, how much is the value based on min, e.g.: 1000 --> 3000, step is 3,
            // then each step value is 1000, 2000, 3000.
            double value = req.min() + (req.max() - req.min()) * i / (req.steps() - 1);
            Map<String, Double> features = new LinkedHashMap<>(req.baseline());
            features.put(req.variable(), value);
            batch.add(features);
            values.add(value);
        }

        Map<String, Object> resp;
        try {
            resp = restClient.post()
                    .uri("/predict")
                    .body(batch)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
        } catch (RestClientException e) {
            logDownstreamError(e);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Model service unavailable", e);
        }

        List<?> predictions = (List<?>) resp.get("predictions");

        // Get the property price for each value of the specific feature.
        List<SensitivityResponse.Point> points = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            double price = ((Number) predictions.get(i)).doubleValue();
            points.add(new SensitivityResponse.Point(values.get(i), price));
        }
        return new SensitivityResponse(req.variable(), points);
    }

    private static void logDownstreamError(RestClientException e) {
        if (e instanceof RestClientResponseException re) {
            logger.error("Downstream returned {}: {}",
                    re.getStatusCode(), re.getResponseBodyAsString());
        } else {
            logger.error("Downstream request failed: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static List<CoefficientsResponse.Coefficient> coefficientsFrom(Map<String, Object> info) {
        Map<String, Number> raw = (Map<String, Number>) info.get("coefficients");
        return raw.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new CoefficientsResponse.Coefficient(
                        e.getKey(), e.getValue().doubleValue()))
                .toList();
    }
}
