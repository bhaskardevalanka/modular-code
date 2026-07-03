package com.techvedika.harmonycvi.gateway.cloud;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;

public class GcpAuthUtil {
	
	private GcpAuthUtil() {}

    private static final Logger LOG =
            LoggerFactory.getLogger(GcpAuthUtil.class);

    public static String getIdToken(String targetAudience) throws IOException {

        LOG.info("Fetching Application Default Credentials for GCP");

        GoogleCredentials credentials =
                GoogleCredentials.getApplicationDefault();

        if (!(credentials instanceof IdTokenProvider)) {
            LOG.error("Provided credentials do not support ID tokens");
            throw new IllegalStateException(
                    "Credentials do not support ID token generation");
        }

        LOG.info("Creating ID token credentials for audience: {}",
                 targetAudience);

        IdTokenCredentials tokenCredentials =
                IdTokenCredentials.newBuilder()
                        .setIdTokenProvider((IdTokenProvider) credentials)
                        .setTargetAudience(targetAudience)
                        .build();

        String token =
                tokenCredentials.refreshAccessToken().getTokenValue();

        LOG.info("Successfully obtained ID token from GCP");

        return token;
    }
}