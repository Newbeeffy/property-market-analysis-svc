package com.interview.market.dto;

import java.util.List;

/**
 * The result of a sensitivity scan: a series of (variable value, predicted
 * price) points the frontend can plot as a line.
 */
public record SensitivityResponse(
        String variable,
        List<Point> points) {

    public record Point(double value, double price) {
    }
}
