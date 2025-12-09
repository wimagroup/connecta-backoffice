package com.connecta.gestor.repository;

import com.connecta.gestor.model.Role;
import com.connecta.gestor.model.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByNome(RoleType nome);
}

