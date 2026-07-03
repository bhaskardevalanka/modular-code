package com.techvedika.harmonycvi.gateway.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.VersionInfo;


@Repository
public interface VersionInfoRepository extends JpaRepository<VersionInfo, Long>{
	
	Optional<VersionInfo> findByType(String type);

}
