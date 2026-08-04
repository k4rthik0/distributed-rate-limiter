package com.karthik.ratelimiter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/resource")
public class TestApiController {

    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getProtectedData(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "tenant_free") String tenantId) {

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "tenantId", tenantId,
                "message", "Request processed successfully through rate limiter gateway.",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
