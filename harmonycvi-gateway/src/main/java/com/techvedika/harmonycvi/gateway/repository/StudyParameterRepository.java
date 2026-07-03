package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.StudyParameter;

import jakarta.transaction.Transactional;

@Repository
public interface StudyParameterRepository extends JpaRepository<StudyParameter, Long> {	

    // Replaces: StudyParameter.getStudyParameterByStudyId
    List<StudyParameter> findByStudyId(String studyId);

    // Replaces: StudyParameter.getStudyParameterByBookmarkId
    List<StudyParameter> findByBookmarkId(Long bookmarkId);

    // Optional single record fetch by studyId + version (if needed)
    Optional<StudyParameter> findFirstByStudyIdAndVersion(String studyId, int version);
    
    
    Optional<List<Object>> findEndVolumeByBookmarkId(@Param("bookmarkId") Long bookmarkId);
    
    @Modifying(clearAutomatically = true)
	@Query("DELETE FROM StudyParameter b WHERE b.studyId = :studyId")
	void deleteByStudyId(@Param("studyId") String studyId);
    
    @Modifying
    @Transactional
    @Query("""
        UPDATE StudyParameter sp
        SET sp.updatedTime = CURRENT_TIMESTAMP,
            sp.parameterJson = :parameterStr,
            sp.radialStrainJson = :radialStrain,
            sp.graph = :graph,
            sp.computedSeries = :computedSeries
        WHERE sp.bookmark.id = :bookmarkId
    """)
    int updateByBookmarkId(@Param("bookmarkId") Long bookmarkId,
                           @Param("parameterStr") String parameterStr,
                           @Param("radialStrain") String radialStrain,
                           @Param("graph") String graph,
                           @Param("computedSeries") String computedSeries);

    @Query("SELECT COUNT(sp) FROM StudyParameter sp WHERE sp.bookmark.id = :bookmarkId")
    long countByBookmarkId(@Param("bookmarkId") Long bookmarkId);
    
    
    @Modifying
    @Transactional
    @Query("DELETE FROM StudyParameter sp WHERE sp.bookmark.id = :bookmarkId")
    int deleteByBookmarkId(@Param("bookmarkId") Long bookmarkId); 

}