package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.OrgApiConfig;

import jakarta.transaction.Transactional;

@Repository
public interface OrgApiConfigRepository extends JpaRepository<OrgApiConfig, Long> {

//    // Fetch all configs for a specific org
    List<OrgApiConfig> findByOrgId(Long orgId);
//
//    // Fetch config by org + method
//    OrgApiConfig findByOrgOrgIdAndMethod(Long orgId, String method);
//
//    // Optional: fetch by org + apiUrl
//    OrgApiConfig findByOrgOrgIdAndApiUrl(Long orgId, String apiUrl);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM OrgApiConfig o WHERE o.org.id = :orgId")
    void deleteByOrgId(@Param("orgId") Long orgId);

}
