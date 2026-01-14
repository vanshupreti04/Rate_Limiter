package com.projects.Rate_Limiter.repository;

import com.projects.Rate_Limiter.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByApiKey(String apiKey);
    Optional<Client> findByServiceName(String serviceName);
    Optional<Client> findByEmail(String email);

    boolean existsByServiceName(String serviceName);
    boolean existsByEmail(String email);
}
