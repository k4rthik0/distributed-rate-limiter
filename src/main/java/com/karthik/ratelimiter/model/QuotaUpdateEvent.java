package com.karthik.ratelimiter.model;

public class QuotaUpdateEvent {
    private String tenantId;
    private String planTier;
    private int newMaxRequests;
    private long newWindowSizeMs;
    private long timestamp;

    public QuotaUpdateEvent() {
    }

    public QuotaUpdateEvent(String tenantId, String planTier, int newMaxRequests, long newWindowSizeMs, long timestamp) {
        this.tenantId = tenantId;
        this.planTier = planTier;
        this.newMaxRequests = newMaxRequests;
        this.newWindowSizeMs = newWindowSizeMs;
        this.timestamp = timestamp;
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

    public int getNewMaxRequests() {
        return newMaxRequests;
    }

    public void setNewMaxRequests(int newMaxRequests) {
        this.newMaxRequests = newMaxRequests;
    }

    public long getNewWindowSizeMs() {
        return newWindowSizeMs;
    }

    public void setNewWindowSizeMs(long newWindowSizeMs) {
        this.newWindowSizeMs = newWindowSizeMs;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public static QuotaUpdateEventBuilder builder() {
        return new QuotaUpdateEventBuilder();
    }

    public static class QuotaUpdateEventBuilder {
        private String tenantId;
        private String planTier;
        private int newMaxRequests;
        private long newWindowSizeMs;
        private long timestamp;

        public QuotaUpdateEventBuilder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public QuotaUpdateEventBuilder planTier(String planTier) {
            this.planTier = planTier;
            return this;
        }

        public QuotaUpdateEventBuilder newMaxRequests(int newMaxRequests) {
            this.newMaxRequests = newMaxRequests;
            return this;
        }

        public QuotaUpdateEventBuilder newWindowSizeMs(long newWindowSizeMs) {
            this.newWindowSizeMs = newWindowSizeMs;
            return this;
        }

        public QuotaUpdateEventBuilder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public QuotaUpdateEvent build() {
            return new QuotaUpdateEvent(tenantId, planTier, newMaxRequests, newWindowSizeMs, timestamp);
        }
    }
}
