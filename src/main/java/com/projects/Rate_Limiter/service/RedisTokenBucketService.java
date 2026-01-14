package com.projects.Rate_Limiter.service;

import com.projects.Rate_Limiter.entity.Client;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisTokenBucketService {

    private final ClientService clientService;
    private final ClientRedisManager clientRedisManager;

    public RedisTokenBucketService(ClientService clientService, ClientRedisManager clientRedisManager) {
        this.clientService = clientService;
        this.clientRedisManager = clientRedisManager;
    }

    public boolean isAllowed(String apiKey, String identifier){
        Client client = clientService.getClientByApiKey(apiKey);
        if(client == null || !client.isActive()){
            return false;
        }

        try(Jedis jedis = clientRedisManager.getJedisForClient(client)) {
            String tokenKey = "rate_limiter:tokens:" + client.getId() + ":" + identifier;
            String lastRefillKey = "rate_limiter:last_refill:" + client.getId() + ":" + identifier;

            refillTokens(client, jedis, tokenKey, lastRefillKey);

            // Atomic decrement and check
            long newTokenCount = jedis.decr(tokenKey);

            if(newTokenCount < 0) {
                jedis.incr(tokenKey);
                return false;
            }

            return true;
        }
        catch (Exception e){
            // Fail open on Redis errors
            return true;
        }
    }

    public long getCapacity(String apiKey){
        Client client = clientService.getClientByApiKey(apiKey);
        return client != null ? client.getCapacity() : 0;
    }

    public long getAvailableTokens(String apiKey, String identifier){
        Client client = clientService.getClientByApiKey(apiKey);
        if (client == null){
            return 0;
        }

        try(Jedis jedis = clientRedisManager.getJedisForClient(client)){
            String tokenKey = "rate_limiter:tokens:" + client.getId() + ":" + identifier;
            String tokenStr = jedis.get(tokenKey);

            if (tokenStr == null) {
                return client.getCapacity();
            }

            long tokens = Long.parseLong(tokenStr);
            return Math.max(0, tokens);
        }
        catch (Exception e){
            return 0;
        }
    }

    public void refillTokens(Client client, Jedis jedis, String tokenKey, String lastRefillKey){
        long now = System.currentTimeMillis();
        String lastRefillStr = jedis.get(lastRefillKey);

        if (lastRefillStr == null){
            jedis.set(tokenKey, String.valueOf(client.getCapacity()));
            jedis.set(lastRefillKey, String.valueOf(now));
            return;
        }

        long lastRefillTime = Long.parseLong(lastRefillStr);
        long elapsedTime = now - lastRefillTime;

        if (elapsedTime <= 0){
            return;
        }

        long tokensToAdd = (elapsedTime * client.getRefillRate()) / 1000;
        if (tokensToAdd <= 0){
            return;
        }

        String tokenStr = jedis.get(tokenKey);
        long currentTokens = tokenStr != null ? Long.parseLong(tokenStr) : client.getCapacity();

        if (currentTokens < 0) {
            currentTokens = 0;
        }

        long newTokens = Math.min(client.getCapacity(), currentTokens + tokensToAdd);
        jedis.set(tokenKey, String.valueOf(newTokens));
        jedis.set(lastRefillKey, String.valueOf(now));
    }

    public boolean resetTokens(String apiKey, String identifier){
        Client client = clientService.getClientByApiKey(apiKey);
        if (client == null){
            return false;
        }

        try(Jedis jedis = clientRedisManager.getJedisForClient(client)){
            String tokenKey = "rate_limiter:tokens:" + client.getId() + ":" + identifier;
            String lastRefillKey = "rate_limiter:last_refill:" + client.getId() + ":" + identifier;
            jedis.del(tokenKey, lastRefillKey);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    public long getRequestCountInWindow(String apiKey, String identifier) {
        Client client = clientService.getClientByApiKey(apiKey);
        if (client == null) {
            return 0;
        }

        try(Jedis jedis = clientRedisManager.getJedisForClient(client)) {
            String tokenKey = "rate_limiter:tokens:" + client.getId() + ":" + identifier;
            String tokenStr = jedis.get(tokenKey);

            if (tokenStr == null) {
                return 0;
            }

            long currentTokens = Long.parseLong(tokenStr);
            long capacity = client.getCapacity();

            if (currentTokens < 0) {
                return capacity + Math.abs(currentTokens);
            }

            return Math.max(0, capacity - currentTokens);
        } catch (Exception e) {
            return 0;
        }
    }
}