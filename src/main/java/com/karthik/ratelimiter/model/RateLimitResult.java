package com.karthik.ratelimiter.model;

public class RateLimitResult {
    private boolean allowed;
    private long currentRequests;
    private long maxRequests;
    private long remainingRequests;
    private long resetTimeMs;
    private String source; // "REDIS" or "LOCAL_FALLBACK"

    public RateLimitResult() {
    }

    public RateLimitResult(boolean allowed, long currentRequests, long maxRequests, long remainingRequests, long resetTimeMs, String source) {
        this.allowed = allowed;
        this.currentRequests = currentRequests;
        this.maxRequests = maxRequests;
        this.remainingRequests = remainingRequests;
        this.resetTimeMs = resetTimeMs;
        this.source = source;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public long getCurrentRequests() {
        return currentRequests;
    }

    public void setCurrentRequests(long currentRequests) {
        this.currentRequests = currentRequests;
    }

    public long getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(long maxRequests) {
        this.maxRequests = maxRequests;
    }

    public long getRemainingRequests() {
        return remainingRequests;
    }

    public void setRemainingRequests(long remainingRequests) {
        this.remainingRequests = remainingRequests;
    }

    public long getResetTimeMs() {
        return resetTimeMs;
    }

    public void setResetTimeMs(long resetTimeMs) {
        this.resetTimeMs = resetTimeMs;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public static RateLimitResultBuilder builder() {
        return new RateLimitResultBuilder();
    }

    public static class RateLimitResultBuilder {
        private boolean allowed;
        private long currentRequests;
        private long maxRequests;
        private long remainingRequests;
        private long resetTimeMs;
        private String source;

        public RateLimitResultBuilder allowed(boolean allowed) {
            this.allowed = allowed;
            return this;
        }

        public RateLimitResultBuilder currentRequests(long currentRequests) {
            this.currentRequests = currentRequests;
            return this;
        }

        public RateLimitResultBuilder maxRequests(long maxRequests) {
            this.maxRequests = maxRequests;
            return this;
        }

        public RateLimitResultBuilder remainingRequests(long remainingRequests) {
            this.remainingRequests = remainingRequests;
            return this;
        }

        public RateLimitResultBuilder resetTimeMs(long resetTimeMs) {
            this.resetTimeMs = resetTimeMs;
            return this;
        }

        public RateLimitResultBuilder source(String source) {
            this.source = source;
            return this;
        }

        public RateLimitResult build() {
            return new RateLimitResult(allowed, currentRequests, maxRequests, remainingRequests, resetTimeMs, source);
        }
    }
}
