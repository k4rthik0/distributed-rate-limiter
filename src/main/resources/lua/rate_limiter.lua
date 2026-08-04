-- Redis Lua Script for Atomic Sliding Window Rate Limiting
-- KEYS[1]: Rate limit Redis key (e.g., rate_limit:{tenant_id})
-- ARGV[1]: Current timestamp in milliseconds
-- ARGV[2]: Sliding window duration in milliseconds
-- ARGV[3]: Max requests allowed in the sliding window
-- ARGV[4]: Unique Request ID (UUID or token)

local key = KEYS[1]
local now = tonumber(ARGV[1])
local window_ms = tonumber(ARGV[2])
local max_requests = tonumber(ARGV[3])
local request_id = ARGV[4]

local clear_before = now - window_ms

-- Step 1: Remove timestamps older than the current window
redis.call('ZREMRANGEBYSCORE', key, '-inf', clear_before)

-- Step 2: Get total current requests within the sliding window
local current_requests = redis.call('ZCARD', key)

-- Step 3: Check if request limit is exceeded
if current_requests < max_requests then
    -- Add the current request timestamp
    redis.call('ZADD', key, now, request_id)
    -- Set TTL on key to auto-cleanup inactive keys
    redis.call('PEXPIRE', key, math.ceil(window_ms / 1000) * 1000)
    return { 1, current_requests + 1, max_requests }
else
    return { 0, current_requests, max_requests }
end
