package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.dto.BookmarkDetailsDTO;
import com.techvedika.harmonycvi.gateway.entity.Bookmarks;
import com.techvedika.harmonycvi.gateway.projection.BookmarkDetailsProjection;

import jakarta.transaction.Transactional;

/* -------------------------------------------------
   Bookmarks Repository
   ------------------------------------------------- */
@Repository
public interface BookmarksRepository extends JpaRepository<Bookmarks, Long> {
	
	Optional<Bookmarks> findById(Long id);
	/* === Simple look‑ups === */
	Optional<Bookmarks> findByIdAndUserId(Long id, Long userId);

	List<Bookmarks> findByStudyInstanceUIDAndIsArchiveOrderByVersionDesc(String studyInstanceUID, int isArchive);

	Optional<Bookmarks> findTopByStudyInstanceUIDOrderByVersionDesc(String studyInstanceUID);

	@Query("SELECT b FROM Bookmarks b WHERE b.studyInstanceUID = :studyId AND b.version = :version")
	List<Bookmarks> findBookmarksByStudyIdAndVersion(@Param("studyId") String studyId, @Param("version") int version);
	
	@Query("SELECT b.id FROM Bookmarks b WHERE b.studyInstanceUID = :studyId AND b.version = :version")
	Optional<Long> findBookmarksIdByStudyIdAndVersion(@Param("studyId") String studyId, @Param("version") int version);
	
	@Query("SELECT b.id FROM Bookmarks b WHERE b.studyInstanceUID = :studyId AND b.version = :version")
	Long findBookmarkIdByStudyIdAndVersion(@Param("studyId") String studyId, @Param("version") int version);

	/* === Aggregate === */
	@Query("SELECT MAX(b.version) FROM Bookmarks b WHERE b.studyInstanceUID = :studyId")
	Integer findLatestVersion(@Param("studyId") String studyId);

	/* === DML === */
	@Modifying(clearAutomatically = true)
	@Query("DELETE FROM Bookmarks b WHERE b.studyInstanceUID = :studyId")
	void deleteByStudyInstanceUID(@Param("studyId") String studyId);

	/** Archive every bookmark of the study *except* the given Id */
	@Modifying
	@Transactional
	@Query("""
			UPDATE Bookmarks b
			SET    b.isArchive = 1 ,
			       b.version   = -1
			WHERE  b.studyInstanceUID = :studyId
			AND    b.id <> :id
			""")
	int archiveOldVersions(@Param("studyId") String studyId, @Param("id") Long id);

	/** Mark the given bookmark as the system “pre‑process” record */
	@Modifying
	@Transactional
	@Query("""
			UPDATE Bookmarks b
			SET    b.version     = 0,
			       b.name        = 'Preprocess',
			       b.description = 'Has AI processed results',
			       b.userId     = 1
			WHERE  b.studyInstanceUID = :studyId
			AND    b.id = :id
			""")
	int markAsPreprocess(@Param("studyId") String studyId, @Param("id") Long id);
	
	List<Bookmarks> findByStudyInstanceUID(String studyInstanceUID);
	
//	@Query("SELECT b.id as id, b.combinedSeriesIds as combinedSeriesIds, b.studyInstanceUID as studyInstanceUID, b.name as name " +
//	           "FROM Bookmarks b WHERE b.id = :id")
//	List<BookmarkDetailsDTO> findBookmarksById(@Param("id") Long id);
	
	@Query("SELECT new com.techvedika.harmonycvi.gateway.dto.BookmarkDetailsDTO(" +
		       "b.id, b.combinedSeriesIds, b.studyInstanceUID, b.name) " +
		       "FROM Bookmarks b WHERE b.id = :id")
	List<BookmarkDetailsDTO> findBookmarksById(@Param("id") Long id);

	
	@Query("SELECT b.id AS id, b.studyInstanceUID AS studyInstanceUID " +
	           "FROM Bookmarks b " +
	           "WHERE b.id = :id")
	Optional<BookmarkDetailsProjection> findBookmarkDetailsById(@Param("id") Long id);
	
	@Query("SELECT b.id AS id, b.studyInstanceUID AS studyInstanceUID " +
	           "FROM Bookmarks b WHERE b.studyInstanceUID = :studyId AND b.version = :version")
	Optional<BookmarkDetailsProjection> findBookmarkByStudyIdAndVersion(@Param("studyId") String studyId, @Param("version") int version);
	
	@Modifying
	@Transactional
	@Query("UPDATE Bookmarks b SET b.isArchive =:isArchive WHERE b.userId = :userId AND b.id = :id")
	int updateArchiveByUserIdAndId(@Param("id") Long id, @Param("userId") Long userId,@Param("isArchive") Integer isArchive);
	
	@Query("SELECT b.lockVersion " +
	           "FROM Bookmarks b " +
	           "WHERE b.id = :id")
	Optional<Long> findLockVersionById(@Param("id") Long id);
	
	@Modifying
	@Transactional
	@Query("UPDATE Bookmarks b SET b.isArchive = :isArchive, b.lockVersion = b.lockVersion + 1 " +
	       "WHERE b.id = :id AND b.userId = :userId AND b.lockVersion = :lockVersion")
	int updateArchiveByUserIdAndId(@Param("id") Long id,
	                               @Param("userId") Long userId,
	                               @Param("isArchive") Integer isArchive,
	                               @Param("lockVersion") Long lockVersion);

}
