package com.karthik.ratelimiter.model;

public class TenantQuota {
    private String tenantId;
    private String planTier;      // e.g., FREE, GOLD, ENTERPRISE
    private int maxRequests;      // Allowed requests per window
    private long windowSizeMs;   // Window size in milliseconds

    public TenantQuota() {
    }

    public TenantQuota(String tenantId, String planTier, int maxRequests, long windowSizeMs) {
        this.tenantId = tenantId;
        this.planTier = planTier;
        this.maxRequests = maxRequests;
        this.windowSizeMs = windowSizeMs;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getPlanTier() {
        return planTier;
    }

    public void setPlanTier(String planTier) {
        this.planTier = planTier;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public long getWindowSizeMs() {
        return windowSizeMs;
    }

    public void setWindowSizeMs(long windowSizeMs) {
        this.windowSizeMs = windowSizeMs;
    }

    public static TenantQuotaBuilder builder() {
        return new TenantQuotaBuilder();
    }

    public static class TenantQuotaBuilder {
        private String tenantId;
        private String planTier;
        private int maxRequests;
        private long windowSizeMs;

        public TenantQuotaBuilder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public TenantQuotaBuilder planTier(String planTier) {
            this.planTier = planTier;
            return this;
        }

        public TenantQuotaBuilder maxRequests(int maxRequests) {
            this.maxRequests = maxRequests;
            return this;
        }

        public TenantQuotaBuilder windowSizeMs(long windowSizeMs) {
            this.windowSizeMs = windowSizeMs;
            return this;
        }

        public TenantQuota build() {
            return new TenantQuota(tenantId, planTier, maxRequests, windowSizeMs);
        }
    }
}
