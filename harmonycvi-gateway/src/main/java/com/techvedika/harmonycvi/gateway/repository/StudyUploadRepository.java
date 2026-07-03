package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.StudyUpload;
import com.techvedika.harmonycvi.gateway.projection.StudyUploadProjection;

import jakarta.transaction.Transactional;

@Repository
public interface StudyUploadRepository extends JpaRepository<StudyUpload, Long> {

    // Custom query based on the named query for finding by ID
	Optional<StudyUpload> findById(Long pk);
    
    @Query("SELECT su.id AS id, su.studyLocation AS studyLocation, su.studyFileName AS studyFileName " +
            "FROM StudyUpload su WHERE su.studyId = :studyId ORDER BY su.createdDate DESC")
     List<StudyUploadProjection> findByStudyIdOrderByCreatedDtDesc(@Param("studyId") String studyId);
    
    // Projection query: only fetch what you need
    @Query("SELECT su.studyLocation AS studyLocation, su.studyFileName AS studyFileName " +
           "FROM StudyUpload su WHERE su.id = :id")
    Optional<StudyUploadProjection> findInfoById(@Param("id") Long id);
    
    @Transactional
    @Modifying
    @Query("UPDATE StudyUpload su SET su.isTransferred = :isTransferred, su.studyId = :studyId WHERE su.id = :id")
    int updateTransferStatus(@Param("id") Long id,
                             @Param("isTransferred") Boolean isTransferred,
                             @Param("studyId") String studyId);

    @Transactional
    @Modifying
    @Query("UPDATE StudyUpload su SET su.isUploaded = :isUploaded WHERE su.id = :id")
    int markUploaded(@Param("id") Long id, @Param("isUploaded") boolean isUploaded);

}