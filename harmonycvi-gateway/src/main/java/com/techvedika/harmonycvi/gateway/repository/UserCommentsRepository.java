package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.UserComments;

@Repository
public interface UserCommentsRepository extends JpaRepository<UserComments, Long> {
    List<UserComments> findByPatientId(String patientId);
}