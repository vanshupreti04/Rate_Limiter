package com.projects.Rate_Limiter.service;

import com.projects.Rate_Limiter.entity.Client;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class ClientRedisManager {

    private final Map<String, JedisPool> clientRedisPools = new ConcurrentHashMap<>();

    public Jedis getJedisForClient(Client client){
        String clientKey = client.getApiKey();

        JedisPool pool = clientRedisPools.get(clientKey);

        if(pool == null){
            synchronized (this){
                pool = clientRedisPools.get(clientKey);
                if (pool == null){
                    pool = createJedisPool(client);
                    clientRedisPools.put(clientKey, pool);
                }
            }
        }

        return pool.getResource();
    }

    private JedisPool createJedisPool(Client client){
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(2);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);

        int timeout = 2000;

        try{
            if(client.getRedisPassword() != null && !client.getRedisPassword().isEmpty()){
                return new JedisPool(
                        poolConfig,
                        client.getRedisHost(),
                        client.getRedisPort(),
                        timeout,
                        client.getRedisPassword()
                );
            }
            else{
                return new JedisPool(
                        poolConfig,
                        client.getRedisHost(),
                        client.getRedisPort(),
                        timeout
                );
            }
        }
        catch (Exception e){
            throw new RuntimeException("Failed to create Redis pool for client: " + client.getServiceName(), e);
        }
    }

    public void cleanupClient(String apiKey){
        JedisPool pool = clientRedisPools.remove(apiKey);
        if(pool != null && !pool.isClosed()){
            pool.close();
        }
    }

    public int getActiveConnectionCount(){
        return clientRedisPools.size();
    }

    public boolean testConnection(Client client){
        try(Jedis jedis = getJedisForClient(client)){
            String response = jedis.ping();
            return "PONG".equals(response);
        }
        catch (Exception  e){
            return false;
        }
    }
}
