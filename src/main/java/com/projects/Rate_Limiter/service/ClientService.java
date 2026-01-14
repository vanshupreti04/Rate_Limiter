package com.projects.Rate_Limiter.service;

import com.projects.Rate_Limiter.dto.RegisterRequest;
import com.projects.Rate_Limiter.dto.RegisterResponse;
import com.projects.Rate_Limiter.entity.Client;

public interface ClientService {

    RegisterResponse registerClient(RegisterRequest request);
    boolean validateApiKey(String apiKey);
    Client getClientByApiKey(String apiKey);
    Client getClientById(String clientId);
    boolean testRedisConnection(String apiKey);
}
