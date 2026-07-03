package com.techvedika.harmonycvi.gateway.service;

import java.util.List;

import com.techvedika.harmonycvi.gateway.entity.UserStudies;

public interface UserStudiesService {
	
	public void deleteUserStudy(String studyId,Long doctorId);

	public List<String> getStudyIdByDoctor(Long id);

	public Long getAllActiveStudyByOrg(Long orgId);

	public boolean getUserStudiesByStudiesIdAndDoctorId(String studyId, Long doctorId);

	public void updateStudyStatus(String studyId, String status);
	
	///
	///
	//List<UserStudies> getUserStudyListByStudyIdList(List<String> studyIds);

    public UserStudies saveOrUpdate(UserStudies us);// add OR update
    public void add(UserStudies us);

    //Optional<UserStudies> getUserStudiesByStudyId(String studyId);
    
    //Optional<UserStudies> getUserStudiesByStudyIdAndDoctorId(String studyId, Long doctorId);

    //void deleteUserStudy(String studyId, Long doctorId);

    //List<UserStudies> getByDoctor(Long doctorId);

    /* Statistics */
    //Long getAllActiveStudy();
    //Long getAllActiveStudyByOrg(Long orgId);

    /* Study status ↔ PACS state sync */
    //void updateStudyStatus(String studyId, String status);

   

}
