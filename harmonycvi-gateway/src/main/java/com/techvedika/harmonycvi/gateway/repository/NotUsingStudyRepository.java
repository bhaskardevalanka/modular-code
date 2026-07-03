package com.techvedika.harmonycvi.gateway.repository;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class NotUsingStudyRepository {
	
	@PersistenceContext
    private EntityManager entityManager;
	
//	 // 1. Get single study by ID
//    public Object[] getStudyById(Long id) {
//        String sql = "SELECT id, name, status FROM public.study WHERE id = :id";
//        Query query = entityManager.createNativeQuery(sql);
//        query.setParameter("id", id);
//        //return (Object[]) query.getSingleResult();  // returns a single Object[]
//        List<Object[]> rows = query.getResultList();
//        if(rows != null && rows.size() > 0) {
//        	List<StudyDTO> dtos = rows.stream()
//        		    .map(r -> new StudyDTO((Long)r[0], (String)r[1], (String)r[2]))
//        		    .collect(Collectors.toList());
//        }
//        
//    }
//
//    // 2. Get studies by status
//    public List<Object[]> getStudiesByStatus(String status) {
//        String sql = "SELECT id, name, status FROM public.study WHERE status = :status";
//        Query query = entityManager.createNativeQuery(sql);
//        query.setParameter("status", status);
//        return query.getResultList();  // returns List<Object[]>
//    }
//
//    // 3. Join query: study + patient by studyInstanceUID
//    public List<Object[]> findStudyWithPatientByUID(String studyInstanceUID) {
//        String sql = """
//            SELECT s.id AS study_id, s.study_instance_uid,
//                   p.id AS patient_id, p.name AS patient_name,
//                   p.patient_height, p.patient_weight
//            FROM public.study s
//            JOIN public.patient p ON s.patient_fk = p.id
//            WHERE s.study_instance_uid = :studyInstanceUID
//        """;
//        Query query = entityManager.createNativeQuery(sql);
//        query.setParameter("studyInstanceUID", studyInstanceUID);
//        return query.getResultList();  // returns List<Object[]>
//    }

//	@Query(value = "SELECT id, name, status FROM public.study WHERE id = :id", nativeQuery = true)
//	Object[] getStudyById(@Param("id") Long id);
//
//	@Query(value = "SELECT id, name, status FROM public.study WHERE status = :status", nativeQuery = true)
//	List<Object[]> getStudiesByStatus(@Param("status") String status);
//
//	@Query(value = "SELECT s.id AS study_id, s.study_instance_uid,p.id AS patient_id, p.name AS patient_name, p.patient_height, p.patient_weight FROM public.study s JOIN public.patient p ON s.patient_fk = p.id WHERE s.study_instance_uid = :studyInstanceUID", nativeQuery = true)
//	List<Object[]> findStudyWithPatientByUID(@Param("studyInstanceUID") String studyInstanceUID);
}