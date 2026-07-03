package com.techvedika.harmonycvi.gateway.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.rmi.server.UID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.entity.Organization;
import com.techvedika.harmonycvi.gateway.entity.Privileges;
import com.techvedika.harmonycvi.gateway.entity.User;
import com.techvedika.harmonycvi.gateway.entity.UserStudies;
import com.techvedika.harmonycvi.gateway.exception.GlobalApiException;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;
import com.techvedika.harmonycvi.gateway.security.SecurityUtil;

@Component
public class UserUtils {
	
	@Value("${server.gateway-url}")
	String gatewayUrl;

	@Autowired
	private UserRepository userRepo;

	public boolean isSuperAdmin(Long id) {
		Optional<String> userRole = userRepo.findRoleNameById(id);
		if (userRole.isPresent()) {
			String role = userRole.get();
			if (role != null && role.equals(UserConstants.SUPER_ADMIN)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}

	public boolean isOrgAdmin(Long id) {
		Optional<String> userRole = userRepo.findRoleNameById(id);
		if (userRole.isPresent()) {
			String role = userRole.get();
			if (role != null && role.equals(UserConstants.ORG_ADMIN)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean isUserAdmin(Long id) {
		Optional<String> userRole = userRepo.findRoleNameById(id);
		if (userRole.isPresent()) {
			String role = userRole.get();
			if (role != null && role.equals(UserConstants.USER_ADMIN)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public void addLoginHistory(Integer userId,
			String email) {/*
							 * try { Date date = new Date(); LoginHistory loginHistory = new LoginHistory
							 * (); loginHistory .setEmail (email .toLowerCase ()); loginHistory .setUserId
							 * (userId); loginHistory. setCreateDateTime (date); loginHistory .setLoginTime
							 * (date); loginHistory. setModifiedDateTime (date); loginHistory .setLogOutTime
							 * (date); session.save( loginHistory ); } catch (Exception e) {
							 * e.printStackTrace (); VTLogger. logException (StatusConstants .
							 * STATUS_FAILURE , e); }
							 */
	}

	@SuppressWarnings("unchecked")
	public JSONObject updateResponse(JSONObject response, String code, String status, String message, Object data) {
		response = response == null ? new JSONObject() : response;
		response.put(UserConstants.STATUS_CODE, code);
		response.put(UserConstants.STATUS, status);
		response.put(UserConstants.STATUS_MESSAGE, message);
		if (data != null) {
			response.put(UserConstants.DATA, data);
		}
		return response;
	}

	public String exceptionAsString(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	@SuppressWarnings("unchecked")
	public JSONObject errResponseAsTextPlain(String errorMsg) {
		JSONObject response = new JSONObject();
		response.put(UserConstants.STATUS_CODE, UserConstants.STATUS_FAILURE);
		response.put(UserConstants.STATUS, UserConstants.FAILURE);
		response.put(UserConstants.STATUS_MESSAGE, errorMsg);
		return response;
	}

	public Map<String, Object> getMailConfig() {

		return null;
	}

	/*
	 * To get the value from property file by sending the key as param
	 */
//	public String getPropertyValue(String propertyKey) {
//		Properties env = null;
//		String value = null;
//		InputStream is = null;
//		env = new Properties();
//		String fileName = System.getProperty("jboss.server.config.dir") + "/application.properties";
//		try (FileInputStream fis = new FileInputStream(fileName)) {
//			env.load(fis);
//		} catch (java.io.IOException e) {
//			e.printStackTrace();
//		}
//		/*
//		 * is = new FileInputStream(UserConstants.PROPERTIES_FILE_PATH); env.load(is);
//		 */
//		value = env.getProperty(propertyKey);
//		return value;
//	}

	public String mailBodyRegistration(String pswd, User user) {
		String emailBody = "<html><body><p>Dear " + user.getFirstName() + " " + user.getLastName() + ",</p>";
		emailBody += "<p>" + "<br><br>Username : <b>" + user.getEmail() + "</b><br><br>" + " Password : <b>" + pswd
				+ "</b><br></p>";
		emailBody += "<p> Note : This is one time system generated password. We request you to change the password after login.<p>";

		emailBody += "<p>Thanks & Regards,";
		emailBody += "<br>Support Team</p>";

		emailBody += "</body></html>";
		String emailText = emailBody;
		return emailText;
	}

	public String mailBodyInvitationToNewOrg(User user, Organization org) {
		String emailBody = "<html><body><p>Dear " + user.getFirstName() + " " + user.getLastName() + ",</p>";
		emailBody += "<p> Invitation Request from " + org.getName() + ". <a href='" + gatewayUrl + "/users/acceptInvitation/"
				+ user.getId() + "/" + org.getId() + "'>Click here </a> to accept the invitaton.<p>";

		emailBody += "<p>Thanks & Regards,";
		emailBody += "<br>Support Team</p>";

		emailBody += "</body></html>";
		String emailText = emailBody;
		return emailText;
	}

	public String mailBodyForgotPwd(String pswd, User user) {
		String emailBody = "<html><body><p>Dear " + user.getFirstName() + " " + user.getLastName() + ",</p>";
		emailBody += "<p>" + "<br><br>Username : <b>" + user.getEmail() + "</b><br><br>" + " Password : <b>" + pswd
				+ "</b><br></p>";
		emailBody += "<p> Note : This is one time system generated password. We request you to change the password after login<p>";

		emailBody += "<p>Thanks & Regards,";
		emailBody += "<br>Support Team</p>";

		emailBody += "</body></html>";
		String emailText = emailBody;
		return emailText;
	}

	public String generate() {
		UUID uid = UUID.randomUUID();
		return String.valueOf(uid).length() > 0 ? String.valueOf(uid) : null;
	}

	public UID UId() {
		UID uid = new UID();
		System.out.println("User Id: " + uid);
		return uid;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> createUserJson(User user) {
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			response.put(UserConstants.ID, user.getId());
			response.put(UserConstants.EMAIL, user.getEmail());
			response.put(UserConstants.FIRST_NAME, user.getFirstName());
			response.put(UserConstants.LAST_NAME, user.getLastName());
			response.put(UserConstants.IS_ACTIVE, user.getActive());
			response.put(UserConstants.ROLE_ID, user.getRole() != null ? user.getRole().getId() : null);
			response.put(UserConstants.CREATED_BY, user.getCreatedBy());
			response.put(UserConstants.ROLE_NAME, user.getRole().getName());
			response.put(UserConstants.LAST_UPDATED_BY, user.getLastUpdatedBy());

			response.put(UserConstants.CITY, user.getCity());
			response.put(UserConstants.STATE, user.getState());
			response.put(UserConstants.IS_CONSULTANT, user.getConsultant());

			List<Organization> orgs = new ArrayList<Organization>();
			List<Map<String, Object>> orgMap = new ArrayList<Map<String, Object>>();
			orgs = (List<Organization>) user.getOrgs();
			if (orgs != null && orgs.size() > 0) {
				for (Organization org : orgs) {
					Map<String, Object> map = buildOrgDetails(org);
					orgMap.add(map);
				}
			}
			response.put(UserConstants.ORG_LIST, orgMap);
			response.put(UserConstants.PHONE_NO, user.getPhoneNo());
			response.put(UserConstants.ADDRESS_ONE, user.getAddressOne());
			response.put(UserConstants.ADDRESS_TWO, user.getAddressTwo());
			response.put(UserConstants.PIN_CODE, user.getPinCode());
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			response = createResponse(UserConstants.OPERATION_FAILED, UserConstants.SERVER_ERROR);
			return response;
		}
	}

	public UserStudies setUserStudies(UserStudies us, long userId, User doctor, String studyId) {

		try {
			if (us == null) {
				us = new UserStudies();
			}
			us.setCreatedBy(userId);
			us.setCreatedDt(new Date());
			us.setActive(Boolean.TRUE);
			us.setLastUpdatedBy(userId);
			us.setLastUpdatedDt(new Date());
			us.setStudyId(studyId);
			us.setUser(doctor);

		} catch (Exception e) {

		}
		return us;
	}

	public String getSHA(String input) {
		try {
			// Static getInstance method is called with hashing SHA
			MessageDigest md = MessageDigest.getInstance(UserConstants.SHA_256);
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger no = new BigInteger(1, messageDigest);
			String hashtext = no.toString(16);
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Exception thrown" + " for incorrect algorithm: " + e);
			return null;
		}
	}

	public JSONObject createResponse(String statusCode, String responseMessage) {

		JSONObject respone = new JSONObject();
		try {
			respone.put(UserConstants.STATUS_CODE, statusCode);
			respone.put(UserConstants.RESPONSE_MESSAGE, responseMessage);
			return respone;
		} catch (Exception e) {
			e.printStackTrace();
			return respone;
		}
	}

//	@SuppressWarnings("unchecked")
//	public WebApplicationException globalException(String statusMessag, Integer statusCode) {
//
//		JSONParser parser = new JSONParser();
//		JSONObject msg = new JSONObject();
//		try {
//			if (statusMessag != null && statusMessag.startsWith("{") && statusMessag.endsWith("}")) {
//				msg = (JSONObject) parser.parse(statusMessag);
//			} else {
//				msg.put(UserConstants.MESSAGE, statusMessag);
//			}
//			System.out.println("Status msg===========>" + msg);
//			System.out.println("Status statusMessag===========>" + statusMessag);
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		JSONObject responseEntity = new JSONObject();
//		JSONObject apiReqErrors = new JSONObject();
//		JSONObject erroObject = new JSONObject();
//		JSONArray errorsArray = new JSONArray();
//		String errorCategeory = "";
//		String type = "";
//
//		if (statusCode != null && String.valueOf(statusCode).startsWith("1")) {
//
//		} else if (String.valueOf(statusCode).startsWith("2")) {
//
//		} else if (String.valueOf(statusCode).startsWith("3")) {
//
//		} else if (String.valueOf(statusCode).startsWith("4")) {
//
//		} else if (String.valueOf(statusCode).startsWith("5")) {
//
//		}
//		erroObject.put(UserConstants.ERROR_CATEGORY, errorCategeory);
//		erroObject.put(UserConstants.ERROR_MESSAGE, msg.get(UserConstants.MESSAGE));
//		erroObject.put(UserConstants.TRANS_NO, null);
//		errorsArray.add(erroObject);
//
//		responseEntity.put(UserConstants.CODE, statusCode);
//		responseEntity.put(UserConstants.TYPE, type);
//		responseEntity.put(UserConstants.MESSAGE, msg.get(UserConstants.MESSAGE));
//		apiReqErrors.put(UserConstants.ERRORS, errorsArray);
//		responseEntity.put(UserConstants.API_REQUEST_ERRORS, apiReqErrors);
//		throw new WebApplicationException(Response.status(statusCode).entity(responseEntity).build());
//	}

	public void globalException(String statusMessage, int statusCode) {
		/* -------- Build JSON error structure (same as before) -------- */
		JSONObject msg = new JSONObject();
		msg.put(UserConstants.MESSAGE, statusMessage);

		JSONObject errObj = new JSONObject();
		errObj.put(UserConstants.ERROR_CATEGORY, "");
		errObj.put(UserConstants.ERROR_MESSAGE, statusMessage);
		errObj.put(UserConstants.TRANS_NO, null);

		JSONArray errorsArr = new JSONArray();
		errorsArr.add(errObj);

		JSONObject apiReqErrors = new JSONObject();
		apiReqErrors.put(UserConstants.ERRORS, errorsArr);

		JSONObject body = new JSONObject();
		body.put(UserConstants.CODE, statusCode);
		body.put(UserConstants.TYPE, "");
		body.put(UserConstants.MESSAGE, statusMessage);
		body.put(UserConstants.API_REQUEST_ERRORS, apiReqErrors);

		/* -------- Throw ApiException instead of WebApplicationException -------- */
		HttpStatus springStatus = HttpStatus.resolve(statusCode);
		if (springStatus == null)
			springStatus = HttpStatus.INTERNAL_SERVER_ERROR;

		throw new GlobalApiException(springStatus, body);
	}
	

	public String validateDeviceMandatoryField(JSONObject json) {
		String missinFiled = "";
		StringBuilder sb = new StringBuilder();
		if (json != null) {

			if (json.get(UserConstants.IS_STATUS_UPDATE) != null
					&& json.get(UserConstants.IS_STATUS_UPDATE).toString().equalsIgnoreCase("yes")) {
				return missinFiled;
			}

			if (json.get(UserConstants.DEVICE_UID) == null || json.get(UserConstants.DEVICE_UID).equals("")) {
				sb.append(UserConstants.DEVICE_UID + ",");
			}

			if (json.get(UserConstants.CENTER_ID) == null || json.get(UserConstants.CENTER_ID).equals("")) {
				sb.append(UserConstants.CENTER_ID + ",");
			}

			if (json.get(UserConstants.DEVICE_TYPE) == null || json.get(UserConstants.DEVICE_TYPE).equals("")) {
				sb.append(UserConstants.DEVICE_TYPE + ",");
			}

			if (json.get(UserConstants.ACTIVE) == null || json.get(UserConstants.ACTIVE).equals("")) {
				sb.append(UserConstants.ACTIVE + ",");
			}
			if (sb.length() > 0) {
				sb.delete(sb.lastIndexOf(","), sb.length());
				missinFiled = "Missing field: " + sb.toString();
			}

		} else {
			missinFiled = "All fields are missing";
		}
		return missinFiled;

	}

	public String validateCenterMandatoryField(JSONObject json) {
		String missinFiled = "";
		StringBuilder sb = new StringBuilder();

		if (json != null) {

			if (json.get(UserConstants.IS_STATUS_UPDATE) != null
					&& json.get(UserConstants.IS_STATUS_UPDATE).toString().equalsIgnoreCase("yes")) {
				return missinFiled;
			}

			if (json.get(UserConstants.ORG_ID) == null || json.get(UserConstants.ORG_ID).equals("")) {
				sb.append(UserConstants.ORG_ID + ",");
			}

			if (json.get(UserConstants.NAME) == null || json.get(UserConstants.NAME).equals("")) {
				sb.append(UserConstants.NAME + ",");
			}

			if (json.get(UserConstants.ACTIVE) == null || json.get(UserConstants.ACTIVE).equals("")) {
				sb.append(UserConstants.ACTIVE + ",");
			}

			if (json.get(UserConstants.PHONE) == null || json.get(UserConstants.PHONE).equals("")) {
				sb.append(UserConstants.PHONE + ",");
			}

			if (json.get(UserConstants.ADDRESS_ONE) == null || json.get(UserConstants.ADDRESS_ONE).equals("")) {
				sb.append(UserConstants.ADDRESS_ONE + ",");
			}

			if (json.get(UserConstants.AREA) == null || json.get(UserConstants.AREA).equals("")) {
				sb.append(UserConstants.AREA + ",");
			}

			if (json.get(UserConstants.CITY) == null || json.get(UserConstants.CITY).equals("")) {
				sb.append(UserConstants.CITY + ",");
			}

			if (json.get(UserConstants.STATE) == null || json.get(UserConstants.STATE).equals("")) {
				sb.append(UserConstants.STATE + ",");
			}

			if (json.get(UserConstants.COUNTRY) == null || json.get(UserConstants.COUNTRY).equals("")) {
				sb.append(UserConstants.COUNTRY + ",");
			}
			if (json.get(UserConstants.PIN_CODE) == null || json.get(UserConstants.PIN_CODE).equals("")) {
				sb.append(UserConstants.PIN_CODE + ",");
			}

			if (sb.length() > 0) {
				sb.delete(sb.lastIndexOf(","), sb.length());
				missinFiled = "Missing field: " + sb.toString();
			}

		} else {
			missinFiled = "All fields are missing";
		}
		return missinFiled;
	}

	public String validateGetOrgDashboardRequest(JSONObject json) {
		String missingField = "";
		StringBuilder sb = new StringBuilder();
		if (json != null) {

			if (json.get(UserConstants.ORG_ID) == null || json.get(UserConstants.ORG_ID).equals("")) {
				sb.append(UserConstants.ORG_ID + ",");
			}

			if (json.get(UserConstants.START_DATE) == null || json.get(UserConstants.START_DATE).equals("")) {
				sb.append(UserConstants.START_DATE + ",");
			}

			if (json.get(UserConstants.END_DATE) == null || json.get(UserConstants.END_DATE).equals("")) {
				sb.append(UserConstants.END_DATE + ",");
			}

			if (json.get(UserConstants.CLASSIFICATIONRANGE) == null
					|| json.get(UserConstants.CLASSIFICATIONRANGE).equals("")) {
				sb.append(UserConstants.CLASSIFICATIONRANGE + ",");
			}

			if (sb.length() > 0) {
				sb.delete(sb.lastIndexOf(","), sb.length());
				missingField = "Missing field: " + sb.toString();
			}

		} else {
			missingField = "All fields are missing";
		}
		return missingField;

	}

	public String validateUserMandatoryField(JSONObject json) {
		String missinFiled = "";
		StringBuilder sb = new StringBuilder();
		if (json != null) {
			if (json.get(UserConstants.IS_STATUS_UPDATE) != null
					&& json.get(UserConstants.IS_STATUS_UPDATE).toString().equalsIgnoreCase("yes")) {
				return missinFiled;
			}

			if (json.get(UserConstants.EMAIL) == null || json.get(UserConstants.EMAIL).equals("")) {
				sb.append(UserConstants.EMAIL + ",");
			}

			if (json.get(UserConstants.ORG_ID) == null || json.get(UserConstants.ORG_ID).equals("")) {
				sb.append(UserConstants.ORG_ID + ",");
			}

			if (json.get(UserConstants.ROLE_ID) == null || json.get(UserConstants.ROLE_ID).equals("")) {
				sb.append(UserConstants.ROLE_ID + ",");
			}

			if (json.get(UserConstants.FIRST_NAME) == null || json.get(UserConstants.FIRST_NAME).equals("")) {
				sb.append(UserConstants.FIRST_NAME + ",");
			}

			if (json.get(UserConstants.ADDRESS_ONE) == null || json.get(UserConstants.ADDRESS_ONE).equals("")) {
				sb.append(UserConstants.ADDRESS_ONE + ",");
			}

			if (json.get(UserConstants.PHONE_NO) == null || json.get(UserConstants.PHONE_NO).equals("")) {
				sb.append(UserConstants.PHONE_NO + ",");
			}

			if (json.get(UserConstants.CITY) == null || json.get(UserConstants.CITY).equals("")) {
				sb.append(UserConstants.CITY + ",");
			}

			if (json.get(UserConstants.STATE) == null || json.get(UserConstants.STATE).equals("")) {
				sb.append(UserConstants.STATE + ",");
			}

			if (json.get(UserConstants.PIN_CODE) == null || json.get(UserConstants.PIN_CODE).equals("")) {
				sb.append(UserConstants.PIN_CODE + ",");
			}

			if (sb.length() > 0) {
				sb.delete(sb.lastIndexOf(","), sb.length());
				missinFiled = "Missing field: " + sb.toString();
			}

		} else {
			missinFiled = "All fields are missing";
		}
		return missinFiled;
	}

	public String validateOrgMandatoryField(JSONObject json) {
		String missinFiled = "";
		StringBuilder sb = new StringBuilder();
		if (json != null) {

			if (json.get(UserConstants.IS_STATUS_UPDATE) != null
					&& json.get(UserConstants.IS_STATUS_UPDATE).toString().equalsIgnoreCase("yes")) {
				return missinFiled;
			}

			if (json.get(UserConstants.NAME) == null || json.get(UserConstants.NAME).equals("")) {
				sb.append(UserConstants.NAME + ",");
			}

			if (json.get(UserConstants.ACTIVE) == null || json.get(UserConstants.ACTIVE).equals("")) {
				sb.append(UserConstants.ACTIVE + ",");
			}

			if (json.get(UserConstants.EMAIL) == null || json.get(UserConstants.EMAIL).equals("")) {
				sb.append(UserConstants.EMAIL + ",");
			}

			if (json.get(UserConstants.ADDRESS_ONE_ORG) == null || json.get(UserConstants.ADDRESS_ONE_ORG).equals("")) {
				sb.append(UserConstants.ADDRESS_ONE_ORG + ",");
			}

			if (json.get(UserConstants.PHONE_NO) == null || json.get(UserConstants.PHONE_NO).equals("")) {
				sb.append(UserConstants.PHONE_NO + ",");
			}
			if (json.get(UserConstants.CITY) == null || json.get(UserConstants.CITY).equals("")) {
				sb.append(UserConstants.CITY + ",");
			}

			if (json.get(UserConstants.STATE) == null || json.get(UserConstants.STATE).equals("")) {
				sb.append(UserConstants.STATE + ",");
			}

			if (json.get(UserConstants.PIN_CODE) == null || json.get(UserConstants.PIN_CODE).equals("")) {
				sb.append(UserConstants.PIN_CODE + ",");
			}
			if (sb.length() > 0) {
				sb.delete(sb.lastIndexOf(","), sb.length());
				missinFiled = "Missing field: " + sb.toString();
			}

		} else {
			missinFiled = "All fields are missing";
		}
		return missinFiled;
	}

	public String validateMandatoryField(JSONObject json) {
		String missinFiled = "";
		StringBuilder sb = new StringBuilder();
		if (json != null) {

			if (json.get(UserConstants.IS_STATUS_UPDATE) != null
					&& json.get(UserConstants.IS_STATUS_UPDATE).toString().equalsIgnoreCase("yes")) {
				return missinFiled;
			}

			if (json.get(UserConstants.NAME) == null || json.get(UserConstants.NAME).equals("")) {
				sb.append(UserConstants.NAME + ",");
			}

			if (json.get(UserConstants.ACTIVE) == null || json.get(UserConstants.ACTIVE).equals("")) {
				sb.append(UserConstants.ACTIVE + ",");
			}

			if (json.get(UserConstants.EMAIL) == null || json.get(UserConstants.EMAIL).equals("")) {
				sb.append(UserConstants.EMAIL + ",");
			}

			if (json.get(UserConstants.ADDRESS_ONE_ORG) == null || json.get(UserConstants.ADDRESS_ONE_ORG).equals("")) {
				sb.append(UserConstants.ADDRESS_ONE_ORG + ",");
			}

			if (json.get(UserConstants.PHONE_NO) == null || json.get(UserConstants.PHONE_NO).equals("")) {
				sb.append(UserConstants.PHONE_NO + ",");
			}
			if (json.get(UserConstants.CITY) == null || json.get(UserConstants.CITY).equals("")) {
				sb.append(UserConstants.CITY + ",");
			}

			if (json.get(UserConstants.STATE) == null || json.get(UserConstants.STATE).equals("")) {
				sb.append(UserConstants.STATE + ",");
			}

			if (json.get(UserConstants.PIN_CODE) == null || json.get(UserConstants.PIN_CODE).equals("")) {
				sb.append(UserConstants.PIN_CODE + ",");
			}
			if (sb.length() > 0) {
				sb.delete(sb.lastIndexOf(","), sb.length());
				missinFiled = "Missing field: " + sb.toString();
			}

		} else {
			missinFiled = "All fields are missing";
		}
		return missinFiled;
	}

	public Map<String, Object> buildOrgDetails(Organization org) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("name", org.getName());
		map.put("id", org.getId());
		map.put("active", org.getActive());

		map.put("email", org.getEmail());
		map.put("addressOne", org.getAddressOne());
		map.put("addressTwo", org.getAddressTwo());

		map.put("phoneNo", org.getPhoneNo());
		map.put("city", org.getCity());
		map.put("state", org.getState());
		map.put("pinCode", org.getPinCode());

		return map;
	}

	public String validateGetStudyListByStatusRequest(JSONObject json) {
		String missingField = "";
		StringBuilder sb = new StringBuilder();
		if (json != null) {

			if (json.get(UserConstants.ORG_ID) == null || json.get(UserConstants.ORG_ID).equals("")) {
				sb.append(UserConstants.ORG_ID + ",");
			}

			if (json.get(UserConstants.START_DATE) == null || json.get(UserConstants.START_DATE).equals("")) {
				sb.append(UserConstants.START_DATE + ",");
			}

			if (json.get(UserConstants.END_DATE) == null || json.get(UserConstants.END_DATE).equals("")) {
				sb.append(UserConstants.END_DATE + ",");
			}

			if (json.get(UserConstants.STATUS) == null || json.get(UserConstants.STATUS).equals("")) {
				sb.append(UserConstants.STATUS + ",");
			}

			if (sb.length() > 0) {
				sb.delete(sb.lastIndexOf(","), sb.length());
				missingField = "Missing field: " + sb.toString();
			}

		} else {
			missingField = "All fields are missing";
		}
		return missingField;
	}
	
	public String validateGetStudyListByClassificationRequest(JSONObject json) {

		String missingField = "";
		StringBuilder sb = new StringBuilder();
		if (json != null) {

			if (json.get(UserConstants.ORG_ID) == null || json.get(UserConstants.ORG_ID).equals("")) {
				sb.append(UserConstants.ORG_ID + ",");
			}

			if (json.get(UserConstants.CLASSIFICATIONRANGE) == null
					|| json.get(UserConstants.CLASSIFICATIONRANGE).equals("")) {
				sb.append(UserConstants.CLASSIFICATIONRANGE + ",");
			}

			if (json.get(UserConstants.STATUS) == null || json.get(UserConstants.STATUS).equals("")) {
				sb.append(UserConstants.STATUS + ",");
			}

			if (sb.length() > 0) {
				sb.delete(sb.lastIndexOf(","), sb.length());
				missingField = "Missing field: " + sb.toString();
			}

		} else {
			missingField = "All fields are missing";
		}
		return missingField;

	}
	
	public String validateGetUsersByTypeRequest(JSONObject json) {

		String missingField = "";
		StringBuilder sb = new StringBuilder();
		if (json != null) {

			if (json.get(UserConstants.ORG_ID) == null || json.get(UserConstants.ORG_ID).equals("")) {
				sb.append(UserConstants.ORG_ID + ",");
			}

			if (json.get(UserConstants.TYPE) == null || json.get(UserConstants.TYPE).equals("")) {
				sb.append(UserConstants.TYPE + ",");
			}

			if (sb.length() > 0) {
				sb.delete(sb.lastIndexOf(","), sb.length());
				missingField = "Missing field: " + sb.toString();
			}

		} else {
			missingField = "All fields are missing";
		}
		return missingField;

	}
}
