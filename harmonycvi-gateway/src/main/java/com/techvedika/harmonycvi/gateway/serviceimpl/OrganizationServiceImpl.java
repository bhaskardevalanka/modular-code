package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.techvedika.harmonycvi.gateway.constant.CommonConstants;
import com.techvedika.harmonycvi.gateway.constant.StatusConstants;
import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.entity.Organization;
import com.techvedika.harmonycvi.gateway.entity.User;
import com.techvedika.harmonycvi.gateway.exception.RequestValidator;
import com.techvedika.harmonycvi.gateway.exception.ValidationResult;
import com.techvedika.harmonycvi.gateway.projection.OrgPacsValidationUrlProjection;
import com.techvedika.harmonycvi.gateway.repository.CenterRepository;
import com.techvedika.harmonycvi.gateway.repository.DeviceDetailsRepository;
import com.techvedika.harmonycvi.gateway.repository.OrganizationRepository;
import com.techvedika.harmonycvi.gateway.repository.UserOrganizationRepository;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;
import com.techvedika.harmonycvi.gateway.repository.UserStudiesRepository;
import com.techvedika.harmonycvi.gateway.security.SecurityUtil;
import com.techvedika.harmonycvi.gateway.service.CommonMethod;
import com.techvedika.harmonycvi.gateway.service.OrganizationService;
import com.techvedika.harmonycvi.gateway.util.EmailService;
import com.techvedika.harmonycvi.gateway.util.UserUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;


@Service
@Transactional
public class OrganizationServiceImpl implements OrganizationService {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationServiceImpl.class);
    
    @Autowired
    private OrganizationRepository orgRepo;
    
    @Autowired
    private UserRepository userRepo;
    
    @Autowired
    private UserUtils userUtils;
    
    @Autowired
    private UserServiceImpl userServiceImpl;
    
    @Autowired
    UserOrganizationRepository userOrgRepo;
    
    @Autowired
	private CenterRepository centerRepo;
    
    @Autowired
	private DeviceDetailsRepository deviceRepo;
    
    
    @Autowired
	private UserStudiesRepository userStudiesRepo;
    
    @Autowired
	private CommonMethod commonMethod;
    
	private EmailService emailService;
        
    public OrganizationServiceImpl(EmailService emailService) {
    	this.emailService = emailService;
    }
    
    @Value("${organization.orgs.default.dicomDownloadUrl}")
    String defaultPacsUrl;
    
    @Transactional
    @Override
    public JSONObject create(JSONObject json, HttpServletRequest request) {
    	String userEmailId = SecurityUtil.currentUserEmailId();

		Optional<String> roleName = userRepo.findRoleNameByEmail(userEmailId);
		
		if(roleName==null || roleName.isEmpty()) {
			userUtils.globalException(UserConstants.UNAUTHORIZED,
                    Integer.parseInt(UserConstants.UNAUTHORIZED));
            return null;
		}
        // Only Super‑Admin may create tenants
        if (!UserConstants.SUPER_ADMIN.equals(roleName.get())) {
            userUtils.globalException(UserConstants.NO_PRIVILAGES_TO_ACCESS,
                    Integer.parseInt(UserConstants.STATUS_RESTRICTED));
            return null;
        }
        
        Long userId = userRepo.findIdByEmail(userEmailId).orElse(0L);
        String name = String.valueOf(json.get("name")).trim();
        boolean orgExists = orgRepo.existsByName(name);
        if(orgExists) {
        	userUtils.globalException(UserConstants.ORG_ALLREADY_EXIST,
                    Integer.parseInt(UserConstants.STATUS_DUPLICATE_CODE));
        }
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> prefs = objectMapper.convertValue(
                json.get("preferences"),
                new TypeReference<Map<String, Object>>() {}
        );
        Organization org = new Organization();
        org.setName(name);
        org.setActive(Boolean.parseBoolean(String.valueOf(json.getOrDefault("active", "true"))));
        org.setCreatedBy(userId);
        org.setCreatedDt(new Date());
        org.setEmail   (String.valueOf(json.get("email")));
        org.setAddressOne(String.valueOf(json.get("addressOne")));
        org.setAddressTwo(String.valueOf(json.getOrDefault("addressTwo", "")));
        org.setPhoneNo(String.valueOf(json.get("phoneNo")));
        org.setCity   (String.valueOf(json.get("city")));
        org.setState  (String.valueOf(json.get("state")));
        org.setPinCode(String.valueOf(json.get("pinCode")));
        org.setUploadLimit(10);
        org.setPacsUrl(String.valueOf(json.getOrDefault("pacsUrl", "")));
        org.setValidationUrl(String.valueOf(json.getOrDefault("validationUrl", "")));
        org.setHasExternalPacs(Boolean.parseBoolean(String.valueOf(json.getOrDefault("hasExternalPacs", "false"))));
        org.setPreferences(prefs);
      //  org.setPreferences((Map<String, Object>) json.get("preferences"));      
        orgRepo.save(org);
        
      //mapping customer admin user
        Optional<Long> organization=orgRepo.findIdByName(org.getName());
        organization.ifPresent(orgId -> {
        	String[] orgName=json.get("name").toString().split(" ");
    		String firstName=orgName!=null && orgName.length>0?orgName[0]:null;
    		String lastName=orgName!=null && orgName.length>1?orgName[1]:null;
    		JSONObject userDetails=new JSONObject();
    		userDetails.put(UserConstants.ID, null);
    		userDetails.put(UserConstants.FIRST_NAME,firstName);
    		userDetails.put(UserConstants.LAST_NAME, lastName);
    		userDetails.put(UserConstants.EMAIL, json.get(UserConstants.EMAIL).toString());
    		userDetails.put(UserConstants.PHONE_NO, json.get(UserConstants.PHONE_NO).toString());
    		userDetails.put(UserConstants.ADDRESS_ONE, json.get(UserConstants.ADDRESS_ONE_ORG).toString());
    		userDetails.put(UserConstants.ADDRESS_TWO, 
    				json.get(UserConstants.ADDRESS_TWO_ORG)!=null ?json.get(UserConstants.ADDRESS_TWO_ORG).toString():"");
    		userDetails.put(UserConstants.STATE, json.get(UserConstants.STATE).toString());
    		userDetails.put(UserConstants.CITY, json.get(UserConstants.CITY).toString());
    		userDetails.put(UserConstants.IS_ACTIVE, true);
    		userDetails.put(UserConstants.ORG_ID, orgId);
    		userDetails.put(UserConstants.ROLE_ID, 2);
    		userDetails.put(UserConstants.PIN_CODE, json.get(UserConstants.PIN_CODE).toString());
    		userDetails.put(UserConstants.IS_CONSULTANT, false);
    		userServiceImpl.registerNewUserAccount(userDetails, request,true,null);
        });

		
		

        /* any extra steps (e.g. auto‑create tenant admin user) left untouched */

        return userUtils.updateResponse(new JSONObject(),
                UserConstants.STATUS_SUCCESS,
                UserConstants.SUCCESS,
                UserConstants.SAVED,
                null);
    }
    
    @Override
    @Transactional
    public JSONObject update(JSONObject json) {
    	String userEmailId = SecurityUtil.currentUserEmailId();
    	Optional<String> roleName = userRepo.findRoleNameByEmail(userEmailId);
		
		if(roleName==null || roleName.isEmpty()) {
			userUtils.globalException(UserConstants.UNAUTHORIZED,
                    Integer.parseInt(UserConstants.UNAUTHORIZED));
            return null;
		}
		String role = roleName.get();
        // Only Super‑Admin may create tenants
        if (!UserConstants.SUPER_ADMIN.equals(role)) {
            userUtils.globalException(UserConstants.NO_PRIVILAGES_TO_ACCESS,
                    Integer.parseInt(UserConstants.STATUS_RESTRICTED));
            return null;
        }

        Long orgId = Long.valueOf(String.valueOf(json.get("id")));
        Organization org = orgRepo.findById(orgId).orElseGet(() -> {
            userUtils.globalException(UserConstants.NO_ORG,
                    Integer.parseInt(UserConstants.NO_CONTENT_CODE));
            return null;
        });

        /* Name duplication check */        
        /*if (json.containsKey("name") && json.get("name")!=null) {
            String newName = json.get("name").toString().trim();

            boolean orgExists = orgRepo.existsByName(newName);
            if (orgExists) {
                userUtils.globalException(UserConstants.ORG_ALLREADY_EXIST,
                        Integer.parseInt(UserConstants.STATUS_DUPLICATE_CODE));
            }

            org.setName(newName);
        }*/


        /* Simple attribute copies – keep same logic as legacy code */
        copyIfPresent(json, "name",      org::setName);
        copyIfPresent(json, "email",      org::setEmail);
        copyIfPresent(json, "addressOne", org::setAddressOne);
        copyIfPresent(json, "addressTwo", v -> org.setAddressTwo(String.valueOf(v)));
        copyIfPresent(json, "phoneNo",    org::setPhoneNo);
        copyIfPresent(json, "city",       org::setCity);
        copyIfPresent(json, "state",      org::setState);
        copyIfPresent(json, "pinCode",    org::setPinCode);
        copyIfPresent(json, "pacsUrl",    org::setPacsUrl);
        copyIfPresent(json, "validationUrl",    org::setValidationUrl);
        if (json.containsKey("active") && json.get("active")!=null) {
        	Boolean active = Boolean.parseBoolean(String.valueOf(json.get("active")));
            org.setActive(active);
        }
        
        if (json.containsKey("hasExternalPacs") && json.get("hasExternalPacs")!=null) {
        	Boolean hasExternalPacs = Boolean.parseBoolean(String.valueOf(json.get("hasExternalPacs")));
            org.setHasExternalPacs(hasExternalPacs);
        }
        
        if(json.containsKey("preferences") && json.get("preferences")!=null)
        {
        	ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> prefs = objectMapper.convertValue(
                    json.get("preferences"),
                    new TypeReference<Map<String, Object>>() {}
            );
            org.setPreferences(prefs);
			/*if (registrationEmail) {
				String email = json.get(UserConstants.EMAIL).toString().toLowerCase();
				Optional<User> userOpt = userRepo.findByEmail(email);

				if (userOpt.isEmpty()) {
					userUtils.globalException(UserConstants.INVALID_USERID, Integer.parseInt(UserConstants.UNAUTHORIZED));
					return null;
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
				String body = userUtils.mailBodyRegistration(randomPasswordToHash, user);
				LOG.info("Mail body: {}", body);
				emailService.sendEmail(user.getEmail(), UserConstants.REGISTRACTION, body);
			}*/

        }
        Long userId = userRepo.findIdByEmail(userEmailId).orElse(0L);
        org.setLastUpdatedBy(userId);
        org.setLastUpdatedDt(new Date());
        orgRepo.save(org);
          if (json.containsKey(UserConstants.IS_STATUS_UPDATE)&& json.get(UserConstants.IS_STATUS_UPDATE) != null
				&& json.get(UserConstants.IS_STATUS_UPDATE).toString().equalsIgnoreCase("yes")) {
			
			LOG.info("update API End");
			if (json.get("isActive").toString().equalsIgnoreCase("false")) {
				LOG.info("Inside org deactivation");
				updateOrgStatus(false, orgId, userId,role);
			} else if (json.get("isActive").toString().equalsIgnoreCase("true")) {
				LOG.info("Inside org activation");
				updateOrgStatus(true, orgId, userId,role);
			}

			String status = Boolean.valueOf(json.get(UserConstants.IS_ACTIVE).toString()) ? UserConstants.ENABLED
					: UserConstants.DISABLED;
			return userUtils.updateResponse(new JSONObject(), UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
					UserConstants.ORGANIZATION + status, null);

		}

        return userUtils.updateResponse(new JSONObject(),
                UserConstants.STATUS_SUCCESS,
                UserConstants.SUCCESS,
                UserConstants.UPDATED,
                null);
    }
    
    @Override
    @Transactional
    public JSONObject delete(JSONObject json) {

    	String userEmailId = SecurityUtil.currentUserEmailId();
    	Optional<String> roleName = userRepo.findRoleNameByEmail(userEmailId);
		
		if(roleName==null || roleName.isEmpty()) {
			userUtils.globalException(UserConstants.UNAUTHORIZED,
                    Integer.parseInt(UserConstants.UNAUTHORIZED));
            return null;
		}
        // Only Super‑Admin may create tenants
        if (!UserConstants.SUPER_ADMIN.equals(roleName.get())) {
            userUtils.globalException(UserConstants.NO_PRIVILAGES_TO_ACCESS,
                    Integer.parseInt(UserConstants.STATUS_RESTRICTED));
            return null;
        }
        
        Long orgId = Long.valueOf(String.valueOf(json.get("id")));
		
		int maxRetries = 3;
		int attempts = 0;
		boolean updatedSuccessfully = false;

		while (attempts < maxRetries && !updatedSuccessfully) {
		    attempts++;
	        Optional<Long> lockVersion = orgRepo.getLockVersionOrganization(orgId);
	        if (lockVersion.isEmpty()) {
		        LOG.warn("Org not found while retrying delete, orgId={}", orgId);
		        break;
		    }
	        Long userId = userRepo.findIdByEmail(userEmailId).orElse(0L);
		    int updated = orgRepo.deactivateOrganization(orgId, userId, new Date(),lockVersion.get());
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
		    LOG.info("End of " + this.getClass().getName() + ".deleteOrganization "
		             + StatusConstants.OPERATION_FAILED);
		    return null;
		}


        return userUtils.updateResponse(new JSONObject(),
                UserConstants.STATUS_SUCCESS,
                UserConstants.SUCCESS,
                UserConstants.ORGANIZATION_DELETE_SUCCESS_MESSAGE,
                null);
    }
    
    @Override
    public JSONObject getList() {
    	String userEmailId = SecurityUtil.currentUserEmailId();
		Optional<String> roleName = userRepo.findRoleNameByEmail(userEmailId);
		
		 Optional<User> user = userRepo.findByEmail(userEmailId);
		 User orgUser = null;
		 if(user!=null )
		 {
		    orgUser = user.get();
		 }
		
		if(roleName==null || roleName.isEmpty()) {
			userUtils.globalException(UserConstants.UNAUTHORIZED,
                    Integer.parseInt(UserConstants.UNAUTHORIZED));
            return null;
		}

        boolean superAdmin = UserConstants.SUPER_ADMIN.equals(roleName.get());

        List<Organization> userOrgs = userRepo.findOrgsByEmail(userEmailId);
        List<Organization> orgs = superAdmin
                ? orgRepo.findAllExcludingTechVedika()
                : userOrgs!=null? new ArrayList<>(userOrgs):new ArrayList<Organization>(); // user‑specific list
        
        if (orgs.isEmpty()) {
            userUtils.globalException(UserConstants.EMPTY_RESULT,
                    Integer.parseInt(UserConstants.NO_CONTENT_CODE));
            return null;
        }
        final User finalUser = orgUser;
		if (!(orgUser.getRole().getName().toString().equalsIgnoreCase("SUPER_ADMIN"))) {
			orgs.removeIf(org -> !checkUserStatus(finalUser.getId(), org.getId())

			);
		}
        List<Map<String, Object>> data =
                orgs.stream()
                    .map(o -> buildOrgDetails(o, o.getConsultant()))
                    .collect(Collectors.toList());

        return userUtils.updateResponse(new JSONObject(),
                UserConstants.STATUS_SUCCESS,
                UserConstants.SUCCESS,
                UserConstants.FETCHED,
                data);
    }

    private Boolean checkUserStatus(Long userId, Long orgId) {
    	return userOrgRepo.getUserStatus(userId,orgId);
		
	}

	@Override
    public JSONObject getOrgById(JSONObject json) {

        Long orgId = Long.valueOf(String.valueOf(json.get(UserConstants.ID)));
        Organization org = orgRepo.findById(orgId).orElseGet(() -> {
            userUtils.globalException(UserConstants.NO_ORG,
                    Integer.parseInt(UserConstants.NO_CONTENT_CODE));
            return null;
        });

        Map<String, Object> view = buildOrgDetails(org, org.getConsultant());

        return userUtils.updateResponse(new JSONObject(),
                UserConstants.STATUS_SUCCESS,
                UserConstants.SUCCESS,
                UserConstants.FETCHED,
                view);
    }
    
    @Override
    public JSONObject getAll() {

        List<Organization> orgs = orgRepo.findAllActive();
        if (orgs.isEmpty()) {
            userUtils.globalException(UserConstants.EMPTY_RESULT,
                    Integer.parseInt(UserConstants.NO_CONTENT_CODE));
            return null;
        }

        List<Map<String, Object>> data =
                orgs.stream()
                    .map(o -> buildOrgDetails(o, o.getConsultant()))
                    .collect(Collectors.toList());

        return userUtils.updateResponse(new JSONObject(),
                UserConstants.STATUS_SUCCESS,
                UserConstants.SUCCESS,
                UserConstants.FETCHED,
                data);
    }

    @Override
    public JSONObject getOrgList() {

        List<Organization> orgs = orgRepo.findAllExcludingTechVedika();
        if (orgs.isEmpty()) {
            userUtils.globalException(UserConstants.EMPTY_RESULT,
                    Integer.parseInt(UserConstants.NO_CONTENT_CODE));
            return null;
        }

        List<Map<String, Object>> data =
                orgs.stream()
                    .map(o -> buildOrgDetails(o, o.getConsultant()))
                    .collect(Collectors.toList());

        return userUtils.updateResponse(new JSONObject(),
                UserConstants.STATUS_SUCCESS,
                UserConstants.SUCCESS,
                UserConstants.FETCHED,
                data);
    }

    private static void copyIfPresent(JSONObject json, String key, java.util.function.Consumer<String> target) {
        if (json.containsKey(key) && json.get(key)!=null)
            target.accept(String.valueOf(json.get(key)));
    }

    private Map<String, Object> buildOrgDetails(Organization org, Boolean isConsultant) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",           org.getId());
        m.put("name",         org.getName());
        m.put("active",       org.getActive());
        m.put("email",        org.getEmail());
        m.put("addressOne",   org.getAddressOne());
        m.put("addressTwo",   org.getAddressTwo());
        m.put("phoneNo",      org.getPhoneNo());
        m.put("city",         org.getCity());
        m.put("state",        org.getState());
        m.put("pinCode",      org.getPinCode());
        m.put("pacsUrl",      org.getPacsUrl());
        m.put("validationUrl", org.getValidationUrl());
        m.put("hasExternalPacs", org.getHasExternalPacs());
        m.put("isConsultant", isConsultant);
        Map<String, Object> prefs = org.getPreferences();
        if(!prefs.containsKey("save_bookmark") || !prefs.containsKey("pdf_report") || !prefs.containsKey("word_report"))
        {
        	prefs.put("save_bookmark",true);
        	prefs.put("pdf_report",true);
        	prefs.put("word_report",false);
        	org.setPreferences(prefs);
        	orgRepo.save(org);
        }
        m.put("preferences", org.getPreferences());   
        m.put("isDeleted",org.getIsDeleted());
        return m;
    }
    
    
	@Transactional
	public void deleteOrUpdateUser(User usr, Long orgId) {

	    List<Organization> userOrgs = new ArrayList<>(usr.getOrgs());
	    LOG.info("users orgs size :" + userOrgs.size());

	    if (userOrgs.size() <= 1) {

	        // 1. Delete child records FIRST
	        userStudiesRepo.deleteByUserId(usr.getId());

	        // 2. Flush to DB so FK is cleared
	        userStudiesRepo.flush();
	        
	        userOrgRepo.deleteByOrgId(orgId);

	        // 3. Now delete parent
	        userRepo.delete(usr);

	    } else {

	        // Remove org mapping first
	        userOrgRepo.deleteOrgByUserIdANDOrgId(usr.getId(), orgId);

	        // Then delete org-scoped studies
	        userStudiesRepo.deleteByUserId(usr.getId());
	        userStudiesRepo.flush();
	    }
	}
	
	public void updateOrgStatus(boolean status, long org, long user,String role) {
		List<Long> users = userServiceImpl.getOrgUser(org, user,role);
		LOG.info("users size :" + users.size());
		for (Long usr : users) {
			long orgCount = userRepo.countOrgsById(usr);
			LOG.info("users orgs size :" + orgCount);
			if (orgCount == 1) {
				updateUser(org,usr,status);
			} else {
				updateUser(org,usr,status);
			}
			updateStatusById(usr,status);
			updateOrgStatusById(org,status);
		}

	}
	
	public void updateUser(Long orgId, Long userId, boolean status) {
		int maxRetries = 3;
		int attempts = 0;
		boolean updatedSuccessfully = false;

		while (attempts < maxRetries && !updatedSuccessfully) {
		    attempts++;
			Optional<Long> lockVersion = userOrgRepo.findLockVersionById(orgId,userId);
			if (lockVersion.isEmpty()) {
		        LOG.warn("User not found while retrying updateTokenById, userId={}", userId);
		        break;
		    }
		    int updated = userOrgRepo.updateUserByLockVersion(orgId, userId,status, lockVersion.get());
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
	        LOG.info("End of {}.updateUser - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
		}else {
            LOG.info("End of {}.updateUser - {}", this.getClass().getName(), StatusConstants.SUCCESS);
		}
		
	}
	
	private void updateStatusById(Long userId, boolean status) {
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
		    int updated = userRepo.updateStatusByIdAndLockVersion(userId, status,lockVersion.get());
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
	
	
	private void updateOrgStatusById(Long orgId, boolean status) {
		int maxRetries = 3;
		int attempts = 0;
		boolean updatedSuccessfully = false;

		while (attempts < maxRetries && !updatedSuccessfully) {
		    attempts++;
			Optional<Long> lockVersion = orgRepo.findLockVersionById(orgId);
		    if (lockVersion.isEmpty()) {
		        LOG.warn("User not found while retrying updateTokenById, userId={}", orgId);
		        break;
		    }
		    int updated = orgRepo.updateStatusByIdAndLockVersion(orgId, status,lockVersion.get());
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
		    LOG.info("End of " + this.getClass().getName() + ".updateOrgnization"
		             + StatusConstants.OPERATION_FAILED);
	        LOG.info("End of {}.updateStatusById - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
		}else {
            LOG.info("End of {}.updateStatusById - {}", this.getClass().getName(), StatusConstants.SUCCESS);
		}
		
	}
	
	
	@Override
	public JSONObject isExternalOrg(String orgId) {
		JSONObject response = new JSONObject();
		
		Optional<OrgPacsValidationUrlProjection> orgOpt = orgRepo.findPacsValidationUrlById(Long.valueOf(orgId));
        if(orgOpt.isEmpty()) {
        	return commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
        }
        Boolean hasExternalPacs = orgOpt.get().getHasExternalPacs();
        if(hasExternalPacs == null)
        	response.put("isExternal", false);
        else
        	response.put("isExternal", hasExternalPacs);
		return response;
	}

	@Override
	public JSONObject getOrgListByEmail(JSONObject json) {
		LOG.info("Inside getOrgListByEmail API");

		if (json == null || json.get("email") == null || json.get("email").toString().isBlank()) {
			userUtils.globalException("Email is required", Integer.parseInt(UserConstants.BAD_REQUEST_CODE));
			return null;
		}

		String email = json.get("email").toString().toLowerCase();

		if (!userRepo.existsByEmail(email)) {
			userUtils.globalException(UserConstants.INVALID_USERID, Integer.parseInt(UserConstants.STATUS_INVALID));
			return null;
		}

		int page = json.containsKey("pageNumber") && json.get("pageNumber") != null
				? Integer.parseInt(json.get("pageNumber").toString()) : 0;
		int size = json.containsKey("pageSize") && json.get("pageSize") != null
				? Integer.parseInt(json.get("pageSize").toString()) : 5;

		PageRequest pageable = PageRequest.of(page, size, Sort.by("createdDt").descending());
		Page<Organization> orgPage = orgRepo.findOrgsByEmail(email, pageable);

		if (orgPage == null || orgPage.isEmpty()) {
			userUtils.globalException(UserConstants.EMPTY_RESULT, Integer.parseInt(UserConstants.STATUS_FAILURE_505));
			return null;
		}

		List<Map<String, Object>> data = orgPage.getContent().stream()
				.map(o -> {
					Map<String, Object> m = new HashMap<>();
					m.put("id", o.getId());
					m.put("name", o.getName());
					return m;
				})
				.collect(Collectors.toList());

		Map<String, Object> pageInfo = new HashMap<>();
		pageInfo.put("page", orgPage.getNumber());
		pageInfo.put("size", orgPage.getSize());
		pageInfo.put("totalPages", orgPage.getTotalPages());
		pageInfo.put("totalElements", orgPage.getTotalElements());

		JSONObject response = userUtils.updateResponse(new JSONObject(),
				UserConstants.STATUS_SUCCESS,
				UserConstants.SUCCESS,
				UserConstants.FETCHED,
				data);
		response.put("pageInfo", pageInfo);

		LOG.info("getOrgListByEmail API End");
		return response;
	}

	@Override
	public ResponseEntity<JSONObject> getPacsUrl(JSONObject request) {
		ValidationResult validationResults = RequestValidator.validateRequestWithDetails(
                request,CommonConstants.ACCESS_KEY,CommonConstants.ORG_ID);

        if (!validationResults.isValid()) {
            return ResponseEntity.badRequest()
                    .body(commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST));
        }

        // Security token validation
        if (!CommonConstants.SECURITY_TOKEN.equals(request.get(CommonConstants.ACCESS_KEY).toString())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN));
        }
        
        JSONObject response = new JSONObject();
        String orgId = request.get(CommonConstants.ORG_ID).toString();
        Optional<OrgPacsValidationUrlProjection> orgOpt = orgRepo.findPacsValidationUrlById(Long.valueOf(orgId));
        if(orgOpt.isEmpty()) {
        	return ResponseEntity.badRequest()
                    .body(commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST));
        }
    	String pacsUrl = orgOpt.get().getPacsUrl();
    	
    	if(pacsUrl == null || pacsUrl.isEmpty() || pacsUrl.isBlank()) {
        	response.put("baseUrl", defaultPacsUrl);
    	}else {
        	response.put("baseUrl", pacsUrl);
    	}
    	
    	response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
