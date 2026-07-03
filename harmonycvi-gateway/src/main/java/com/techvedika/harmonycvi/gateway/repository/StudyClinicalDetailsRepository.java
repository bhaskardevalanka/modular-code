package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.StudyClinicalDetails;

@Repository
public interface StudyClinicalDetailsRepository extends JpaRepository<StudyClinicalDetails, Long> {

    List<StudyClinicalDetails> findByStudyIdAndStatusOrderByCreatedTimeDesc(String studyId, String status);
    List<StudyClinicalDetails> findByStudyIdAndFileNameAndStatus(String studyId, String fileName, String status);
    StudyClinicalDetails findByIdAndStatus(Long id, String status);
    
    @Modifying(clearAutomatically = true)
	@Query("DELETE FROM StudyClinicalDetails s WHERE s.studyId = :studyId")
	void deleteByStudyId(@Param("studyId") String studyId);
}