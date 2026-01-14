package com.projects.Rate_Limiter.filter;

import com.projects.Rate_Limiter.service.RateLimiterService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class TokenBucketRateLimiterFilter extends AbstractGatewayFilterFactory<TokenBucketRateLimiterFilter.Config> {

    private final RateLimiterService rateLimiterService;

    public TokenBucketRateLimiterFilter(RateLimiterService rateLimiterService) {
        super(Config.class);
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public TokenBucketRateLimiterFilter.Config newConfig() {
        return new Config();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().toString();

            // Public endpoints - NO rate limiting
            if (path.equals("/gatway/health") ||
                    path.equals("/api/v1/health") ||
                    path.startsWith("/api/v1/register") ||
                    path.equals("/api/v1/client-info") ||
                    path.startsWith("/api/v1/reset-tokens")) {
                return chain.filter(exchange);
            }

            return processRateLimit(exchange, chain);
        };
    }

    private Mono<Void> processRateLimit(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String apiKey = request.getHeaders().getFirst("X-API-Key");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return unauthorized(response, "Missing X-API-Key header");
        }

        String identifier = getClientIdentifier(request);

        try {
            boolean allowed = rateLimiterService.isAllowed(apiKey, identifier);

            if (!allowed) {
                response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                addRateLimitHeaders(response, apiKey, identifier);

                String errorBody = String.format(
                        "{\"error\":\"Rate limit exceeded\",\"identifier\":\"%s\",\"apiKey\":\"%s\"}",
                        identifier, maskKey(apiKey)
                );

                return response.writeWith(
                        Mono.just(response.bufferFactory().wrap(errorBody.getBytes(StandardCharsets.UTF_8)))
                );
            }

            addRateLimitHeaders(response, apiKey, identifier);
            return chain.filter(exchange);

        } catch (Exception e) {
            // Fail open - allow request if rate limiter fails
            return chain.filter(exchange);
        }
    }

    private void addRateLimitHeaders(ServerHttpResponse response, String apiKey, String identifier) {
        try {
            long capacity = rateLimiterService.getCapacity(apiKey);
            long remaining = rateLimiterService.getAvailableTokens(apiKey, identifier);

            response.getHeaders().add("X-RateLimit-Limit", String.valueOf(capacity));
            response.getHeaders().add("X-RateLimit-Remaining", String.valueOf(remaining));
            response.getHeaders().add("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 1000));

        } catch (Exception e) {
            // Silently fail - headers are optional
        }
    }

    private String getClientIdentifier(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }

    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");

        String errorBody = String.format("{\"error\":\"%s\"}", message);
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(errorBody.getBytes(StandardCharsets.UTF_8)))
        );
    }

    private String maskKey(String key) {
        if (key == null || key.length() <= 8) return "***";
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }

    public static class Config { }
}