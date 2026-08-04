package com.karthik.ratelimiter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.util.List;

@Configuration
public class RedisConfig {

    @Bean
    public RedisScript<List> rateLimiterScript() {
        return RedisScript.of(new ClassPathResource("lua/rate_limiter.lua"), List.class);
    }
}
