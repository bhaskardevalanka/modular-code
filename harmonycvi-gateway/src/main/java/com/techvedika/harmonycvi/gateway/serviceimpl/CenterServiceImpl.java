package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.entity.Center;
import com.techvedika.harmonycvi.gateway.entity.Organization;
import com.techvedika.harmonycvi.gateway.entity.User;
import com.techvedika.harmonycvi.gateway.repository.CenterRepository;
import com.techvedika.harmonycvi.gateway.repository.OrganizationRepository;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;
import com.techvedika.harmonycvi.gateway.security.SecurityUtil;
import com.techvedika.harmonycvi.gateway.service.CenterService;
import com.techvedika.harmonycvi.gateway.util.UserUtils;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CenterServiceImpl implements CenterService {

	@Autowired
	private CenterRepository centerRepo;

	@Autowired
	private OrganizationRepository orgRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private UserUtils userUtils;

	@Override
	public JSONObject create(JSONObject json) {
		
		String fieldValidation =userUtils.validateCenterMandatoryField(json);
		if (fieldValidation!=null && !fieldValidation.equals("")) {
			
			userUtils.globalException(fieldValidation, 
					Integer.parseInt(UserConstants.BAD_REQUEST_CODE));
		}

		String userEmailId = SecurityUtil.currentUserEmailId();

		boolean allowed = userRepo.hasPrivilege(userEmailId, UserConstants.PRIVILEGE_ADD_CENTER);

		if (!allowed) {
		    userUtils.globalException(
		        UserConstants.NO_PRIVILAGES_TO_ACCESS,
		        Integer.parseInt(UserConstants.STATUS_RESTRICTED)
		    );
		}
		
		Long userId = userRepo.findIdByEmail(userEmailId).orElse(0L);

		Long orgId = Long.parseLong(json.get(UserConstants.ORG_ID).toString());
		Optional<Organization> orgOtp = orgRepo.findById(orgId);
		if (orgOtp.isEmpty()) {
			userUtils.globalException(UserConstants.NO_ORG, Integer.parseInt(UserConstants.NO_CONTENT_CODE));
		}

		Organization org = orgOtp.get();

		String centerName = json.get(UserConstants.NAME).toString();
		if (centerRepo.existsByNameIgnoreCase(centerName)) {
		    userUtils.globalException(UserConstants.DUPLICATE_CENTER,
		            Integer.parseInt(UserConstants.STATUS_DUPLICATE_CODE));
		}

		Center center = new Center();
		center.setName(json.get(UserConstants.NAME).toString());
		center.setOrganization(org);
		center.setActive(Boolean.parseBoolean(json.get(UserConstants.ACTIVE).toString()));
		center.setCreatedBy(userId);
		center.setCreatedDt(Date.from(Instant.now()));

		// Optional fields		
		center.setAddress(getStringValue(json, UserConstants.ADDRESS));
		center.setLatitude(getStringValue(json, UserConstants.LATITUDE));
		center.setLongitude(getStringValue(json, UserConstants.LONGITUDE));
		center.setPhone(getStringValue(json, UserConstants.PHONE));
		center.setOffsetValue(getStringValue(json, UserConstants.OFFSET_VALUE));
		center.setTimeZone(getStringValue(json, UserConstants.TIME_ZONE));
		center.setAddress1(getStringValue(json, UserConstants.ADDRESS_ONE));
		center.setAddress2(getStringValue(json, UserConstants.ADDRESS_TWO));
		center.setCity(getStringValue(json, UserConstants.CITY));
		center.setState(getStringValue(json, UserConstants.STATE));
		center.setCountry(getStringValue(json, UserConstants.COUNTRY));
		center.setArea(getStringValue(json, UserConstants.AREA));
		center.setPinCode(getStringValue(json, UserConstants.PIN_CODE));


		centerRepo.save(center);

		return userUtils.updateResponse(new JSONObject(), UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
				UserConstants.SAVED, null);
	}
	
	private String getStringValue(JSONObject json, String key) {
	    Object value = json.get(key);
	    return (value != null) ? value.toString() : "";
	}

	@Override
	public JSONObject update(JSONObject json) {
		String userEmailId = SecurityUtil.currentUserEmailId();
		boolean allowed = userRepo.hasPrivilege(userEmailId, UserConstants.PRIVILEGE_ADD_CENTER);

		if (!allowed) {
		    userUtils.globalException(
		        UserConstants.NO_PRIVILAGES_TO_ACCESS,
		        Integer.parseInt(UserConstants.STATUS_RESTRICTED)
		    );
		}

		Long userId = userRepo.findIdByEmail(userEmailId).orElse(0L);
		Long centerId = Long.parseLong(json.get(UserConstants.ID).toString());

		Optional<Center> centerOpt = centerRepo.findById(centerId);

		if (centerOpt.isEmpty()) {
			userUtils.globalException(UserConstants.NO_CENTER, Integer.parseInt(UserConstants.NO_CONTENT_CODE));
		}

		Center center = centerOpt.get();

		// Name duplication check		
		if (json.containsKey(UserConstants.NAME) && json.get(UserConstants.NAME)!=null) {
		    String newName = json.get(UserConstants.NAME).toString().trim();		    
		    if (centerRepo.existsByNameIgnoreCaseAndIdNot(newName, centerId)) {
		        userUtils.globalException(UserConstants.DUPLICATE_CENTER, 
		                Integer.parseInt(UserConstants.STATUS_DUPLICATE_CODE));
		    }else {
		        center.setName(newName);
		    }
		}

		// Switch organization if provided
		if (json.containsKey(UserConstants.ORG_ID) && json.get(UserConstants.ORG_ID)!=null) {
			Long newOrgId = Long.parseLong(json.get(UserConstants.ORG_ID).toString());
			Optional<Organization> newOrgOtp = orgRepo.findById(newOrgId);
			if (newOrgOtp.isEmpty()) {
				userUtils.globalException(UserConstants.NO_ORG, Integer.parseInt(UserConstants.NO_CONTENT_CODE));
			}

			center.setOrganization(newOrgOtp.get());
		}

		// Simple patch		
		center.setActive(parseBooleanOrDefault(json.get(UserConstants.ACTIVE), center.getActive()));
		center.setAddress(getSafeString(json, UserConstants.ADDRESS, center.getAddress()));
		center.setLatitude(getSafeString(json, UserConstants.LATITUDE, center.getLatitude()));
		center.setLongitude(getSafeString(json, UserConstants.LONGITUDE, center.getLongitude()));
		center.setPhone(getSafeString(json, UserConstants.PHONE, center.getPhone()));
		center.setOffsetValue(getSafeString(json, UserConstants.OFFSET_VALUE, center.getOffsetValue()));
		center.setTimeZone(getSafeString(json, UserConstants.TIME_ZONE, center.getTimeZone()));
		center.setAddress1(getSafeString(json, UserConstants.ADDRESS_ONE, center.getAddress1()));
		center.setAddress2(getSafeString(json, UserConstants.ADDRESS_TWO, center.getAddress2()));
		center.setCity(getSafeString(json, UserConstants.CITY, center.getCity()));
		center.setState(getSafeString(json, UserConstants.STATE, center.getState()));
		center.setCountry(getSafeString(json, UserConstants.COUNTRY, center.getCountry()));
		center.setArea(getSafeString(json, UserConstants.AREA, center.getArea()));
		center.setPinCode(getSafeString(json, UserConstants.PIN_CODE, center.getPinCode()));


		center.setLastUpdatedBy(userId);
		center.setLastUpdatedDt(Date.from(Instant.now()));
		centerRepo.save(center);

		if (json.containsKey(UserConstants.IS_STATUS_UPDATE) && json.get(UserConstants.IS_STATUS_UPDATE)!=null &&"yes".equalsIgnoreCase(String.valueOf(json.get(UserConstants.IS_STATUS_UPDATE)))) {
			String status = center.getActive() ? UserConstants.ENABLED : UserConstants.DISABLED;
			return userUtils.updateResponse(new JSONObject(), UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
					UserConstants.CENTER + status, null);
		}
		return userUtils.updateResponse(new JSONObject(), UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
				UserConstants.CENTER + UserConstants.UPDATED, null);
	}
	
	private String getSafeString(JSONObject json, String key, String defaultValue) {
	    Object value = json.get(key);
	    return (value != null) ? value.toString() : defaultValue;
	}

	private boolean parseBooleanOrDefault(Object value, boolean defaultValue) {
	    if (value == null) return defaultValue;
	    try {
	        return Boolean.parseBoolean(value.toString());
	    } catch (Exception e) {
	        return defaultValue;
	    }
	}
	
	@Override
	@Transactional
	public JSONObject getList(JSONObject json) {

		String userEmailId = SecurityUtil.currentUserEmailId();
		
		boolean allowed = userRepo.hasPrivilege(userEmailId, UserConstants.PRIVILEGE_LIST_CENTER);

		if (!allowed) {
		    userUtils.globalException(
		        UserConstants.NO_PRIVILAGES_TO_ACCESS,
		        Integer.parseInt(UserConstants.STATUS_RESTRICTED)
		    );
		}

		Long orgId = Long.parseLong(json.get(UserConstants.ORG_ID).toString());

		boolean orgExists = orgRepo.existsById(orgId);
		if (!orgExists) {
			userUtils.globalException(UserConstants.NO_ORG, Integer.parseInt(UserConstants.NO_CONTENT_CODE));
		}


		List<Center> centerList = centerRepo.findByOrganizationIdOrderByCreatedDtAsc(orgId);
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		if (centerList == null || centerList.size() == 0) {
			userUtils.globalException(UserConstants.EMPTY_RESULT, Integer.parseInt(UserConstants.NO_CONTENT_CODE));
			return null;
		}
		for (Center center : centerList) {
			Map<String, Object> map = buildCenterDetails(center);
			mapList.add(map);
		}
		return userUtils.updateResponse(new JSONObject(), UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
				UserConstants.FETCHED, mapList);
	}
	
	@Override
	@Transactional
	public JSONObject getById(String id) {
		
		String userEmailId = SecurityUtil.currentUserEmailId();
		
		boolean allowed = userRepo.hasPrivilege(userEmailId, UserConstants.PRIVILEGE_LIST_CENTER);

		if (!allowed) {
		    userUtils.globalException(
		        UserConstants.NO_PRIVILAGES_TO_ACCESS,
		        Integer.parseInt(UserConstants.STATUS_RESTRICTED)
		    );
		}

		Optional<Center> centerOpt = centerRepo.findById(Long.parseLong(id));
		
		if(centerOpt.isEmpty()) {
			userUtils.globalException(UserConstants.NO_CENTER, Integer.parseInt(UserConstants.NO_CONTENT_CODE));			
		}
		
		Center center = centerOpt.get();

		Map<String, Object> map = buildCenterDetails(center);

		return userUtils.updateResponse(new JSONObject(), UserConstants.STATUS_SUCCESS,
				UserConstants.SUCCESS, UserConstants.FETCHED, map);
	}
	
	public Map<String, Object> buildCenterDetails(Center center) {
	    Map<String, Object> map = new HashMap<>();

	    map.put(UserConstants.ID, center.getId());
	    
	    if (center.getOrganization() != null) {
	        map.put(UserConstants.ORG_NAME, safeValue(center.getOrganization().getName()));
	        map.put(UserConstants.ORG_ID, safeValue(center.getOrganization().getId()));
	    } else {
	        map.put(UserConstants.ORG_NAME, "");
	        map.put(UserConstants.ORG_ID, null);
	    }

	    map.put(UserConstants.ACTIVE, safeValue(center.getActive()));
	    map.put(UserConstants.LATITUDE, safeValue(center.getLatitude()));
	    map.put(UserConstants.LONGITUDE, safeValue(center.getLongitude()));
	    map.put(UserConstants.ADDRESS, safeValue(center.getAddress()));
	    map.put(UserConstants.TIME_ZONE, safeValue(center.getTimeZone()));
	    map.put(UserConstants.PHONE, safeValue(center.getPhone()));
	    map.put(UserConstants.OFFSET_VALUE, safeValue(center.getOffsetValue()));
	    map.put(UserConstants.NAME, safeValue(center.getName()));
	    map.put(UserConstants.ADDRESS_ONE, safeValue(center.getAddress1()));
	    map.put(UserConstants.ADDRESS_TWO, safeValue(center.getAddress2()));
	    map.put(UserConstants.CITY, safeValue(center.getCity()));
	    map.put(UserConstants.STATE, safeValue(center.getState()));
	    map.put(UserConstants.COUNTRY, safeValue(center.getCountry()));
	    map.put(UserConstants.AREA, safeValue(center.getArea()));
	    map.put(UserConstants.PIN_CODE, safeValue(center.getPinCode()));

	    return map;
	}

	
	private Object safeValue(Object value) {
	    return (value != null) ? value : "";
	}
}
