package com.techvedika.harmonycvi.gateway.entity;

import java.util.List;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class LocationDao {

    @PersistenceContext
    private EntityManager em;

    public List<String> getStudyStoragePath(String studyId) {
        return em.createNativeQuery(
                "SELECT l.storage_path " +
                "FROM public.location l " +
                "JOIN public.instance i ON l.instance_fk = i.pk " +
                "JOIN public.series s ON i.series_fk = s.pk " +
                "JOIN public.study st ON s.study_fk = st.pk " +
                "WHERE st.study_iuid = :studyId")
            .setParameter("studyId", studyId)
            .getResultList();
    }
}
