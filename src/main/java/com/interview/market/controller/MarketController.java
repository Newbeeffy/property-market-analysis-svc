package com.interview.market.controller;

import com.interview.market.dto.CoefficientsResponse;
import com.interview.market.dto.PriceDistributionResponse;
import com.interview.market.dto.SegmentsResponse;
import com.interview.market.dto.SensitivityRequest;
import com.interview.market.dto.SensitivityResponse;
import com.interview.market.service.MarketAnalysisService;
import com.interview.market.service.WhatIfService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Market analysis endpoints: segment breakdown, price distribution, and what-if
 * analysis backed by the downstream model service.
 */
@Validated
@RestController
@RequestMapping("/market")
public class MarketController {

    private final MarketAnalysisService marketAnalysisService;
    private final WhatIfService whatIfService;

    public MarketController(
            MarketAnalysisService marketAnalysisService,
            WhatIfService whatIfService) {
        this.marketAnalysisService = marketAnalysisService;
        this.whatIfService = whatIfService;
    }

    @GetMapping("/segments")
    public SegmentsResponse segments(@RequestParam String groupBy) {
        return marketAnalysisService.segments(groupBy);
    }

    @GetMapping("/price-distribution")
    public PriceDistributionResponse priceDistribution(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int divisions) {
        return marketAnalysisService.priceDistribution(divisions);
    }

    @GetMapping("/what-if/coefficients")
    public CoefficientsResponse coefficients() {
        return whatIfService.coefficients();
    }

    @PostMapping("/what-if/sensitivity")
    public SensitivityResponse sensitivity(@Valid @RequestBody SensitivityRequest request) {
        return whatIfService.sensitivity(request);
    }
}
