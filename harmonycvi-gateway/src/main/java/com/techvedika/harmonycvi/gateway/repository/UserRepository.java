package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.Organization;
import com.techvedika.harmonycvi.gateway.entity.User;
import com.techvedika.harmonycvi.gateway.projection.AuthUserProjection;
import com.techvedika.harmonycvi.gateway.projection.LoginProjection;
import com.techvedika.harmonycvi.gateway.projection.PrivilegeRecord;
import com.techvedika.harmonycvi.gateway.projection.ResetPasswordProjection;

import jakarta.transaction.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	
	boolean existsByEmail(String email);
	
	boolean existsByEmailAndJwtToken(String email, String jwtToken);
	
	Optional<User> findByEmail(String email);
	
	@Query("SELECT u.id FROM User u WHERE u.email = :email")
	Optional<Long> findIdByEmail(@Param("email") String email);
	
	@Query("SELECT new com.techvedika.harmonycvi.gateway.projection.AuthUserProjection(u.email, u.password, u.onetimePassword) " +
	           "FROM User u WHERE u.email = :email")
	Optional<AuthUserProjection> findAuthUserByEmail(@Param("email") String email);
	
	@Query("SELECT new com.techvedika.harmonycvi.gateway.projection.ResetPasswordProjection(u.id,u.email, u.password, u.onetimePassword,u.jwtToken) " +
	           "FROM User u WHERE u.email = :email")
	Optional<ResetPasswordProjection> findUserByEmail(@Param("email") String email);
	
	@Query("SELECT u.role.name AS roleName FROM User u WHERE u.email = :email")
	Optional<String> findRoleNameByEmail(@Param("email") String email);
	
	@Query("SELECT u.role.name AS roleName FROM User u WHERE u.id = :id")
	Optional<String> findRoleNameById(@Param("id") Long id);
	
	@Query("SELECT u.firstName FROM User u WHERE u.id = :id")
	Optional<String> findFirstNameById(@Param("id") Long id);
	
	@Query("SELECT u.jwtToken FROM User u WHERE u.email = :email")
    Optional<String> findJwtTokenByEmail(@Param("email") String email);
	
	@Query("""
		    SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
		    FROM User u
		    JOIN u.role r
		    JOIN r.privileges p
		    WHERE u.email = :email
		      AND LOWER(p.name) = LOWER(:action)
		""")
	boolean hasPrivilege(@Param("email") String email, @Param("action") String action);
	
	@Query("""
		    SELECT new com.techvedika.harmonycvi.gateway.projection.PrivilegeRecord(p.id, p.name, p.active)
		    FROM User u
		    JOIN u.role r
		    JOIN r.privileges p
		    WHERE u.email = :email
		""")
	List<PrivilegeRecord> findPrivilegesByEmail(@Param("email") String email);
	
	@Query("SELECT COUNT(o) FROM User u JOIN u.orgs o WHERE u.email = :email")
    long countOrgsByEmail(@Param("email") String email);
	
	@Query("SELECT o FROM User u JOIN u.orgs o WHERE u.email = :email")
	List<Organization> findOrgsByEmail(@Param("email") String email);
	
	@Query("SELECT o FROM User u JOIN u.orgs o WHERE u.id = :id")
	List<Organization> findOrgsById(@Param("id") long id);
	
	@Query("SELECT COUNT(o) FROM User u JOIN u.orgs o WHERE u.id = :id")
    long countOrgsById(@Param("id") Long id);
	
	@Modifying
    @Transactional
	@Query("UPDATE User u SET u.active = :status WHERE u.id= :id")
	int updateStatusById(@Param("id") Long id,@Param("status") boolean status);
	
	@Query("SELECT u.lockVersion FROM User u WHERE u.id = :id")
	Optional<Long> findLockVersionById(@Param("id") Long id);
	
	@Modifying
    @Transactional
	@Query("UPDATE User u SET u.active = :status, u.lockVersion = u.lockVersion + 1 WHERE u.id= :id "
			+ " AND u.lockVersion = :lockVersion")
	int updateStatusByIdAndLockVersion(@Param("id") Long id,@Param("status") boolean status,@Param("lockVersion") Long lockVersion);
	
	@Query("SELECT u.isConsultant FROM User u WHERE u.email = :email")
	Optional<Boolean> isConsultantByEmail(@Param("email") String email);
	
	@Query("SELECT u.uploadLimit FROM User u WHERE u.email = :email")
	Optional<Integer> findUploadLimitByEmail(@Param("email") String email);
    
    @Query("SELECT u.id AS id, u.email AS email, u.firstName AS firstName, u.lastName AS lastName, " +
    	       "u.jwtToken AS jwtToken, u.active AS active, u.role AS role, u.mfaEnabled AS mfaEnabled, u.loginCount AS loginCount,u.deviceInfo AS deviceInfo " +
    	       "FROM User u WHERE u.email = :email AND u.password = :password")
    Optional<LoginProjection> findLoginUser(@Param("email") String email, @Param("password") String password);

    @Query("SELECT u.id AS id, u.email AS email, u.firstName AS firstName, u.lastName AS lastName, " +
    	       "u.jwtToken AS jwtToken, u.active AS active, u.role AS role, u.mfaEnabled AS mfaEnabled, u.loginCount AS loginCount,u.deviceInfo AS deviceInfo " +
    	       "FROM User u WHERE u.email = :email AND u.onetimePassword = :otp AND u.onetimePwdStatus = false")
    Optional<LoginProjection> findLoginUserByOtp(@Param("email") String email, @Param("otp") String otp);
    
    @Modifying
    @Transactional
	@Query("UPDATE User u SET u.jwtToken = :jwtToken WHERE u.id= :id")
	int updateTokenById(@Param("id") Long id,@Param("jwtToken") String jwtToken);
    
    @Modifying
    @Transactional
	@Query("UPDATE User u SET u.jwtToken = :jwtToken, u.lockVersion = u.lockVersion + 1, u.loginCount = :loginCount WHERE u.id= :id"
			+ " AND u.lockVersion = :lockVersion")
	int updateTokenById(@Param("id") Long id,@Param("jwtToken") String jwtToken,@Param("lockVersion") Long lockVersion, @Param("loginCount") Integer loginCount);


//    Optional<User> findByEmailAndOnetimePasswordAndOnetimePwdStatusFalse(String email, String onetimePassword);
//
//    List<User> findByIdInOrderByCreatedDtAsc(List<Long> idList);
//
//    List<User> findByIdInAndRoleIdGreaterThanEqualOrderByCreatedDtAsc(List<Long> idList, Integer roleId);

    Long countByActiveTrue();

    Long countByIdInAndActiveTrue(List<Long> idList);
    
    Long countByActiveTrueAndRoleId(Long roleId);
    
    /* one‑time password match (onetimePwdStatus = false) */
    //@Query("SELECT u FROM User u WHERE u.email = :email AND u.onetimePassword = :otp AND u.onetimePwdStatus = false")
    //Optional<User> findByEmailAndOtp(@Param("email") String email,@Param("otp")   String otp);
    
    
//    @Query("SELECT u FROM User u WHERE u.email = :email AND u.onetimePassword = :otp AND u.onetimePwdStatus = false")
//    Optional<User> findByEmailAndOnetimePasswordAndOnetimePwdStatusFalse(@Param("email") String email,@Param("otp") String otp);
    
    //@Query("SELECT u FROM User u WHERE u.id IN :ids AND u.role.name = 'Doctor'")
    //List<User> findDoctorsByIdList(@Param("ids") List<Long> ids);
    
    @Query("SELECT u FROM User u WHERE  u.id IN :ids AND (u.role.id = 4 OR u.role.id = 5)")
    List<User> findDoctorsByIdList(@Param("ids") List<Long> ids);
    
    @Query("SELECT count(u) FROM User u WHERE  u.id IN :ids AND (u.role.id = 4 OR u.role.id = 5)")
    long countDoctorsByIdList(@Param("ids") List<Long> ids);
    
 // Role ID 5 = Consultant Doctor (as per your original logic)
    @Query("SELECT u FROM User u WHERE  u.role.id = 5 OR  u.isConsultant = true")
    List<User> findConsultantDoctors();
    
    @Query("SELECT u FROM User u WHERE u.id IN :idList")
    List<User> findUsersByIdList(@Param("idList") List<Long> idList);
    
    @Query("SELECT u FROM User u WHERE u.id IN :idList")
    Page<User> findUsersByIdList(@Param("idList") List<Long> idList,Pageable page);
    
    @Query("SELECT u.id FROM User u WHERE u.id IN :idList")
    List<Long> findUsersIdByIdList(@Param("idList") List<Long> idList);
    /////////////
    
//    @Query("SELECT u FROM User u WHERE u.role.id = 2 and u.active=true")
//    List<User> getAdminsList();
    
//    @Query("SELECT u FROM User u WHERE u.role.name = 'CONSULTANT_DOCTOR'")
//    List<User> getConsultantDoctorList();
    
//    @Query("SELECT u FROM User u WHERE u.role.name = 'TECHNICIAN'")
//    List<User> getTechniciansList();
    
//    @Query("SELECT u FROM User u WHERE u.role.name = 'RESIDENT_DOCTOR'")
//    List<User> getResidentDoctorsList();
    ///////////////////

    @Query("select u from User u where u.id in :idList and u.role.id >= 2 ORDER BY u.createdDt ASC")
    List<User> findForOrgAdmin(@Param("idList") List<Long> idList);
    
    @Query("select u from User u where u.id in :idList and u.role.id >= 2 ORDER BY u.createdDt ASC")
    Page<User> findForOrgAdmin(@Param("idList") List<Long> idList,Pageable page);
    
    @Query("select u.id from User u where u.id in :idList and u.role.id >= 2 ORDER BY u.createdDt ASC")
    List<Long> findIdForOrgAdmin(@Param("idList") List<Long> idList);
    
    @Query("SELECT DISTINCT u.role.name FROM User u JOIN u.orgs o WHERE o.id = :orgId")
    List<String> findRolesForAdmin(@Param("orgId") Long orgId);

    @Query("select u from User u where u.id in :idList and u.role.id > 1 ORDER BY u.createdDt ASC")
    List<User> findForUserAdmin(@Param("idList") List<Long> idList);
    
    @Query("select u from User u where u.id in :idList and u.role.id > 1 ORDER BY u.createdDt ASC")
    Page<User> findForUserAdmin(@Param("idList") List<Long> idList,Pageable page);
    
    @Query("select u.id from User u where u.id in :idList and u.role.id > 1 ORDER BY u.createdDt ASC")
    List<Long> findIdForUserAdmin(@Param("idList") List<Long> idList);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.id IN :ids AND u.active = true AND u.role.name = 'CONSULTANT_DOCTOR'")
    Long countActiveConsultantsByUserIds(@Param("ids") List<Long> ids);
    
   
    @Query("SELECT COUNT(u) FROM User u WHERE u.id IN :ids AND u.active = true AND u.role.id >= 2")
    long countActiveUsersWithRoleAtLeast2ByIds(@Param("ids") List<Long> ids);
    

    @Query("SELECT COUNT(u) FROM User u WHERE  u.id IN :ids AND (u.role.id = 4 OR u.role.id = 5) AND u.active=true")
    long findAllActiveDoctorsByIdList(@Param("ids") List<Long> ids);
    
 // Update password without loading entity
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.password = :newPassword, u.onetimePwdStatus = true, " +
           "u.lastUpdatedBy = :userId, u.lastUpdatedDt = CURRENT_TIMESTAMP " +
           "WHERE u.id = :userId")
    int updatePassword(@Param("userId") Long userId, @Param("newPassword") String newPassword);
    
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.password = :newPassword, u.onetimePwdStatus = true, " +
           "u.lastUpdatedBy = :userId, u.lastUpdatedDt = CURRENT_TIMESTAMP , u.lockVersion = u.lockVersion + 1" +
           "WHERE u.id = :userId AND u.lockVersion = :lockVersion")
    int updatePassword(@Param("userId") Long userId, @Param("newPassword") String newPassword, @Param("lockVersion") Long lockVersion);
    
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.jwtToken = :jwtToken,  u.lastUpdatedDt = CURRENT_TIMESTAMP, " +
           "u.lastUpdatedBy = :userId " +
           "WHERE u.id = :userId")
    int updateToken(@Param("userId") Long userId, @Param("jwtToken") String jwtToken);
    
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.jwtToken = :jwtToken,  u.lastUpdatedDt = CURRENT_TIMESTAMP, " +
           "u.lastUpdatedBy = :userId , u.lockVersion = u.lockVersion + 1" +
           "WHERE u.id = :userId AND u.lockVersion = :lockVersion")
    int updateToken(@Param("userId") Long userId, @Param("jwtToken") String jwtToken, @Param("lockVersion") Long lockVersion);
    
    @Query("SELECT u FROM User u WHERE u.role.name = 'CONSULTANT_DOCTOR'")
    Page<User> findConsultantDoctors(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.role.name = 'ADMIN'")
    Page<User> findAllAdmins(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.role.name = 'RESIDENT_DOCTOR'")
    Page<User> findAllResidentDoctors(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.role.name = 'TECHNICIAN'")
    Page<User> findAllTechnicians(Pageable pageable);
    
    @Query("SELECT u FROM User u")
    Page<User> findAllUsers(Pageable pageable);
    
    @Query("SELECT u FROM User u where u.id=1")
    Page<User> findSuperAdmin(Pageable pageable);
    
    @Query("select u from User u where u.id in :idList and (u.role.id = 2 OR u.role.id = 3) AND (LOWER(u.firstName) LIKE LOWER(CONCAT(:search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT(:search, '%'))) ORDER BY u.createdDt DESC")
    Page<User> getUsersForOrgAdmin(@Param("idList") List<Long> idList, @Param("search") String search,Pageable page);
    
    @Query("select u from User u where u.id in :idList AND (LOWER(u.firstName) LIKE LOWER(CONCAT(:search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT(:search, '%'))) ORDER BY u.createdDt DESC")
    Page<User> getAllUsersForOrgAdmin(@Param("idList") List<Long> idList,@Param("search") String search,Pageable page);
    
    @Query("select u from User u where u.id in :idList and (u.role.id = 4 OR u.role.id = 5) AND (LOWER(u.firstName) LIKE LOWER(CONCAT(:search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT(:search, '%'))) ORDER BY u.createdDt DESC")
    Page<User> getDoctorsForOrgAdmin(@Param("idList") List<Long> idList,@Param("search") String search,Pageable page);
    
    @Query("select u from User u where u.id in :idList and (u.role.id = 5) AND (LOWER(u.firstName) LIKE LOWER(CONCAT(:search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT(:search, '%'))) ORDER BY u.createdDt DESC")
    Page<User> getConsultantDoctorsForOrgAdmin(@Param("idList") List<Long> idList,@Param("search") String search,Pageable page);
    
    @Query("select u from User u where u.id in :idList and (u.role.id = 4) AND (LOWER(u.firstName) LIKE LOWER(CONCAT(:search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY u.createdDt DESC")
    Page<User> getResidentDoctorsForOrgAdmin(@Param("idList") List<Long> idList,@Param("search") String search,Pageable page);
    
    @Query("select u from User u where u.id in :idList and (u.role.id = 2) AND (LOWER(u.firstName) LIKE LOWER(CONCAT(:search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT(:search, '%'))) ORDER BY u.createdDt DESC")
    Page<User> getAdminsForOrgAdmin(@Param("idList") List<Long> idList,@Param("search") String search,Pageable page);
    
    @Query("select u from User u where (u.role.id = 2) ORDER BY u.createdDt DESC")
    Page<User> getAllAdmins(@Param("idList") List<Long> idList,Pageable page);
    
    
    @Query("select u from User u where u.id in :idList and (u.role.id = 3) AND (LOWER(u.firstName) LIKE LOWER(CONCAT(:search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT(:search, '%'))) ORDER BY u.createdDt DESC")
    Page<User> getTechniciansForOrgAdmin(@Param("idList") List<Long> idList,@Param("search") String search,Pageable page);
    
    
    

    @Query("SELECT u FROM User u WHERE u.tokenHash = :hash AND u.revoked = false AND u.email = :email")
	Optional<User> findByTokenHashAndRevokedFalseAndEmail(@Param("hash") String hash,@Param("email") String email);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.mfaEnabled = :status, u.deviceInfo = :userAgent, u.lastUpdatedDt = CURRENT_TIMESTAMP, " +
           "u.lastUpdatedBy = :userId , u.lockVersion = u.lockVersion + 1" +
           "WHERE u.id = :userId AND u.lockVersion = :lockVersion")
	int updateMfaStatus(@Param("userId") Long userId, @Param("status") boolean status, @Param("lockVersion") Long lockVersion,@Param("userAgent") String userAgent);
}