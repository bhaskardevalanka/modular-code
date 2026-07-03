package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.dto.KeycloakToken;
import com.techvedika.harmonycvi.gateway.security.SecurityUtil;
import com.techvedika.harmonycvi.gateway.service.CommonMethod;


	@Service
	public class PacsTokenService {
		
		private static final Logger LOG = LoggerFactory.getLogger(PacsTokenService.class);

	    // userEmail → KeycloakToken
	    private final Map<String, KeycloakToken> tokenCache = new ConcurrentHashMap<>();

	    @Autowired
	    private CommonMethod commonMethod;

	    public String getToken(String orgId) throws Exception {
	    	
	    	 if (!"true".equalsIgnoreCase(commonMethod.getKeyClokaSwitch())) {
	    		System.out.println("Returning empty string");
	 	        return "";
	 	    }

	        String userEmail = SecurityUtil.currentUserEmailId();
	        String token = getKeycloakTokenFromCache(userEmail,orgId);
	        return token;
	     }
	    
	    
	    public String getTokenWithEmail(String orgId, String userEmail) throws Exception {
	    	
	    	 if (!"true".equalsIgnoreCase(commonMethod.getKeyClokaSwitch())) {
	    		System.out.println("Returning empty string");
	 	        return "";
	 	    }
	    	 String token = getKeycloakTokenFromCache(userEmail,orgId);
	    	 return token;
	    }
	    
	    private String getKeycloakTokenFromCache(String userEmail,String orgId) throws Exception
	    {
	    	KeycloakToken token = tokenCache.get(userEmail);
	        if (token == null || token.isExpired()) {
	        	LOG.info("Inside if at pacstoken------------------");
	        	synchronized (this) {
	                token = tokenCache.get(userEmail);
	                if (token == null || token.isExpired()) {
	                	LOG.info("token is expired getting new token");
	                    KeycloakToken newToken = commonMethod.getPacsToken(orgId,userEmail);
	                    if (newToken == null) {
	                        throw new IllegalStateException("Unable to fetch Keycloak token");
	                    }
	                    tokenCache.put(userEmail, newToken);
	                    token = newToken;
	                }
	            }
	        }
	        return token.getAccessToken();
	    }

	    // optional cleanup
	    public void evictUser(String userEmail) {
	        tokenCache.remove(userEmail);
	    }
	    
	    }
