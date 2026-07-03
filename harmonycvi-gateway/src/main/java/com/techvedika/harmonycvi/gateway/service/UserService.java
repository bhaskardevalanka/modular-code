package com.techvedika.harmonycvi.gateway.service;

import java.util.List;

import org.json.simple.JSONObject;

import com.techvedika.harmonycvi.gateway.entity.User;

import jakarta.servlet.http.HttpServletRequest;

public interface UserService {
	
//	public JSONObject login(JSONObject jsonRequest);
	public JSONObject registerNewUserAccount(JSONObject jsonRequest, HttpServletRequest request,boolean triggerEmail,String jwtToken);
	public JSONObject updateUser(JSONObject json, HttpServletRequest request);
	public JSONObject resetPassword(JSONObject json, HttpServletRequest request);
	public JSONObject getList(JSONObject json);
	public JSONObject forgotPassword(JSONObject json);
	public JSONObject logout();
	public JSONObject getUser(String userId);
	public JSONObject assignPatient(JSONObject json);
	
	public JSONObject acceptInvitation(String userId, String orgId);
	public JSONObject getDoctorList(String orgId);
	public JSONObject deleteUserStudy(String studyId,String doctorId);
	public JSONObject getConsultantDoctors();
	public JSONObject getConsultant(String emailId);
	public JSONObject getAll();
	public JSONObject licenseCheck(String orgId);
	
	Long getAllActiveUserByOrgTechnicians(Long orgId);
	Long getAllActiveUserByOrgDoctors(Long orgId);
	Long getAllActiveUserByOrgConsultants(Long orgId);
	Long getAllActiveUserByOrgAdmins(Long orgId);
	Long getAllActiveUserByOrg(Long orgId);
	
	User ensureUserExists(String email, String firstName, String lastName, String jwtToken,String orgId);
	public JSONObject loginWithGoogle(JSONObject json, String type) throws Exception;
	public JSONObject login(JSONObject jsonObject, String mfaToken,String userAgent);
}
