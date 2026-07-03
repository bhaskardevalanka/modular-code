package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.entity.StudyExtension;
import com.techvedika.harmonycvi.gateway.repository.StudyExtensionRepository;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;

@Service
public class AsyncDeleteServiceImpl {
	
	private static final Logger LOG = LoggerFactory
			.getLogger(AsyncDeleteServiceImpl.class);

    private RestTemplate restTemplate;

    private StudyCleanupService cleanupService;
    
    private StudyArchiveServiceImpl studyArchiveServiceImpl;
    
    private UserRepository userRepo;
    
    private StudyExtensionRepository studyExtensionRepo;

    private PacsTokenService pacsTokenService;
	
	public AsyncDeleteServiceImpl(RestTemplate restTemplate,StudyCleanupService cleanupService,
			StudyArchiveServiceImpl studyArchiveServiceImpl,UserRepository userRepo,StudyExtensionRepository studyExtensionRepo,
			PacsTokenService pacsTokenService) {
		this.restTemplate = restTemplate;
		this.cleanupService = cleanupService;
		this.studyArchiveServiceImpl = studyArchiveServiceImpl;
		this.userRepo = userRepo;
		this.studyExtensionRepo = studyExtensionRepo;
		this.pacsTokenService = pacsTokenService;
	}


    @Value("${dcm4cheeBaseUrl}")
	private String dcm4cheeBaseUrl;
    
    @Value("${upload.to-s3-deleted}")
	private String uploadTOS3;

    @Async
    public void performAsyncDelete(String studyInstanceUID, String userEmailId) {

        try {
            
            
            if(userEmailId!=null && uploadTOS3.contains("on")) {
    			callArchiving(userEmailId,studyInstanceUID);
            }
            
            deleteStudy(userEmailId, studyInstanceUID);
            LOG.info("Study deleted successfully: {}" , studyInstanceUID);

            // 3️⃣ Cleanup
            
            cleanupService.cleanUp(studyInstanceUID);

            LOG.info("Cleanup done for {}", studyInstanceUID);
        } catch (Exception e) {
            LOG.error("❌ Async deletion failed for {} : {} ",studyInstanceUID, e.getMessage());
            e.printStackTrace();
        }
    }

    
    private boolean deleteStudy(String userEmailId, String studyInstanceUID) {
        try {

            String deleteUrl = dcm4cheeBaseUrl 
                    + "/aets/DCM4CHEE/rs/studies/" 
                    + studyInstanceUID;

            if (userEmailId == null || userEmailId.isEmpty()) {
                userEmailId = "admin@techvedika.com";
            }

            String token = pacsTokenService.getTokenWithEmail(null, userEmailId);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<Void> deleteRequest = new HttpEntity<>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    deleteUrl,
                    HttpMethod.DELETE,
                    deleteRequest,
                    Void.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                LOG.info("Study deleted successfully: {}", studyInstanceUID);
                return true;
            }

            return false;

        } catch (HttpClientErrorException.NotFound e) {
            LOG.warn("Study not found: {}", studyInstanceUID);
            return true; // already deleted

        } catch (Exception e) {
            LOG.error("Delete failed: ", e);
            return false;
        }
    }
    
    private void callArchiving(String userEmailId,String studyInstanceUID) {
    	Optional<String> roleName = userRepo.findRoleNameByEmail(userEmailId);
    	if (roleName.isPresent() && roleName.get().equalsIgnoreCase(UserConstants.SUPER_ADMIN)) {
    		Optional<StudyExtension> seObj = studyExtensionRepo.findByStudyId(studyInstanceUID);
    		if(seObj.isPresent()) {
	        	List<StudyExtension> studiesList = new ArrayList<>();
	        	studiesList.add(seObj.get());
	        	studyArchiveServiceImpl.archiveStudies(studiesList);
    		}
    	}
    }
}
