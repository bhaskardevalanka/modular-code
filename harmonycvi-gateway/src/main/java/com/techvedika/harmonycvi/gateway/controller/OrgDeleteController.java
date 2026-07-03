package com.techvedika.harmonycvi.gateway.controller;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.exception.ResponseUtil;
import com.techvedika.harmonycvi.gateway.repository.OrganizationRepository;
import com.techvedika.harmonycvi.gateway.security.SecurityUtil;
import com.techvedika.harmonycvi.gateway.serviceimpl.AsyncDeleteOrgServiceImpl;
import com.techvedika.harmonycvi.gateway.util.ApiException;
import com.techvedika.harmonycvi.gateway.util.UserUtils;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/organization")
public class OrgDeleteController {
	
	private static final Logger LOG = LoggerFactory.getLogger(OrgDeleteController.class);
	private AsyncDeleteOrgServiceImpl asyncDeleteOrgServiceImpl;
	private final OrganizationRepository orgRepo;
    private UserUtils userUtils;
	
	public OrgDeleteController(AsyncDeleteOrgServiceImpl asyncDeleteOrgServiceImpl,
			OrganizationRepository orgRepo,
            UserUtils userUtils) {
		this.asyncDeleteOrgServiceImpl = asyncDeleteOrgServiceImpl;
		this.orgRepo = orgRepo;
		this.userUtils = userUtils;
	}

	@PostMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONObject> deleteOrg(@RequestBody JSONObject json, HttpServletRequest request) {
        try {
            LOG.info("Service hit===========>{}" , json);
            return ResponseEntity.ok(deleteOrg(json));
        } catch (ApiException ae) {
            LOG.error("ApiException:", ae);
            // Clean response without stack trace
            return ResponseUtil.buildErrorResponse(ae);
        }
    }
	
	public JSONObject deleteOrg(JSONObject json) {
		JSONObject resp = new JSONObject();
		Long orgId = Long.valueOf(json.get("orgId").toString());
		orgRepo.deleteOrg(orgId, true);
		resp = userUtils.updateResponse(resp, UserConstants.STATUS_SUCCESS, "Organization deleted successfully.",
				"Organization deleted successfully.", null);
		String userEmailId = SecurityUtil.currentUserEmailId();
		asyncDeleteOrgServiceImpl.performAsyncDeleteOrg(json, userEmailId);

		return resp;
	}
}
