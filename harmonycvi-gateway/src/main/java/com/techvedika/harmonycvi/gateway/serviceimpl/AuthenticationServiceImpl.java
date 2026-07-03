package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.binary.Base32;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.entity.User;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;
import com.techvedika.harmonycvi.gateway.security.SecurityUtil;
import com.techvedika.harmonycvi.gateway.service.AuthenticationService;
import com.techvedika.harmonycvi.gateway.util.UserUtils;

import dev.samstevens.totp.exceptions.QrGenerationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

	@Autowired
	private UserRepository userRepo;

	@Lazy
	@Autowired
	private UserUtils userUtils;

	@Autowired
	private MFAService mfaService;
	
	@Autowired
	private TrustedDeviceService trustedDeviceService;
	
	@Value("${mfa.trust-days:30}")
    private int trustDays;

	

	@Override
	public JSONObject setupTotp(HttpServletRequest request) {
		// TODO Auto-generated method stub
		String userEmailId = SecurityUtil.currentUserEmailId();
		Optional<User> optionalUser = userRepo.findByEmail(userEmailId);
		if (optionalUser.isEmpty()) {
			userUtils.globalException(UserConstants.INVALID_USERID, Integer.parseInt(UserConstants.UNAUTHORIZED));
		}
		User user = optionalUser.get();

		// Generate a new TOTP secret
		String secret = mfaService.generateTotpSecret();

		// Persist secret in user (but only after they confirm)
		user.setTotpSecret(secret);
		//user.setMfaEnabled(true);
		userRepo.save(user);

		// Generate a QR code for authenticator apps
		byte[] qrImage = null;
		try {
			qrImage = mfaService.generateQrCode(secret, user.getEmail());
		} catch (QrGenerationException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to generate QR code", e);
		}
		String qrDataUrl = "data:image/png;base64," + Base64.getEncoder().encodeToString(qrImage);

		// TotpSetupResponse setupResponse = new TotpSetupResponse(secret, qrDataUrl,
		// "Scan the QR code with your authenticator app.");

		// return AuthenticationResult.successWithTotpSetup(setupResponse);
		JSONObject response = new JSONObject();
		response.put("QrDataUrl", qrDataUrl);

		return response;
	}
	
	
	@Override
	public JSONObject getMfaStatus(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<Map<String, Object>> enableMfa(JSONObject req, HttpServletResponse resp, String userAgent) {
		//JSONObject response = new JSONObject();
		  Map<String, Object> responseBody = new HashMap<>();
		    HttpHeaders headers = new HttpHeaders();

		String userEmailId = SecurityUtil.currentUserEmailId();
		Optional<User> optionalUser = userRepo.findByEmail(userEmailId);
		if (optionalUser.isEmpty()) {
			userUtils.globalException(UserConstants.INVALID_USERID, Integer.parseInt(UserConstants.UNAUTHORIZED));
		}
		User user = optionalUser.get();
		boolean verified = false;
		// Validate TOTP code
		verified = mfaService.verifyTotpCode(user.getTotpSecret(), req.get("verificationCode").toString());
		System.out.println("Verified--------------"+verified);
		if(verified) {
			user.setMfaEnabled(true);
			user.setLoginCount(5);
			boolean isRememberDevice = (boolean) req.get("isRememberDevice");
			if (isRememberDevice) {
	            String deviceInfo = userAgent == null ? "unknown" : userAgent;
	            String token = trustedDeviceService.createTrustedDevice(user.getId(), deviceInfo);
	            ResponseCookie cookie = ResponseCookie.from("mfa_trust", token)
	                    .httpOnly(true)
	                    .secure(false)
	                    .path("/")
	                    .maxAge(Duration.ofDays(trustDays))
	                    .sameSite("Lax")
	                    .build();

	            headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
	        }
			userRepo.save(user);
		        responseBody.put("status", "success");
		        responseBody.put("message", "TOTP verified successfully");
		    } else {
		        responseBody.put("status", "failed");
		        responseBody.put("message", "Invalid OTP. Please try again.");
		    }

		    return ResponseEntity.ok()
		            .headers(headers)
		            .body(responseBody);
		}

}
