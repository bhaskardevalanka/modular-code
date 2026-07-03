package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.Role;
import com.techvedika.harmonycvi.gateway.projection.RoleProjection;

import jakarta.transaction.Transactional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    Optional<Role> findByIdAndActiveTrue(Long id);
    
 // Super Admin roles
    @Query("SELECT r.id AS id, r.name AS name, r.active AS active FROM Role r WHERE r.active = true ORDER BY r.createdDt ASC")
    List<RoleProjection> findByActiveTrueOrderByCreatedDtAsc();

    // Organization Admin roles (exclude SUPER ADMIN)
    @Query("SELECT r.id AS id, r.name AS name, r.active AS active FROM Role r WHERE r.active = true AND r.name <> :excludedName ORDER BY r.createdDt ASC")
    List<RoleProjection> findByActiveTrueAndNameNotOrderByCreatedDtAsc(@Param("excludedName") String excludedName);

    // User Admin / Doctors & Consultants (id > 1)
    @Query("SELECT r.id AS id, r.name AS name, r.active AS active FROM Role r WHERE r.active = true AND r.id > 1 ORDER BY r.createdDt ASC")
    List<RoleProjection> findActiveDoctorsAndConsultantsOrderedByCreatedDate();
    
    @Modifying
	@Transactional
	@Query("DELETE FROM Role r WHERE r.orgId = :orgId")
	void deleteByOrgId(@Param("orgId") Long orgId);


 
}