package com.techvedika.harmonycvi.gateway.controller;

import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvedika.harmonycvi.gateway.dto.AssignPatientRequest;
import com.techvedika.harmonycvi.gateway.dto.CreateUserRequest;
import com.techvedika.harmonycvi.gateway.dto.ForgotPasswordRequest;
import com.techvedika.harmonycvi.gateway.dto.LoginRequest;
import com.techvedika.harmonycvi.gateway.dto.ResetPasswordRequest;
import com.techvedika.harmonycvi.gateway.dto.UpdateUserRequest;
import com.techvedika.harmonycvi.gateway.dto.UserListRequest;
import com.techvedika.harmonycvi.gateway.exception.ResponseUtil;
import com.techvedika.harmonycvi.gateway.service.PrivilegesService;
import com.techvedika.harmonycvi.gateway.service.UserService;
import com.techvedika.harmonycvi.gateway.service.VersionService;
import com.techvedika.harmonycvi.gateway.util.ApiException;
import com.techvedika.harmonycvi.gateway.util.UserUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserService userService;

    @Autowired
    private PrivilegesService privilegesService;

    @Autowired
    private UserUtils userUtils;
    
    @Autowired
    private VersionService versionService;
    

   /* @Operation(
            summary = "login user",
            description = "This endpoint allows user to login",
            security = {}
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User Login successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
   @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try{
        	Map<String, Object> json = objectMapper.convertValue(loginRequest, Map.class);

            LOG.info("Service hit===========>" + json);
            JSONObject response = userService.login(new JSONObject(json));
            return ResponseEntity.ok(response);
        } catch (ApiException ae) {
            LOG.error("ApiException:", ae);
            // Clean response without stack trace
            return ResponseUtil.buildErrorResponse(ae);
        }
    }*/
   
    
   @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest,@CookieValue(value = "mfa_trust", required = false) String mfaToken,@RequestHeader(value = "User-Agent", required = false) String userAgent) {
        try{
        	Map<String, Object> json = objectMapper.convertValue(loginRequest, Map.class);
        	System.out.println("userAgent-------------------"+userAgent);
            LOG.info("Service hit===========>" + json);
            JSONObject response = userService.login(new JSONObject(json),mfaToken,userAgent);
            return ResponseEntity.ok(response);
        } catch (ApiException ae) {
            LOG.error("ApiException:", ae);
            // Clean response without stack trace
            return ResponseUtil.buildErrorResponse(ae);
        }
    }
   
    
    @Operation(
            summary = "users list",
            description = "This endpoint to get the users list",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users Fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
    @PostMapping(value = "/getList", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getList(HttpServletRequest request, @RequestBody UserListRequest userListRequest) {

//        try {
    	Map<String, Object> json = objectMapper.convertValue(userListRequest, Map.class);
            LOG.info("Service hit===========> {}", json);
            JSONObject versionResponse = versionService.checkVersion(request);
            if (versionResponse != null) {
                return ResponseEntity.ok(versionResponse);
            }
            JSONObject response = userService.getList(new JSONObject(json));
            return ResponseEntity.ok(response);

//        } catch (ApiException ae) {
//            LOG.error("ApiException:", ae);
//            return ResponseEntity
//                    .status(ae.getStatus())
//                    .body(ae.getBody());
//
//        }
    }

	@GetMapping(value = "/getAll", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getAll(HttpServletRequest request) {

		try {
			
			var versionResp = versionService.checkVersion(request);
			if (versionResp != null) {
				return ResponseEntity.ok(versionResp);
			}

			JSONObject response = userService.getAll();
			return ResponseEntity.ok(response);

		} catch (ApiException ae) {
			LOG.error("ApiException:", ae);
			return ResponseEntity 
					.status(ae.getStatus()).body(ae.getBody());

		}
	}
	
	@Operation(
            summary = "Create User",
            description = "This endpoint allows to create the user ",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createUser(@RequestBody CreateUserRequest createUserRequest, HttpServletRequest request) {
		var versionResp = versionService.checkVersion(request);
		if (versionResp != null) {
			return ResponseEntity.ok(versionResp);
		}
		Map<String, Object> jsonMap = objectMapper.convertValue(createUserRequest, Map.class);
		JSONObject json = new JSONObject(jsonMap);
		JSONObject resp = userService.registerNewUserAccount(json, request,true,null);
        return ResponseEntity.ok(resp);

	}
	
	@Operation(
            summary = "Update User",
            description = "This endpoint allows to update the user ",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User details updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest updateUserRequest , HttpServletRequest request) {
	    try {
	        var versionResp = versionService.checkVersion(request);
	        if (versionResp != null) return ResponseEntity.ok(versionResp);
	        Map<String, Object> jsonMap = objectMapper.convertValue(updateUserRequest, Map.class);
			JSONObject json = new JSONObject(jsonMap);
	        JSONObject resp = userService.updateUser(json, request);
	        return ResponseEntity.ok(resp);

	    } catch (ApiException ae) {
	        return ResponseEntity.status(ae.getStatusCode()).body(ae.getResponseBody());
	    }
	}
	
	@Operation(
            summary = "reset password",
            description = "This endpoint allow to reset the user password",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reset password successfull"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
	@PostMapping(value = "/resetPassword", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest, HttpServletRequest request) {
	    try {
	    	Map<String, Object> jsonMap = objectMapper.convertValue(resetPasswordRequest, Map.class);
	    	JSONObject json = new JSONObject(jsonMap);
	        return ResponseEntity.ok(userService.resetPassword(json, request));
	    } catch (ApiException ae) {
	        return ResponseEntity.status(ae.getStatusCode()).body(ae.getResponseBody());
	    }
	}
	
	@Operation(
            summary = "Forgot password",
            description = "This endpoint sends OTP to reset the password",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Forgot password successfull"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
	@PostMapping(value = "/forgotPassword", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest ) {
		Map<String, Object> jsonMap = objectMapper.convertValue(forgotPasswordRequest, Map.class);
    	JSONObject json = new JSONObject(jsonMap);
	        return ResponseEntity.ok(userService.forgotPassword(json));
	    
	}
	
	@GetMapping(value = "/privileges", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getUserPrivileges() {
	    try {
	        return ResponseEntity.ok(privilegesService.getList());
	    } catch (ApiException ae) {
	        return ResponseEntity.status(ae.getStatusCode()).body(ae.getResponseBody());
	    }
	}
	
	@Operation(
            summary = "logout user",
            description = "This endpoint allows user to logout",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged out successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
	@PostMapping(value = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> logout(@RequestBody UserListRequest userListRequest) {
	    try {
	        return ResponseEntity.ok(userService.logout());
	    } catch (ApiException ae) {
	        return ResponseEntity.status(ae.getStatusCode()).body(ae.getResponseBody());
	    }
	}
	
	
	@Operation(
            summary = "Get User details",
            description = "This endpoint allows to fetch the user details",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "fetched user details successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
	@GetMapping(value = "/getUser/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getUser(@PathVariable String userId, HttpServletRequest request) {
	    try {
	        var versionResp = versionService.checkVersion(request);
	        if (versionResp != null) return ResponseEntity.ok(versionResp);

	        return ResponseEntity.ok(userService.getUser(userId));
	    } catch (ApiException ae) {
	        return ResponseEntity.status(ae.getStatusCode()).body(ae.getResponseBody());
	    }
	}
	
	
	@Operation(
            summary = "Assign patient",
            description = "This endpoint allows to assign the patient",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "assigned patient successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
	@PostMapping(value = "/assignPatient", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> assignPatient(@RequestBody AssignPatientRequest assignPatientRequest , HttpServletRequest request) {
	    try {
	        var versionResp = versionService.checkVersion(request);
	        if (versionResp != null) return ResponseEntity.ok(versionResp);
	        Map<String, Object> jsonMap = objectMapper.convertValue(assignPatientRequest, Map.class);
	 		JSONObject json = new JSONObject(jsonMap);
	        return ResponseEntity.ok(userService.assignPatient(json));
	    } catch (ApiException ae) {
	        return ResponseEntity.status(ae.getStatusCode()).body(ae.getResponseBody());
	    }
	}
	
	@GetMapping(value = "/acceptInvitation/{userId}/{orgId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> acceptInvitation(@PathVariable String userId, @PathVariable String orgId) {
	    try {
	        return ResponseEntity.ok(userService.acceptInvitation(userId, orgId));
	    } catch (ApiException ae) {
	        return ResponseEntity.status(ae.getStatusCode()).body(ae.getResponseBody());
	    }
	}
	
	
	@Operation(
            summary = "Get list of doctors",
            description = "This endpoint allows to fetch the list of doctors",
            security = { @SecurityRequirement(name = "jwtAuth") }
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "fetched list of doctors successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
        })
	@GetMapping(value = "/getDoctorList/{orgId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getDoctorList(@PathVariable String orgId, HttpServletRequest request) {
	    try {
	        var versionResp = versionService.checkVersion(request);
	        if (versionResp != null) return ResponseEntity.ok(versionResp);

	        return ResponseEntity.ok(userService.getDoctorList(orgId));
	    } catch (ApiException ae) {
	        return ResponseEntity.status(ae.getStatusCode()).body(ae.getResponseBody());
	    }
	}
	
	@GetMapping(value = "/deleteUserStudy/{studyId}/{doctorId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> deleteUserStudy(@PathVariable String studyId, @PathVariable String doctorId, HttpServletRequest request) {
	    try {
	        var versionResp = versionService.checkVersion(request);
	        if (versionResp != null) return ResponseEntity.ok(versionResp);

	        return ResponseEntity.ok(userService.deleteUserStudy(studyId, doctorId));
	    } catch (ApiException ae) {
	        return ResponseEntity.status(ae.getStatusCode()).body(ae.getResponseBody());
	    }
	}
	
	@GetMapping(value = "/getConsultantDoctors", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getConsultantDoctors() {
	    try {
	        return ResponseEntity.ok(userService.getConsultantDoctors());
	    } catch (ApiException ae) {
	        return ResponseEntity.status(ae.getStatusCode()).body(ae.getResponseBody());
	    }
	}
	
	@GetMapping(value = "/getConsultant/{emailId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getConsultant(@PathVariable String emailId,HttpServletRequest request) {
	    try {
	        var versionResp = versionService.checkVersion(request);
	        if (versionResp != null) return ResponseEntity.ok(versionResp);

	        return ResponseEntity.ok(userService.getConsultant(emailId));
	    } catch (ApiException ae) {
	        return ResponseEntity.status(ae.getStatusCode()).body(ae.getResponseBody());
	    }
	}
	
	@GetMapping(value = "/licenseCheck/{orgId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> licenseCheck(@PathVariable String orgId) {
	    try {
	        return ResponseEntity.ok(userService.licenseCheck(orgId));
	    } catch (ApiException ae) {
	        return ResponseEntity.status(ae.getStatusCode()).body(ae.getResponseBody());
	    }
	}

    @PostMapping("/google/login")
    public ResponseEntity<?> loginWithGoogle(@RequestBody JSONObject json,@RequestHeader(value = "Type", required = false) String Type) throws Exception {
        try {
        	  System.out.println("Type---------------- " + Type);
        	return ResponseEntity.ok(userService.loginWithGoogle(json,Type));
        } catch (ApiException ae) {
            //return ResponseEntity.badRequest().body("Invalid Google token");
        	return ResponseEntity.status(ae.getStatusCode()).body(ae.getResponseBody());
        }
    }


}