package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.UserOrganization;

import jakarta.transaction.Transactional;


@Repository
public interface UserOrganizationRepository extends JpaRepository<UserOrganization, Long> {

//    @Query("select uo.id.userId from UserOrganization uo where uo.id.orgId = :orgId")
//    List<Long> findUserIdsByOrgId(@Param("orgId") Long orgId);

    @Query("select uo.orgId from UserOrganization uo where uo.userId = :userId")
    List<Long> findOrgIdsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT uo.userId FROM UserOrganization uo WHERE uo.orgId = :orgId")
    List<Long> findUserIdsByOrgId(@Param("orgId") Long orgId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserOrganization uo WHERE uo.orgId = :orgId AND uo.userId = :userId")
	void deleteOrgByUserIdANDOrgId(@Param("orgId") Long orgId,@Param("userId") Long userId);
    
    @Query("SELECT uo.lockVersion " +
	           "FROM UserOrganization uo " +
	           "WHERE uo.orgId = :orgId AND uo.userId = :userId")
	Optional<Long> findLockVersionById(@Param("orgId") Long orgId,@Param("userId") Long userId);
    
    @Modifying
    @Transactional
    @Query("UPDATE UserOrganization uo SET uo.status = :status WHERE uo.orgId = :orgId AND uo.userId = :userId")
	void updateUser(@Param("orgId") Long orgId,@Param("userId") Long userId,@Param("status")boolean status);
    
    @Modifying
    @Transactional
    @Query("UPDATE UserOrganization uo SET uo.status = :status, uo.lockVersion = uo.lockVersion + 1 WHERE uo.orgId = :orgId AND uo.userId = :userId"
    		+ " AND uo.lockVersion = :lockVersion")
	int updateUserByLockVersion(@Param("orgId") Long orgId,@Param("userId") Long userId,@Param("status")boolean status,@Param("lockVersion") Long lockVersion);

    @Query("SELECT uo.status from UserOrganization uo WHERE uo.orgId = :orgId AND uo.userId = :userId")
	Boolean getUserStatus(@Param("userId") Long userId,@Param("orgId") Long orgId);
    
    @Modifying
	@Transactional
	@Query("DELETE FROM UserOrganization uo WHERE uo.orgId = :orgId")
	void deleteByOrgId(@Param("orgId") Long orgId);
    
    @Query("select uo from UserOrganization uo where uo.userId = :userId")
    List<UserOrganization> findByUserId(@Param("userId") Long userId);
    
    @Query("select uo.userGroup from UserOrganization uo where uo.userId = :userId AND uo.orgId = :orgId")
    String findByUserIdANDOrgId(@Param("userId") Long userId,@Param("orgId") Long orgId);

    @Modifying
    @Transactional
    @Query("UPDATE UserOrganization uo SET uo.userGroup = :userGroup WHERE uo.orgId = :orgId AND uo.userId = :userId ")
	void updateUserGroup(@Param("userId") Long userId,@Param("orgId") Long orgId,@Param("userGroup") String userGroup);
    
    
    
}