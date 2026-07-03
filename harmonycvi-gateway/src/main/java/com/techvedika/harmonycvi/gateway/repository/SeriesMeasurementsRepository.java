package com.techvedika.harmonycvi.gateway.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.SeriesMeasurements;

import jakarta.transaction.Transactional;

@Repository
public interface SeriesMeasurementsRepository extends JpaRepository<SeriesMeasurements, Long> {
	
	List<SeriesMeasurements> findByStudyId(String studyId);

    List<SeriesMeasurements> findByStudyIdAndBookmarkId(String studyId, Long bookmarkId);

    List<SeriesMeasurements> findByStudyIdAndSeriesIdAndBookmarkId(String studyId, String seriesId, Long bookmarkId);
    
//    @Query("select sm.commonData from SeriesMeasurements sm where sm.studyId = :studyId and sm.seriesId = :seriesId and sm.bookmark.id = :bookmarkId")
//    List<HashMap<String, Object>> findCommonDataByStudyIdAndSeriesIdAndBookmarkId(String studyId, String seriesId, Long bookmarkId);
    
    @Query("select sm.commonData from SeriesMeasurements sm where sm.studyId = :studyId and sm.seriesId = :seriesId and sm.bookmark.id = :bookmarkId")
    List<HashMap<String, Object>> findCommonDataByStudyIdAndSeriesIdAndBookmarkId(
            @Param("studyId") String studyId,
            @Param("seriesId") String seriesId,
            @Param("bookmarkId") Long bookmarkId
    );

    List<SeriesMeasurements> findByBookmarkId(Long bookmarkId);
    
    Optional<SeriesMeasurements> findFirstByStudyIdAndSeriesIdAndBookmarkId(String studyId, String seriesId, Long bookmarkId);

    long countByBookmarkId(Long bookmarkId);

    @Modifying
    @Transactional
    void deleteByBookmarkId(Long bookmarkId);
    
    @Modifying
    @Query("""
    DELETE FROM SeriesMeasurements d
    WHERE d.bookmark.id IN (
        SELECT b.id FROM Bookmarks b WHERE b.studyInstanceUID = :studyId
    )
    """)
    void deleteByStudyId(@Param("studyId") String studyId);

    @Modifying
    @Transactional
    @Query(value = "delete from harmonycvi.series_measurements_data where patient_id = ?1", nativeQuery = true)
    void deleteMeasurementByPatientId(String patientId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM SeriesMeasurements sm WHERE sm.studyId = :studyId AND sm.bookmark.id = :bookmarkId")
    int deleteByStudyIdAndBookmarkId(@Param("studyId") String studyId, @Param("bookmarkId") Long bookmarkId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM SeriesMeasurements sm WHERE sm.studyId = :studyId AND sm.seriesId = :seriesId AND sm.bookmark.id = :bookmarkId")
    int deleteByStudyIdAndSeriesIdAndBookmarkId(
            @Param("studyId") String studyId,
            @Param("seriesId") String seriesId,
            @Param("bookmarkId") Long bookmarkId); 
    
   

}
