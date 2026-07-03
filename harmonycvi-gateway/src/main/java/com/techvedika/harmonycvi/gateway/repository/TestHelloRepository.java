package com.techvedika.harmonycvi.gateway.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.TestHello;

@Repository
public interface TestHelloRepository extends JpaRepository<TestHello, Long> {
    Optional<TestHello> findByMessage(String message);
}