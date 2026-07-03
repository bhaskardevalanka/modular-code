package com.techvedika.harmonycvi.gateway.repository;

import java.util.HashMap;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.AIOrgTags;
import com.techvedika.harmonycvi.gateway.projection.TagsImageProjection;

import jakarta.transaction.Transactional;

@Repository
public interface AIOrgTagsRepository extends JpaRepository<AIOrgTags, Integer> {

    // You can call the named query directly
    Optional<AIOrgTags> findByOrgId(Long orgId);
    
    @Query("SELECT a.tagsData as tagsData " +
            "FROM AIOrgTags a WHERE a.orgId = :orgId")
    Optional<HashMap<String, Object>> findTagsDataByOrgId(@Param("orgId") Long orgId);
    
    @Transactional
    @Modifying
    @Query("UPDATE AIOrgTags a SET a.tagsData = :tagsData " +
            "WHERE a.orgId = :orgId")
    int updateTagsDataByOrgId(@Param("orgId") Long orgId,@Param("tagsData") HashMap<String, Object> tagsData);
    
    @Transactional
    @Modifying
    @Query("UPDATE AIOrgTags a SET a.tagsData = :tagsData, a.lockVersion = a.lockVersion + 1 " +
           "WHERE a.orgId = :orgId AND a.lockVersion = :version")
    int updateTagsDataByOrgIdWithVersion(@Param("orgId") Long orgId,
                                         @Param("tagsData") HashMap<String, Object> tagsData,
                                         @Param("version") Long version);
    
    // New method with projection
    @Query("SELECT a.tagsData as tagsData, a.imageData as imageData " +
            "FROM AIOrgTags a WHERE a.orgId = :orgId")
    Optional<TagsImageProjection> findTagsDataAndImageDataByOrgId(@Param("orgId") Long orgId);
    
    @Query("SELECT a.lockVersion " +
            "FROM AIOrgTags a WHERE a.orgId = :orgId")
    Optional<Long> findLockVersionByOrgId(@Param("orgId") Long orgId);
}