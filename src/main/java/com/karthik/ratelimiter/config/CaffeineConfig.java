package com.karthik.ratelimiter.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.karthik.ratelimiter.model.TenantQuota;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class CaffeineConfig {

    @Bean
    public Cache<String, TenantQuota> tenantQuotaCache() {
        return Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    @Bean
    public Cache<String, AtomicInteger> localFallbackRateCounter() {
        return Caffeine.newBuilder()
                .maximumSize(100_000)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }
}
