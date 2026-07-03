package com.techvedika.harmonycvi.gateway.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Repository
public class StudentRepository{
	
	@PersistenceContext
    private EntityManager entityManager;
	
	//@Query(value = "SELECT id, name, study_id FROM public.student WHERE study_id = :studyId", nativeQuery = true)
	//List<Object[]> findStudentsByStudyId(@Param("studyId") Long studyId);
	
	
	public List<Object[]> findStudentsByStudyId(Long studyId) {
        String sql = "SELECT id, name, study_id FROM public.student WHERE study_id = :studyId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("studyId", studyId);
        return query.getResultList(); // returns List<Object[]>
    }
}