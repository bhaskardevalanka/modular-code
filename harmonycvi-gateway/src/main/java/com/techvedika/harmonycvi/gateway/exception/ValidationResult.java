package com.techvedika.harmonycvi.gateway.exception;

public class ValidationResult {
    private final boolean valid;
    private final String errorMessage;
    private final String missingField;
    
    public ValidationResult(boolean valid, String errorMessage, String missingField) {
        this.valid = valid;
        this.errorMessage = errorMessage;
        this.missingField = missingField;
    }
    
    // Getters
    public boolean isValid() { return valid; }
    public String getErrorMessage() { return errorMessage; }
    public String getMissingField() { return missingField; }
    
    // Static factory methods
    public static ValidationResult valid() {
        return new ValidationResult(true, null, null);
    }
    
    public static ValidationResult invalid(String errorMessage, String missingField) {
        return new ValidationResult(false, errorMessage, missingField);
    }
}