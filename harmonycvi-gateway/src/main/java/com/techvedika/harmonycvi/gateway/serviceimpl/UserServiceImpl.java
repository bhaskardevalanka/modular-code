package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.constant.StatusConstants;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.entity.Center;
import com.techvedika.harmonycvi.gateway.entity.DeviceDetails;
import com.techvedika.harmonycvi.gateway.entity.Organization;
import com.techvedika.harmonycvi.gateway.entity.Role;
import com.techvedika.harmonycvi.gateway.entity.User;
import com.techvedika.harmonycvi.gateway.entity.UserOrganization;
import com.techvedika.harmonycvi.gateway.entity.UserStudies;
import com.techvedika.harmonycvi.gateway.projection.LoginProjection;
import com.techvedika.harmonycvi.gateway.projection.OrgConsultantProjection;
import com.techvedika.harmonycvi.gateway.projection.ResetPasswordProjection;
import com.techvedika.harmonycvi.gateway.repository.CenterRepository;
import com.techvedika.harmonycvi.gateway.repository.DeviceDetailsRepository;
import com.techvedika.harmonycvi.gateway.repository.OrganizationRepository;
import com.techvedika.harmonycvi.gateway.repository.RoleRepository;
import com.techvedika.harmonycvi.gateway.repository.StudyExtensionRepository;
import com.techvedika.harmonycvi.gateway.repository.UserOrganizationRepository;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;
import com.techvedika.harmonycvi.gateway.repository.UserStudiesRepository;
import com.techvedika.harmonycvi.gateway.security.JwtUtils;
import com.techvedika.harmonycvi.gateway.security.SecurityUtil;
import com.techvedika.harmonycvi.gateway.service.UserService;
import com.techvedika.harmonycvi.gateway.service.UserStudiesService;
import com.techvedika.harmonycvi.gateway.util.ApiException;
import com.techvedika.harmonycvi.gateway.util.EmailService;
import com.techvedika.harmonycvi.gateway.util.UserUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
	
    private static final Map<String, String> GROUP_CACHE = new ConcurrentHashMap<>();

	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private UserStudiesRepository userStudiesRepo;

	@Autowired
	private OrganizationRepository orgRepo;

	@Autowired
	private CenterRepository centerRepo;

	@Autowired
	private DeviceDetailsRepository deviceRepo;

	@Autowired
	private RoleRepository roleRepo;

	@Lazy
	@Autowired
	private JwtUtils jwtUtil;

	@Lazy
	@Autowired
	private UserUtils userUtils;

	@Autowired
	private EmailService emailService;

	@Autowired
	private StudyExtensionRepository studyRepo;

	@Autowired
	private UserStudiesService userStudiesService;

	@Autowired
	private UserOrganizationRepository userOrganizationRepo;
	
	 @Autowired
	 private GoogleTokenValidator googleTokenValidator;
	    
	@Autowired
	private TrustedDeviceService trustedDeviceService;

	@Autowired
	private OrganizationServiceImpl organizationServiceImpl;
	
	private static final String EMAIL_EXISTS = "emailAlreadyExists";
	@Override
	public JSONObject login(JSONObject reqJson,String mfaToken,String userAgent) {
	    // 1. Validate request
	    if (reqJson == null || reqJson.get(UserConstants.EMAIL) == null 
	            || reqJson.get(UserConstants.PASSWORD) == null 
	            || reqJson.get(UserConstants.IS_ADMIN) == null) {
	        userUtils.globalException(UserConstants.INVALID_REQUEST_MESSAGE,
	                Integer.parseInt(UserConstants.UNAUTHORIZED));
	    }

	    String email = reqJson.get(UserConstants.EMAIL).toString().toLowerCase();
	    String shaPwd = userUtils.getSHA(reqJson.get(UserConstants.PASSWORD).toString());
	    boolean isAdmin = reqJson.get(UserConstants.IS_ADMIN).toString()
	            .equalsIgnoreCase(UserConstants.YES);

	    // 2. Fetch using password first
	    Optional<LoginProjection> optUser = userRepo.findLoginUser(email, shaPwd);

	    // 3. If not found, try OTP
	    boolean onetimePwdUsed = false;
	    if (optUser.isEmpty()) {
	        optUser = userRepo.findLoginUserByOtp(email, shaPwd);
	        onetimePwdUsed = optUser.isPresent();
	    }

	    if (optUser.isEmpty()) {
	        userUtils.globalException(UserConstants.INVALID_CREDENTIAL,
	                Integer.parseInt(UserConstants.UNAUTHORIZED));
	    }

	    LoginProjection user = optUser.get();

	    // 4. Check active
	    if (!user.getActive()) {
	        userUtils.globalException(UserConstants.INVALID_EMAIL_REQUEST_MESSAGE,
	                Integer.parseInt(UserConstants.UNAUTHORIZED));
	    }

	    // 5. Role guard
	    String roleName = user.getRole() != null ? user.getRole().getName() : "";
	    boolean isDoctorLike = roleName.equals(UserConstants.DOCTOR)
	            || roleName.equals(UserConstants.CONSULTANT_DOCTOR);

	    if ((isAdmin && isDoctorLike) || (!isAdmin && !isDoctorLike && !roleName.equals(UserConstants.TECHNICIAN))) {
	        userUtils.globalException(UserConstants.NO_PRIVILAGES_TO_LOGIN_THROUGH_THIS_PORTAL,
	                Integer.parseInt(UserConstants.STATUS_RESTRICTED));
	    }

	    // 6. JWT handling
	    String jwtToken = user.getJwtToken();
	    if(!onetimePwdUsed && !user.getMfaEnabled())
		{
	    	System.out.println("Generating------------------------");
			jwtToken = jwtUtil.generateToken(email);
			updateTokenById(user.getId(),jwtToken, user.getLoginCount()+1);
		}
	    else if (!reqJson.containsKey("existingToken") 
	            || "no".equalsIgnoreCase(reqJson.get("existingToken").toString())) {
	    	System.out.println("Generating token-----------------");
	        jwtToken = jwtUtil.generateToken(email);
	        updateTokenById(user.getId(), jwtToken,user.getLoginCount());
	    }
			//jwtToken = jwtUtil.createJWT(user.getId().toString(), "DICOM", "DICOM-WEB", ttlMillis);
			
			
			
			//user.setJwtToken(jwtToken); // persist new token
			
			//user.setLoginCount(user.getLoginCount()+1);
			//userRepo.save(user);
		//}

	    // 7. Fetch privileges & orgs only if required
	    List<Organization> orgs = userRepo.findOrgsById(user.getId());
	    List<Map<String, Object>> orgList = orgs.stream().map(userUtils::buildOrgDetails).toList();
	    List<Map<String, Object>> privilegeList = Optional.ofNullable(user.getRole().getPrivileges())
				.orElse(Collections.emptyList()).stream().map(pvr -> {
					Map<String, Object> map = new HashMap<>();
					map.put("name", pvr.getName());
					map.put("id", pvr.getId());
					map.put("active", pvr.getActive());
					return map;
				}).collect(Collectors.toList());
	    
	    boolean skipMfa = false;
	    int mfaUpdated  = 0;
	    if(!userAgent.equalsIgnoreCase(user.getDeviceInfo()))
	    {
	    	Optional<Long> lockVersion = userRepo.findLockVersionById(user.getId());
	    	
		    if (lockVersion.isEmpty()) {
		        LOG.warn("User not found while retrying updateTokenById, userId={}", user.getId());
	    	}
		    mfaUpdated = userRepo.updateMfaStatus(user.getId(),skipMfa,lockVersion.get(),userAgent);
    	 
	    }

	    if(mfaToken!=null)
	    {
	    skipMfa = trustedDeviceService.validateToken(mfaToken,email,user.getId(),false,userAgent);
	    if(!skipMfa && !user.getMfaEnabled())
	    {
	    	 Optional<Long> lockVersion = userRepo.findLockVersionById(user.getId());
			    if (lockVersion.isEmpty()) {
			        LOG.warn("User not found while retrying updateTokenById, userId={}", user.getId());
			    }
	    	 mfaUpdated = userRepo.updateMfaStatus(user.getId(),skipMfa,lockVersion.get(),userAgent);
	    	
	    }
	    }
	    


	    // 8. Build response
	    JSONObject resp = userUtils.updateResponse(new JSONObject(),
	            UserConstants.STATUS_SUCCESS, UserConstants.LOGIN_SUCCESS,
	            UserConstants.LOGIN_SUCCESS, null);
				
		resp.put(UserConstants.JWT_TOKEN, jwtToken);
		resp.put(UserConstants.NAME, user.getFirstName() + " " + user.getLastName());
		resp.put(UserConstants.ORG_LIST, orgList);
		resp.put(UserConstants.EMAIL, user.getEmail());
		resp.put(UserConstants.USER_ID, user.getId());
		resp.put(UserConstants.ROLE_ID, user.getRole().getId());
		resp.put(UserConstants.ROLE_NAME, roleName);
		resp.put(UserConstants.PRIVILEGE_LIST_USER,privilegeList);
		resp.put(UserConstants.IS_LOGINE_THROUGH_ONE_TIME_PASS, onetimePwdUsed ? UserConstants.YES : UserConstants.NO);
		if(mfaUpdated == 1)
		{
			resp.put(UserConstants.IS_MFA_ENABLED, false);
		}
		else
		resp.put(UserConstants.IS_MFA_ENABLED, user.getMfaEnabled());
		resp.put(UserConstants.LOG_IN_COUNT, user.getLoginCount());
		resp.put(UserConstants.SKIP_MFA, skipMfa);
		
		return resp;
	}
	
	public void updateTokenById(Long userId,String jwtToken,Integer loginCount) {
		boolean exists = userRepo.existsById(userId);
		if(!exists) {
			LOG.info("No row exists to update");
            LOG.info("End of {}.updateTokenById - {}", this.getClass().getName(), StatusConstants.NOT_FOUND);
		}else {
			int maxRetries = 3;
			int attempts = 0;
			boolean updatedSuccessfully = false;
	
			while (attempts < maxRetries && !updatedSuccessfully) {
			    attempts++;
			    Optional<Long> lockVersion = userRepo.findLockVersionById(userId);
			    if (lockVersion.isEmpty()) {
			        LOG.warn("User not found while retrying updateTokenById, userId={}", userId);
			        break;
			    }
			    int updated = userRepo.updateTokenById(userId, jwtToken,lockVersion.get(),loginCount);
				if (updated == 1) {
					System.out.println("Updated in one go");
			        updatedSuccessfully = true;
			    } else {
			        // Optional: wait a bit before retrying
			        try {
			        	LOG.info("Sleeping for 100ms::"+attempts);
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // 100ms backoff
			    }
			}
			if (!updatedSuccessfully) {
			    LOG.info("End of " + this.getClass().getName() + ".Login"
			             + StatusConstants.OPERATION_FAILED);
		        LOG.info("End of {}.updateTokenById - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
			}else {
                LOG.info("End of {}.updateTokenById - {}", this.getClass().getName(), StatusConstants.SUCCESS);
			}
		} 
	}

	
	private boolean updateToken(Long userId,String jwtToken) {
		boolean updatedSuccessfully = false;
		boolean exists = userRepo.existsById(userId);
		if(!exists) {
			LOG.info("No row exists to update");
            LOG.info("End of {}.updateTokenById - {}", this.getClass().getName(), StatusConstants.NOT_FOUND);
		}else {
			int maxRetries = 3;
			int attempts = 0;
	
			while (attempts < maxRetries && !updatedSuccessfully) {
			    attempts++;
			    Optional<Long> lockVersion = userRepo.findLockVersionById(userId);
			    if (lockVersion.isEmpty()) {
			        LOG.warn("User not found while retrying updateToken, userId={}", userId);
			        break;
			    }
			    int updated = userRepo.updateToken(userId, jwtToken,lockVersion.get());
				if (updated == 1) {
			        updatedSuccessfully = true;
			    } else {
			        // Optional: wait a bit before retrying
			        try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // 100ms backoff
			    }
			}
			if (!updatedSuccessfully) {
			    LOG.info("End of " + this.getClass().getName() + ".Login"
			             + StatusConstants.OPERATION_FAILED);
		        LOG.info("End of {}.updateTokenById - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
			}else {
                LOG.info("End of {}.updateTokenById - {}", this.getClass().getName(), StatusConstants.SUCCESS);
			}
		} 
		return updatedSuccessfully;
	}


	@Override
	@Transactional
	public JSONObject registerNewUserAccount(JSONObject json, HttpServletRequest request,boolean triggerEmail,String jwtToken) {
		try {
		Organization orgResult;
		/* --- 1. Authenticate caller ------------------------------ */
//		String jwt = request.getHeader(UserConstants.JWT_TOKEN);
//		if (jwt == null) {
//			userUtils.globalException(UserConstants.MISSING_JWT_TOKKEN,
//					Integer.parseInt(UserConstants.BAD_REQUEST_CODE));
//		}
//		Map<String, Object> claims = jwtUtil.decodeJWT(jwt);
//		User orgUser = userRepo.findById(Long.parseLong(claims.get(UserConstants.ID).toString()))
//				.orElseThrow(() -> new ApiException(UserConstants.INVALID_USERID,
//						Integer.parseInt(UserConstants.UNAUTHORIZED), UserConstants.INVALID_USERID));
//
//		if (!jwt.equals(orgUser.getJwtToken())) {
//			userUtils.globalException(UserConstants.USER_ALREADY_LOGOUT, Integer.parseInt(UserConstants.UNAUTHORIZED));
//		}
//
//		if (!userUtils.validatePriviliges(orgUser, UserConstants.PRIVILEGE_ADD_USER)) {
//			userUtils.globalException(UserConstants.NO_PRIVILAGES_TO_ACCESS,
//					Integer.parseInt(UserConstants.STATUS_RESTRICTED));
//		}

		/* --- 2. Field validation --------------------------------- */
		String missing = userUtils.validateUserMandatoryField(json);
		if (!missing.isEmpty()) {
			userUtils.globalException(missing, Integer.parseInt(UserConstants.BAD_REQUEST_CODE));
		}
		String email = json.get("email").toString();
		/* --- 3. Resolve / create Organization -------------------- */
		// Organization org;
		boolean isConsultant = "true".equalsIgnoreCase(json.get(UserConstants.IS_CONSULTANT).toString());
		String orgIdStr = json.get(UserConstants.ORG_ID).toString();
		Long orgId = Long.parseLong(orgIdStr);
		
		
			if (isConsultant && ("0".equals(orgIdStr))) {
			String orgName = json.get(UserConstants.FIRST_NAME) + " "
					+ (json.get(UserConstants.LAST_NAME) != null ? json.get(UserConstants.LAST_NAME) : "");
			boolean orgExists = orgRepo.existsByName(orgName);
			if (orgExists) {
				userUtils.globalException(UserConstants.ORG_ALLREADY_EXIST,
						Integer.parseInt(UserConstants.STATUS_DUPLICATE_CODE));
			}
			Map<String, Object> prefs = new HashMap<>();
			prefs.put("delete", true);
			prefs.put("report", true);
			prefs.put("show_all", false);
			prefs.put("reprocess", true);
			prefs.put("upload_button", true);
			prefs.put("clinical_uploads", true);
			prefs.put("save_bookmark", true);
			prefs.put(StatusConstants.REGISTRATION_EMAIL, true);
			prefs.put(StatusConstants.INVITATION_EMAIL, true);
			Organization newOrg = new Organization();
			newOrg.setName(orgName);
			newOrg.setConsultant(true);
			newOrg.setUploadLimit(5);
			newOrg.setCreatedBy(1L);
			newOrg.setCreatedDt(new Date());
			newOrg.setActive(true);
			newOrg.setEmail(json.get(UserConstants.EMAIL).toString());
			newOrg.setAddressOne(json.get(UserConstants.ADDRESS_ONE).toString());
			newOrg.setAddressTwo((String) json.getOrDefault(UserConstants.ADDRESS_TWO, ""));
			newOrg.setPhoneNo(json.get(UserConstants.PHONE_NO).toString());
			newOrg.setCity(json.get(UserConstants.CITY).toString());
			newOrg.setState(json.get(UserConstants.STATE).toString());
			newOrg.setPinCode(json.get(UserConstants.PIN_CODE).toString());
			newOrg.setPreferences(prefs);
			orgResult = orgRepo.save(newOrg);
		} else {
			orgResult = orgRepo.findById(Long.parseLong(orgIdStr))
					.orElseThrow(() -> new ApiException(UserConstants.NO_ORG,
							Integer.parseInt(UserConstants.NO_CONTENT_CODE), UserConstants.NO_ORG));
		}

		/* --- 4. Optional Center / Device ------------------------- */
		Center center = null;
		if (json.get(UserConstants.CENTER_ID) != null) {
			center = centerRepo.findById(Long.parseLong(json.get(UserConstants.CENTER_ID).toString()))
					.orElseThrow(() -> new ApiException(UserConstants.NO_CENTER,
							Integer.parseInt(UserConstants.NO_CONTENT_CODE), UserConstants.NO_CENTER));
		}

		DeviceDetails device = null;
		if (json.get(UserConstants.DEVICE_ID) != null) {
			device = deviceRepo.findById(Long.parseLong(json.get(UserConstants.DEVICE_ID).toString()))
					.orElseThrow(() -> new ApiException(UserConstants.NO_DEVICE,
							Integer.parseInt(UserConstants.NO_CONTENT_CODE), UserConstants.NO_DEVICE));
		}

		Optional<Organization> organization = Optional.empty();
		Map<String, Object> prefs = null;
		boolean isRegistrationEmailEnabled = false;
		boolean isInvitationEmailEnabled = false;
		if(isConsultant && ("0".equals(orgIdStr)))
		{
			if(json.get(StatusConstants.ORGNAME)!=null && json.get(StatusConstants.ORGNAME).toString().equalsIgnoreCase("TECHVEDIKA"))
			{
			Long organizationId = orgResult.getId();
			organization = orgRepo.findById(organizationId);
			}
			else
			{
				Long organizationId = Long.parseLong(json.get("adminOrgId").toString());
				organization = orgRepo.findById(organizationId);
			}
		}
		else
			organization = orgRepo.findById(orgId);
		if(organization.isPresent())
		{
			Organization org = organization.get();
			prefs = org.getPreferences();
			isRegistrationEmailEnabled = prefs.containsKey(StatusConstants.REGISTRATION_EMAIL)?(boolean) prefs.get(StatusConstants.REGISTRATION_EMAIL):true;
			isInvitationEmailEnabled = prefs.containsKey(StatusConstants.INVITATION_EMAIL)?(boolean) prefs.get(StatusConstants.INVITATION_EMAIL):true;
		}
		final boolean finalInvitationEmailEnabled = isInvitationEmailEnabled;

		/* --- 5. Email duplicate check ---------------------------- */		
		Optional<User> optionalUser = userRepo.findByEmail(json.get(UserConstants.EMAIL).toString().toLowerCase());
		if (optionalUser.isPresent()) {
			User usr = optionalUser.get();

			// existing consultant can still receive invitation
//			if (isConsultant && !existing.getOrgs().contains(orgResult)) {
			if(!usr.getOrgs().contains(orgResult) && usr.getRole().getName()!=null && usr.getRole().getName().equals(UserConstants.CONSULTANT_DOCTOR)){
				if(finalInvitationEmailEnabled) {
				String body = userUtils.mailBodyInvitationToNewOrg(usr, orgResult);
				emailService.sendEmail(usr.getEmail(), UserConstants.REGISTRACTION, body);
				}
				else
				{
					acceptInvitation(usr.getId().toString(),json.get("orgId").toString());
				}

				return userUtils.updateResponse(new JSONObject(), UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
						UserConstants.USER_ADD_SUCCESS_MESSAGE, null);
			} else {
				userUtils.globalException(UserConstants.EMAIL_ALREADY_EXIST_MESSAGE,
						Integer.parseInt(UserConstants.STATUS_DUPLICATE_CODE));
			}
		}

		/* --- 6. Role --------------------------------------------- */
		Role role = roleRepo.findById(Long.parseLong(json.get(UserConstants.ROLE_ID).toString()))
				.orElseThrow(() -> new ApiException(UserConstants.NO_ROLE_ID,
						Integer.parseInt(UserConstants.NO_CONTENT_CODE), UserConstants.NO_ROLE_ID));

		/* --- 7. Build & save new User ---------------------------- */
		String plainPwd = userUtils.generate().substring(0, 8);
		String shaPwd = userUtils.getSHA(plainPwd);

		User newUser = new User();
		newUser.setEmail(json.get(UserConstants.EMAIL).toString().toLowerCase());
		newUser.setFirstName(json.get(UserConstants.FIRST_NAME).toString());
		newUser.setLastName((String) json.getOrDefault(UserConstants.LAST_NAME, ""));
		newUser.setOnetimePassword(shaPwd);
		newUser.setOnetimePwdStatus(false);
		newUser.setUploadLimit(5);
		newUser.setRole(role);
		//newUser.setOrgs(List.of(orgResult));
		List<Organization> orgs = new ArrayList<>();
		orgs.add(orgResult);
		newUser.setOrgs(orgs);
		newUser.setActive(true);
		newUser.setCreatedBy(1L);
		newUser.setCreatedDt(new Date());
		newUser.setCity(json.get(UserConstants.CITY).toString());
		newUser.setState(json.get(UserConstants.STATE).toString());
		newUser.setPhoneNo(json.get(UserConstants.PHONE_NO).toString());
		newUser.setAddressOne(json.get(UserConstants.ADDRESS_ONE).toString());
		newUser.setAddressTwo((String) json.getOrDefault(UserConstants.ADDRESS_TWO, null));
		newUser.setPinCode(json.get(UserConstants.PIN_CODE).toString());
		newUser.setJwtToken(jwtToken);
		if (isConsultant)
			newUser.setConsultant(true);
		if (center != null)
			newUser.setCenters(List.of(center));
		if (device != null)
			newUser.setDeviceDetails(List.of(device));
		userRepo.save(newUser);

		if(triggerEmail) {
		/* --- 8. Registration email ------------------------------- */
			if(isRegistrationEmailEnabled)
			{
		String body = userUtils.mailBodyRegistration(plainPwd, newUser);
		LOG.info("Mail body: {}",body);
		emailService.sendEmail(newUser.getEmail(), UserConstants.REGISTRACTION, body);
			}
		/* Optional: second invitation email */
		if (json.containsKey("adminOrgId") && !"0".equals(json.get("adminOrgId").toString())) {
			orgRepo.findById(Long.parseLong(json.get("adminOrgId").toString())).ifPresent(targetOrg -> {
				if (!newUser.getOrgs().contains(targetOrg) && (role.getName().equals(UserConstants.DOCTOR)
						|| role.getName().equals(UserConstants.CONSULTANT_DOCTOR))) {
					if(finalInvitationEmailEnabled)
					{
						String body2 = userUtils.mailBodyInvitationToNewOrg(newUser, targetOrg);
						LOG.info("Mail body2: {}",body2);
					emailService.sendEmail(newUser.getEmail(), UserConstants.REGISTRACTION, body2);
					}
					else
					{
						acceptInvitation(newUser.getId().toString(),json.get("adminOrgId").toString());
					}
				}
			});
		}
		}

		/* --- 9. Success response -------------------------------- */
		return userUtils.updateResponse(new JSONObject(), UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
				UserConstants.USER_ADD_SUCCESS_MESSAGE, null);
		}catch (Exception e){
			e.printStackTrace();
			return userUtils.updateResponse(new JSONObject(), UserConstants.ERROR, UserConstants.ERROR,
					UserConstants.ERROR, null);
		}
	}

	@Override
	@Transactional
	public JSONObject updateUser(JSONObject json, HttpServletRequest request) {
	
		/* ---------- 1. Auth & privilege check ---------- */
		String userEmailId = SecurityUtil.currentUserEmailId();
		
		boolean allowed = userRepo.hasPrivilege(userEmailId, UserConstants.PRIVILEGE_UPDATE_USER);

		if (!allowed) {
		    userUtils.globalException(
		        UserConstants.NO_PRIVILAGES_TO_ACCESS,
		        Integer.parseInt(UserConstants.STATUS_RESTRICTED)
		    );
		    return null;
		}

		/* ---------- 2. Mandatory‑field validation ---------- */
		String missing = userUtils.validateUserMandatoryField(json);
		
		if (!missing.isEmpty()) {
			userUtils.globalException(missing, Integer.parseInt(UserConstants.BAD_REQUEST_CODE));
		}

		/* ---------- 3. Load target user ---------- */
		long targetId = Long.parseLong(json.get("id").toString());
		Optional<User> userResult = userRepo.findById(targetId);
		if (userResult == null || !userResult.isPresent()) {
			return userUtils.updateResponse(new JSONObject(), UserConstants.STATUS_INVALID, UserConstants.INVALID_REQ,
					UserConstants.INVALID_USERID, null);
		}

		User user = userResult.get();

		/* ---------- 4. Duplicate‑email check ---------- */
		if(json.get(UserConstants.EMAIL)!=null)
		{	
		String newEmail = (String) json.get(UserConstants.EMAIL);
		if (newEmail != null && !newEmail.equalsIgnoreCase(user.getEmail())) {
			if(userRepo.existsByEmail(newEmail.toLowerCase())) {
				userUtils.globalException(UserConstants.EMAIL_ALREADY_EXIST_MESSAGE,
						Integer.parseInt(UserConstants.STATUS_DUPLICATE_CODE));
			}
			
			user.setEmail(newEmail.toLowerCase());
		}
		}
		/* ---------- 5. Optional look‑ups ---------- */
		if (json.get(UserConstants.ROLE_ID)!=null) {
			Optional<Role> roleResult = roleRepo.findById(Long.parseLong(json.get(UserConstants.ROLE_ID).toString()));
			if (roleResult == null || !roleResult.isPresent()) {
				userUtils.globalException(UserConstants.NO_ROLE_ID, Integer.parseInt(UserConstants.NO_CONTENT_CODE));
			}
			user.setRole(roleResult.get());
		}
		

		if (json.get("centerId")!=null) {
			Optional<Center> centerResult = centerRepo.findById(Long.parseLong(json.get("centerId").toString()));
			if (centerResult == null || !centerResult.isPresent()) {
				userUtils.globalException(UserConstants.NO_CENTER, Integer.parseInt(UserConstants.NO_CONTENT_CODE));
			}
			user.setCenters(new ArrayList<>(List.of(centerResult.get())));
		}

		if (json.get("deviceId")!=null) {
			Optional<DeviceDetails> ddResult = deviceRepo.findById(Long.parseLong(json.get("deviceId").toString()));
			if (ddResult == null || !ddResult.isPresent()) {
				userUtils.globalException(UserConstants.NO_DEVICE, Integer.parseInt(UserConstants.NO_CONTENT_CODE));
			}
			user.setDeviceDetails(new ArrayList<>(List.of(ddResult.get())));
		}

		/* ---------- 6. Simple scalar updates ---------- */
		Map<String, String> scalars = Map.of(UserConstants.FIRST_NAME, "firstName", UserConstants.LAST_NAME, "lastName",
				UserConstants.CITY, "city", UserConstants.STATE, "state", UserConstants.PHONE_NO, "phoneNo",
				UserConstants.ADDRESS_ONE, "addressOne", UserConstants.ADDRESS_TWO, "addressTwo",
				UserConstants.PIN_CODE, "pinCode");
		scalars.forEach((jsonKey, field) -> {
			Object v = json.get(jsonKey);
			if (v != null && !v.toString().isBlank()) {
				switch (field) {
				case "firstName" -> user.setFirstName(v.toString());
				case "lastName" -> user.setLastName(v.toString());
				case "city" -> user.setCity(v.toString());
				case "state" -> user.setState(v.toString());
				case "phoneNo" -> user.setPhoneNo(v.toString());
				case "addressOne" -> user.setAddressOne(v.toString());
				case "addressTwo" -> user.setAddressTwo(v.toString());
				case "pinCode" -> user.setPinCode(v.toString());
				}
			}
		});
		
		if (json.containsKey(UserConstants.IS_ACTIVE)) {
			//user.setActive(Boolean.parseBoolean(json.get(UserConstants.ACTIVE).toString()));
			List<Organization> orgs = new ArrayList<>(user.getOrgs());
			Long userId = Long.valueOf(json.get("id").toString());
			Long orgId = Long.valueOf(json.get("orgId").toString());
			boolean status = Boolean.parseBoolean(json.get(UserConstants.IS_ACTIVE).toString());
			organizationServiceImpl.updateUser(orgId,userId,status);
			int activeCount = 0;
			for(Organization organization : orgs){
				boolean isActive = userOrganizationRepo.getUserStatus(userId,organization.getId());
				if(isActive){
					activeCount++;
				}	
			}
			if(activeCount == 0)
			user.setActive(false);
			else
			user.setActive(true);
			
		}

		/* ---------- 7. Audit & save ---------- */
		Long userId=  userRepo.findIdByEmail(userEmailId).orElse(0L);
		user.setLastUpdatedBy(userId);
		user.setLastUpdatedDt(new Date());
		userRepo.save(user);
		/* ---------- 8. Response ---------- */
		String isStatusUpdate = json.get(UserConstants.IS_STATUS_UPDATE) != null
		        ? json.get(UserConstants.IS_STATUS_UPDATE).toString()
		        : "no";
		JSONObject res = userUtils.updateResponse(
			    new JSONObject(),
			    UserConstants.STATUS_SUCCESS,
			    UserConstants.SUCCESS,
			    "yes".equalsIgnoreCase(isStatusUpdate)
			        ? ("User " + (user.getActive() ? UserConstants.ENABLED : UserConstants.DISABLED))
			        : UserConstants.USER_UPDATE_SUCCESS_MESSAGE,
			    null
			);

		return res;
	}
	
	

	@Override
	@Transactional
	public JSONObject resetPassword(JSONObject json, HttpServletRequest request) {
		LOG.info("Inside resetPassword API");
		LOG.debug("Request : " + json);

		JSONObject response = new JSONObject();

		String jwtToken = request.getHeader(UserConstants.JWT_TOKEN);
		
		String userEmailId = SecurityUtil.currentUserEmailId();
		Optional<ResetPasswordProjection> userOpt = userRepo.findUserByEmail(userEmailId);

		if (userOpt.isEmpty()) {
			userUtils.globalException(UserConstants.INVALID_USERID, Integer.parseInt(UserConstants.UNAUTHORIZED));
		}

		ResetPasswordProjection user = userOpt.get();
		
		LOG.info("userid:"+user.id()+" "+user.email());

		if (!jwtToken.equals(user.jwtToken())) {
			userUtils.globalException(UserConstants.USER_ALREADY_LOGOUT, Integer.parseInt(UserConstants.UNAUTHORIZED));
		}

		if (json == null || json.get(UserConstants.OLD_PASSWORD) == null
				|| json.get(UserConstants.NEW_PASSWORD) == null) {
			userUtils.globalException(UserConstants.INVALID_OLD_OR_NEW_PASSWORD,
					Integer.parseInt(UserConstants.BAD_REQUEST_CODE));
		}

		/*String oldPwdHash = userUtils.getSHA(json.get(UserConstants.OLD_PASSWORD).toString());

		// Verify old password
		boolean passwordMatches = oldPwdHash.equalsIgnoreCase(user.password());
		boolean oneTimePwdMatches = oldPwdHash.equalsIgnoreCase(user.onetimePassword());

		if (user.password() != null && !passwordMatches) {
			userUtils.globalException(UserConstants.WRONG_OLD_PASSWORD, Integer.parseInt(UserConstants.UNAUTHORIZED));
		}

		if ((user.password() == null || user.password().isEmpty()) && user.onetimePassword() != null
				&& !oneTimePwdMatches) {
			userUtils.globalException(UserConstants.WRONG_OLD_PASSWORD, Integer.parseInt(UserConstants.UNAUTHORIZED));
		}*/

		// Check new password is not same as old
		String newPwdHash = userUtils.getSHA(json.get(UserConstants.NEW_PASSWORD).toString());
		if (newPwdHash != null && user.password() != null && newPwdHash.equalsIgnoreCase(user.password())) {
			userUtils.globalException(UserConstants.OLD_AND_NEW_PASSWORD_SAME,
					Integer.parseInt(UserConstants.STATUS_INVALID));
		}

		LOG.info("User password before: {}", user.password());
		
		 // Update password via JPQL without loading entity
		if(!updatePassword(user.id(), newPwdHash)) {
			 userUtils.globalException(UserConstants.FAILURE,
		                Integer.parseInt(UserConstants.STATUS_INVALID));
		}

		LOG.info("User password after: {}", user.password());
		LOG.info("resetPassword API End");
		LOG.info(UserConstants.PASSWORD_UPDATE_SUCCESS_MESSAGE);

		return userUtils.updateResponse(response, UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
				UserConstants.PASSWORD_UPDATE_SUCCESS_MESSAGE, null);
	}
	
	private boolean updatePassword(Long userId, String newPassword) {
		boolean updatedSuccessfully = false;
		boolean exists = userRepo.existsById(userId);
		if(!exists) {
			LOG.info("No row exists to update");
            LOG.info("End of {}.updateStatusById - {}", this.getClass().getName(), StatusConstants.NOT_FOUND);
            updatedSuccessfully = false;
		}else {
			int maxRetries = 3;
			int attempts = 0;
	
			while (attempts < maxRetries && !updatedSuccessfully) {
			    attempts++;
			    Optional<Long> lockVersion = userRepo.findLockVersionById(userId);
			    if (lockVersion.isEmpty()) {
			        LOG.warn("User not found while retrying updatePassword, userId={}", userId);
			        break;
			    }
			    int updated = userRepo.updatePassword(userId, newPassword,lockVersion.get());
				if (updated == 1) {
			        updatedSuccessfully = true;
			    } else {
			        // Optional: wait a bit before retrying
			        try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // 100ms backoff
			    }
			}
			if (!updatedSuccessfully) {
			    LOG.info("End of " + this.getClass().getName() + ".updateUser"
			             + StatusConstants.OPERATION_FAILED);
		        LOG.info("End of {}.updateStatusById - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
			}else {
                LOG.info("End of {}.updateStatusById - {}", this.getClass().getName(), StatusConstants.SUCCESS);
			}
		}
		return updatedSuccessfully;
	}

	
	@Override
	public JSONObject getList(JSONObject json) {
	    LOG.info("Inside getList User API");
	    LOG.debug("Request : " + json);
	    JSONObject response = new JSONObject();

	    String email = SecurityUtil.currentUserEmailId();

	    boolean allowed = userRepo.hasPrivilege(email, UserConstants.PRIVILEGE_LIST_USER);
	    if (!allowed) {
	        userUtils.globalException(
	            UserConstants.NO_PRIVILAGES_TO_ACCESS,
	            Integer.parseInt(UserConstants.STATUS_RESTRICTED)
	        );
	        return null;
	    }

	    Optional<User> userOtp = userRepo.findByEmail(email);
	    User orgUser = userOtp.get();

	    LOG.info("jsonrequest::"+json);
	    // ✅ Read pagination params
	    int page = json.containsKey("pageNumber") ? Integer.parseInt(json.get("pageNumber").toString()) : 0;
	    int size = json.containsKey("pageSize") ? Integer.parseInt(json.get("pageSize").toString()) : 10;
	    String search="";
	    if(json.get("search")!=null)
	    {
	    	search = json.get("search").toString();
	    }
	    
	    String type = json.containsKey("type")?json.get("type").toString():"users";
	    String filter = json.get("filter").toString();
	    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

	    Page<User> userPage = null;
	    List<String> roles = null;
	    Long orgId = Long.valueOf(json.get(UserConstants.ORG_ID).toString());

	    if ("0".equals(orgId.toString())) {
	    	if(filter.equalsIgnoreCase("ADMIN"))
	    	{
	    		userPage = userRepo.findAllAdmins(pageable);
	    	}
	    	else if(filter.equalsIgnoreCase("CONSULTANT_DOCTOR"))
	    	{
	    		userPage = userRepo.findConsultantDoctors(pageable);
	    	}
	    	else if(filter.equalsIgnoreCase("RESIDENT_DOCTOR"))
	    	{
	    		userPage = userRepo.findAllResidentDoctors(pageable);
	    	}
	    	else if(filter.equalsIgnoreCase("TECHNICIAN"))
	    	{
	    		userPage = userRepo.findAllTechnicians(pageable);
	    	}
	    	else if(filter.equalsIgnoreCase("ALL"))
	    	{
	    		userPage = userRepo.findAllUsers(pageable);
	    	}
	    	else if(filter.equalsIgnoreCase("SUPER_ADMIN"))
	    	{
	    		userPage = userRepo.findSuperAdmin(pageable);
	    	}

	        if (userPage == null || userPage.isEmpty()) {
	            userUtils.globalException(UserConstants.EMPTY_RESULT, Integer.parseInt(UserConstants.NO_CONTENT_CODE));
	            return null;
	        }
	    } else {
	        boolean orgExists = orgRepo.existsById(orgId);
	        if (!orgExists) {
	            userUtils.globalException(UserConstants.NO_ORG, 404);
	        }
	        userPage = getOrgUser(orgId, orgUser, pageable,type,filter,search);  // ⬅ change method signature to accept pageable
	        roles = userRepo.findRolesForAdmin(orgId);
	        }

	    List<Map<String, Object>> listMap = new ArrayList<>();
		for (User usr : userPage.getContent()) {
			Map<String, Object> userMap = userUtils.createUserJson(usr);
			//if (!orgId.equals("-1") && !orgId.equals("0")) 
				if (!("-1".equals(orgId.toString()))&& !("0".equals(orgId.toString())))
				userMap = updateUserStatus(usr.getId(), Long.valueOf(orgId), userMap);
			if (type.equalsIgnoreCase("doctors")) {
				List<String> studyIdList = userStudiesService.getStudyIdByDoctor(usr.getId());
				if (userMap != null) {
					userMap.put(UserConstants.STUDY_LIST, studyIdList);
				}
				
			}
			listMap.add(userMap);
		}

	    // ✅ Build pageable response
	    Map<String, Object> pageInfo = new HashMap<>();
	    pageInfo.put("page", userPage.getNumber());
	    pageInfo.put("size", userPage.getSize());
	    pageInfo.put("totalPages", userPage.getTotalPages());
	    pageInfo.put("totalElements", userPage.getTotalElements());

	    response = userUtils.updateResponse(response,
	            UserConstants.STATUS_SUCCESS,
	            UserConstants.SUCCESS,
	            UserConstants.USER_GET_SUCCESS_MESSAGE,
	            listMap);

	    response.put("pageInfo", pageInfo);
	    response.put("roles",roles);

	    LOG.info("getList User API End");
	    LOG.debug("Response : " + response);

	    return response;
	}
	
	public List<User> getOrgUser(Long orgId, User orgUser) {
		List<Long> userIdList = userOrganizationRepo.findUserIdsByOrgId(orgId);
		List<User> users = new ArrayList<User>();
		if (userIdList.isEmpty() || orgUser.getRole() == null) {
			return users;
		}
		switch (orgUser.getRole().getName()) {
		case UserConstants.SUPER_ADMIN:
			return userRepo.findUsersByIdList(userIdList);
		case UserConstants.ORG_ADMIN:
			return userRepo.findForOrgAdmin(userIdList);
		case UserConstants.TECHNICIAN:
			return userRepo.findForUserAdmin(userIdList);
		case UserConstants.DOCTOR:
			return Collections.singletonList(orgUser);
		default:
			return Collections.emptyList();
		}
	}
	
	public Page<User> getOrgUser(Long orgId, User orgUser, Pageable page,String type,String filter, String search) {
		List<Long> userIdList = null;
		if(orgId != -1)
		{
		userIdList = userOrganizationRepo.findUserIdsByOrgId(orgId);

	    if (userIdList.isEmpty() || orgUser.getRole() == null) {
	        return Page.empty(page); // ✅ Proper empty Page
	    }
		}
		
		String role = orgUser.getRole().getName();
		String normalizedType = type == null ? "" : type.toUpperCase();
		String normalizedFilter = filter == null ? "ALL" : filter.toUpperCase();

		switch (role) {

		case UserConstants.SUPER_ADMIN:
		case UserConstants.ORG_ADMIN:
		case UserConstants.TECHNICIAN:
			return handleOrgAdminUsers(normalizedType, normalizedFilter, userIdList, page,role,search);

		case UserConstants.DOCTOR:
			List<User> doctorList = Collections.singletonList(orgUser);
			return new PageImpl<>(doctorList, page, doctorList.size());

		default:
			return Page.empty(page);
		}        
	   	}
	
		private Page<User> handleOrgAdminUsers(String type, String filter, List<Long> userIdList, Pageable page, String role,String search) {

			boolean isUsersType = "USERS".equals(type);

			switch (filter) {

			case "ALL":
				if(role.equalsIgnoreCase(UserConstants.SUPER_ADMIN))
				{
					if(isUsersType)
						return userRepo.getAllUsersForOrgAdmin(userIdList, search,page);
					else
						return Page.empty();
				}
				else
				{
				return isUsersType ? userRepo.getUsersForOrgAdmin(userIdList,search, page)
						: userRepo.getDoctorsForOrgAdmin(userIdList, search, page);
				}

			case "ADMIN":
				return isUsersType ? userRepo.getAdminsForOrgAdmin(userIdList, search,page) : Page.empty(page);

			case "TECHNICIAN":
				return isUsersType ? userRepo.getTechniciansForOrgAdmin(userIdList,search, page) : Page.empty(page);

			case "CONSULTANT_DOCTOR":
				return userRepo.getConsultantDoctorsForOrgAdmin(userIdList, search,page);

			case "RESIDENT_DOCTOR":
				return userRepo.getResidentDoctorsForOrgAdmin(userIdList, search,page);

			default:
				return Page.empty(page);
			}
		}

	public List<Long> getOrgUser(long orgId, long userId,String role) {
		List<Long> userIdList = userOrganizationRepo.findUserIdsByOrgId(orgId);
		List<Long> userIds = new ArrayList<Long>();
		if (userIdList.isEmpty() || role == null) {
			return userIds;
		}
		switch (role) {
		case UserConstants.SUPER_ADMIN:
			return userRepo.findUsersIdByIdList(userIdList);
		case UserConstants.ORG_ADMIN:
			return userRepo.findIdForOrgAdmin(userIdList);
		case UserConstants.TECHNICIAN:
			return userRepo.findIdForUserAdmin(userIdList);
		case UserConstants.DOCTOR:
			return Collections.singletonList(userId);
		default:
			return Collections.emptyList();
		}
	}

	@Override
	@Transactional
	public JSONObject forgotPassword(JSONObject json) {
		LOG.info("Inside forgotPassword API");
		LOG.debug("Request : {}", json);

		JSONObject response = new JSONObject();

		// Validate email input
		if (json == null || !json.containsKey(UserConstants.EMAIL)) {
			LOG.warn(UserConstants.MISSING_EMAIL);
			userUtils.globalException(UserConstants.MISSING_EMAIL, Integer.parseInt(UserConstants.BAD_REQUEST_CODE));
		}

		String email = json.get(UserConstants.EMAIL).toString().toLowerCase();
		Optional<User> userOpt = userRepo.findByEmail(email);

		if (userOpt.isEmpty()) {
			userUtils.globalException(UserConstants.INVALID_USERID, Integer.parseInt(UserConstants.UNAUTHORIZED));
		}

		User user = userOpt.get();

		// Generate random password and hash it
		String randomPasswordToHash = userUtils.generate().substring(0, 8);
		String hashedPassword = userUtils.getSHA(randomPasswordToHash);

		// Update user object
		user.setPassword(null);
		user.setOnetimePassword(hashedPassword);
		user.setOnetimePwdStatus(false);
		user.setLastUpdatedBy(user.getId());
		user.setLastUpdatedDt(new Date());

		userRepo.save(user); // Spring Data JPA saves/updates the entity

		// Prepare email
		String body = userUtils.mailBodyForgotPwd(randomPasswordToHash, user);
		LOG.info("Forgot password email body: {}", body);

		// Send email
		emailService.sendEmail(user.getEmail(), UserConstants.FORGOT_PASSWORD, body);

		LOG.info("forgotPassword API End");

		return userUtils.updateResponse(response, UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
				UserConstants.ONETIME_PASSWORD_SENT_MSG, null);
	}

	@Transactional
	@Override
	public JSONObject logout() {
		LOG.info("Inside logout API");

		JSONObject response = new JSONObject();

		// Get userId from SecurityContext (populated by JWT filter)
		//Long userId = SecurityUtil.currentUserId(); // You already added this utility
		
		String userEmailId = SecurityUtil.currentUserEmailId();
		if(userEmailId.equalsIgnoreCase("anonymousUser")) {
			return userUtils.updateResponse(response, UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
					UserConstants.USER_LOG_OUT_SUCCESS, null);
		}
		Optional<Long> optionalUserId = userRepo.findIdByEmail(userEmailId);

		
		if (optionalUserId.isEmpty()) {
			userUtils.globalException(UserConstants.INVALID_USERID, Integer.parseInt(UserConstants.UNAUTHORIZED));
		}
		// Clear JWT token from DB or in-memory store
		Long userId = optionalUserId.get();
		if(!updateToken(userId, null)) {
			userUtils.globalException(UserConstants.FAILURE, Integer.parseInt(UserConstants.SERVER_ERROR));
		}

		LOG.info("logout API End");
		LOG.info(UserConstants.USER_LOG_OUT_SUCCESS);

		return userUtils.updateResponse(response, UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
				UserConstants.USER_LOG_OUT_SUCCESS, null);
	}

	@Override
	@Transactional
	public JSONObject getUser(String userId) {
		LOG.info("Inside getUser API");
		LOG.debug("userId : " + userId);

		JSONObject response = new JSONObject();

		try {
			// Fetching user based on provided userId or using current user
			User user;
			if (userId != null && !userId.equalsIgnoreCase("null")) {
				Optional<User> userOpt = userRepo.findById(Long.parseLong(userId));
				if (userOpt.isEmpty()) {
					userUtils.globalException(UserConstants.INVALID_USERID,
							Integer.parseInt(UserConstants.UNAUTHORIZED));
				}
				user = userOpt.get();
			} else {
				String userEmailId = SecurityUtil.currentUserEmailId();
				Optional<User> loggedUserOpt = userRepo.findByEmail(userEmailId);
				if (loggedUserOpt.isEmpty()) {
					userUtils.globalException(UserConstants.INVALID_USERID, Integer.parseInt(UserConstants.UNAUTHORIZED));
				}

				user = loggedUserOpt.get();
			}

			// Create response object
			Map<String, Object> userObj = userUtils.createUserJson(user);
			LOG.info("getUser API End");

			return userUtils.updateResponse(response, UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
					UserConstants.USER_GET_SUCCESS_MESSAGE, userObj);
		} catch (Exception e) {
			LOG.error("Exception : " + e.getMessage());
			e.printStackTrace();
			return userUtils.errResponseAsTextPlain(userUtils.exceptionAsString(e));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public JSONObject assignPatient(JSONObject json) {
		LOG.info("Inside assignPatient API");
		LOG.debug("Request: {}", json);

		JSONObject response = new JSONObject();

		// 1. Authenticated user info from Spring Security context
		//Long currentUserId = SecurityUtil.currentUserId();
		//String currentJwt = SecurityUtil.currentJwtToken();
		
		String userEmailId = SecurityUtil.currentUserEmailId();
		
		boolean allowed = userRepo.hasPrivilege(userEmailId, UserConstants.PRIVILEGE_LIST_STUDY);

		if (!allowed) {
		    userUtils.globalException(
		        UserConstants.NO_PRIVILAGES_TO_ACCESS,
		        Integer.parseInt(UserConstants.STATUS_RESTRICTED)
		    );
		}

		// 4. Fetch doctor user by ID
		Long doctorId = Long.parseLong(json.get(UserConstants.USER_ID).toString());
		Optional<User> doctorOpt = userRepo.findById(doctorId);
		if (doctorOpt.isEmpty()) {
			userUtils.globalException(UserConstants.INVALID_DOCTOR, Integer.parseInt(UserConstants.NO_CONTENT_CODE));
			return null;
		}
		User doctor = doctorOpt.get();

		// 5. Assign studies
		List<String> studyList = (List<String>) json.get(UserConstants.STUDY_LIST);
		if (studyList != null) {
			for (String studyId : studyList) {
				boolean userStudiesExists = userStudiesService.getUserStudiesByStudiesIdAndDoctorId(studyId,
						doctor.getId());
				Long userId = userRepo.findIdByEmail(userEmailId).orElse(0L);
				if (userStudiesExists) {
					userStudiesRepo.updateUserStudiesFields(userId, new Date(), Boolean.TRUE, doctorId, new Date(), studyId, doctorId);
				}else {
					UserStudies newUserStudy = new UserStudies();
					newUserStudy = userUtils.setUserStudies(newUserStudy, userId, doctor, studyId);
					userStudiesService.saveOrUpdate(newUserStudy);
				}
				userStudiesService.updateStudyStatus(studyId, "Assigned");
			}
		}

		LOG.info("assignPatient API End");

		return userUtils.updateResponse(response, UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
				UserConstants.USER_STUDIES, null);
	}

	@Override
	@Transactional
	public JSONObject acceptInvitation(String userId, String orgId) {
		LOG.info("Inside acceptInvitation API");
		LOG.debug("UserId : {}", userId);
		LOG.debug("OrgId : {}", orgId);

		JSONObject response = new JSONObject();

		// 1. Find User
		Optional<User> optionalUser = userRepo.findById(Long.parseLong(userId));
		if (optionalUser.isEmpty()) {
			userUtils.globalException(UserConstants.INVALID_USERID, Integer.parseInt(UserConstants.UNAUTHORIZED));
		}
		User orgUser = optionalUser.get();
		// 2. Find Organization
		Optional<Organization> optionalOrg = orgRepo.findById(Long.parseLong(orgId));
		if (optionalOrg.isEmpty()) {
			userUtils.globalException(UserConstants.NO_ORG, Integer.parseInt(UserConstants.NO_CONTENT_CODE));
		}
		Organization org = optionalOrg.get();
		// 3. Check if already part of organization
		if (orgUser.getOrgs() != null && orgUser.getOrgs().contains(org)) {
			response.put("message", "You are already part of this tenant");
			return response;
		}
		// 4. Add org to user
		orgUser.getOrgs().add(org);
		orgUser.setLastUpdatedDt(new Date());
		orgUser.setLastUpdatedBy(orgUser.getId());
		userRepo.save(orgUser); // update
		String userGroup = "org-"+org.getId();
		userOrganizationRepo.updateUserGroup(orgUser.getId(),org.getId(),userGroup);
		
		response.put("message", "Thank You.. Now you can login and see the newly added tenant");

		LOG.debug("Response : {}", response);
		LOG.info("acceptInvitation API End");

		return response;
	}

	private List<User> geDoctorList(Long orgId) {
		List<Long> userIdList = userOrganizationRepo.findUserIdsByOrgId(orgId);
		ArrayList<Long> idList = new ArrayList<Long>();
		for (int i = 0; userIdList != null && i < userIdList.size(); i++) {
			idList.add(Long.valueOf(String.valueOf(userIdList.get(i))));
		}
		return userRepo.findDoctorsByIdList(idList);
	}

	@Override
	@Transactional
	public JSONObject getDoctorList(String orgId) {
		LOG.info("Inside getDoctorList API");

		JSONObject response = new JSONObject();
		String userEmailId = SecurityUtil.currentUserEmailId();
		
		boolean allowed = userRepo.hasPrivilege(userEmailId, UserConstants.PRIVILEGE_LIST_USER);

		if (!allowed) {
		    userUtils.globalException(
		        UserConstants.NO_PRIVILAGES_TO_ACCESS,
		        Integer.parseInt(UserConstants.STATUS_RESTRICTED)
		    );
		    return null;
		}

		// Determine users list
		List<User> users;
		if (orgId == null) {
			userUtils.globalException(UserConstants.NO_ORG, Integer.parseInt(UserConstants.NO_CONTENT_CODE));
			return null;
		}

		if (orgId.equals("0")) {
			users = userRepo.findConsultantDoctors();
		} else if (orgId.equals("-1")) {
			users = userRepo.findAll(); // get all users
		} else {
			boolean optionalOrgExists = orgRepo.existsById(Long.valueOf(orgId));
			if (!optionalOrgExists) {
				userUtils.globalException(UserConstants.NO_ORG, Integer.parseInt(UserConstants.NO_CONTENT_CODE));
			}
//			Organization org = optionalOrg.get();
			users = geDoctorList(Long.valueOf(orgId));
		}

		// Build response
		if (users != null && !users.isEmpty()) {
			List<Map<String, Object>> listMap = new ArrayList<>();
			for (User usr : users) {
				List<String> studyIdList = userStudiesService.getStudyIdByDoctor(usr.getId());

				Map<String, Object> userMap = userUtils.createUserJson(usr);
				if( !orgId.equals("-1") && !orgId.equals("0"))
					userMap = updateUserStatus(usr.getId(), Long.valueOf(orgId), userMap);
					if(userMap!=null) {
						userMap.put(UserConstants.STUDY_LIST, studyIdList);
					}
					
				listMap.add(userMap);
			}

			return userUtils.updateResponse(response, UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
					UserConstants.USER_GET_SUCCESS_MESSAGE, listMap);
		} else {
			userUtils.globalException(UserConstants.EMPTY_RESULT, Integer.parseInt(UserConstants.NO_CONTENT_CODE));
			return null;
		}
	}

	@Override
	@Transactional
	public JSONObject deleteUserStudy(String studyId, String doctorId) {
		LOG.info("Inside deleteUserStudy API  |  studyId={}, doctorId={}", studyId, doctorId);

		JSONObject response = new JSONObject();
		String userEmailId = SecurityUtil.currentUserEmailId();
		
		boolean allowed = userRepo.hasPrivilege(userEmailId, UserConstants.PRIVILEGE_DELETE_STUDY);

		if (!allowed) {
		    userUtils.globalException(
		        UserConstants.NO_PRIVILAGES_TO_ACCESS,
		        Integer.parseInt(UserConstants.STATUS_RESTRICTED)
		    );
		    return null;
		}

		/* Validate doctor */
		boolean doctorExists = userRepo.existsById(Long.parseLong(doctorId));
		if (!doctorExists) {
			userUtils.globalException(UserConstants.INVALID_DOCTOR, Integer.parseInt(UserConstants.NO_CONTENT_CODE));
			return null;
		}
		/* Locate the UserStudies record */
		boolean userStudyExists = userStudiesService.getUserStudiesByStudiesIdAndDoctorId(studyId, Long.parseLong(doctorId));

		if (!userStudyExists) {
			userUtils.globalException(UserConstants.INVALID_STUDY_ID, Integer.parseInt(UserConstants.NO_CONTENT_CODE));
			return null;
		}

		/* Delete and respond */
		userStudiesService.deleteUserStudy(studyId, Long.parseLong(doctorId));

		LOG.info("deleteUserStudy API End");

		return userUtils.updateResponse(response, UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
				UserConstants.USER_STUDIES_DELETED, null);
	}

	@Transactional
	@Override
	public JSONObject getConsultantDoctors() {
		LOG.info("Inside getConsultantDoctors API");

		//Long userId = SecurityUtil.currentUserId();
		// Load user via Optional
		//Optional<User> orgUserOpt = userRepo.findById(userId);
		String userEmailId = SecurityUtil.currentUserEmailId();
		boolean allowed = userRepo.hasPrivilege(userEmailId, UserConstants.PRIVILEGE_LIST_USER);

		if (!allowed) {
		    userUtils.globalException(
		        UserConstants.NO_PRIVILAGES_TO_ACCESS,
		        Integer.parseInt(UserConstants.STATUS_RESTRICTED)
		    );
		    return null;
		}

		// Fetch consultant doctor list
		List<User> users = userRepo.findConsultantDoctors();

		if (users == null || users.isEmpty()) {
			userUtils.globalException(UserConstants.EMPTY_RESULT, Integer.parseInt(UserConstants.NO_CONTENT_CODE));
		}

		// Convert users to response
		List<Map<String, Object>> listMap = users.stream().map(userUtils::createUserJson).toList();

		LOG.info("getConsultantDoctors API End");

		// Prepare response
		return userUtils.updateResponse(new JSONObject(), UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
				UserConstants.USER_GET_SUCCESS_MESSAGE, listMap);
	}

	@Transactional
	@Override
	public JSONObject getConsultant(String emailId) {
		LOG.info("Inside getConsultant API");
		LOG.debug("emailId : " + emailId);
		String userEmailId = SecurityUtil.currentUserEmailId();
		
		boolean allowed = userRepo.hasPrivilege(userEmailId, UserConstants.PRIVILEGE_LIST_USER);

		if (!allowed) {
		    userUtils.globalException(
		        UserConstants.NO_PRIVILAGES_TO_ACCESS,
		        Integer.parseInt(UserConstants.STATUS_RESTRICTED)
		    );
		    return null;
		}

		Optional<User> userOpt = userRepo.findByEmail(emailId);

		if (userOpt.isEmpty()) {
			JSONObject result = userUtils.updateResponse(new JSONObject(), UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS, "Email is available", null);
			result.put(EMAIL_EXISTS, false);
			LOG.info("getConsultant API End - email not found");
			return result;
		}

		User user = userOpt.get();

		if (Boolean.TRUE.equals(user.getConsultant())) {
			List<Map<String, Object>> listMap = new ArrayList<>();
			Map<String, Object> map = userUtils.createUserJson(user);
			listMap.add(map);
			JSONObject result = userUtils.updateResponse(new JSONObject(), UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS, UserConstants.USER_GET_SUCCESS_MESSAGE, listMap);
			result.put(EMAIL_EXISTS, true);
			LOG.info("getConsultant API End");
			return result;
		} else {
			JSONObject result = userUtils.updateResponse(new JSONObject(), UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS, UserConstants.INVALID_DOCTOR, null);
			result.put(EMAIL_EXISTS, true);
			LOG.info("getConsultant API End - user exists but not a consultant");
			return result;
		}
	}

	@Transactional
	@Override
	public JSONObject getAll() {
		LOG.info("Inside getAll User API");
		
		//Long userId = SecurityUtil.currentUserId();
		//String jwtToken = SecurityUtil.currentJwtToken();
		
		String userEmailId = SecurityUtil.currentUserEmailId();
		
		boolean allowed = userRepo.hasPrivilege(userEmailId, UserConstants.PRIVILEGE_LIST_USER);

		if (!allowed) {
		    userUtils.globalException(
		        UserConstants.NO_PRIVILAGES_TO_ACCESS,
		        Integer.parseInt(UserConstants.STATUS_RESTRICTED)
		    );
		    return null;
		}
		
		List<User> users = userRepo.findAll();

		if (users.isEmpty()) {
			userUtils.globalException(UserConstants.EMPTY_RESULT, Integer.parseInt(UserConstants.NO_CONTENT_CODE));
		}
		
		List<Map<String, Object>> listMap = new ArrayList<>();
		for (User usr : users) {
			listMap.add(userUtils.createUserJson(usr));
		}

		JSONObject response = new JSONObject();
		response = userUtils.updateResponse(response, UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS, UserConstants.USER_GET_SUCCESS_MESSAGE, listMap);

		LOG.info("getAll API End");
		LOG.debug("Response : {}", response);
		return response;
	}

	
	@Transactional
	@Override
	public JSONObject licenseCheck(String orgIdStr) {
		LOG.info("Inside licenseCheck User API");
		JSONObject response = new JSONObject();
		// Step 4: Load Organization
		Long orgId = Long.parseLong(orgIdStr);
		Optional<OrgConsultantProjection> orgOpt = orgRepo.findConsultantDetailsById(orgId);
		if (orgOpt.isEmpty()) {
			userUtils.globalException(UserConstants.ORGANIZATION_NOT_EXIST_MESSAGE,
					Integer.parseInt(UserConstants.UNAUTHORIZED));
		}

		OrgConsultantProjection org = orgOpt.get();

		Integer uploadLimit = 0;
		boolean isConsultantTechnician = false;

		if (org.getConsultant()) {
			// If user is consultant, check their personal upload limit
			String email = SecurityUtil.currentUserEmailId();
			Optional<Boolean> isUserConsultant = userRepo.isConsultantByEmail(email);
			if (isUserConsultant.isPresent() && isUserConsultant.get().equals(Boolean.TRUE)) {
				Optional<Integer> userUploadLimit = userRepo.findUploadLimitByEmail(email);
				uploadLimit = userUploadLimit.isPresent()? userUploadLimit.get():0;
			} else {
				uploadLimit = org.getUploadLimit();
				isConsultantTechnician = true;
			}

			if (uploadLimit == 0) {
				userUtils.globalException(UserConstants.ORGANIZATION_NOT_EXIST_MESSAGE,
						Integer.parseInt(UserConstants.UNAUTHORIZED));
			}

			Long studyCount = 0L;
			if (isConsultantTechnician) {
				studyCount = studyRepo.countByOrgId(orgId);
			} else {
				Long userId = userRepo.findIdByEmail(email).orElse(0L);
				studyCount = studyRepo.countByUserOrgId(String.valueOf(userId), orgId);
			}

			if (uploadLimit > studyCount) {
				LOG.info("licenseCheck API End - Upload limit available");
				response.put("statusCode", UserConstants.STATUS_SUCCESS);
				response.put("statusMsg", "Upload limit is available");
			} else {
				LOG.info("licenseCheck API End - Upload limit exceeded");
				response.put("statusCode", UserConstants.OPERATION_FAILED);
				response.put("statusMsg", "Contact admin for licence upgrade");
			}

			return response;
		}

		// Case: Not a consultant org
		LOG.info("licenseCheck API End - Not a consultant org");
		response.put("statusCode", UserConstants.STATUS_SUCCESS);
		response.put("statusMsg", "Upload limit is available");
		return response;
	}
	
	
	public Long getAllActiveUserByOrg(Long orgId) {
		List<Long> userIdList = userOrganizationRepo.findUserIdsByOrgId(orgId);
		ArrayList<Long> idList = new ArrayList<Long>();
		for (int i = 0; userIdList != null && i < userIdList.size(); i++) {
			idList.add(Long.valueOf(String.valueOf(userIdList.get(i))));
		}
		
		Long total = userRepo.countActiveUsersWithRoleAtLeast2ByIds(idList);
		return total;
	}
	
	public Long getAllActiveUserByOrgAdmins(Long orgId){
		List<Long> userIdList = userOrganizationRepo.findUserIdsByOrgId(orgId);
		ArrayList<Long> idList = new ArrayList<Long>();
		for (int i = 0; userIdList != null && i < userIdList.size(); i++) {
			idList.add(Long.valueOf(String.valueOf(userIdList.get(i))));
		}
		Long userCount = userRepo.countByIdInAndActiveTrue(idList);
		return userCount;
	}
	
	
	public Long getAllActiveUserByOrgTechnicians(Long orgId) {
	    List<Long> userIds = userOrganizationRepo.findUserIdsByOrgId(orgId);
	    if (userIds.isEmpty()) return 0L;
	    List<User> list = userRepo.findForUserAdmin(userIds);
	    return list.size()>0 ? Long.valueOf(list.size()) : 0;
	}

	
	public Long getAllActiveUserByOrgDoctors(Long orgId) {
	    List<Long> userIds = userOrganizationRepo.findUserIdsByOrgId(orgId);
	    if (userIds.isEmpty()) return 0L;
	    long doctorsCount = userRepo.countDoctorsByIdList(userIds);
	    return doctorsCount;
	}


	public Long getAllActiveUserByOrgConsultants(Long orgId) {
	    List<Long> userIds = userOrganizationRepo.findUserIdsByOrgId(orgId);
	    if (userIds.isEmpty()) return 0L;
	    return userRepo.countActiveConsultantsByUserIds(userIds);
	}
	
	public Map<String,Object> updateUserStatus(Long userId, Long orgId, Map<String, Object> map) {
		boolean status = userOrganizationRepo.getUserStatus(userId,orgId);
		map.put(UserConstants.IS_ACTIVE, status);
		return map;
	}
	
	private String createUserIfNotExists(UsersResource usersResource, RealmResource realmResource, String username,
			String email, String clientRoleName, String clientId, String password,Long newOrgId,Long Id, String adminOrgId) {
		username = username.trim().replaceAll("[^a-zA-Z0-9_\\-]", "_");
		if (username.isEmpty()) {
			username = "user_" + UUID.randomUUID().toString().substring(0, 6);
		}
		final String finalUsername = username;
		String userId;
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
			        LOG.info("Created user: " + username + " (" + email + ")");

			    } else if (status == 409) {
			        username = username + "-" + UUID.randomUUID().toString().substring(0, 6);
			        LOG.info(
			            "Username conflict for '" + finalUsername + "'. Trying new username: " + username
			        );

			        return createUserIfNotExists(
			                usersResource,
			                realmResource,
			                username,
			                email,
			                clientRoleName,
			                clientId,
			                password,
			                newOrgId,
			                Id,
			                adminOrgId
			        );

			    } else {
			        throw new RuntimeException(
			            "Failed to create user: " + username + ", Status: " + status
			        );
			    }
			}
		

		/*if (clientRoleName != null && !clientRoleName.isEmpty() && clientId != null && !clientId.isEmpty()) {
			List<ClientRepresentation> clients = realmResource.clients().findByClientId(clientId);
			if (clients.isEmpty())
				throw new RuntimeException("Client not found: " + clientId);
			String clientUUID = clients.get(0).getId();

			RoleRepresentation clientRole = realmResource.clients().get(clientUUID).roles().get(clientRoleName)
					.toRepresentation();

			List<RoleRepresentation> assignedClientRoles = usersResource.get(userId).roles().clientLevel(clientUUID)
					.listAll();
			boolean clientRoleAssigned = assignedClientRoles.stream().anyMatch(r -> r.getName().equals(clientRoleName));
			if (!clientRoleAssigned) {
				usersResource.get(userId).roles().clientLevel(clientUUID).add(Collections.singletonList(clientRole));
				System.out.println("Assigned client role '" + clientRoleName + "' to user " + username);
			}
			String groupName = "org-" + orgId;
            String groupId = createGroupIfNotExists(realmResource, groupName);

            // Add admin to the org group
            String uId = String.valueOf(Id);
            joinGroupSafely(usersResource, userId, groupId, uId,groupName);

		}*/
		String groupName = "org-" + newOrgId;
		String orgGroup = "org-"+adminOrgId;
		List<String> groups = new ArrayList<String>();
		groups.add(groupName);
		groups.add(orgGroup);
		//List<String> groupId = createGroupsIfNotExists(realmResource, groups);

        // Add admin to the org group
        String uId = String.valueOf(Id);
        //joinGroupSafely(usersResource, userId, groupId, uId,groupName);
        List<String> groupIds = createGroupsIfNotExists(realmResource, groups);
        joinGroupsSafely(usersResource, userId, groupIds, uId, groups,newOrgId);

        
		if (!"admin".equalsIgnoreCase(username)) {
            UserResource userResource = usersResource.get(userId);

            // Remove all current realm roles
            List<RoleRepresentation> currentRealmRoles = userResource.roles().realmLevel().listAll();
            if (!currentRealmRoles.isEmpty()) {
                userResource.roles().realmLevel().remove(currentRealmRoles);
                System.out.println("Removed default realm roles for user: " + username);
            }

            // Assign required realm roles
            List<String> rolesToAssign = Arrays.asList("user", "auth");
            for (String roleName : rolesToAssign) {
                try {
                    RoleRepresentation roleRep = realmResource.roles().get(roleName).toRepresentation();
                    userResource.roles().realmLevel().add(Collections.singletonList(roleRep));
                    System.out.println("Assigned realm role '" + roleName + "' to user: " + username);
                } catch (Exception e) {
                    System.err.println("⚠️ Failed to assign realm role '" + roleName + "' to user: " + username + " — " + e.getMessage());
                }
            }
        } else {
            System.out.println("Skipping role modification for admin user: " + username);
        }

		return userId;
	}

	/*private String createGroupIfNotExists(RealmResource realmResource, String groupName) {
		if (GROUP_CACHE.containsKey(groupName)) {
			return GROUP_CACHE.get(groupName);
		}

		List<GroupRepresentation> groups = realmResource.groups().groups();
		Optional<GroupRepresentation> existingGroup = groups.stream().filter(g -> g.getName().equals(groupName))
				.findFirst();

		String groupId;
		if (existingGroup.isPresent()) {
			groupId = existingGroup.get().getId();
		} else {
			GroupRepresentation g = new GroupRepresentation();
			g.setName(groupName);
			realmResource.groups().add(g);
			System.out.println("Created group: " + groupName);

			groupId = realmResource.groups().groups().stream().filter(gr -> gr.getName().equals(groupName)).findFirst()
					.get().getId();
		}

		GROUP_CACHE.put(groupName, groupId);
		return groupId;
	}*/
	
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

	                System.out.println("Created group: " + groupName);

	                // Fetch newly created group
	                groupId = realmResource.groups().groups().stream()
	                        .filter(gr -> gr.getName().equals(groupName))
	                        .findFirst().get().getId();
	            }

	            GROUP_CACHE.put(groupName, groupId);
	        }

	        groupIds.add(groupId);
	    }

	    return groupIds;
	}


	/*private void joinGroupSafely(UsersResource usersResource, String userId, String groupId, String Id,
			String groupName) {
		List<GroupRepresentation> groups = usersResource.get(userId).groups();
		boolean alreadyMember = groups.stream().anyMatch(g -> g.getId().equals(groupId));

		if (!alreadyMember) {
			usersResource.get(userId).joinGroup(groupId);
			System.out.println("Added user " + userId + " to group " + groupId);

			List<UserOrganization> userOrgs = userOrganizationRepo.findByUserId(Long.parseLong(Id));
			if (userOrgs != null) {
				for (UserOrganization users : userOrgs) {
					if (groupId.contains(users.getOrgId().toString()))
					{
						users.setUserGroup(groupName);
						userOrganizationRepo.save(users);
					}

				}
			}
		} else {
			System.out.println("User " + userId + " already in group " + groupId);
		}
	}*/
	private void joinGroupsSafely(UsersResource usersResource, String userId, List<String> groupIds, String orgUserId,
			List<String> groupNames, Long newOrgId) {

// Fetch the user's current Keycloak groups only once (optimization)
		List<GroupRepresentation> userGroups = usersResource.get(userId).groups();
		String newUserGroupName = "";
		for (int i = 0; i < groupIds.size(); i++) {

			String groupId = groupIds.get(i);
			String groupName = groupNames.get(i);

			boolean alreadyMember = userGroups.stream().anyMatch(g -> g.getId().equals(groupId));

			if (!alreadyMember) {
				usersResource.get(userId).joinGroup(groupId);
				System.out.println("Added user " + userId + " to group " + groupId);
			} else {
				System.out.println("User " + userId + " already in group " + groupId);
			}

// Update UserOrganization table
			//updateUserOrganization(orgUserId, groupId, groupName);
			
		}
		String newGroup = "org-"+newOrgId;
		userOrganizationRepo.updateUserGroup(Long.parseLong(orgUserId),newOrgId,newGroup);
	}


	@Override
	public JSONObject loginWithGoogle(JSONObject json,String type) throws Exception {
    	String token = json.get("token").toString();
    	GoogleIdToken.Payload payload = googleTokenValidator.validateToken(token);

        String userId = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        System.out.println("Email---------------"+email);
        System.out.println("Name---------------"+name);	
        // Generate your own JWT
        String jwtToken = jwtUtil.generateToken(email);
        JSONObject resp = new JSONObject();
        
        //String userEmailId = SecurityUtil.currentUserEmailId();
		Optional<User> userOpt = userRepo.findByEmail(email);
		User user = userOpt.get();
		boolean isAdmin = false;
		String roleName = user.getRole() != null ? user.getRole().getName() : "";
		if(roleName.equalsIgnoreCase(UserConstants.ORG_ADMIN) || roleName.equalsIgnoreCase(UserConstants.TECHNICIAN))
		isAdmin = true;
	    boolean isDoctorLike = roleName.equals(UserConstants.DOCTOR)
	            || roleName.equals(UserConstants.CONSULTANT_DOCTOR);

	    System.out.println("isAdmin--------------"+isAdmin);
	    System.out.println("isDoctorLike--------------"+isDoctorLike);
	    if ((!isAdmin && type.equalsIgnoreCase(UserConstants.ORG_ADMIN)) || (isAdmin && type.equalsIgnoreCase("doctor"))) {
	        userUtils.globalException(UserConstants.NO_PRIVILAGES_TO_LOGIN_THROUGH_THIS_PORTAL,
	                Integer.parseInt(UserConstants.STATUS_RESTRICTED));
	    }


		System.out.println(userOpt.isEmpty());
		if (userOpt.isEmpty()) {
			userUtils.globalException(UserConstants.INVALID_EMAIL_ID, Integer.parseInt(UserConstants.UNAUTHORIZED));
		}

		user.setJwtToken(jwtToken); 
		userRepo.save(user);
		System.out.println("email---------------"+user.getEmail());		
		List<Map<String, Object>> orgJson = user.getOrgs().stream().map(userUtils::buildOrgDetails).toList();
		
		
		List<Map<String, Object>> listMap = Optional.ofNullable(user.getRole().getPrivileges())
				.orElse(Collections.emptyList()).stream().map(pvr -> {
					Map<String, Object> map = new HashMap<>();
					map.put("name", pvr.getName());
					map.put("id", pvr.getId());
					map.put("active", pvr.getActive());
					return map;
				}).collect(Collectors.toList());
		
		
		boolean onetimePwdUsed = false;

       // JSONObject resp = new JSONObject();
		resp = userUtils.updateResponse(resp, UserConstants.STATUS_SUCCESS, UserConstants.LOGIN_SUCCESS,
				UserConstants.LOGIN_SUCCESS, null);

		resp.put(UserConstants.JWT_TOKEN, jwtToken);
		resp.put(UserConstants.NAME, user.getFirstName() + " " + user.getLastName());
		resp.put(UserConstants.ORG_LIST, orgJson);
		resp.put(UserConstants.EMAIL, user.getEmail());
		resp.put(UserConstants.USER_ID, user.getId());
		resp.put(UserConstants.ROLE_ID, user.getRole().getId());
		resp.put(UserConstants.ROLE_NAME, roleName);
		resp.put(UserConstants.PRIVILEGE_LIST_USER, listMap);
		resp.put(UserConstants.IS_LOGINE_THROUGH_ONE_TIME_PASS, onetimePwdUsed ? UserConstants.YES : UserConstants.NO);
		resp.put(UserConstants.IS_MFA_ENABLED, user.getMfaEnabled());
//        resp.put("token", jwtToken) ;
//        resp.put("email", email) ;
//        resp.put("name", name) ;
		return resp;
	}
	
	@Override
	public User ensureUserExists(String email, String firstName, String lastName,String jwtToken,String orgId) {
        Optional<User> existingUser = userRepo.findByEmail(email);
        if(!existingUser.isPresent()) {
        	LOG.info("Use does not exist returning ");
        	return null;
        }
        return existingUser.get();

//        if (existingUser.isPresent()) {
//        	Organization orgResult = orgRepo.findById(Long.parseLong(orgId))
//					.orElseThrow(() -> new ApiException(UserConstants.NO_ORG,
//							Integer.parseInt(UserConstants.NO_CONTENT_CODE), UserConstants.NO_ORG));
//            // ✅ User already exists
//			User existing = existingUser.get();
//
//			// existing consultant can still receive invitation
//			if (!existing.getOrgs().contains(orgResult)) {
//				String body = userUtils.mailBodyInvitationToNewOrg(existing, orgResult);
//				emailService.sendEmail(existing.getEmail(), UserConstants.REGISTRACTION, body);
//			}
//            return existingUser.get();
//        } else {
//            // ⚙️ Create new user with default settings
//        	JSONObject userDetails=new JSONObject();
//    		userDetails.put(UserConstants.FIRST_NAME,firstName);
//    		userDetails.put(UserConstants.LAST_NAME, lastName);
//    		userDetails.put(UserConstants.EMAIL, email);
//    		userDetails.put(UserConstants.PHONE_NO, "1111111111");
//    		userDetails.put(UserConstants.ADDRESS_ONE, "Unknown");
//    		userDetails.put(UserConstants.ADDRESS_TWO, "Unknown");
//    		userDetails.put(UserConstants.STATE, "Unknown");
//    		userDetails.put(UserConstants.CITY, "Unknown");
//    		userDetails.put(UserConstants.IS_ACTIVE, true);
//    		userDetails.put(UserConstants.ORG_ID, orgId);
//    		userDetails.put(UserConstants.ROLE_ID, 5);
//    		userDetails.put(UserConstants.PIN_CODE, "111111");
//    		userDetails.put(UserConstants.IS_CONSULTANT, true);
//    		
//
//            // internally call your create user logic
//            registerNewUserAccount(userDetails, null,true,jwtToken);
//
//            return userRepo.findByEmail(email).get();
//        }
    }
}
