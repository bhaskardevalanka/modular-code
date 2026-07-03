package com.techvedika.harmonycvi.gateway.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.JwtBlackListToken;

@Repository
public interface JwtBlackListTokenRepository extends JpaRepository<JwtBlackListToken, Long> {

    // Derived query using Spring Data JPA
//    Optional<JwtBlackListToken> findByToken(String token);
}
