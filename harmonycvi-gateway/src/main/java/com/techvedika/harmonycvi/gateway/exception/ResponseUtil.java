package com.techvedika.harmonycvi.gateway.exception;

import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;

import com.techvedika.harmonycvi.gateway.util.ApiException;

public class ResponseUtil {
	
	 public static ResponseEntity<JSONObject> buildErrorResponse(ApiException ae) {
	        JSONObject err = new JSONObject();
	        err.put("status", "FAILURE");
	        err.put("statusCode", ae.getStatus());
	        err.put("statusMessage", ae.getMessage());
	        return ResponseEntity.status(ae.getStatus()).body(err);
	    }

}
