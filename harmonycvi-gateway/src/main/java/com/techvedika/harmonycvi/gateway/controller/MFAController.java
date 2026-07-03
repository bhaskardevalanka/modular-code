package com.techvedika.harmonycvi.gateway.controller;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techvedika.harmonycvi.gateway.service.AuthenticationService;
import com.techvedika.harmonycvi.gateway.util.ApiException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/multifactor")
public class MFAController {
	
	@Autowired 
	AuthenticationService authenticationService;
	
	
	/**
	 * Get MFA setup status for current user.
	 *
	 * @param request the HTTP servlet request for user extraction
	 * @return response entity with MFA setup status
	 */
	@GetMapping("/mfa/status")
	@Operation(summary = "Get MFA status", description = "Get current user's MFA setup status and requirements")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "MFA status retrieved"),
			@ApiResponse(responseCode = "401", description = "Unauthorized") })
	public ResponseEntity<?> getMfaStatus(final HttpServletRequest request) {
		try {
			return ResponseEntity.ok(authenticationService.getMfaStatus(request));
		}
			catch (ApiException ae) {
		        return ResponseEntity.status(ae.getStatusCode()).body(ae.getResponseBody());
		    }
	}
 
	/**
	 * Setup TOTP MFA for user.
	 *
	 * @param request the HTTP servlet request for user extraction
	 * @return response entity with TOTP setup information
	 */
	@PostMapping("/mfa/totp/setup")
	@Operation(summary = "Setup TOTP MFA", description = "Generate TOTP secret and QR code for MFA setup")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "TOTP setup information generated"),
			@ApiResponse(responseCode = "401", description = "Unauthorized") })
	public ResponseEntity<?> setupTotp(final HttpServletRequest request) {
		
		
		try {
			return ResponseEntity.ok(authenticationService.setupTotp(request));
		}
			catch (ApiException ae) {
		        return ResponseEntity.status(ae.getStatusCode()).body(ae.getResponseBody());
		    }
		
	}
 
	/**
	 * Enable MFA method for user.
	 *
	 * @param request     the MFA enable request
	 * @param httpRequest the HTTP servlet request for user extraction
	 * @return response entity with enable result
	 */
	@PostMapping("/mfa/enable")
	@Operation(summary = "Enable MFA method", description = "Enable a specific MFA method for the user")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "MFA method enabled successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid MFA method or verification failed"),
			@ApiResponse(responseCode = "401", description = "Unauthorized") })
	public ResponseEntity<Map<String, Object>>enableMfa(@RequestBody JSONObject request,
			HttpServletResponse response,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
		try {
			return authenticationService.enableMfa(request,
					response,userAgent);
		}
			catch (ApiException ae) {
				 Map<String, Object> errorBody = new HashMap<>();
			        errorBody.put("status", "error");
			        errorBody.put("message", ae.getMessage());
			        errorBody.put("details", ae.getResponseBody());
			        return ResponseEntity.status(ae.getStatusCode()).body(errorBody);
			    } catch (Exception e) {
			        Map<String, Object> errorBody = new HashMap<>();
			        errorBody.put("status", "error");
			        errorBody.put("message", "Unexpected server error");
			        errorBody.put("details", e.getMessage());
			        return ResponseEntity.status(500).body(errorBody);
			    }
		
	}

}
