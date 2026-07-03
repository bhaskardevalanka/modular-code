package com.techvedika.harmonycvi.gateway.repository;

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

import com.techvedika.harmonycvi.gateway.entity.Organization;
import com.techvedika.harmonycvi.gateway.projection.OrgConsultantProjection;
import com.techvedika.harmonycvi.gateway.projection.OrgPacsValidationUrlProjection;

import jakarta.transaction.Transactional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

	
	@Query("SELECT o.id FROM Organization o WHERE o.name = :name")
	Optional<Long> findIdByName(@Param("name") String name);
	
	@Query("SELECT o.name FROM Organization o WHERE o.id = :id")
	Optional<String> findNameById(@Param("id") Long id);
	
	boolean existsByName(String name);

	Optional<Organization> findById(Long id);
		
	@Query("SELECT o.preferences FROM Organization o WHERE o.id = :id")
	Optional<Map<String, Object>> findPreferencesById(@Param("id") Long id);
	
	@Query("SELECT o.lockVersion FROM Organization o WHERE o.id = :orgId")
	Optional<Long> getLockVersionOrganization(@Param("orgId") Long orgId);
	
	@Transactional
	@Modifying
	@Query("UPDATE Organization o SET " +
	       "o.active = false, " +
	       "o.lastUpdatedBy = :userId, " +
	       "o.lastUpdatedDt = :updatedDt, " +
	       "o.lockVersion = o.lockVersion + 1 " +
	       "WHERE o.id = :orgId AND o.lockVersion = :lockVersion")
	int deactivateOrganization(@Param("orgId") Long orgId,
	                           @Param("userId") Long userId,
	                           @Param("updatedDt") Date updatedDt,
	                           @Param("lockVersion") Long lockVersion);

	
	@Query("SELECT o.consultant AS consultant, o.email AS email, o.uploadLimit AS uploadLimit " +
	           "FROM Organization o " +
	           "WHERE o.id = :orgId")
	Optional<OrgConsultantProjection> findConsultantDetailsById(@Param("orgId") Long orgId);



	Long countByActiveTrue();

	// active=true only
	@Query("select o from Organization o where o.active = true")
	List<Organization> findAllActive();

	// keep the “findAll non‑TechVedika” behaviour of Organization.FIND_ALL
	@Query("select o from Organization o where lower(o.name) <> 'techvedika' order by o.createdDt desc")
	List<Organization> findAllExcludingTechVedika();
	
	
	Optional<OrgPacsValidationUrlProjection> findPacsValidationUrlById(Long id);
	
	@Query("SELECT o.lockVersion FROM Organization o WHERE o.id = :id")
	Optional<Long> findLockVersionById(@Param("id") Long id);

	
	@Modifying
    @Transactional
	@Query("UPDATE Organization o SET o.active = :status, o.lockVersion = o.lockVersion + 1 WHERE o.id= :id "
			+ " AND o.lockVersion = :lockVersion")
	int updateStatusByIdAndLockVersion(@Param("id") Long id,@Param("status") boolean status,@Param("lockVersion") Long lockVersion);

	@Modifying
    @Transactional
	@Query("UPDATE Organization o SET o.isDeleted = :status WHERE o.id= :id")
	void deleteOrg(@Param("id") Long id,@Param("status") boolean status);
	
	@Query("SELECT o FROM User u JOIN u.orgs o WHERE u.email = :email ORDER BY o.createdDt DESC")
	Page<Organization> findOrgsByEmail(@Param("email") String email, Pageable pageable);

	@Modifying
    @Transactional
	@Query("DELETE from Organization o WHERE o.id= :id")
	void deleteByOrgId(@Param("id") Long id);
	
	
}