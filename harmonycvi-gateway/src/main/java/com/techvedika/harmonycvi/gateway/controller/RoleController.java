package com.techvedika.harmonycvi.gateway.controller;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techvedika.harmonycvi.gateway.exception.ResponseUtil;
import com.techvedika.harmonycvi.gateway.service.RoleService;
import com.techvedika.harmonycvi.gateway.service.VersionService;
import com.techvedika.harmonycvi.gateway.util.ApiException;
import com.techvedika.harmonycvi.gateway.util.UserUtils;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/role", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoleController {
	private static final Logger log = LoggerFactory.getLogger(RoleController.class);

	@Autowired
    private RoleService roleService;
	
	@Autowired
    private UserUtils userUtils;
	
	@Autowired
    private VersionService versionService;

    /**
     * GET /role/getList – returns all roles visible to the current user.
     */
    @GetMapping("/getList")
    public ResponseEntity<JSONObject> getList(HttpServletRequest request) {
        try {
            JSONObject versionResponse = versionService.checkVersion(request);
            if (versionResponse != null) {
            	return ResponseEntity.ok(versionResponse);
            }
            
            return ResponseEntity.ok(roleService.getList());

        } catch (ApiException ae) {
            // Clean response without stack trace
            return ResponseUtil.buildErrorResponse(ae);
        }
    }
}