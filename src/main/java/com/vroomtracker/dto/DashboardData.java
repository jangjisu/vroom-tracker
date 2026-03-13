package com.vroomtracker.dto;

import java.util.List;

public record DashboardData(
        NationwideTrafficDto summary,
        List<TollGateTrafficDto> ranking
) {}
