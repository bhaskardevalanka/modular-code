package com.techvedika.harmonycvi.gateway.controller;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techvedika.harmonycvi.gateway.constant.CommonConstants;
import com.techvedika.harmonycvi.gateway.service.LaunchViewerService;

@RestController
@RequestMapping("/launch")
public class LaunchViewerController {

	@Autowired
	LaunchViewerService viewerService;
	/**
     * API to create and return the Weasis launch URL for a given study.
     * Example call:
     * GET /api/weasis/launch?studyUID=1.2.826...&orgId=100&token=abc123
     */
	@GetMapping("/viewer")
	public ResponseEntity<String> launchWeasis(
	        @RequestParam String studyUID,
	        @RequestParam String orgId,
	        @RequestHeader("Authorization") String tokenHeader,
	        @RequestHeader(value = "X-Client-App", required = false) String clientApp// <-- add this
	        ) {

	    String isExternalApp = clientApp != null && !clientApp.isEmpty()? clientApp: "";

	    String launchUrl = viewerService.launchViewer(studyUID, orgId, tokenHeader, isExternalApp,null);
	    return ResponseEntity.ok(launchUrl);
	}

	@PostMapping(value ="/viewer", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> launchWeasis(
	        @RequestBody JSONObject requestBody,
	        @RequestHeader("Authorization") String tokenHeader,
	        @RequestHeader(value = "X-Client-App", required = false) String clientApp
	) {
		
	    String studyUID = (String) requestBody.get("studyId");
	    String orgId = String.valueOf(requestBody.get("orgId"));

	    if (studyUID == null || orgId == null) {
	        return ResponseEntity.badRequest().body("studyUID and orgId are required");
	    }

	    String authorization = null;
	    if(requestBody.containsKey(CommonConstants.AUTHORIZATION) && !requestBody.get(CommonConstants.AUTHORIZATION).toString().isEmpty())
	    	authorization = String.valueOf(requestBody.get(CommonConstants.AUTHORIZATION));
	    String isExternalApp = (clientApp != null && !clientApp.isEmpty()) ? clientApp : "";

	    String launchUrl = viewerService.launchViewer(studyUID, orgId, tokenHeader, isExternalApp,authorization);

	    return ResponseEntity.ok(launchUrl);
	}

}
