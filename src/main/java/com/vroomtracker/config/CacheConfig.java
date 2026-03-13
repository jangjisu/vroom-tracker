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
     * - dashboard: 5min TTL — getDashboardData() (trafficIc API)
     * - regionRanking: 5min TTL — getRegionRanking() (trafficRegion API)
     *
     * trafficFlowByTime data is read from DB, no separate cache needed.
     */
    @Bean
    public CacheManager cacheManager() {
        var dashboard = new CaffeineCache("dashboard",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .build());

        var regionRanking = new CaffeineCache("regionRanking",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .build());

        var manager = new SimpleCacheManager();
        manager.setCaches(List.of(dashboard, regionRanking));
        return manager;
    }
}
