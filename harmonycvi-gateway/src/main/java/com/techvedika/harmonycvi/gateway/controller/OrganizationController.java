package com.techvedika.harmonycvi.gateway.controller;

import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvedika.harmonycvi.gateway.dto.CreateOrganizationRequest;
import com.techvedika.harmonycvi.gateway.dto.IdDto;
import com.techvedika.harmonycvi.gateway.dto.UpdateOrganizationDto;
import com.techvedika.harmonycvi.gateway.exception.ResponseUtil;
import com.techvedika.harmonycvi.gateway.service.OrganizationService;
import com.techvedika.harmonycvi.gateway.service.VersionService;
import com.techvedika.harmonycvi.gateway.util.ApiException;
import com.techvedika.harmonycvi.gateway.util.UserUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/organization")
public class OrganizationController {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationController.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private OrganizationService orgService;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private VersionService versionService;
    
    @Operation(
            summary = "Create Organization",
            description = "This endpoint allows to create the organization",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Organization created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONObject> create(@RequestBody CreateOrganizationRequest createOrganizationRequest , HttpServletRequest request) {
        try {
            JSONObject versionResponse = versionService.checkVersion(request);
            if (versionResponse != null)
                return ResponseEntity.ok(versionResponse);
            Map<String, Object> jsonMap = objectMapper.convertValue(createOrganizationRequest, Map.class);
    		JSONObject json = new JSONObject(jsonMap);

            return ResponseEntity.ok(orgService.create(json,request));
        } catch (ApiException ae) {
            LOG.error("ApiException:", ae);
            // Clean response without stack trace
            return ResponseUtil.buildErrorResponse(ae);
        } 
    }
    
    @Operation(
            summary = "Update Organization",
            description = "This endpoint allows to update the organization",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Organization updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })

    @PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONObject> update(@RequestBody UpdateOrganizationDto updateOrganizationDto , HttpServletRequest request) {
        try {
            LOG.info("Service hit===========>" + updateOrganizationDto);
            JSONObject versionResponse = versionService.checkVersion(request);
            if (versionResponse != null)
                return ResponseEntity.ok(versionResponse);
            Map<String, Object> jsonMap = objectMapper.convertValue(updateOrganizationDto, Map.class);
    		JSONObject json = new JSONObject(jsonMap);
            return ResponseEntity.ok(orgService.update(json));
        } catch (ApiException ae) {
            LOG.error("ApiException:", ae);
            // Clean response without stack trace
            return ResponseUtil.buildErrorResponse(ae);
        }
    }
    
    @Operation(
            summary = "Fetches List of Organization's",
            description = "This endpoint allows to fetch the list of organizations.",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched List of organizations successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
    @GetMapping(value = "/getList", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONObject> getList(HttpServletRequest request) {
        try {
            JSONObject versionResponse = versionService.checkVersion(request);
            if (versionResponse != null)
            	ResponseEntity.ok(versionResponse);

            return ResponseEntity.ok(orgService.getList());
        } catch (ApiException ae) {
            LOG.error("ApiException:", ae);
            // Clean response without stack trace
            return ResponseUtil.buildErrorResponse(ae);
        }
    }

    @GetMapping(value = "/getOrgList", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONObject> getOrgList() {
        try {
            return ResponseEntity.ok(orgService.getOrgList());
        } catch (ApiException ae) {
            LOG.error("ApiException:", ae);
            // Clean response without stack trace
            return ResponseUtil.buildErrorResponse(ae);
        }
    }

    
    @Operation(
            summary = "Get the Organization",
            description = "This endpoint allows to get the organization",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Organization Fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
    @PostMapping(value = "/get", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONObject> getOrgById(@RequestBody IdDto idDto, HttpServletRequest request) {
        try {
            LOG.info("Service hit===========>" + idDto);
            JSONObject versionResponse = versionService.checkVersion(request);
            if (versionResponse != null)
                return ResponseEntity.ok(versionResponse);;
                Map<String, Object> jsonMap = objectMapper.convertValue(idDto, Map.class);
        		JSONObject json = new JSONObject(jsonMap);
            return ResponseEntity.ok(orgService.getOrgById(json));
        } catch (ApiException ae) {
            LOG.error("ApiException:", ae);
            // Clean response without stack trace
            return ResponseUtil.buildErrorResponse(ae);
        }
    }

    @Operation(
            summary = "List Organizations",
            description = "This endpoint allows to fetch the organizations",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched Organizations successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })

    @GetMapping(value = "/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONObject> getAll(HttpServletRequest request, @RequestHeader("Version") String version, @RequestHeader("Type") String type) {
        try {
            JSONObject versionResponse = versionService.checkVersion(request);
            if (versionResponse != null)
                return ResponseEntity.ok(versionResponse);

            return ResponseEntity.ok(orgService.getAll());
        } catch (ApiException ae) {
            LOG.error("ApiException:", ae);
            // Clean response without stack trace
            return ResponseUtil.buildErrorResponse(ae);
        }
    }
    
    /**
     * API to check if the given orgId belongs to an external organization
     */
    @GetMapping(value = "/is-external/{orgId}",produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject isExternalOrg(@PathVariable String orgId) {
        return orgService.isExternalOrg(orgId);
    }
    
    /**
     * API to get pacsUrl for organization
     */
    @GetMapping(value = "/getPacsUrl", consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONObject> getPacsUrl(@RequestBody JSONObject requestBody,HttpServletRequest request) {
        return orgService.getPacsUrl(requestBody);
    }

    @Operation(
            summary = "Get organizations by user email",
            description = "Returns a paginated list of all organizations that the specified user is a part of.",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched organizations successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "User not found or no organizations")
        })
    @PostMapping(value = "/getOrgListByEmail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONObject> getOrgListByEmail(@RequestBody JSONObject requestBody, HttpServletRequest request) {
        try {
            JSONObject versionResponse = versionService.checkVersion(request);
            if (versionResponse != null)
                return ResponseEntity.ok(versionResponse);
            return ResponseEntity.ok(orgService.getOrgListByEmail(requestBody));
        } catch (ApiException ae) {
            LOG.error("ApiException:", ae);
            return ResponseUtil.buildErrorResponse(ae);
        }
    }
}