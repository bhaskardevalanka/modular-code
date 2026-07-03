package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techvedika.harmonycvi.gateway.entity.ParameterReference;

@Repository
public interface ParameterReferenceRepository extends JpaRepository<ParameterReference, Integer> {

    // Equivalent to: select sm from ParameterReference sm
    //@Query("SELECT p FROM ParameterReference p")
    //List<ParameterReference> getStudyParameterReference();
    
    // Equivalent to: select p from ParameterReference p where p.sex = :sex
    List<ParameterReference> findBySex(String sex);
}