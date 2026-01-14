package com.projects.Rate_Limiter.controller;

import com.projects.Rate_Limiter.service.RateLimiterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/gatway")

public class StatusController {

    private final RateLimiterService rateLimiterService;

    public StatusController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> getHealth(){
        return Mono.just(ResponseEntity.ok(Map.of("status", "UP", "service", "rate-limiting-gateway","timestamp", System.currentTimeMillis())));
    }

    @GetMapping("/rate-limit/status")
    public Mono<ResponseEntity<Map<String, Object>>> getRateLimitStatus(ServerWebExchange exchange){

        String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
        if(apiKey == null || apiKey.trim().isEmpty()){
            return Mono.just(ResponseEntity.badRequest().body(Map.of("error","Missing X-API-Key header", "message","Please provide X-API-Key header")));
        }

        String identifier = getClientId(exchange);

        long capacity = rateLimiterService.getCapacity(apiKey);
        long availableTokens = rateLimiterService.getAvailableTokens(apiKey,identifier);

        return Mono.just(ResponseEntity.ok(Map.of("status", "UP", "service", "rate-limiting-gateway", "apiKey", maskApiKey(apiKey), "clientIdentifier", identifier, "capacity", capacity, "availableTokens",availableTokens)));
    }

    private String getClientId(ServerWebExchange exchange) {
        String xForwardFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardFor != null && !xForwardFor.isEmpty()){
            return xForwardFor.split(",")[0].trim();
        }

        var remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getHostName() != null){
            return remoteAddress.getAddress().getHostAddress();
        }

        return "unknown";
    }

    private String maskApiKey(String apiKey){
        if (apiKey == null || apiKey.length() <= 8){
            return "***";
        }
        return apiKey.substring(0,4) + "..." + apiKey.substring(apiKey.length()-4);
    }
}
