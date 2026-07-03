package com.techvedika.harmonycvi.gateway.controller;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techvedika.harmonycvi.gateway.exception.ResponseUtil;
import com.techvedika.harmonycvi.gateway.service.DashBoardService;
import com.techvedika.harmonycvi.gateway.service.VersionService;
import com.techvedika.harmonycvi.gateway.util.ApiException;
import com.techvedika.harmonycvi.gateway.util.UserUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/dashboard")
public class DashBoardController {

    private static final Logger LOG = LoggerFactory.getLogger(DashBoardController.class);

    @Autowired
    private DashBoardService dashBoardService;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private VersionService versionService;

    @Autowired
    private HttpServletRequest request;

    @Operation(
    	    summary = "Fetches count of users for org admin dashboard",
    	    description = "This endpoint allows an organization admin to fetch the count of users for their dashboard",
    	    security = { @SecurityRequirement(name = "jwtAuth") }
    	)
    	@ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Org admin users count fetched successfully"),
    	    @ApiResponse(responseCode = "400", description = "Validation error")
    	})
    @GetMapping("/getAllCount/{orgId}")
    public ResponseEntity<JSONObject> getAllCount(@PathVariable("orgId") String orgId) {
        try {
            LOG.info("Service hit===========>" + orgId);
            JSONObject versionResponse = versionService.checkVersion(request);
            if (versionResponse != null) return ResponseEntity.ok(versionResponse);

            return ResponseEntity.ok(dashBoardService.getAllCount(orgId));
        } catch (ApiException ae) {
            LOG.error("ApiException:", ae);
            // Clean response without stack trace
            return ResponseUtil.buildErrorResponse(ae);
        }
    }

    @Operation(
            summary = "Get Super Admin",
            description = "This endpoint allows to fetch the super admin",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched Super Admin successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
    @GetMapping("/getSuperAdmin")
    public ResponseEntity<JSONObject> getSuperAdmin() {
        try {
            JSONObject versionResponse = versionService.checkVersion(request);
            if (versionResponse != null) return ResponseEntity.ok(versionResponse);

            return ResponseEntity.ok(dashBoardService.getSuperAdmin());
        } catch (ApiException ae) {
            LOG.error("ApiException:", ae);
            // Clean response without stack trace
            return ResponseUtil.buildErrorResponse(ae);
        }
    }

    @Operation(
            summary = "Fetches Dashboard data",
            description = "This endpoint allows to fetch the dashboard data",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
    @PostMapping("/getOrgDashboard")
    public ResponseEntity<JSONObject> getOrgDashboard(@RequestBody JSONObject json) {
        try {
            LOG.info("Service hit===========>" + json);
            JSONObject versionResponse = versionService.checkVersion(request);
            if (versionResponse != null) return ResponseEntity.ok(versionResponse);

            return ResponseEntity.ok(dashBoardService.getOrgDashboard(json));
        } catch (ApiException ae) {
            LOG.error("ApiException:", ae);
            // Clean response without stack trace
            return ResponseUtil.buildErrorResponse(ae);
        }
    }
}
