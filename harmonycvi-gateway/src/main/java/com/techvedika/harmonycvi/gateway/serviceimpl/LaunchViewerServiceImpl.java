package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.repository.UserRepository;
import com.techvedika.harmonycvi.gateway.security.SecurityUtil;
import com.techvedika.harmonycvi.gateway.service.CommonMethod;
import com.techvedika.harmonycvi.gateway.service.LaunchViewerService;

@Service
public class LaunchViewerServiceImpl implements LaunchViewerService {
	private static final Logger LOG = LoggerFactory.getLogger(LaunchViewerServiceImpl.class);
	
	@Value("${weasis.base-url}")
	String weasisBaseUrl;
	
	@Value("${server.gateway-url}")
	String serverUrl;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	CommonMethod commonMethod;

	@Autowired
	PacsTokenService pacsTokenService;
	
    @Override
    public String launchViewer(String studyUID, String orgId, String token,String isExternalApp,String authorization) {
        	try {
        		
        		String email = SecurityUtil.currentUserEmailId();
        		
        		Optional<Long> userIdOpt = userRepo.findIdByEmail(email);
        		if(userIdOpt.isEmpty()) {
        			return "Unauthorized";
        		}
        		Long userId = userIdOpt.get();
                // Get org-specific DICOM-RS URL, fallback to default
                String pacsUrl = commonMethod.getPacsUrlForViewer(orgId);
                String keyCloaktoken = pacsTokenService.getToken(orgId);
        		keyCloaktoken = "Bearer "+keyCloaktoken;
                // Encode each component
                String encodedRsUrl = URLEncoder.encode(pacsUrl, StandardCharsets.UTF_8.toString());
                String encodedStudyUID = URLEncoder.encode("studyUID=" + studyUID, StandardCharsets.UTF_8.toString());
                String encodedToken = URLEncoder.encode("token=" + token, StandardCharsets.UTF_8.toString());
                String encodedOrgId = URLEncoder.encode("orgId=" + orgId, StandardCharsets.UTF_8.toString());
                String encodedUserId = URLEncoder.encode("userId=" + userId, StandardCharsets.UTF_8.toString());
                String encodedServerurl = URLEncoder.encode("serverUrl=" + serverUrl.strip(), StandardCharsets.UTF_8.toString());
                String encodedKeyCloakToken = URLEncoder.encode("Authorization=" + keyCloaktoken, StandardCharsets.UTF_8.toString());
                String externalApp = URLEncoder.encode("externalApp=" + isExternalApp.strip(), StandardCharsets.UTF_8.toString());
                String encodedCdb = URLEncoder.encode(weasisBaseUrl + "/weasis", StandardCharsets.UTF_8.toString());
                String encodedCdbExt = URLEncoder.encode(weasisBaseUrl + "/weasis-ext", StandardCharsets.UTF_8.toString());

                // Build the final launch URL
                String launchingUrl = String.format(
                        "weasis://%%24dicom%%3Ars+--url+\"%s\"+-r+\"%s\"+\"%s\"+\"%s\"+\"%s\"+\"%s\"+\"%s\"+\"%s\"+%%24weasis%%3Aconfig+cdb%%3D\"%s\"+cdb-ext%%3D\"%s\"",
                        encodedRsUrl, encodedStudyUID, encodedToken, encodedOrgId, encodedUserId,encodedServerurl,externalApp,encodedKeyCloakToken, encodedCdb, encodedCdbExt
                );
                LOG.info("Launching URL:"+launchingUrl);
                return launchingUrl;

            } catch (Exception e) {
            	LOG.error("Error creating Weasis launch URL:"+e.getLocalizedMessage());
                throw new RuntimeException("Error creating Weasis launch URL", e);
            }
        }
    

}
