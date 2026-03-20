package com.vroomtracker.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    /**
     * Cache strategy:
     * - regionRanking: 60min TTL — getRegionRanking() (trafficRegion API, 1-hour aggregation)
     * - dashboard: 60min TTL — getDashboardData() (trafficIc API, currently unused from UI)
     *
     * trafficFlowByTime data is read from DB, no separate cache needed.
     */
    @Bean
    public CacheManager cacheManager() {
        var regionRanking = new CaffeineCache("regionRanking",
                Caffeine.newBuilder()
                        .expireAfterWrite(60, TimeUnit.MINUTES)
                        .build());

        var dashboard = new CaffeineCache("dashboard",
                Caffeine.newBuilder()
                        .expireAfterWrite(60, TimeUnit.MINUTES)
                        .build());

        var manager = new SimpleCacheManager();
        manager.setCaches(List.of(regionRanking, dashboard));
        return manager;
    }
}
