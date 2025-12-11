package com.connecta.gestor.repository;

import com.connecta.gestor.model.RefreshToken;
import com.connecta.gestor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    Optional<RefreshToken> findByToken(String token);
    
    List<RefreshToken> findByUser(User user);
    
    void deleteByUser(User user);
    
    void deleteByToken(String token);
}


