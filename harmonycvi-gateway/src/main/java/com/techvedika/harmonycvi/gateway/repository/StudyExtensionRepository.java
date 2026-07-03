package com.techvedika.harmonycvi.gateway.repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.StudyExtension;
import com.techvedika.harmonycvi.gateway.projection.PatientHeightWeightProjection;

import jakarta.transaction.Transactional;

@Repository
public interface StudyExtensionRepository extends JpaRepository<StudyExtension, Long> {
	
//	@Query("SELECT se.studyInstanceUID FROM StudyExtension se WHERE se.studyInstanceUID = :studyInstanceUID")
//	List<Study> findStudyByStudyInstanceUID(@Param("studyInstanceUID") String studyInstanceUID);

	@Query("SELECT se.orgId FROM StudyExtension se WHERE se.studyInstanceUID = :studyInstanceUID")
	Optional<Long> findOrgIdByStudyInstanceUID(@Param("studyInstanceUID") String studyInstanceUID);

	@Query("SELECT se.aiProcessStatus FROM StudyExtension se WHERE se.studyInstanceUID = :studyInstanceUID")
	Optional<Map<String, Object>> findAIProcessStatusByStudyInstanceUID(@Param("studyInstanceUID") String studyInstanceUID);

	@Query("SELECT se.status FROM StudyExtension se WHERE se.studyInstanceUID = :studyInstanceUID")
	Optional<String> findStatusByStudyInstanceUID(@Param("studyInstanceUID") String studyInstanceUID);
    
    @Modifying
    @Transactional
    @Query("UPDATE StudyExtension s " +
           "SET s.patientHeight = :height, s.patientWeight = :weight " +
           "WHERE s.studyInstanceUID = :studyUid")
    int updatePatientInfo(@Param("studyUid") String studyUid,
                          @Param("height") String height,
                          @Param("weight") String weight);

    @Query("SELECT se.dicomImagesCount FROM StudyExtension se WHERE se.studyInstanceUID = :studyInstanceUID")
    Optional<Long> findDicomImagesCountByStudyInstanceUID(@Param("studyInstanceUID") String studyInstanceUID);

    @Query("SELECT COUNT(se) FROM StudyExtension se WHERE se.orgId = :orgId")
    long countByOrgId(@Param("orgId") Long orgId);
    
    @Query("SELECT COUNT(se) FROM StudyExtension se")
    long count();
    
    @Query("SELECT COUNT(se) FROM StudyExtension se WHERE se.studyInstanceUID = :studyInstanceUID")
    long countByStudyId(@Param("studyInstanceUID") String studyId);
    
    @Query("SELECT se.lockVersion FROM StudyExtension se WHERE se.studyInstanceUID = :studyInstanceUID")
    Optional<Long> findLockVersionByStudyId(@Param("studyInstanceUID") String studyId);
    
    @Query("SELECT se FROM StudyExtension se WHERE se.studyInstanceUID = :studyInstanceUID")
    Optional<StudyExtension> findByStudyId(@Param("studyInstanceUID") String studyId);
    
    @Query("SELECT se.orgId FROM StudyExtension se WHERE se.studyInstanceUID = :studyInstanceUID")
    List<StudyExtension> findOrgIdByStudyId(@Param("studyInstanceUID") String studyId);

    @Query("SELECT COUNT(se) FROM StudyExtension se WHERE se.createdBy = :userOrgId AND se.orgId = :orgId")
    long countByUserOrgId(@Param("userOrgId") String userOrgId, @Param("orgId") Long orgId);

    @Modifying
    @Transactional
    @Query("UPDATE StudyExtension se SET se.orgId = :orgId, se.createdBy = :createdBy WHERE se.studyInstanceUID = :studyInstanceUID")
    int updateOrgAndCreatedByByStudyInstanceUID(@Param("orgId") Long orgId,
                                                @Param("createdBy") String createdBy,
                                                @Param("studyInstanceUID") String studyInstanceUID);
    
    @Query("""
    	    select se
    	    from StudyExtension se
    	    where se.orgId = :orgId
    	      and (:search is null or :search = ''
    	           or se.patientName ILIKE CONCAT('%', :search, '%'))
    	    order by se.studyDate desc, se.studyTime desc
    	""")
    	Page<StudyExtension> findByOrgIdAndOptionalPatientName(
    	    @Param("orgId") Long orgId,
    	    @Param("search") String search,
    	    Pageable pageable
    	);
    
    
    @Query("""
    	    select se
    	    from StudyExtension se
    	    where (:search is null or :search = ''
    	           or se.patientName ILIKE CONCAT('%', :search, '%'))
    	    order by se.studyDate desc, se.studyTime desc
    	""")
    	Page<StudyExtension> findByPatientFullNameLike(
    	    @Param("search") String search,
    	    Pageable pageable
    	);
    

    @Query("""
    	    select se
    	    from StudyExtension se
    	    join UserStudies us on se.studyInstanceUID = us.studyId
    	    where se.orgId = :orgId
    	      and us.user.id = :userId
    	      and (:search is null or :search = ''
    	           or se.patientName ILIKE CONCAT('%', :search, '%'))
    	    order by se.studyDate desc, se.studyTime desc
    	""")
    	Page<StudyExtension> findStudiesByOrgIdAndUserIdAndPatientNameLike(
    	    @Param("orgId") Long orgId,
    	    @Param("userId") Long userId,
    	    @Param("search") String search,
    	    Pageable pageable
    	);
    
    @Query("SELECT s.aiProcessStatus FROM StudyExtension s WHERE s.orgId = :orgId")
    List<Map<String, Object>> findAiProcessStatusByOrgId(@Param("orgId") Long orgId);

    @Query("SELECT se FROM StudyExtension se WHERE se.status = :status AND se.createdTime BETWEEN :startDate AND :endDate")
    List<StudyExtension> getStudyByStatusAndDate(@Param("status") String status,
                                                  @Param("startDate") Date startDate,
                                                  @Param("endDate") Date endDate);

    @Query("SELECT se FROM StudyExtension se WHERE se.status = :status AND se.orgId = :orgId AND se.createdTime BETWEEN :startDate AND :endDate")
    List<StudyExtension> getStudyByOrgIdAndStatusAndDate(@Param("status") String status,
                                                          @Param("orgId") Long orgId,
                                                          @Param("startDate") Date startDate,
                                                          @Param("endDate") Date endDate);
    
    @Query("SELECT count(se) FROM StudyExtension se WHERE se.status = :status AND se.createdTime BETWEEN :startDate AND :endDate")
    long countStudyByStatusAndDate(@Param("status") String status,
                                                  @Param("startDate") Date startDate,
                                                  @Param("endDate") Date endDate);

    @Query("SELECT count(se) FROM StudyExtension se WHERE se.status = :status AND se.orgId = :orgId AND se.createdTime BETWEEN :startDate AND :endDate")
    long countStudyByOrgIdAndStatusAndDate(@Param("status") String status,
                                                          @Param("orgId") Long orgId,
                                                          @Param("startDate") Date startDate,
                                                          @Param("endDate") Date endDate);    
    
    @Query("SELECT COUNT(se) FROM StudyExtension se WHERE se.qflowStatus = :status")
    long countStudiesByQflowStatus(@Param("status") String status);

    @Query("SELECT COUNT(se) FROM StudyExtension se WHERE se.orgId = :orgId AND se.qflowStatus = :status")
    long countStudiesByQflowStatusAndOrgId(@Param("orgId") Long orgId, @Param("status") String status);

    @Query("SELECT COUNT(se) FROM StudyExtension se WHERE se.ventricleAssessmentStatus = :status")
    long countStudiesByVentricleAssessmentStatus(@Param("status") String status);

    @Query("SELECT COUNT(se) FROM StudyExtension se WHERE se.orgId = :orgId AND se.ventricleAssessmentStatus = :status")
    long countStudiesByVentricleAssessmentStatusAndOrgId(@Param("orgId") Long orgId, @Param("status") String status);

    @Query("SELECT COUNT(se) FROM StudyExtension se WHERE se.classificationStatus = :status")
    long countStudiesByClassificationStatus(@Param("status") String status);

    @Query("SELECT COUNT(se) FROM StudyExtension se WHERE se.orgId = :orgId AND se.classificationStatus = :status")
    long countStudiesByClassificationStatusAndOrgId(@Param("orgId") Long orgId, @Param("status") String status);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM StudyExtension se WHERE se.studyInstanceUID = :studyInstanceUID")
	int deleteByStudyId(@Param("studyInstanceUID") String studyInstanceUID);
	
	@Query("SELECT se.patientHeight as patientHeight, se.patientWeight as patientWeight " +
	           "FROM StudyExtension se WHERE se.studyInstanceUID = :studyId")
	List<PatientHeightWeightProjection> findHeightWeightByStudyId(@Param("studyId") String studyId);
	
	@Query(value = """
		    SELECT 
		        CASE
		            WHEN status IS NULL OR status = '' THEN 'notStarted'
		            WHEN LOWER(status) LIKE '%inprogress%' THEN 'inprogress'
		            WHEN LOWER(status) LIKE '%no relevant series%' THEN 'noSeriesFound'
		            WHEN LOWER(status) LIKE '%completed%' THEN 'completed'
		            WHEN LOWER(status) LIKE '%failed%' OR LOWER(status) LIKE '%not completed%' THEN 'notCompleted'
		            ELSE 'notCompleted'
		        END AS statusCategory,
		        COUNT(*) AS count
		    FROM (
		        SELECT se.ai_process_status ->> :classificationRange AS status
		        FROM harmonycvi.study_extension se
		        WHERE se.created_time BETWEEN :startDate AND :endDate
		    ) t
		    GROUP BY statusCategory
		    """, nativeQuery = true)
		List<Object[]> getStatusCounts(
		        @Param("classificationRange") String classificationRange,
		        @Param("startDate") LocalDateTime startDate,
		        @Param("endDate") LocalDateTime endDate
		);
		
		@Query(value = """
			    SELECT 
			        CASE
			            WHEN status IS NULL OR status = '' THEN 'notStarted'
			            WHEN LOWER(status) LIKE '%inprogress%' THEN 'inprogress'
			            WHEN LOWER(status) LIKE '%no relevant series%' THEN 'noSeriesFound'
			            WHEN LOWER(status) LIKE '%completed%' THEN 'completed'
			            WHEN LOWER(status) LIKE '%failed%' OR LOWER(status) LIKE '%not completed%' THEN 'notCompleted'
			            ELSE 'notCompleted'
			        END AS statusCategory,
			        COUNT(*) AS count
			    FROM (
			        SELECT se.ai_process_status ->> :classificationRange AS status
			        FROM harmonycvi.study_extension se
			        WHERE se.created_time BETWEEN :startDate AND :endDate
			          AND se.org_id = :orgId
			    ) t
			    GROUP BY statusCategory
			    """, nativeQuery = true)
			List<Object[]> getStatusCountsByOrgId(
			        @Param("classificationRange") String classificationRange,
			        @Param("startDate") LocalDateTime startDate,
			        @Param("endDate") LocalDateTime endDate,
			        @Param("orgId") Long orgId
			);
			
			@Modifying
			@Transactional
			@Query("UPDATE StudyExtension se SET se.orgId = :orgId, se.createdBy = :createdBy, se.lockVersion = se.lockVersion + 1 " +
			       "WHERE se.studyInstanceUID = :studyInstanceUID AND se.lockVersion = :lockVersion")
			int updateOrgAndCreatedByByStudyInstanceUID(@Param("orgId") Long orgId,
			                                            @Param("createdBy") String createdBy,
			                                            @Param("studyInstanceUID") String studyInstanceUID,
			                                            @Param("lockVersion") Long lockVersion);

			@Modifying
			@Transactional
			@Query("UPDATE StudyExtension se SET se.isAIProcessed = :isAIProcessed, se.lockVersion = se.lockVersion + 1 " +
			       "WHERE se.studyInstanceUID = :studyInstanceUID AND se.lockVersion = :lockVersion")
			int updateIsAIProcessedByStudyInstanceUID(@Param("isAIProcessed") Boolean isAIProcessed,
			                                          @Param("studyInstanceUID") String studyInstanceUID,
			                                          @Param("lockVersion") Long lockVersion);

			@Modifying
			@Transactional
			@Query("UPDATE StudyExtension se SET se.status = :status, se.lockVersion = se.lockVersion + 1 " +
			       "WHERE se.studyInstanceUID = :studyInstanceUID AND se.lockVersion = :lockVersion")
			int updateStatusByStudyInstanceUID(@Param("status") String status,
			                                   @Param("studyInstanceUID") String studyInstanceUID,
			                                   @Param("lockVersion") Long lockVersion);

			@Modifying
			@Transactional
			@Query("UPDATE StudyExtension s SET s.aiProcessStatus = :status, s.aiProcessTime = :time, s.lockVersion = s.lockVersion + 1 " +
			       "WHERE s.studyInstanceUID = :studyId AND s.lockVersion = :lockVersion")
			int updateAiStatus(@Param("studyId") String studyId,
			                   @Param("status") Map<String, Object> status,
			                   @Param("time") Date time,
			                   @Param("lockVersion") Long lockVersion);

			@Modifying
			@Transactional
			@Query("UPDATE StudyExtension s SET " +
			       "s.noOfImages = :imgCount, " +
			       "s.dicomImagesCount = :dicomCount, " +
			       "s.patientHeight = :height, " +
			       "s.patientWeight = :weight, " +
			       "s.lockVersion = s.lockVersion + 1 " +
			       "WHERE s.studyInstanceUID = :studyUID AND s.lockVersion = :lockVersion")
			int updateStudyExtension(@Param("studyUID") String studyUID,
			                         @Param("imgCount") String imgCount,
			                         @Param("dicomCount") Long dicomCount,
			                         @Param("height") String height,
			                         @Param("weight") String weight,
			                         @Param("lockVersion") Long lockVersion);

			@Modifying
			@Transactional
			@Query("UPDATE StudyExtension s SET " +
			       "s.studyInstanceUID = :studyId, " +
			       "s.createdBy = :userId, " +
			       "s.updatedBy = :userId, " +
			       "s.orgId = :orgId, " +
			       "s.updatedTime = :updatedTime, " +
			       "s.lockVersion = s.lockVersion + 1 " +
			       "WHERE s.studyInstanceUID = :studyIuid AND s.lockVersion = :lockVersion")
			int updateStudyExtensionMeta(@Param("studyIuid") String studyIuid,
			                             @Param("studyId") String study,
			                             @Param("orgId") Long orgId,
			                             @Param("userId") String userId,
			                             @Param("updatedTime") Date updatedTime,
			                             @Param("lockVersion") Long lockVersion);

			@Modifying
			@Transactional
			@Query("UPDATE StudyExtension s SET " +
			       "s.patientHeight = :height, " +
			       "s.patientWeight = :weight, " +
			       "s.lockVersion = s.lockVersion + 1 " +
			       "WHERE s.studyInstanceUID = :studyUid AND s.lockVersion = :lockVersion")
			int updatePatientInfo(@Param("studyUid") String studyUid,
			                      @Param("height") String height,
			                      @Param("weight") String weight,
			                      @Param("lockVersion") Long lockVersion);
			
			@Query(value = """
				    SELECT * 
				    FROM harmonycvi.study_extension se
				    WHERE se.created_time < :createdTime
				      AND se.org_id NOT IN (17564, 264313, 5454, 273344, 484689)
				    ORDER BY se.created_time
				    LIMIT :maxResults
				    """, 
				    nativeQuery = true)
				List<StudyExtension> findExpiredStudies(
				    @Param("createdTime") Date createdTime,
				    @Param("maxResults") int maxResults
				);
			
			@Query("""
		    	    select s.patientName
		    	    from StudyExtension s 
		    	    where s.studyInstanceUID = ?1
		    	""")
		    	List<String> findPatientFullName(String studyId);
			
			@Query("SELECT se.patSex FROM StudyExtension se WHERE se.studyInstanceUID = :studyInstanceUID")
		    Optional<String> findPatSexByStudyId( @Param("studyInstanceUID") String studyInstanceUID);

			
			@Modifying()
            @Transactional
           @Query("UPDATE StudyExtension se SET se.isDeleted = true WHERE se.studyInstanceUID = :studyInstanceUID")
           void updateDeleteStatus(@Param("studyInstanceUID") String studyInstanceUID);
			
			@Query("""
					    select se
					    from StudyExtension se
					    where se.orgId = :orgId
					      and (:search is null or :search = ''
					           or se.patientName ILIKE CONCAT('%', :search, '%'))
					    order by se.studyDate desc, se.studyTime desc
					""")
			List<StudyExtension> findAllByOrgIdAndOptionalPatientName(@Param("orgId") Long orgId,
					@Param("search") String search);
			
			@Query("""
					    select se
					    from StudyExtension se
					    where se.orgId = :orgId and se.studyDate >= :startDate
						and se.studyDate <= :endDate
					      and (:search is null or :search = ''
					           or se.patientName ILIKE CONCAT('%', :search, '%'))
					    order by se.studyDate desc, se.studyTime desc
					""")
			List<StudyExtension> findByStudyDate(@Param("orgId") Long orgId, @Param("search") String search,
					@Param("startDate") String startDate, @Param("endDate") String endDate);

}
