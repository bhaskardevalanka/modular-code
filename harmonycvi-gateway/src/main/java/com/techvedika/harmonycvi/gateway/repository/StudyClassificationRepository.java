package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.StudyClassification;

import jakarta.transaction.Transactional;

@Repository
public interface StudyClassificationRepository extends JpaRepository<StudyClassification, Long> {

    // Custom query methods can be added here if needed
    List<StudyClassification> findByStudyId(String studyId);
    
    List<StudyClassification> findByStudyIdAndSeriesId(String studyId, String seriesId);
    
    List<StudyClassification> findBySeriesId(String seriesId);
    

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM StudyClassification sc WHERE sc.studyId = :studyId")
    int deleteByStudyId(@Param("studyId") String studyId);
}