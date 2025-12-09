package com.connecta.gestor.repository;

import com.connecta.gestor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByTokenRecuperacao(String token);
    Boolean existsByEmail(String email);
}

