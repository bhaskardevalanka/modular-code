package com.techvedika.harmonycvi.gateway.exception;

import java.util.Collection;
import java.util.Map;

import org.json.simple.JSONObject;

public class RequestValidator {
    
    /**
     * Validates request and returns detailed result
     */
    public static ValidationResult validateRequestWithDetails(JSONObject jsonRequest, 
                                                             String... requiredFields) {
        if (jsonRequest == null) {
            return ValidationResult.invalid("Request cannot be null", null);
        }
        
        for (String field : requiredFields) {
            ValidationResult fieldValidation = validateFieldWithDetails(jsonRequest, field);
            if (!fieldValidation.isValid()) {
                return fieldValidation;
            }
        }
        
        return ValidationResult.valid();
    }
    
    private static ValidationResult validateFieldWithDetails(JSONObject jsonRequest, String field) {
        if (!jsonRequest.containsKey(field)) {
            return ValidationResult.invalid("Missing required field: " + field, field);
        }
        
        Object data = jsonRequest.get(field);
        if (!hasValidData(data)) {
            return ValidationResult.invalid("Field cannot be empty: " + field, field);
        }
        
        return ValidationResult.valid();
    }
    
    private static boolean hasValidData(Object data) {
        // Same implementation as above
        if (data == null) return false;
        if (data instanceof String) return !((String) data).trim().isEmpty();
        if (data instanceof Map) return !((Map<?, ?>) data).isEmpty();
        if (data instanceof Collection) return !((Collection<?>) data).isEmpty();
        if (data instanceof Object[]) return ((Object[]) data).length > 0;
        if (data.getClass().isArray()) return java.lang.reflect.Array.getLength(data) > 0;
        return true;
    }
}