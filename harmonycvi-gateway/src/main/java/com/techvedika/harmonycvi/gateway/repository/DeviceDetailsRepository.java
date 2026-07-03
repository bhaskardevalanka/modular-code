package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.DeviceDetails;

import jakarta.transaction.Transactional;

@Repository
public interface DeviceDetailsRepository extends JpaRepository<DeviceDetails, Long> {
    
    boolean existsByDevUID(String devUID);
    
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DeviceDetails d WHERE d.devUID = :devUID AND d.id <> :id")
    boolean existsByDevUIDAndIdNot(@Param("devUID") String devUID, @Param("id") Long id);
    
    Optional<DeviceDetails> findById(Long id);

	/** centre‑scoped list */
	@Query("select d from DeviceDetails d where d.center.id = :centerId ORDER BY d.createdDt DESC")
	List<DeviceDetails> findByCenterId(Long centerId);

	/** licence counters */
	@Query("select count(d) from DeviceDetails d where d.organization.id = :orgId and d.active = true")
	Long countActiveByOrg(Long orgId);

	@Query("select count(d) from DeviceDetails d where d.active = true")
	Long countAllActive();
	
	@Modifying
	@Transactional
	@Query("DELETE FROM DeviceDetails d WHERE d.organization.id = :orgId")
	void deleteByOrgId(@Param("orgId") Long orgId);
	
	@Query("SELECT c.id FROM DeviceDetails c WHERE c.organization.id = :orgId")
	Optional<Long> findIdByOrgId(@Param("orgId") Long orgId);
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM harmonycvi.user_devices c WHERE c.device_details_id = :deviceId",nativeQuery = true)
	void deleteUserDevicesByOrgId(@Param("deviceId") Long centerId);

	@Modifying
	@Transactional
	@Query("DELETE FROM DeviceDetails d WHERE d.center.id = :centerId")
	void deleteByCenterId(@Param("centerId") Long centerId);
}
