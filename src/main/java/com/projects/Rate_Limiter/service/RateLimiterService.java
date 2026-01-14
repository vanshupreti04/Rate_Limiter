package com.projects.Rate_Limiter.service;

import com.projects.Rate_Limiter.entity.Client;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    private final RedisTokenBucketService redisTokenBucketService;
    private final ClientService clientService;

    public RateLimiterService(RedisTokenBucketService redisTokenBucketService,
                              ClientService clientService) {
        this.redisTokenBucketService = redisTokenBucketService;
        this.clientService = clientService;
    }

    public boolean isAllowed(String apiKey, String identifier) {
        if (!clientService.validateApiKey(apiKey)) {
            return false;
        }
        return redisTokenBucketService.isAllowed(apiKey, identifier);
    }

    public long getCapacity(String apiKey){
        if (!clientService.validateApiKey(apiKey)){
            return 0;
        }
        return redisTokenBucketService.getCapacity(apiKey);
    }

    public long getAvailableTokens(String apiKey, String identifier){
        if (!clientService.validateApiKey(apiKey)){
            return 0;
        }
        return redisTokenBucketService.getAvailableTokens(apiKey, identifier);
    }

    public boolean resetTokens(String apiKey, String identifier){
        if (!clientService.validateApiKey(apiKey)){
            return false;
        }
        return redisTokenBucketService.resetTokens(apiKey, identifier);
    }

    public long getRequestCountInWindow(String apiKey, String identifier) {
        if (!clientService.validateApiKey(apiKey)) {
            return 0;
        }
        return redisTokenBucketService.getRequestCountInWindow(apiKey, identifier);
    }

    private String maskKey(String key) {
        if (key == null || key.length() <= 8) return "***";
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }
}