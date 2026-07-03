package com.techvedika.harmonycvi.gateway.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.UserStudies;

@Repository
public interface UserStudiesRepository extends JpaRepository<UserStudies, Long> {
 
    @Query(value = "SELECT study_id FROM harmonycvi.user_studies WHERE user_id = :id", nativeQuery = true)
    List<String> findStudyIdByDoctorId(@Param("id") Long doctorId);

    
    boolean existsByStudyIdAndUserId(String studyId, Long userId);
    
    @Modifying
    @Query(value = "DELETE FROM harmonycvi.user_studies WHERE study_id = :studyId AND user_id = :doctorId", nativeQuery = true)
    void deleteByStudyIdAndDoctorId(@Param("studyId") String studyId, @Param("doctorId") Long doctorId);
    
    @Modifying
    @Query(value = "DELETE FROM harmonycvi.user_studies WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") Long userId);
    
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserStudies u WHERE u.studyId = :studyId")
    void deleteByStudyId(@Param("studyId") String studyId);
    
    @Modifying
    @Query("""
        UPDATE UserStudies us 
        SET us.createdBy = :createdBy,
            us.createdDt = :createdDt,
            us.active = :active,
            us.lastUpdatedBy = :lastUpdatedBy,
            us.lastUpdatedDt = :lastUpdatedDt
        WHERE us.studyId = :studyId
          AND us.user.id = :doctorId
    """)
    void updateUserStudiesFields(
        @Param("createdBy") Long createdBy,
        @Param("createdDt") Date createdDt,
        @Param("active") Boolean active,
        @Param("lastUpdatedBy") Long lastUpdatedBy,
        @Param("lastUpdatedDt") Date lastUpdatedDt,
        @Param("studyId") String studyId,
        @Param("doctorId") Long doctorId
    );
}