package com.karthik.ratelimiter.controller;

import com.karthik.ratelimiter.model.TenantQuota;
import com.karthik.ratelimiter.service.TenantQuotaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantQuotaService tenantQuotaService;

    public TenantController(TenantQuotaService tenantQuotaService) {
        this.tenantQuotaService = tenantQuotaService;
    }

    @GetMapping("/{tenantId}")
    public ResponseEntity<TenantQuota> getTenantQuota(@PathVariable String tenantId) {
        return ResponseEntity.ok(tenantQuotaService.getQuotaForTenant(tenantId));
    }

    @PostMapping("/{tenantId}/quota")
    public ResponseEntity<TenantQuota> updateTenantQuota(
            @PathVariable String tenantId,
            @RequestParam String planTier,
            @RequestParam int maxRequests,
            @RequestParam(defaultValue = "60000") long windowSizeMs) {

        TenantQuota updated = tenantQuotaService.updateTenantQuota(tenantId, planTier, maxRequests, windowSizeMs);
        return ResponseEntity.ok(updated);
    }
}
