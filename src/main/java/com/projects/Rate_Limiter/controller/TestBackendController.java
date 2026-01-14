package com.projects.Rate_Limiter.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/real-backend")
public class TestBackendController {

    @GetMapping("/ping")
    public Map<String, Object> ping(@RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        System.out.println("=== REAL BACKEND CALLED ===");
        System.out.println("API Key in backend: " + (apiKey != null ? apiKey.substring(0, 8) + "..." : "none"));

        return Map.of(
                "service", "Real Backend Service",
                "timestamp", System.currentTimeMillis(),
                "apiKeyReceived", apiKey != null,
                "message", "This response came through the gateway filter!"
        );
    }

    @GetMapping("/data")
    public Map<String, Object> getData() {
        return Map.of(
                "data", "Sample data from backend",
                "items", new String[]{"item1", "item2", "item3"},
                "count", 3,
                "timestamp", System.currentTimeMillis()
        );
    }
}
