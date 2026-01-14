package com.projects.Rate_Limiter.dto;

public class RegisterRequest {

    private String serviceName;
    private String email;
    private String redisHost;
    private Integer redisPort;
    private String redisPassword;
    private Integer capacity;
    private Long refillRate;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public Integer getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(Integer redisPort) {
        this.redisPort = redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Long getRefillRate() {
        return refillRate;
    }

    public void setRefillRate(Long refillRate) {
        this.refillRate = refillRate;
    }
}
