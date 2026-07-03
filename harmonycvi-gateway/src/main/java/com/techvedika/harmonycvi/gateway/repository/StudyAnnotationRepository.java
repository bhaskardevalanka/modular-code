package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.StudyAnnotation;

@Repository
public interface StudyAnnotationRepository extends JpaRepository<StudyAnnotation, Long> {

	//select sa from StudyAnnotation sa " +"where sa.studyId = :studyId
    List<StudyAnnotation> findByStudyId(String studyId);
}