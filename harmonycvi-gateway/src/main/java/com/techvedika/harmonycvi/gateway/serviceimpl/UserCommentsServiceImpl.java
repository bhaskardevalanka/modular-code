package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.constant.CommonConstants;
import com.techvedika.harmonycvi.gateway.constant.StatusConstants;
import com.techvedika.harmonycvi.gateway.entity.UserComments;
import com.techvedika.harmonycvi.gateway.repository.UserCommentsRepository;
import com.techvedika.harmonycvi.gateway.service.CommonMethod;
import com.techvedika.harmonycvi.gateway.service.UserCommentsService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserCommentsServiceImpl implements UserCommentsService {

	private static final Logger LOG = LoggerFactory.getLogger(UserCommentsServiceImpl.class);

	@Autowired
	private CommonMethod commonMethod;
	
	@Autowired
	private UserCommentsRepository userCommentsRepo;

	@Override
	@Transactional
	public JSONObject save(JSONObject jsonRequest) {
		JSONObject response = new JSONObject();
		try {
			if (jsonRequest != null && jsonRequest.get("comments") != null && jsonRequest.get("userName") != null
					&& jsonRequest.get("userId") != null && jsonRequest.get("patientId") != null
					&& !jsonRequest.get("patientId").toString().isEmpty() && jsonRequest.get("userEmail") != null
					&& jsonRequest.get(CommonConstants.ACCESS_KEY) != null
					&& !jsonRequest.get(CommonConstants.ACCESS_KEY).toString().isEmpty()) {

				if (!CommonConstants.SECURITY_TOKEN.equals(jsonRequest.get(CommonConstants.ACCESS_KEY).toString())) {
					return commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
				}

				UserComments ucom = new UserComments();
				ucom.setActive(true);
				ucom.setComments(jsonRequest.get("comments").toString());
				ucom.setPatientId(jsonRequest.get("patientId").toString());
				ucom.setUserEmail(jsonRequest.get("userEmail").toString());
				ucom.setCreatedTime(new Date());
				ucom.setUserName(jsonRequest.get("userName").toString());
				ucom.setCreatedBy(Long.parseLong(jsonRequest.get("userId").toString()));

				//em.persist(ucom);
				userCommentsRepo.save(ucom);

				return commonMethod.createResponse(StatusConstants.SUCCESS_CODE, "Saved Successfully");
			} else {
				return commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
			}
		} catch (Exception e) {
			LOG.error("Error saving user comment", e);
			return commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
		}
	}

	@Override
	@Transactional
	public JSONObject getByPatientId(JSONObject jsonRequest) {
		JSONObject response = new JSONObject();
		try {
			if (jsonRequest != null && jsonRequest.get("patientId") != null
					&& !jsonRequest.get("patientId").toString().isEmpty()
					&& jsonRequest.get(CommonConstants.ACCESS_KEY) != null
					&& !jsonRequest.get(CommonConstants.ACCESS_KEY).toString().isEmpty()) {

				if (!CommonConstants.SECURITY_TOKEN.equals(jsonRequest.get(CommonConstants.ACCESS_KEY).toString())) {
					return commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
				}

				List<UserComments> userList = userCommentsRepo.findByPatientId(jsonRequest.get("patientId").toString());
				//em.createNamedQuery(UserComments.FIND_BY_PATIENT_ID, UserComments.class).setParameter(CommonConstants.ID, jsonRequest.get("patientId").toString()).getResultList();

				List<Map<String, Object>> comments = new ArrayList<>();
				if (userList != null && !userList.isEmpty()) {
					for (UserComments ucom : userList) {
						Map<String, Object> map = new HashMap<>();
						map.put("userName", ucom.getUserName());
						map.put("userEmail", ucom.getUserEmail());
						map.put("patientId", ucom.getPatientId());
						map.put("comments", ucom.getComments());
						map.put("userId", ucom.getCreatedBy());
						comments.add(map);
					}
				}

				response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, "Retrieved successfully");
				response.put("comments", comments);
				return response;

			} else {
				return commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
			}
		} catch (Exception e) {
			LOG.error("Error retrieving comments by patientId", e);
			return commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
		}
	}
}
