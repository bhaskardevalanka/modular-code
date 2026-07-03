package com.techvedika.harmonycvi.gateway.serviceimpl;

import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.entity.User;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;
import com.techvedika.harmonycvi.gateway.util.TokenUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class TrustedDeviceService {
	private static final Logger LOG = LoggerFactory.getLogger(TrustedDeviceService.class);

   // @Value("${mfa.trust-days:30}")
    private int trustDays = 90;

   @Autowired
   UserRepository repo;
   

    public String createTrustedDevice(Long userId, String deviceInfo) {
        String token = TokenUtils.generateToken();
        String hash = TokenUtils.sha256Hex(token);
        System.out.println("trustDays-----------------"+trustDays);
        Optional<User> user= repo.findById(userId);
        User d = user.get();
        d.setTokenHash(hash);
        d.setDeviceInfo(deviceInfo);
        d.setCreatedAt(Instant.now());
        d.setExpiresAt(Instant.now().plus(trustDays, ChronoUnit.DAYS));
        //d.setExpiresAt(Instant.now().plus(2, ChronoUnit.MINUTES));
        repo.save(d);
        return token;
    }

    public boolean validateToken(String token,String email, Long userId, boolean skipMfa, String userAgent) {
        String hash = TokenUtils.sha256Hex(token);
        Optional<User> o = repo.findByTokenHashAndRevokedFalseAndEmail(hash,email);
        if (o.isEmpty()) 
        return false;
        User d = o.get();
        if(d.getExpiresAt() !=null)
        {
        	if (d.getExpiresAt().isBefore(Instant.now())) 
        	{
        		System.out.println("expired------------");
        		Optional<Long> lockVersion = repo.findLockVersionById(userId);
			    if (lockVersion.isEmpty()) {
			        LOG.warn("User not found while retrying updateTokenById, userId={}", userId);
			    }
	    	repo.updateMfaStatus(userId,skipMfa,lockVersion.get(),userAgent);
        		
        	return false;
        	}
        	else
        	return true;
        }
        else
        return false;
        	
        
        
    }

    public void revokeToken(String token,String email) {
        String hash = TokenUtils.sha256Hex(token);
        repo.findByTokenHashAndRevokedFalseAndEmail(hash,email).ifPresent(td -> {
            td.setRevoked(true);
            repo.save(td);
        });
    }

    // revoke all devices for user
   /* public void revokeAll(String userId) {
        repo.findByUserIdAndRevokedFalse(userId).forEach(td -> {
            td.setRevoked(true);
            repo.save(td);
        });
    }*/
}
