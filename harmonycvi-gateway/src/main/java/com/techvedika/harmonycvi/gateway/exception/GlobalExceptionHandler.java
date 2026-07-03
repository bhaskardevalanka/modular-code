package com.techvedika.harmonycvi.gateway.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.techvedika.harmonycvi.gateway.util.UserUtils;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@Autowired
	UserUtils userUtils;
	
	@ExceptionHandler(GlobalApiException.class)
	public ResponseEntity<Object> handleApiException(GlobalApiException ex) {
		return ResponseEntity.status(ex.getStatus()).body(ex.getBody());
    }
	
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
		Map<String, Object> response = new HashMap<>();
	    response.put("status", HttpStatus.METHOD_NOT_ALLOWED.value());
	    response.put("error", "Method Not Allowed");
	    response.put("message", "HTTP method not supported: " + ex.getMethod());
	    response.put("timestamp", Instant.now());
	    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
	}
	
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<Map<String, Object>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
	    Map<String, Object> response = new HashMap<>();
	    response.put("status", HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
	    response.put("error", "Unsupported Media Type");
	    response.put("message", ex.getMessage());
	    response.put("supported", ex.getSupportedMediaTypes());
	    response.put("timestamp", Instant.now());
	    
	    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
	}
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> handleMissingBody(HttpMessageNotReadableException ex) {
	    Map<String, Object> response = new HashMap<>();
	    response.put("status", HttpStatus.BAD_REQUEST.value());
	    response.put("error", "Bad Request");
	    response.put("message", "Request body is missing or malformed.");
	    response.put("timestamp", Instant.now());
	    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<JSONObject> handleAll(Exception ex) {
	    JSONObject err = new JSONObject();
	    err.put("status", "FAILURE");
	    err.put("statusCode", 500);
	    err.put("statusMessage", ex.getMessage());
	    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
	}
	
	
	/*

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
		return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN.value(), "FAILURE"), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(InvalidInputException.class)
	public ResponseEntity<ErrorResponse> handleInvalidInput(InvalidInputException ex) {
		return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), "FAILURE"),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
		String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
		return new ResponseEntity<>(new ErrorResponse(errorMessage, HttpStatus.BAD_REQUEST.value(), "FAILURE"),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
		return new ResponseEntity<>(
				new ErrorResponse("Method not allowed. Please check your HTTP verb.", 405, "FAILURE"),
				HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleInvalidJson(HttpMessageNotReadableException ex) {
		return new ResponseEntity<>(new ErrorResponse("Malformed JSON or incorrect request format.", 400, "FAILURE"),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException ex) {
		return new ResponseEntity<>(new ErrorResponse("The requested endpoint does not exist.", 404, "FAILURE"),
				HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
		return new ResponseEntity<>(new ErrorResponse("Internal server error. Please try again later.", 500, "FAILURE"),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ResponseStatus(HttpStatus.FORBIDDEN)
	public static class RestrictedAccessException extends RuntimeException {
		public RestrictedAccessException(String message) {
			super(message);
		}
	}

	@ExceptionHandler(RestrictedAccessException.class)
	public ResponseEntity<ErrorResponse> handleRestrictedAccess(RestrictedAccessException ex) {
		return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN.value(), "FAILURE"),
				HttpStatus.FORBIDDEN);
	}
	
	@ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ResponseEntity<ErrorResponse> handleNotFound(HttpClientErrorException.NotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("Study not found. Please check the Study UID or Patient ID.",404, "FAILURE"));
    }
    */
}
