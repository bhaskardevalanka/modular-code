package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techvedika.harmonycvi.gateway.entity.UserStudies;
import com.techvedika.harmonycvi.gateway.repository.StudyExtensionRepository;
import com.techvedika.harmonycvi.gateway.repository.UserStudiesRepository;
import com.techvedika.harmonycvi.gateway.service.UserStudiesService;

@Service
@Transactional
public class UserStudiesServiceImpl implements UserStudiesService {
	
	@Autowired
	private UserStudiesRepository userStudiesRepo;
	
	@Autowired
    private StudyExtensionRepository studyExtensionRepo;
	
	
	@Override
	public void deleteUserStudy(String studyId, Long doctorId) {
		userStudiesRepo.deleteByStudyIdAndDoctorId(studyId, doctorId);
		
	}

	@Override
	public List<String> getStudyIdByDoctor(Long doctorId) {
		return userStudiesRepo.findStudyIdByDoctorId(doctorId);
	}

	@Override
	public Long getAllActiveStudyByOrg(Long orgId) {
		return studyExtensionRepo.countByOrgId(orgId);
	}

	@Override
	public boolean getUserStudiesByStudiesIdAndDoctorId(String studyId, Long doctorId) {
		return userStudiesRepo.existsByStudyIdAndUserId(studyId, doctorId);
	}

	@Override
	public void updateStudyStatus(String studyId, String status) {
		Optional<String> existingStatus = studyExtensionRepo.findStatusByStudyInstanceUID(studyId);
		if(existingStatus.isPresent() && existingStatus.get().equalsIgnoreCase("Unassigned")){
			int maxRetries = 3;
			int attempts = 0;
			boolean updatedSuccessfully = false;

			while (attempts < maxRetries && !updatedSuccessfully) {
			    attempts++;
			    Optional<Long> lockVersion = studyExtensionRepo.findLockVersionByStudyId(studyId);
				if(lockVersion.isEmpty()) {
					System.out.println("No row exists to update");
					break;
				}
			    int updated = studyExtensionRepo.updateStatusByStudyInstanceUID(status, studyId,lockVersion.get());
				if (updated == 1) {
			        updatedSuccessfully = true;
			    } else {
			        // Optional: wait a bit before retrying
			        try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // 100ms backoff
			    }
			}
			if (!updatedSuccessfully) {
				System.out.println("Not updated successfully");
			}
			
		}
	}
	
	@Override
    public UserStudies saveOrUpdate(UserStudies us) {
        return userStudiesRepo.save(us);
    }

	@Override
    public void add(UserStudies us) { userStudiesRepo.save(us); }
}
