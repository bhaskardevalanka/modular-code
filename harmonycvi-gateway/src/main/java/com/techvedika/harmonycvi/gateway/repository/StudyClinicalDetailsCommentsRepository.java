package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.StudyClinicalDetailsComments;

@Repository
public interface StudyClinicalDetailsCommentsRepository extends JpaRepository<StudyClinicalDetailsComments, Long> {

    List<StudyClinicalDetailsComments> findByStudyIdAndStatusOrderByCreatedTimeDesc(String studyId, String status);
    StudyClinicalDetailsComments findByIdAndStatus(Long id, String status);
    boolean existsByStudyIdAndStatusOrderByCreatedTimeDesc(String studyId, String status);
    
    @Modifying(clearAutomatically = true)
	@Query("DELETE FROM StudyClinicalDetailsComments s WHERE s.studyId = :studyId")
	void deleteByStudyId(@Param("studyId") String studyId);
}