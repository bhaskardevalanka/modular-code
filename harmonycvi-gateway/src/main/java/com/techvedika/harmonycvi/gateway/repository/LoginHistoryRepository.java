package com.techvedika.harmonycvi.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.LoginHistory;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    // Custom query methods if needed
}
