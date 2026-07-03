package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; 

import com.techvedika.harmonycvi.gateway.entity.SeriesParameter;

@Repository
public interface SeriesParameterRepository extends JpaRepository<SeriesParameter, Long> {

	List<SeriesParameter> findBySeriesIdAndType(String seriesId, String type);

	List<SeriesParameter> findBySeriesIdAndBookmarkId(String seriesId, Long bookmarkId);
	
	Optional<SeriesParameter> findFirstBySeriesIdAndBookmarkId(String seriesId, Long bookmarkId);

	List<SeriesParameter> findByStudyIdAndBookmarkIdAndType(String studyId, Long bookmarkId, String type);

	List<SeriesParameter> findByStudyIdAndSeriesIdAndBookmarkId(String studyId, String seriesId, Long bookmarkId);
	
	@Modifying(clearAutomatically = true)
	@Query("DELETE FROM SeriesParameter s WHERE s.studyId = :studyId")
	void deleteByStudyId(@Param("studyId") String studyId);
	
	@Modifying 
	@Transactional  
	@Query("DELETE FROM SeriesParameter sp WHERE sp.studyId = :studyId AND sp.seriesId = :seriesId AND sp.bookmark.id = :bookmarkId")
	int deleteByStudyIdAndSeriesIdAndBookmarkId(
	        @Param("studyId") String studyId,
	        @Param("seriesId") String seriesId,
	        @Param("bookmarkId") Long bookmarkId);  

	
	
}
