package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.TimeProvider;

@Service
public class MFAService {
	
	@Autowired
	private SecretGenerator secretGenerator;
	
	@Autowired
	private CodeVerifier codeVerifier;
	
	@Autowired
	private QrGenerator qrGenerator;
	
	@Autowired
	private CodeGenerator codeGenerator;
	
	@Autowired
	private TimeProvider timeProvider;
	
	
	   // private final TimeProvider timeProvider;
	    private int timePeriod = 30;
	    private int allowedTimePeriodDiscrepancy = 0;
	
	/**
     * Generate TOTP secret for user.
     *
     * @return TOTP secret string
     */
    public String generateTotpSecret() {
        String secret = secretGenerator.generate();
       // LOGGER.debug("Generated new TOTP secret");
        return secret;
    }
    
 
 
    /**
     * Generate QR code for TOTP setup.
     *
     * @param secret the TOTP secret
     * @param userIdentifier the user identifier for the QR codel
     * @return QR code as byte array
     * @throws QrGenerationException if QR code generation fails
     */
    public byte[] generateQrCode(final String secret, final String userIdentifier) throws QrGenerationException {
        QrData data = new QrData.Builder()
            .label(userIdentifier)
            .secret(secret)
            .issuer("harmony-cvi")
            .algorithm(HashingAlgorithm.SHA1)  // Use enum instead of string
            .digits(6)
            .period(30)
            .build();
 
      //  LOGGER.debug("Generated QR code for user: {}", userIdentifier);
        return qrGenerator.generate(data);
    }
 
    /**
     * Verify TOTP code.
     *
     * @param secret the TOTP secret
     * @param code the TOTP code to verify
     * @return true if code is valid
     */
    public boolean verifyTotpCode(final String secret, final String code) {
        if (secret == null || code == null) {
            return false;
        }
 
        try {
         //  boolean isValid = codeVerifier.isValidCode(secret, code);
           boolean isValid = isValidCode(secret, code);
            return isValid;
        } catch (Exception e) {
          //  LOGGER.error("TOTP verification failed", e);
            return false;
        }
    }
 
 
    public boolean isValidCode(String secret, String code) {
        // Get the current number of seconds since the epoch and
        // calculate the number of time periods passed.
        long currentBucket = Math.floorDiv(timeProvider.getTime(), timePeriod);
 
        // Calculate and compare the codes for all the "valid" time periods,
        // even if we get an early match, to avoid timing attacks
        boolean success = false;
        for (int i = -allowedTimePeriodDiscrepancy; i <= allowedTimePeriodDiscrepancy; i++) {
            success = checkCode(secret, currentBucket + i, code) || success;
        }
 
        return success;
    }
 
    /**
     * Check if a code matches for a given secret and counter.
     */
    private boolean checkCode(String secret, long counter, String code) {
        try {
            String actualCode = codeGenerator.generate(secret, counter);
            System.out.println("actualCode--------------------"+actualCode);
            return timeSafeStringComparison(actualCode, code);
        } catch (CodeGenerationException e) {
            return false;
        }
    }
 
    /**
     * Compare two strings for equality without leaking timing information.
     */
    private boolean timeSafeStringComparison(String a, String b) {
        byte[] aBytes = a.getBytes();
        byte[] bBytes = b.getBytes();
 
        if (aBytes.length != bBytes.length) {
            return false;
        }
 
        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
 
        return result == 0;
    }
 

}
