package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.Center;

import jakarta.transaction.Transactional;

@Repository
public interface CenterRepository extends JpaRepository<Center, Long> {

	Optional<Center> findById(Long id);

//	Optional<Center> findByName(String name);

	/* FIND_BY_ID_LIST */
//	List<Center> findByIdIn(Collection<Long> idList);

	/* FIND_BY_NAME */
//	Optional<Center> findByNameIgnoreCase(String name);
	
	boolean existsByNameIgnoreCase(String name);
	
	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

	/* FIND_BY_ORG_ID */
	@Query("SELECT c FROM Center c WHERE c.organization.id = :orgId ORDER BY c.createdDt ASC")
	List<Center> findByOrganizationIdOrderByCreatedDtAsc(Long orgId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM Center c WHERE c.organization.id = :orgId")
	void deleteByOrgId(@Param("orgId") Long orgId);
	
	@Query("SELECT c.id FROM Center c WHERE c.organization.id = :orgId")
	Optional<Long> findIdByOrgId(@Param("orgId") Long orgId);
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM harmonycvi.user_centers c WHERE c.center_id = :centerId",nativeQuery = true)
	void deleteUserCentersByOrgId(@Param("centerId") Long centerId);
}