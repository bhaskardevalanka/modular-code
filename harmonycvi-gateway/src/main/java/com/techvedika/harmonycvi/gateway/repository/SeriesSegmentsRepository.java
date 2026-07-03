package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.techvedika.harmonycvi.gateway.entity.SeriesSegments;

public interface SeriesSegmentsRepository extends JpaRepository<SeriesSegments, Long> {

//	@Query("SELECT ss FROM SeriesSegments ss WHERE ss.studyId = :studyId AND ss.type = :type")
//	List<SeriesSegments> findByStudyIdAndType(@Param("studyId") String studyId, @Param("type") String type);
	
	List<SeriesSegments> findByStudyIdAndType(String studyId, String type);
}
