package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.dicomweb.DicomWebClient;
import com.techvedika.harmonycvi.gateway.entity.User;
import com.techvedika.harmonycvi.gateway.repository.DeviceDetailsRepository;
import com.techvedika.harmonycvi.gateway.repository.OrganizationRepository;
import com.techvedika.harmonycvi.gateway.repository.StudyExtensionRepository;
import com.techvedika.harmonycvi.gateway.repository.UserOrganizationRepository;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;
import com.techvedika.harmonycvi.gateway.security.SecurityUtil;
import com.techvedika.harmonycvi.gateway.service.DashBoardService;
import com.techvedika.harmonycvi.gateway.service.DeviceService;
import com.techvedika.harmonycvi.gateway.service.UserService;
import com.techvedika.harmonycvi.gateway.util.UserUtils;

import jakarta.transaction.Transactional;

@Service
public class DashBoardServiceImpl implements DashBoardService {

	private static final Logger LOG = LoggerFactory.getLogger(DashBoardServiceImpl.class);

	@Autowired
	private DeviceDetailsRepository DeviceRepo;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private DicomWebClient dicomWebClient;

	@Autowired
	private StudyExtensionRepository studyExRepo;

	@Autowired
	private OrganizationRepository organizationRepo;

//	@Autowired
//	private UserStudiesRepository userStudiesRepo;

	@Autowired
	StudyServiceImpl studyService;

	@Autowired
	UserOrganizationRepository UserOrganizationRepo;

	@Autowired
	private UserService userService;

//	@Autowired
//	private HttpServletRequest request;

	@Autowired
	private UserUtils userUtils;

	@Override
	@Transactional
	public JSONObject getAllCount(String orgId) {
		LOG.info("Inside getAllCount API");

		String email = SecurityUtil.currentUserEmailId();

		JSONObject response = new JSONObject();
		Optional<String> roleName = userRepo.findRoleNameByEmail(email);

		if (roleName!=null && roleName.isEmpty()) {
			userUtils.globalException(UserConstants.INVALID_USERID, Integer.parseInt(UserConstants.UNAUTHORIZED));
		}

		boolean isAdmin = UserConstants.SUPER_ADMIN.equals(roleName.get());

		Long deviceCount;
		Long userCount;
		Long hospitalCount;
		Long studyCount;
		Long doctorsCount = 0l;
		Long totalUsersCount = 0l;


		if (isAdmin) {
			deviceCount = deviceService.getAllActiveDevice();
			userCount = userRepo.countByActiveTrue();
			hospitalCount = organizationRepo.countByActiveTrue();
			studyCount = studyExRepo.count();
		} else {
			Long orgIdLong = Long.parseLong(orgId);
			deviceCount = deviceService.getAllActiveDeviceByOrg(orgIdLong);

			List<Long> userIdList = UserOrganizationRepo.findUserIdsByOrgId(orgIdLong);
			ArrayList<Long> idList = new ArrayList<Long>();
			for (int i = 0; userIdList != null && i < userIdList.size(); i++) {
				idList.add(Long.valueOf(String.valueOf(userIdList.get(i))));
			}

			//userCount = userRepo.countByIdInAndActiveTrue(idList);
			totalUsersCount=  userRepo.countByIdInAndActiveTrue(idList);
			hospitalCount = userRepo.countOrgsByEmail(email);
			studyCount = studyExRepo.countByOrgId(orgIdLong);
			doctorsCount = userRepo.findAllActiveDoctorsByIdList(idList);
			userCount = totalUsersCount - doctorsCount;
		}

		response.put(UserConstants.TOTAL_DEVICE, deviceCount);
		response.put(UserConstants.TOTAL_USER, userCount);
		response.put(UserConstants.TOTAL_ORG, hospitalCount);
		response.put(UserConstants.TOTAL_STUDY, studyCount);
		response.put(UserConstants.TOTAL_DOCTOR,doctorsCount);

		LOG.info("getAllCount API End");

		return userUtils.updateResponse(response, UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
				UserConstants.FETCHED, null);
	}

	@Override
	@Transactional
	public JSONObject getSuperAdmin() {

		LOG.info("Inside getSuperAdmin Dashboard API");
		JSONObject response = new JSONObject();

		String email = SecurityUtil.currentUserEmailId();

		Optional<String> roleName = userRepo.findRoleNameByEmail(email);

		if (roleName!=null && roleName.isEmpty()) {
			userUtils.globalException(UserConstants.INVALID_USERID, Integer.parseInt(UserConstants.UNAUTHORIZED));
		}

		boolean isAdmin = UserConstants.SUPER_ADMIN.equals(roleName.get());

		if (isAdmin) {
			Long orgCount = organizationRepo.countByActiveTrue();
			Long usersCount = userRepo.countByActiveTrue();
			List<User> consultantList = userRepo.findConsultantDoctors();
			int consultantCount = consultantList != null ? consultantList.size() : 0;
			Map<String, Object> globalCount = new HashMap<>();
			globalCount.put("totalOrganizations", orgCount);
			globalCount.put("totalUsers", usersCount);
			globalCount.put("totalConsultants", consultantCount);
			response.put("globalData", globalCount);

			LOG.info("getSuperAdmin Dashboard API End");

			return userUtils.updateResponse(response, UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
					UserConstants.FETCHED, null);
		} else {
			userUtils.globalException(UserConstants.NO_PRIVILAGES_TO_ACCESS,
					Integer.parseInt(UserConstants.UNAUTHORIZED));

		}

		return response;
	}

	@Override
	@Transactional
	public JSONObject getOrgDashboard(JSONObject json) {
		LOG.info("Inside getOrgDashboard Dashboard API");
		JSONObject response = new JSONObject();

		String email = SecurityUtil.currentUserEmailId();

		Optional<String> roleName = userRepo.findRoleNameByEmail(email);

		if (roleName!=null && roleName.isEmpty()) {
			userUtils.globalException(UserConstants.INVALID_USERID, Integer.parseInt(UserConstants.UNAUTHORIZED));
		}

		boolean isAdmin = UserConstants.SUPER_ADMIN.equals(roleName.get());
		if (isAdmin) {

			String fieldValidation = userUtils.validateGetOrgDashboardRequest(json);
			if (fieldValidation != null && !fieldValidation.equals("")) {

				userUtils.globalException(fieldValidation, Integer.parseInt(UserConstants.BAD_REQUEST_CODE));
			}
			ArrayList<String> usersKeys = new ArrayList<>();
			usersKeys.add("admins");
			usersKeys.add("technicians");
			usersKeys.add("doctors");
			usersKeys.add("consultants");

			ArrayList<String> studyKeys = new ArrayList<>();
			studyKeys.add("drafted");
			studyKeys.add("assigned");
			studyKeys.add("unassigned");
			studyKeys.add("completed");

			ArrayList<String> studyClassificationKeys = new ArrayList<>();
			studyClassificationKeys.add("notStarted");
			studyClassificationKeys.add("inprogress");
			studyClassificationKeys.add("noSeriesFound");
			studyClassificationKeys.add("completed");
			studyClassificationKeys.add("notCompleted");

			response.put("usersKeys", usersKeys);
			response.put("studyKeys", studyKeys);
			response.put("studyClassificationKeys", studyClassificationKeys);
			Map<String,String> classfiedMaps = new HashMap<String, String>();
			classfiedMaps.put("classification", "Classification");
			classfiedMaps.put("va", "VentricleAssessment");
			classfiedMaps.put("qflow", "Qflow");
			classfiedMaps.put("gls", "GLS");
			classfiedMaps.put("de", "DE");
			if (json.get(UserConstants.ORG_ID).toString().equals("0") || json.get(UserConstants.ORG_ID).toString().equals("1")) {
				Long adminCount = userRepo.countByActiveTrueAndRoleId(2L);
				Long techniciansCount = userRepo.countByActiveTrueAndRoleId(3L);
				Long doctorsCount = userRepo.countByActiveTrueAndRoleId(4L);
				Long consultantsCount = userRepo.countByActiveTrueAndRoleId(5L);

				Map<String, Object> usersChart = new HashMap<>();
				usersChart.put("admins", adminCount);
				usersChart.put("technicians", techniciansCount);
				usersChart.put("doctors", doctorsCount);
				usersChart.put("consultants", consultantsCount);

				response.put("usersChart", usersChart);

				Map<String, Object> studyChart = new HashMap<>();
				long draftListSize = studyService.countStudyByDateDrafted(
						json.get(UserConstants.START_DATE).toString(), json.get(UserConstants.END_DATE).toString());
				long assignedListSize = studyService.getStudyCountByDateAssigned(
						json.get(UserConstants.START_DATE).toString(), json.get(UserConstants.END_DATE).toString());
				long unassignedListSize = studyService.getStudyCountByDateUnassigned(
						json.get(UserConstants.START_DATE).toString(), json.get(UserConstants.END_DATE).toString());
				long completedListSize = studyService.getStudyCountByDateCompleted(
						json.get(UserConstants.START_DATE).toString(), json.get(UserConstants.END_DATE).toString());

				studyChart.put("drafted", draftListSize);
				studyChart.put("assigned", assignedListSize);
				studyChart.put("unassigned", unassignedListSize);
				studyChart.put("completed", completedListSize);

				response.put("studyChart", studyChart);
				
				LocalDate endDate = LocalDate.parse(json.get(UserConstants.END_DATE).toString());
				LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // 2025-09-30T23:59:59.999999999
				LocalDate startDate = LocalDate.parse(json.get(UserConstants.START_DATE).toString());
				LocalDateTime startDateTime = startDate.atTime(LocalTime.MAX); // 2025-09-30T23:59:59.999999999
				String classificationrange = json.get(UserConstants.CLASSIFICATIONRANGE).toString();
				Map<String,Object> classificaitonStatus = getClassificationStatusChart(classfiedMaps.get(classificationrange), startDateTime, endDateTime,null);
				if(classificaitonStatus.containsKey("studyClassificationChart"))
					response.put("studyClassificationChart", classificaitonStatus.get("studyClassificationChart"));
				LOG.info("getOrgDashboard Dashboard API End");

				return userUtils.updateResponse(response, UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
						UserConstants.FETCHED, null);

			} else {
				List<Long> adminCount = UserOrganizationRepo
						.findUserIdsByOrgId(Long.parseLong(json.get(UserConstants.ORG_ID).toString()));
				Long techniciansCount = userService
						.getAllActiveUserByOrgTechnicians(Long.parseLong(json.get(UserConstants.ORG_ID).toString()));
				Long doctorsCount = userService
						.getAllActiveUserByOrgDoctors(Long.parseLong(json.get(UserConstants.ORG_ID).toString()));
				Long consultantsCount = userService
						.getAllActiveUserByOrgConsultants(Long.parseLong(json.get(UserConstants.ORG_ID).toString()));

				Map<String, Object> usersChart = new HashMap<>();
				usersChart.put("admins", adminCount);
				usersChart.put("technicians", techniciansCount);
				usersChart.put("doctors", doctorsCount);
				usersChart.put("consultants", consultantsCount);

				response.put("usersChart", usersChart);

				Map<String, Object> studyChart = new HashMap<>();
				long draftListSize = studyService.countStudyByDateOrgIdDrafted(
						Long.parseLong(json.get(UserConstants.ORG_ID).toString()),
						json.get(UserConstants.START_DATE).toString(), json.get(UserConstants.END_DATE).toString());
				long assignedListSize = studyService.countStudyByDateOrgIdAssigned(
						Long.parseLong(json.get(UserConstants.ORG_ID).toString()),
						json.get(UserConstants.START_DATE).toString(), json.get(UserConstants.END_DATE).toString());
				long unassignedListSize = studyService.countStudyByDateOrgIdUnassigned(
						Long.parseLong(json.get(UserConstants.ORG_ID).toString()),
						json.get(UserConstants.START_DATE).toString(), json.get(UserConstants.END_DATE).toString());
				long completedListSize = studyService.countStudyByDateOrgIdCompleted(
						Long.parseLong(json.get(UserConstants.ORG_ID).toString()),
						json.get(UserConstants.START_DATE).toString(), json.get(UserConstants.END_DATE).toString());

				studyChart.put("drafted", draftListSize);
				studyChart.put("assigned", assignedListSize);
				studyChart.put("unassigned", unassignedListSize);
				studyChart.put("completed", completedListSize);

				response.put("studyChart", studyChart);
				
				LocalDate endDate = LocalDate.parse(json.get(UserConstants.END_DATE).toString());
				LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // 2025-09-30T23:59:59.999999999
				LocalDate startDate = LocalDate.parse(json.get(UserConstants.START_DATE).toString());
				LocalDateTime startDateTime = startDate.atTime(LocalTime.MAX); // 2025-09-30T23:59:59.999999999
				String classificationrange = json.get(UserConstants.CLASSIFICATIONRANGE).toString();
				Map<String,Object> classificaitonStatus = getClassificationStatusChart(classfiedMaps.get(classificationrange), startDateTime, endDateTime,Long.parseLong(json.get(UserConstants.ORG_ID).toString()));
				if(classificaitonStatus.containsKey("studyClassificationChart"))
					response.put("studyClassificationChart", classificaitonStatus.get("studyClassificationChart"));

				LOG.info("getOrgDashboard Dashboard API End");

				return userUtils.updateResponse(response, UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
						UserConstants.FETCHED, null);
			}

		} else {
			userUtils.globalException(UserConstants.NO_PRIVILAGES_TO_ACCESS,
					Integer.parseInt(UserConstants.UNAUTHORIZED));

		}

		return response;
	}
	
	public Map<String, Object> getClassificationStatusChart(
	        String classificationRange, LocalDateTime startDate, LocalDateTime endDate,Long orgId) {
		List<Object[]> results = new ArrayList<Object[]>();
		if(orgId == null)
			results = studyExRepo.getStatusCounts(classificationRange, startDate, endDate);
		else
			results = studyExRepo.getStatusCountsByOrgId(classificationRange, startDate, endDate,orgId);

	    
	    // Default response keys
	    List<String> keys = List.of("notStarted", "inprogress", "noSeriesFound", "completed", "notCompleted");

	    Map<String, Integer> chart = new HashMap<>();
	    keys.forEach(k -> chart.put(k, 0));

	    for (Object[] row : results) {
	        String category = (String) row[0];
	        System.out.println("category:"+category);
	        Long count = ((Number) row[1]).longValue();
	        System.out.println("count::"+count);
	        chart.put(category, count.intValue());
	    }

	    Map<String, Object> response = new HashMap<>();
	    response.put("studyClassificationKeys", keys);
	    response.put("studyClassificationChart", chart);
	    return response;
	}




	private static final Map<String, String> STATUS_MAPPING = new HashMap<String, String>() {
		{
			put("Completed", "completed");
			put("not completed", "notCompleted");
			put("inprogress", "inprogress");
			put("No relevant series found", "noSeriesFound");
			put("not started", "notStarted");
		}
	};

	private Map<String, Long> countAIProcessStatus(Long orgId, String key) {
	    List<Map<String, Object>> aiStatuses = studyExRepo.findAiProcessStatusByOrgId(orgId);

	    // Initialize status count with all possible statuses
	    Map<String, Long> statusCount = STATUS_MAPPING.values().stream()
	            .collect(Collectors.toMap(status -> status, status -> 0L));

	    if (aiStatuses == null || aiStatuses.isEmpty()) {
	        return statusCount;
	    }

	    // Count statuses
	    aiStatuses.stream()
	            .map(statusMap -> statusMap.get(key))
	            .filter(Objects::nonNull)
	            .map(Object::toString)
	            .map(STATUS_MAPPING::get)
	            .filter(Objects::nonNull)
	            .forEach(status -> statusCount.merge(status, 1L, Long::sum));

	    return statusCount;
	}

}
