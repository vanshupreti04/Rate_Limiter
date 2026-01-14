package com.projects.Rate_Limiter.service.impl;

import com.projects.Rate_Limiter.dto.RegisterRequest;
import com.projects.Rate_Limiter.dto.RegisterResponse;
import com.projects.Rate_Limiter.entity.Client;
import com.projects.Rate_Limiter.repository.ClientRepository;
import com.projects.Rate_Limiter.service.ClientService;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.UUID;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public RegisterResponse registerClient(RegisterRequest request){

        if(request.getServiceName() == null || request.getServiceName().trim().isEmpty()){
            return new RegisterResponse(false, "Service name is Required", null, null);
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return new RegisterResponse(false, "Email is required", null, null);
        }
        if (request.getRedisHost() == null || request.getRedisHost().trim().isEmpty()) {
            return new RegisterResponse(false, "Redis host is required", null, null);
        }
        if(clientRepository.existsByServiceName(request.getServiceName())) {
            return new RegisterResponse(false, "Service name '" + request.getServiceName() + "' is already registered", null, null);
        }
        if (clientRepository.existsByEmail(request.getEmail())) {
            return new RegisterResponse(false, "Email '" + request.getEmail() + "' is already registered", null, null);
        }

        Client client = new Client();
        client.setServiceName(request.getServiceName().trim());
        client.setEmail(request.getEmail().trim());
        client.setRedisHost(request.getRedisHost().trim());
        client.setRedisPort(request.getRedisPort() != null ? request.getRedisPort() : 6379);

        if(request.getRedisPassword() != null && !request.getRedisPassword().trim().isEmpty()){
            client.setRedisPassword(request.getRedisPassword().trim());
        }

        client.setCapacity(request.getCapacity() != null ? request.getCapacity() : 10);
        client.setRefillRate(request.getRefillRate() != null ? request.getRefillRate() : 5);

        Client savedClient = clientRepository.save(client);

        boolean redisConnected = testRedisConnection(savedClient);
        String message = redisConnected ? "Registration Successful. Redis connection Verified." : "Registration Successful but could not connect to Reids. Please Check it.";

        return new RegisterResponse(true,message,savedClient.getApiKey(),savedClient.getId().toString());
    }

    @Override
    public boolean validateApiKey(String apiKey){
        return clientRepository.findByApiKey(apiKey).map(Client::isActive).orElse(false);
    }

    @Override
    public Client getClientByApiKey(String apiKey){
        return clientRepository.findByApiKey(apiKey).orElse(null);
    }

    @Override
    public Client getClientById(String clientId){
        try{
            UUID uuid = UUID.fromString(clientId);
            return clientRepository.findById(uuid).orElse(null);
        }
        catch(IllegalArgumentException e){
            return null;
        }
    }

    @Override
    public boolean testRedisConnection(String apiKey){
        Client client = getClientByApiKey(apiKey);

        if(client == null){
          return false;
        }
        return testRedisConnection(client);
    }

    private boolean testRedisConnection(Client client){
        try(Jedis jedis = new Jedis(client.getRedisHost(), client.getRedisPort(), 2000)) {

            if(client.getRedisPassword() != null && !client.getRedisPassword().isEmpty()){
                jedis.auth(client.getRedisPassword());
            }

            String response = jedis.ping();
            return "PONG".equals(response);
        }
        catch(Exception e){
            return false;
        }
    }

}
