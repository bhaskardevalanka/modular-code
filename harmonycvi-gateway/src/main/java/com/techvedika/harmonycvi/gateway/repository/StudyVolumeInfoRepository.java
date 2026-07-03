package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.StudyVolumeInfo;

import jakarta.transaction.Transactional;

@Repository
public interface StudyVolumeInfoRepository extends JpaRepository<StudyVolumeInfo, Long> {

    /** replaces the old named query StudyVolumeInfo.FIND_BY_BOOKMARK_ID */
    List<StudyVolumeInfo> findByBookmarkId(Long bookmarkId);

    /** convenience helper if you ever need only one record */
    Optional<StudyVolumeInfo> findFirstByBookmarkId(Long bookmarkId);
    
    @Query("SELECT COUNT(svi) FROM StudyVolumeInfo svi WHERE svi.bookmark.id = :bookmarkId")
    long countByBookmarkId(@Param("bookmarkId") Long bookmarkId);
    
    @Query("SELECT s.endVolume FROM StudyVolumeInfo s WHERE s.bookmark.id = :bookmarkId")
    Optional<List<Object>> findEndVolumeByBookmarkId(@Param("bookmarkId") Long bookmarkId);
    
    @Modifying(clearAutomatically = true)
	@Query("DELETE FROM StudyVolumeInfo s WHERE s.studyId = :studyId")
	void deleteByStudyId(@Param("studyId") String studyId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE StudyVolumeInfo svi
        SET svi.endVolume = :endVolume,
            svi.studyId = :studyId,
            svi.version = :version
        WHERE svi.bookmark.id = :bookmarkId
    """)
    int updateByBookmarkId(@Param("bookmarkId") Long bookmarkId,
                           @Param("studyId") String studyId,
                           @Param("endVolume") List<Object> endVolume,
                           @Param("version") int version);
}