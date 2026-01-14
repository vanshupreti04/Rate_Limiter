package com.projects.Rate_Limiter.controller;

import com.projects.Rate_Limiter.dto.RegisterRequest;
import com.projects.Rate_Limiter.dto.RegisterResponse;
import com.projects.Rate_Limiter.entity.Client;
import com.projects.Rate_Limiter.service.ClientService;
import com.projects.Rate_Limiter.service.RateLimiterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class RegistrationController {

    private final ClientService clientService;

    public RegistrationController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerClient(@RequestBody RegisterRequest request){
        RegisterResponse response = clientService.registerClient(request);

        if(response.isSuccess()){
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", response.getMessage());
            successResponse.put("data", Map.of("apiKey",response.getApiKey(), "clientId", response.getClientId()));

            return ResponseEntity.ok(successResponse);
        }
        else{
            Map<String,Object> errorResponse = new HashMap<>();

            errorResponse.put("success",false);
            errorResponse.put("message", response.getMessage());

            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    @GetMapping("/clients/{apiKey}")
    public ResponseEntity<?> getClient(@PathVariable String apiKey){
        Client client = clientService.getClientByApiKey(apiKey);

        if(client == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Client not Found"));
        }

        Map<String,Object> clientInfo = new HashMap<>();
        clientInfo.put("success", true);
        clientInfo.put("data", Map.of("serviceName",client.getServiceName(), "email",client.getEmail(), "redisHost",client.getRedisHost(), "redisPort",client.getRedisPort(), "capacity",client.getCapacity(), "refillRate",client.getRefillRate(), "isActive",client.isActive(), "createdAt",client.getCreatedAt()));

        return ResponseEntity.ok(clientInfo);
    }

    @PostMapping("/clients/{apiKey}/test-redis")
    public ResponseEntity<?> testRedis(@PathVariable String apiKey){
        boolean isConnected = clientService.testRedisConnection(apiKey);

        Map<String,Object> response = new HashMap<>();

        response.put("success", true);
        response.put("connected", isConnected);
        response.put("message", isConnected ? "Redis connection successful" : "Redis connection failed");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck(){
        Map<String,Object> health = new HashMap<>();
        health.put("status","UP");
        health.put("service", "Rate Limiter API");
        health.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(health);
    }
    @RestController
    @RequestMapping("/api/v1")
    public class ClientController {

        private final RateLimiterService rateLimiterService;
        private final ClientService clientService;

        public ClientController(RateLimiterService rateLimiterService, ClientService clientService) {
            this.rateLimiterService = rateLimiterService;
            this.clientService = clientService;
        }

        @PostMapping("/reset-tokens")
        public Map<String, Object> resetTokens(
                @RequestHeader("X-API-Key") String apiKey,
                @RequestBody Map<String, String> request) {

            String identifier = request.get("identifier");
            boolean success = rateLimiterService.resetTokens(apiKey, identifier);

            return Map.of(
                    "success", success,
                    "message", success ? "Tokens reset successfully" : "Failed to reset tokens",
                    "apiKey", maskKey(apiKey),
                    "identifier", identifier
            );
        }

        @GetMapping("/client-info")
        public Map<String, Object> getClientInfo(@RequestHeader("X-API-Key") String apiKey) {
            Client client = clientService.getClientByApiKey(apiKey);

            if (client == null) {
                return Map.of("error", "Client not found");
            }

            return Map.of(
                    "serviceName", client.getServiceName(),
                    "capacity", client.getCapacity(),
                    "refillRate", client.getRefillRate(),
                    "active", client.isActive()
            );
        }

        private String maskKey(String key) {
            if (key == null || key.length() <= 8) return "***";
            return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
        }
    }
}
