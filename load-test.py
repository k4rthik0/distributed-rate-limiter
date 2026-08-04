#!/usr/bin/env python3
"""
Load & Benchmarking Script for Distributed Multi-Tenant Rate Limiter
Uses Python Standard Library only (urllib + concurrent.futures) - No external dependencies required.
"""

import time
import urllib.request
import urllib.error
from concurrent.futures import ThreadPoolExecutor, as_completed
from collections import Counter

URL = "http://localhost:8080/api/v1/resource/data"

def make_request(tenant_id):
    req = urllib.request.Request(URL, headers={"X-Tenant-Id": tenant_id})
    try:
        with urllib.request.urlopen(req, timeout=5) as response:
            return response.status
    except urllib.error.HTTPError as e:
        return e.code
    except Exception:
        return 500

def benchmark_tenant(tenant_id, req_count, max_workers=10):
    print()
    print("--- Starting Load Test for Tenant: " + str(tenant_id) + " (" + str(req_count) + " requests) ---")
    
    start_time = time.time()
    results = Counter()

    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        futures = [executor.submit(make_request, tenant_id) for _ in range(req_count)]
        for future in as_completed(futures):
            status = future.result()
            results[status] += 1

    duration = time.time() - start_time
    print("Completed in " + str(round(duration, 2)) + " seconds.")
    print("Status Code Breakdown:")
    for status, count in sorted(results.items()):
        print("  HTTP " + str(status) + ": " + str(count))

def main():
    print("==========================================================")
    print(" Distributed Multi-Tenant Rate Limiter Benchmark Driver  ")
    print(" (Standard Library - Zero External Dependencies Needed)   ")
    print("==========================================================")
    
    # Test Free Tier (Max 10 requests allowed in 60s)
    benchmark_tenant("tenant_free", 30)
    
    # Test Gold Tier (Max 100 requests allowed in 60s)
    benchmark_tenant("tenant_gold", 50)

if __name__ == "__main__":
    main()
