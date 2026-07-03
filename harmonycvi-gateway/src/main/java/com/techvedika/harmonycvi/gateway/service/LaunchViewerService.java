package com.techvedika.harmonycvi.gateway.service;

import org.springframework.stereotype.Service;

@Service
public interface LaunchViewerService {
	
	public String launchViewer(String studyId, String orgId, String token, String isExternalApp, String authorization);

}
