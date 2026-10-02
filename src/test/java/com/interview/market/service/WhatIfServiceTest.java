package com.interview.market.service;

import com.interview.market.dto.CoefficientsResponse;
import com.interview.market.dto.SensitivityRequest;
import com.interview.market.dto.SensitivityResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WhatIfServiceTest {

    private WhatIfService service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        service = new WhatIfService(builder.build());
    }

    @AfterEach
    void verify() {
        server.verify();
    }

    @Test
    void coefficientsParsesModelInfo() {
        server.expect(requestTo("/model-info"))
                .andRespond(withSuccess(
                        "{\"intercept\": -123000.0, \"coefficients\": "
                                + "{\"bedrooms\": 18500.0, \"square_footage\": 98.2}}",
                        MediaType.APPLICATION_JSON));

        CoefficientsResponse response = service.coefficients();

        assertThat(response.intercept()).isEqualTo(-123000.0);
        assertThat(response.coefficients()).hasSize(2);
        assertThat(response.coefficients().get(0).feature()).isEqualTo("bedrooms");
        assertThat(response.coefficients().get(0).coefficient()).isEqualTo(18500.0);
    }

    @Test
    void sensitivityBuildsBatchAndAlignsPredictions() {
        server.expect(requestTo("/predict"))
                .andRespond(withSuccess(
                        "{\"predictions\": [100.0, 200.0, 300.0]}",
                        MediaType.APPLICATION_JSON));

        SensitivityRequest request = new SensitivityRequest(
                Map.of("square_footage", 1850.0, "bedrooms", 3.0),
                "square_footage", 1000.0, 2000.0, 3);

        SensitivityResponse response = service.sensitivity(request);

        assertThat(response.variable()).isEqualTo("square_footage");
        assertThat(response.points()).hasSize(3);
        assertThat(response.points().get(0).value()).isEqualTo(1000.0);
        assertThat(response.points().get(2).value()).isEqualTo(2000.0);
        assertThat(response.points().get(1).price()).isEqualTo(200.0);
    }
}
