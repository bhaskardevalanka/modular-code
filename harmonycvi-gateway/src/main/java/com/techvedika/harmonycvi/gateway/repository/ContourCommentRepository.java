package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.ContourComment;

@Repository
public interface ContourCommentRepository extends JpaRepository<ContourComment, Integer> {
	
	@Query("SELECT cc from ContourComment cc WHERE cc.studyId = :studyId")
	List<ContourComment> findByStudyId(@Param("studyId") String studyId);
	
	@Query("SELECT cc.comment from ContourComment cc WHERE cc.studyId = :studyId")
	List<String> findCommentByStudyId(@Param("studyId") String studyId);
}
