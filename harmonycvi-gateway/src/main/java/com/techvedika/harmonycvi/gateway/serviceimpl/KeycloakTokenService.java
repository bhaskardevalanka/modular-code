package com.techvedika.harmonycvi.gateway.serviceimpl;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvedika.harmonycvi.gateway.dto.KeycloakToken;
import com.techvedika.harmonycvi.gateway.entity.User;
import com.techvedika.harmonycvi.gateway.entity.UserOrganization;
import com.techvedika.harmonycvi.gateway.exception.UnexpectedRunTimeException;
import com.techvedika.harmonycvi.gateway.repository.UserOrganizationRepository;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;


@Service
public class KeycloakTokenService {

	private static final Logger LOG = LoggerFactory.getLogger(KeycloakTokenService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    
    private static final Map<String, String> GROUP_CACHE = new ConcurrentHashMap<>();

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.server-url}")
    String keycloakServerUrl;

    @Value("${keycloak.realm}")
    String realm;

    @Value("${keycloak.admin-user}")
    String adminUser;

    @Value("${keycloak.user-password}")
    String userPassword;

    @Value("${keycloak.admin-password}")
    String adminPassword;

    private final UserOrganizationRepository userOrganizationRepo;
    private final UserRepository userRepo;

    public KeycloakTokenService(UserOrganizationRepository userOrganizationRepo,
                       UserRepository userRepo) {
        this.userOrganizationRepo = userOrganizationRepo;
        this.userRepo = userRepo;
    }
	 

    public KeycloakToken getUserAccessToken(String userEmailId, String password) throws JsonProcessingException, NullPointerException {
        LOG.info("Fetching access token for user: {}", userEmailId);
        Keycloak keycloak = null;
        try {
        LOG.info("User Email Id---------------{}",userEmailId);
        Optional<User> optUser = userRepo.findByEmail(userEmailId);
		User newUser = null;
		if(optUser.isPresent())
		{
			newUser = optUser.get();
		}
		keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakServerUrl)
                .realm("master")
               .username(adminUser)
               .password(adminPassword)
               .clientId("admin-cli")
                .grantType("password")
               .build();

        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        LOG.info("Connected to Keycloak.");
        if(newUser == null) throw new NullPointerException("NewUser is null");
        String newUsername = newUser.getFirstName();
        String newUserPassword = "";
        if(newUser.getRole().getId() == 1) {
        	newUserPassword = adminPassword;
        }
        else {
        	newUserPassword = userPassword;
        }
        Long id = newUser.getId();
        LOG.info("User Id-------------------{}",id);
        createUserIfNotExists(usersResource, realmResource, newUsername, userEmailId,clientId, newUserPassword,id);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Build request body with client secret (confidential client)
        String body = "grant_type=password" +
                      "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                      "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8) +
                      "&username=" + URLEncoder.encode(userEmailId, StandardCharsets.UTF_8) +
                      "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        String tokenUrl = keycloakServerUrl + "realms/" + realm + "/protocol/openid-connect/token";
        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new UnexpectedRunTimeException("Failed to get token: HTTP " + response.getStatusCode() +
                                       " - " + response.getBody());
        }

        JsonNode json = mapper.readTree(response.getBody());

        if (json.has("access_token")) {
            String accessToken = json.get("access_token").asText();
            long expiresIn = json.get("expires_in").asLong(); // Keycloak provides this

            return new KeycloakToken(accessToken, expiresIn);
        }

        if (json.has("error")) {
            throw new UnexpectedRunTimeException(
                    "Error from Keycloak: " +
                    json.get("error").asText() + " - " +
                    json.get("error_description").asText()
            );
        }

        throw new UnexpectedRunTimeException(
                "Unexpected response from Keycloak: " + response.getBody()
        );
        }finally {
        	 if (keycloak != null) {
        	        keycloak.close();
        	 }
        }
    }
    
    private String createUserIfNotExists(UsersResource usersResource, RealmResource realmResource, String username,
			String email,String clientId, String password,Long id) {
    	LOG.info("Inside create user--------------------");
    	List<UserRepresentation> existingByEmail;
    	try {
    	    LOG.info("Calling Keycloak searchByEmail for: {}", email);
    	    existingByEmail = usersResource.searchByEmail(email, true);
    	    LOG.info("Keycloak searchByEmail returned: {}", existingByEmail);
    	} catch (Exception e) {
    	    LOG.error("Keycloak searchByEmail failed for {}: {}", email, e.getMessage(), e);
    	    throw new UnexpectedRunTimeException("Keycloak user lookup failed: " + e.getMessage(), e);
    	}
    	if (existingByEmail != null && !existingByEmail.isEmpty()) {
            String existingId = existingByEmail.get(0).getId();
            LOG.info("User already exists with email:{}, ID={}",email ,existingId);
            return existingId;
        }
		username = username.trim().replaceAll("[^a-zA-Z0-9_\\-]", "_");
		if (username.isEmpty()) {
			username = "user_" + UUID.randomUUID().toString().substring(0, 6);
		}
		final String finalUsername = username;
		String userId = null;
			username = username + "_" + UUID.randomUUID().toString().substring(0, 6);
			CredentialRepresentation credential = new CredentialRepresentation();
			credential.setTemporary(false);
			credential.setType(CredentialRepresentation.PASSWORD);
			credential.setValue(password);

			UserRepresentation user = new UserRepresentation();
			user.setUsername(username);
			user.setEmail(email);
			user.setEnabled(true);
			user.setEmailVerified(true);
			user.setCredentials(Collections.singletonList(credential));
			user.setRequiredActions(Collections.emptyList());
			
			try (var response = usersResource.create(user)) {

			    int status = response.getStatus();

			    if (status == 201) {
			        userId = response.getLocation()
			                         .getPath()
			                         .replaceAll(".*/([^/]+)$", "$1");
			        LOG.info("Created user: {} , email: {}",username,email);

			    } else if (status == 409) {
			        username = username + "-" + UUID.randomUUID().toString().substring(0, 6);
			        LOG.info(
			            "Username conflict for {}. Trying new username:{} " ,finalUsername, username);

			        return createUserIfNotExists(
			                usersResource,
			                realmResource,
			                username,
			                email,
			                clientId,
			                password,
			                id
			        );

			    } else {
			    	LOG.error("Failed to create user: {}, Status: {}",username,status);
			    }
			}
			
			List<String> groups = new ArrayList<>();
			List<UserOrganization> userOrgs = userOrganizationRepo.findByUserId(id);
			if (userOrgs != null) {
				for (UserOrganization users : userOrgs) {
					String groupName = "org-" + users.getOrgId();
					
					groups.add(groupName);
					
			        // Add admin to the org group
			        List<String> groupIds = createGroupsIfNotExists(realmResource, groups);
			        joinGroupsSafely(usersResource, userId, groupIds, users.getUserId(),users.getOrgId());

				}
			}
		
        
		if (!"admin".equalsIgnoreCase(username)) {
			assignRealmRoles(usersResource, userId, username, realmResource);
        } else {
        	LOG.info("Skipping role modification for admin user: {}", username);
        }

		return userId;
	}
    
    private void assignRealmRoles(UsersResource usersResource, String userId, String username,RealmResource realmResource) {
    	UserResource userResource = usersResource.get(userId);

        // Remove all current realm roles
        List<RoleRepresentation> currentRealmRoles = userResource.roles().realmLevel().listAll();
        if (!currentRealmRoles.isEmpty()) {
            userResource.roles().realmLevel().remove(currentRealmRoles);
            LOG.info("Removed default realm roles for user: {}", username);
        }

        // Assign required realm roles
        List<String> rolesToAssign = Arrays.asList("user", "auth");
        for (String roleName : rolesToAssign) {
            try {
                RoleRepresentation roleRep = realmResource.roles().get(roleName).toRepresentation();
                userResource.roles().realmLevel().add(Collections.singletonList(roleRep));
                LOG.info("Assigned realm role {} to user: {}",roleName, username);
            } catch (Exception e) {
            	LOG.error("⚠️ Failed to assign realm role {} to user:{} — {}",roleName,username,e.getMessage());
            }
        }
    }
	
	private List<String> createGroupsIfNotExists(RealmResource realmResource, List<String> groups) {
	    List<String> groupIds = new ArrayList<>();

	    // Fetch all existing groups once
	    List<GroupRepresentation> existingGroups = realmResource.groups().groups();

	    for (String groupName : groups) {
	        String groupId;

	        if (GROUP_CACHE.containsKey(groupName)) {
	            groupId = GROUP_CACHE.get(groupName);
	        } else {
	            Optional<GroupRepresentation> existingGroup =
	                    existingGroups.stream().filter(g -> g.getName().equals(groupName)).findFirst();

	            if (existingGroup.isPresent()) {
	                groupId = existingGroup.get().getId();
	            } else {
	                // Create new group
	                GroupRepresentation g = new GroupRepresentation();
	                g.setName(groupName);
	                realmResource.groups().add(g);

	                LOG.info("Created group: {}", groupName);

	                // Fetch newly created group
	                groupId = realmResource.groups()
	                        .groups()
	                        .stream()
	                        .filter(gr -> groupName.equals(gr.getName()))
	                        .findFirst()
	                        .orElseThrow(() ->
	                                new IllegalStateException(
	                                        "Group not found in Keycloak: " + groupName
	                                )
	                        )
	                        .getId();
	            }

	            GROUP_CACHE.put(groupName, groupId);
	        }

	        groupIds.add(groupId);
	    }

	    return groupIds;
	}
	
	private void joinGroupsSafely(UsersResource usersResource, String userId, List<String> groupIds, Long newUserId, Long newOrgId) {

// Fetch the user's current Keycloak groups only once (optimization)
		List<GroupRepresentation> userGroups = usersResource.get(userId).groups();
		for (int i = 0; i < groupIds.size(); i++) {

			String groupId = groupIds.get(i);

			boolean alreadyMember = userGroups.stream().anyMatch(g -> g.getId().equals(groupId));

			if (!alreadyMember) {
				usersResource.get(userId).joinGroup(groupId);
				LOG.info("Added user {} to group {}",userId, groupId);
			} else {
				LOG.info("User {} already in group {}",userId, groupId);
			}
			
		}
		String newGroup = "org-"+newOrgId;
		userOrganizationRepo.updateUserGroup(newUserId,newOrgId,newGroup);
	}

}
