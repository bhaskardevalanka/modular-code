package com.techvedika.harmonycvi.gateway.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.Privileges;

import jakarta.transaction.Transactional;

@Repository
public interface PrivilegesRepository extends JpaRepository<Privileges, Long> {

    Optional<Privileges> findByName(String name);

    // Optionally, if you want to fetch only active privileges by org:
    Optional<Privileges> findByOrgIdAndActiveTrue(Long orgId);
    
    @Modifying
   	@Transactional
   	@Query("DELETE FROM Privileges p WHERE p.orgId = :orgId")
   	void deleteByOrgId(@Param("orgId") Long orgId);
}