package com.techvedika.harmonycvi.gateway.controller;

import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvedika.harmonycvi.gateway.dto.CreateCenterRequest;
import com.techvedika.harmonycvi.gateway.dto.UpdateCenterDto;
import com.techvedika.harmonycvi.gateway.dto.UserListRequest;
import com.techvedika.harmonycvi.gateway.service.CenterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/center")
public class CenterController {
	
	private static final Logger LOG = LoggerFactory.getLogger(CenterController.class);
	
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private CenterService centerService;

	
	@Operation(
            summary = "centers list",
            description = "This endpoint to get the centers list",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Centers Fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
	@PostMapping(value = "/getList", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public JSONObject getList(@RequestBody UserListRequest userListRequest) {
		Map<String, Object> jsonMap = objectMapper.convertValue(userListRequest, Map.class);
 		JSONObject json = new JSONObject(jsonMap);
		return centerService.getList(json);
	}
	
	@Operation(
            summary = "Create Center",
            description = "This endpoint allows to create the center",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "center created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public JSONObject create(@RequestBody CreateCenterRequest createCenterRequest) {
		 Map<String, Object> jsonMap = objectMapper.convertValue(createCenterRequest, Map.class);
 		JSONObject json = new JSONObject(jsonMap);
		return centerService.create(json);
	}
	
	@Operation(
            summary = "Update Center",
            description = "This endpoint allows to update the center",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "center updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public JSONObject update(@RequestBody UpdateCenterDto updateCenterDto) {
		Map<String, Object> jsonMap = objectMapper.convertValue(updateCenterDto, Map.class);
 		JSONObject json = new JSONObject(jsonMap);
		return centerService.update(json);
	}
	
	@GetMapping(value = "/getById/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public JSONObject getById(@PathVariable String id) {
		return centerService.getById(id);
	}
}