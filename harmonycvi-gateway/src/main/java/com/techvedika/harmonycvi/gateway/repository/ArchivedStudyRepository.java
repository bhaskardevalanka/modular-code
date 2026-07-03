package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.ArchivedStudy;

@Repository
public interface ArchivedStudyRepository extends JpaRepository<ArchivedStudy, Long> {

    // Custom query methods can go here
    List<ArchivedStudy> findByStudyId(String studyId);

    List<ArchivedStudy> findByStatus(int status);
}