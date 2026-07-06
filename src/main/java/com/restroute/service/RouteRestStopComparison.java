package com.restroute.service;

import com.restroute.controller.response.RouteRestStopResponse.ComparisonSummary;

record RouteRestStopComparison(RouteRestStopCandidate candidate, ComparisonSummary summary) {

    static RouteRestStopComparison of(RouteRestStopCandidate candidate, ComparisonSummary summary) {
        return new RouteRestStopComparison(candidate, summary);
    }
}
