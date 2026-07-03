package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.techvedika.harmonycvi.gateway.cloud.StoragePresignService;
import com.techvedika.harmonycvi.gateway.constant.CommonConstants;
import com.techvedika.harmonycvi.gateway.constant.StatusConstants;
import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.dicomweb.DicomMapper;
import com.techvedika.harmonycvi.gateway.dicomweb.DicomWebClient;
import com.techvedika.harmonycvi.gateway.dicomweb.SeriesDTO;
import com.techvedika.harmonycvi.gateway.dicomweb.StudyDTO;
import com.techvedika.harmonycvi.gateway.dto.SaveAIOrgTagsDto;
import com.techvedika.harmonycvi.gateway.entity.AIOrgTags;
import com.techvedika.harmonycvi.gateway.entity.Bookmarks;
import com.techvedika.harmonycvi.gateway.entity.Organization;
import com.techvedika.harmonycvi.gateway.entity.ParameterReference;
import com.techvedika.harmonycvi.gateway.entity.StudyAnnotation;
import com.techvedika.harmonycvi.gateway.entity.StudyClassification;
import com.techvedika.harmonycvi.gateway.entity.StudyClinicalDetails;
import com.techvedika.harmonycvi.gateway.entity.StudyClinicalDetailsComments;
import com.techvedika.harmonycvi.gateway.entity.StudyExtension;
import com.techvedika.harmonycvi.gateway.entity.StudyParameter;
import com.techvedika.harmonycvi.gateway.entity.StudyVolumeInfo;
import com.techvedika.harmonycvi.gateway.entity.User;
import com.techvedika.harmonycvi.gateway.entity.UserStudies;
import com.techvedika.harmonycvi.gateway.exception.RequestValidator;
import com.techvedika.harmonycvi.gateway.exception.ValidationResult;
import com.techvedika.harmonycvi.gateway.messaging.QueueProducer;
import com.techvedika.harmonycvi.gateway.pacsproxy.PacsProxyStudyServiceImpl;
import com.techvedika.harmonycvi.gateway.projection.BookmarkDetailsProjection;
import com.techvedika.harmonycvi.gateway.projection.OrgConsultantProjection;
import com.techvedika.harmonycvi.gateway.projection.PatientHeightWeightProjection;
import com.techvedika.harmonycvi.gateway.projection.StudyUploadProjection;
import com.techvedika.harmonycvi.gateway.projection.TagsImageProjection;
import com.techvedika.harmonycvi.gateway.repository.AIOrgTagsRepository;
import com.techvedika.harmonycvi.gateway.repository.BookmarksRepository;
import com.techvedika.harmonycvi.gateway.repository.OrganizationRepository;
import com.techvedika.harmonycvi.gateway.repository.ParameterReferenceRepository;
import com.techvedika.harmonycvi.gateway.repository.StudyAnnotationRepository;
import com.techvedika.harmonycvi.gateway.repository.StudyClassificationRepository;
import com.techvedika.harmonycvi.gateway.repository.StudyClinicalDetailsCommentsRepository;
import com.techvedika.harmonycvi.gateway.repository.StudyClinicalDetailsRepository;
import com.techvedika.harmonycvi.gateway.repository.StudyExtensionRepository;
import com.techvedika.harmonycvi.gateway.repository.StudyParameterRepository;
import com.techvedika.harmonycvi.gateway.repository.StudyUploadRepository;
import com.techvedika.harmonycvi.gateway.repository.StudyVolumeInfoRepository;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;
import com.techvedika.harmonycvi.gateway.repository.UserStudiesRepository;
import com.techvedika.harmonycvi.gateway.security.SecurityUtil;
import com.techvedika.harmonycvi.gateway.service.BookmarkService;
import com.techvedika.harmonycvi.gateway.service.CommonMethod;
import com.techvedika.harmonycvi.gateway.service.SeriesMeasurementsService;
import com.techvedika.harmonycvi.gateway.service.SeriesParameterService;
import com.techvedika.harmonycvi.gateway.service.StudyParameterService;
import com.techvedika.harmonycvi.gateway.service.UserService;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Service
public class StudyParameterServiceImpl implements StudyParameterService {

	private static final Logger LOG = LoggerFactory.getLogger(StudyParameterServiceImpl.class);
	
//	private String showAll = "false";

	@Value("${ai.study-url}")
	String aiStudyUrl;

	@Value("${license.server.url:http://localhost:8082}")
	String licenseServerUrl;

	@Value("${messaging.activemq.enabled:false}")
	String activemqSwitch;

	@Value("#{environment['app.data-dir'] + '/uploads/'}")
	String uploadStorageDir;

	@Value("#{environment['server.gateway-url'] + '/study/uploads/'}")
	String uploadRetriveDir;

	@Value("${studyupload.bucket-name}")
	String bucketName;

//	    @PersistenceContext
//	    private EntityManager em;
	
	@Autowired
	private EntityManager em;

	@Autowired
	private CommonMethod commonMethod;

	@Autowired
	private UserStudiesRepository UserStudiesRepo;
	
	@Autowired
	private DicomWebClient dicomWebClient;

	@Autowired
	private StudyExtensionRepository studyExtensionRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private BookmarksRepository bookmarksRepo;

	@Autowired
	private BookmarkService bookmarkService;

	@Autowired
	private SeriesMeasurementsService seriesMeasurementsService;

	@Autowired
	private SeriesParameterService seriesService;

	@Autowired
	private StudyParameterRepository studyParameterRepo;

	@Autowired
	private OrganizationRepository orgRepo;

	@Autowired
	private StudyAnnotationRepository studyAnnotationRepo;

	@Autowired
	private StudyClassificationRepository studyClassificationRepo;

	
	@Autowired
	private StudyClinicalDetailsRepository studyClinicalDetailsRepo;
	
	@Autowired
	private StudyClinicalDetailsCommentsRepository studyClinicalDetailsCommentsRepo;
	
	@Autowired
	private AIQueueService aiQueueService;
	
	@Autowired
	private AIOrgTagsRepository aIOrgTagsRepository;
	
	@Autowired
	private StudyUploadRepository studyUploadRepo;
	
	@Autowired
	private StudyVolumeInfoRepository studyVolumeInfoRepo;
	
	@Autowired
	private ParameterReferenceRepository parameterReferenceRepo;

	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	PacsProxyStudyServiceImpl pacsProxyStudyServiceImpl;
	
	@Autowired
    private UserService userService;
	
	@Autowired
	PacsTokenService pacsTokenService;
	
	private final int connectionRetryLimit = 6;
	 
	 private QueueProducer queueProducer;
	 private StoragePresignService storagePresignService;
	 
	 public StudyParameterServiceImpl(QueueProducer queueProducer,StoragePresignService storagePresignService) {
		 this.queueProducer = queueProducer;
		 this.storagePresignService = storagePresignService;
	 }

	@Override
	public ResponseEntity<JSONObject> getParameter(JSONObject jsonRequest) {
		LOG.info("Start of {}.getParameter", this.getClass().getName());
		LOG.debug("Request: {}", jsonRequest);

		JSONObject response = new JSONObject();

		try {
			String email = SecurityUtil.currentUserEmailId();
			Optional<String> roleName = userRepo.findRoleNameByEmail(email);
			if (!roleName.isPresent()) {
				response = commonMethod.createResponse(UserConstants.INVALID_USERID, UserConstants.UNAUTHORIZED);
				LOG.info("End of {}.getParameter - Invalid UserID", this.getClass().getName());
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}

			String role = roleName.get();
			if (jsonRequest != null && jsonRequest.containsKey(CommonConstants.ACCESS_KEY)
					&& !jsonRequest.get(CommonConstants.ACCESS_KEY).toString().isEmpty()
					&& jsonRequest.containsKey(CommonConstants.STUDY_ID)
					&& !jsonRequest.get(CommonConstants.STUDY_ID).toString().isEmpty()) {

				// Validating security token
				if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {
					List<StudyParameter> smObject = null;

//					List<Study> stObject = em.createNamedQuery(Study.GET_STUDY_BY_STUDY_ID)
//							.setParameter(1, jsonRequest.get(CommonConstants.STUDY_ID).toString()).getResultList();


					String bookmarkId = jsonRequest.containsKey(CommonConstants.BOOKMARK_ID)
							? (String) jsonRequest.get(CommonConstants.BOOKMARK_ID)
							: null;

					// Getting bookmark details and creating if doesn't exist
					Long bookmark = bookmarkService
							.getBookmarkId(jsonRequest.get(CommonConstants.STUDY_ID).toString(), bookmarkId, false);

					// Getting record based on seriesId & studyId
//					smObject = em.createNamedQuery(StudyParameter.FIND_BY_BOOKMARK_ID)
//							.setParameter(CommonConstants.BOOKMARK_ID, bookmark.getId()).getResultList();

					smObject = studyParameterRepo.findByBookmarkId(bookmark);

					if (smObject == null || smObject.size() == 0) {
						response = commonMethod.createResponse(StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE,
								StatusConstants.EMPTY_RESULT);
						LOG.info("End of {}.getParameter - Empty Result", this.getClass().getName());
						return ResponseEntity.status(HttpStatus.OK).body(response);
					} else {
						response.putAll(
								prepareResponse(smObject.get(0).getStudyId(), smObject.get(0).getParameterJson()));
						response.put(CommonConstants.RADIAL_STRAIN, smObject.get(0).getRadialStrainJson());
						response.put(CommonConstants.GRAPH, smObject.get(0).getGraph());
						response.put(CommonConstants.SUMMARY, smObject.get(0).getSummary());
						response.put("userRole", role);
						LOG.info("End of {}.getParameter - Success", this.getClass().getName());
						LOG.debug("Response: {}", response);
						return ResponseEntity.status(HttpStatus.OK).body(response);
					}
				} else {
					response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
					LOG.info("End of {}.getParameter - Unauthorized", this.getClass().getName());
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
				}
			} else {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of {}.getParameter - Bad Request", this.getClass().getName());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}
		} catch (Exception e) {
			LOG.error("Exception: ", e);
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of {}.getParameter - Operation Failed", this.getClass().getName());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}


	public ResponseEntity<JSONObject> saveParameter(JSONObject jsonRequest) {
	    LOG.info("Start of {}.saveParameter", this.getClass().getName());
	    LOG.debug("Request: {}", jsonRequest);

	    JSONObject response = new JSONObject();
	    try {
	        ValidationResult validationResults = RequestValidator.validateRequestWithDetails(
	                jsonRequest,CommonConstants.ACCESS_KEY,CommonConstants.STUDY_ID);

	        if (!validationResults.isValid()) {
	            return ResponseEntity.badRequest()
	                    .body(commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST));
	        }

	        // Security token validation
	        if (!CommonConstants.SECURITY_TOKEN.equals(jsonRequest.get(CommonConstants.ACCESS_KEY).toString())) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                    .body(commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN));
	        }

	        // Pre-process JSON once
	        String radialStrain = convertToJsonString(jsonRequest, CommonConstants.RADIAL_STRAIN);
	        String graph = convertToJsonString(jsonRequest, CommonConstants.GRAPH);
	        String computedSeriesIds = convertToJsonString(jsonRequest, CommonConstants.COMPUTED_SERIES);

	        // LV + RV mandatory validation
	       
	        List<StudyParameter> smObject = null;
	        StudyParameter sp = null;

			Bookmarks bookmark = null;
			String parameterStr = null;
	        if (jsonRequest.containsKey(CommonConstants.BOOKMARK_ID)
					&& jsonRequest.get(CommonConstants.BOOKMARK_ID) != null
					&& !jsonRequest.get(CommonConstants.BOOKMARK_ID).toString().isEmpty()) {
				Optional<Bookmarks> bookmarkotp = bookmarksRepo
						.findById(Long.parseLong(jsonRequest.get(CommonConstants.BOOKMARK_ID).toString()));

				if (bookmarkotp.isPresent()) {
					bookmark = bookmarkotp.get();
				}
				
				smObject = studyParameterRepo.findByBookmarkId(bookmark.getId());
				parameterStr = jsonRequest.get("parameterStr").toString();
			} else {
				if (jsonRequest.get(CommonConstants.LV) != null
						&& !jsonRequest.get(CommonConstants.LV).toString().isEmpty()
						&& jsonRequest.get(CommonConstants.RV) != null
						&& !jsonRequest.get(CommonConstants.RV).toString().isEmpty()) {

					bookmark = bookmarkService.getBookmark(jsonRequest.get(CommonConstants.STUDY_ID).toString(),
							null, true);
					smObject = studyParameterRepo.findByBookmarkId(bookmark.getId());

				} else {
					response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE,
							StatusConstants.BAD_REQUEST);
					LOG.info("End of {}.saveParameter - Bad Request", this.getClass().getName());
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
				}
				 HashMap<String, String> lv = (HashMap<String, String>) jsonRequest.get(CommonConstants.LV);
			        HashMap<String, String> rv = (HashMap<String, String>) jsonRequest.get(CommonConstants.RV);
			        if (lv == null || rv == null) {
			            return ResponseEntity.badRequest()
			                    .body(commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST));
			        }
			        JSONObject lvOb = new JSONObject(lv);
					JSONObject rvOb = new JSONObject(rv);
					parameterStr = lvOb.toJSONString() + "BREAKFROMHERE" + rvOb.toJSONString();
			}
	        	        
	     // If records exist, update them
			if (smObject != null && !smObject.isEmpty()) {
				for (StudyParameter spObj : smObject) {
					spObj.setUpdatedTime(new Date());
					spObj.setParameterJson(parameterStr);
					if (radialStrain != null) {
						spObj.setRadialStrainJson(radialStrain);
					}
					if (graph != null) {
						spObj.setGraph(graph);
					}
					spObj.setBookmark(bookmark);
					spObj.setComputedSeries(computedSeriesIds);
					studyParameterRepo.save(spObj);
					// em.merge(spObj);
				}

			} else {
				// If no records exist, create new record
				sp = new StudyParameter();
				sp.setCreatedTime(new Date());
				sp.setUpdatedTime(new Date());
				sp.setStudyId(jsonRequest.get(CommonConstants.STUDY_ID).toString());
				sp.setParameterJson(parameterStr);
				if (radialStrain != null) {
					sp.setRadialStrainJson(radialStrain);
				}
				if (graph != null) {
					sp.setGraph(graph);
				}
				sp.setBookmark(bookmark);
				sp.setVersion(bookmark.getVersion());
				sp.setComputedSeries(computedSeriesIds);
				// em.persist(sp);
				studyParameterRepo.save(sp);
			}

	        response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SAVED);
	        return ResponseEntity.ok(response);

	    } catch (Exception e) {
	        LOG.error("Exception: ", e);
	        return ResponseEntity.internalServerError()
	                .body(commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR));
	    }
	}

	private String convertToJsonString(JSONObject jsonRequest, String key) {
	    if (jsonRequest.containsKey(key) && jsonRequest.get(key) != null && !jsonRequest.get(key).toString().isEmpty()) {
	        return new JSONObject((HashMap<String, String>) jsonRequest.get(key)).toJSONString();
	    }
	    return null;
	}

	@Override
	public ResponseEntity<JSONObject> updateIsAiProccessed(JSONObject jsonRequest) {
		LOG.info("Start of {}.updateIsAiProccessed", this.getClass().getName());
		LOG.debug("Request: {}", jsonRequest);

		JSONObject response = new JSONObject();

		try {
			if (jsonRequest != null && jsonRequest.get(CommonConstants.ACCESS_KEY) != null
					&& !jsonRequest.get(CommonConstants.ACCESS_KEY).toString().isEmpty()
					&& jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID) != null
					&& !jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString().isEmpty()) {

				// Validating security token
				if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

					String studyId = jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString();
					long studyCount = studyExtensionRepo.countByStudyId(studyId);
					
					if (studyCount>0) {
						updateIsAIprocessedByStudyUID(studyId, true);
					} else {
						response = commonMethod.createResponse(StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE,
								StatusConstants.INVALID_STUDY_ID);
						LOG.info("End of {}.updateIsAiProccessed - {}", this.getClass().getName(),
								StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
						return ResponseEntity.status(HttpStatus.OK).body(response);
					}

					response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.UPDATED);
					LOG.info("End of {}.updateIsAiProccessed - {}", this.getClass().getName(), StatusConstants.SUCCESS);
					return ResponseEntity.status(HttpStatus.OK).body(response);
				} else {
					response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
					LOG.info("End of {}.updateIsAiProccessed - {}", this.getClass().getName(),
							StatusConstants.UNAUTHORIZED);
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
				}

			} else {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of {}.updateIsAiProccessed - {}", this.getClass().getName(),
						StatusConstants.BAD_REQUEST_CODE);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

		} catch (Exception e) {
			LOG.error("Exception: ", e);
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of {}.updateIsAiProccessed - {}", this.getClass().getName(),
					StatusConstants.OPERATION_FAILED);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
	
	private void processViaActiveMQ(String aiUrl,JSONObject request) {
	    LOG.info("Start of "+this.getClass().getName()+".processViaActiveMQ");
	    System.out.println("aiUrl :: " + aiUrl);
	    aiQueueService.enqueueAIRequest(aiUrl,request);  // This is async
	    System.out.println("processViaActiveMQ end");
	    LOG.info("End of "+this.getClass().getName()+".processViaActiveMQ" + StatusConstants.SUCCESS);
	}

	@Override
	//@Transactional
	public ResponseEntity<JSONObject> proccesseAI(String studyUID, String orgId, String isAll,String authorization) {
		LOG.info("Start of {}.proccesseAI", this.getClass().getName());
		LOG.debug("studyUID: {}", studyUID);
		LOG.debug("orgId: {}", orgId);
		JSONObject response = new JSONObject();
		
		String email = SecurityUtil.currentUserEmailId();
		
		Optional<Long> userIdOpt = userRepo.findIdByEmail(email);
		if(userIdOpt.isEmpty()) {
			response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
			LOG.info("End of {}.updateIsAiProccessed - {}", this.getClass().getName(),
					StatusConstants.UNAUTHORIZED);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
		Long userId = userIdOpt.get();
		LOG.debug("userId: {}", userId);
		LOG.debug("isAll: {}", isAll);
		
        mapStudyUser(orgId, userId.toString(), studyUID);
        

        JSONObject request = new JSONObject();
        
        request.put(CommonConstants.STUDY__ID, studyUID);
        request.put(CommonConstants.ORG__ID, orgId);
        request.put(CommonConstants.USER__ID, userId);
        request.put("is_all", "no");
        request.put(CommonConstants.SERVER_BASE_URL, commonMethod.getPacsUrl(orgId));
        try {
        	authorization = pacsTokenService.getToken(orgId);
			request.put("Authorization", authorization);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String aiUrl = aiStudyUrl +"preprocess";

		LOG.info("aiUrl::"+aiUrl);

		try {
			updatePatientIdInPatientTable(studyUID, activemqSwitch);

			// Getting bookmark details and creating if it doesn't exist
			try {
				Long bookmark = bookmarkService.getBookmarkId(studyUID, null, true);
				LOG.info("Bookmark in proccesseAI: {}", bookmark);
			} catch (Exception e) {
				LOG.error("Exception while creating preprocess bookmark for {}", studyUID, e);
			}

			if (activemqSwitch.equalsIgnoreCase("true")) {
				LOG.info("Inside ActiveMQ on for proccesseAI");
				processViaActiveMQ(aiUrl,request);
				LOG.info("Initiated thread");

				// Update AI flag
				
				updateIsAIprocessedByStudyUID(studyUID, true);
				
				LOG.info("Updated AI flag");

				JSONObject res = new JSONObject();
				res.put("studyInstanceUid", studyUID);
				res.put("status", 1000);
				response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, res.toString());

				LOG.debug("Response: {}", response);

			} else {
				LOG.info("Inside ELSE");

				LOG.info("AI URL: {}", aiUrl);
				HttpHeaders headers = new HttpHeaders();
		        headers.setContentType(MediaType.APPLICATION_JSON);
		        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		        LOG.info("request:"+request.toJSONString());
	            HttpEntity<String> entity = new HttpEntity<>(request.toJSONString(), headers);
	            ResponseEntity<String> clientResponse = restTemplate.exchange(aiUrl, HttpMethod.POST, entity, String.class);

				LOG.info("Status code: {}", clientResponse.getStatusCode());

				if (clientResponse.getStatusCode() != HttpStatus.OK) {
					response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED,
							StatusConstants.SERVER_ERROR);
					throw new RuntimeException("Failed: HTTP error code: " + clientResponse.getStatusCode());
				}

				String output = clientResponse.getBody();
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(output);

				if (json.get(CommonConstants.STATUS) != null
						&& json.get(CommonConstants.STATUS).toString().equals("1000")) {
					LOG.info("AI response: {}", json);

					// Update AI flag
					updateIsAIprocessedByStudyUID(studyUID, true);
				} else {
					updateIsAIprocessedByStudyUID(studyUID, false);
				}

				LOG.info("proccesseAI API End");
				response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, output);
				LOG.debug("Response: {}", response);
			}

		} catch (Exception e) {
			LOG.error("Exception: ", e);
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of {}.proccesseAI - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
		}

		LOG.info("End of {}.proccesseAI - {}", this.getClass().getName(), StatusConstants.SUCCESS);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	private void updateIsAIprocessedByStudyUID(String studyUID,boolean status) {	
		int maxRetries = 3;
		int attempts = 0;
		boolean updatedSuccessfully = false;

		while (attempts < maxRetries && !updatedSuccessfully) {
		    attempts++;
		    Optional<Long> lockVersion = studyExtensionRepo.findLockVersionByStudyId(studyUID);
			if(lockVersion.isEmpty()) {
				LOG.info("No row exists to update");
				break;
			}
		    int updated = studyExtensionRepo.updateIsAIProcessedByStudyInstanceUID(status, studyUID,lockVersion.get());
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
		    LOG.info("End of " + this.getClass().getName() + ".updateClassificationData "
		             + StatusConstants.OPERATION_FAILED);
		}
		
	}

	// updatePatientID in patient table with StudyID : Laxmi
	@Transactional
	private void updatePatientIdInPatientTable(String studyUID, String activeMQFlag) {
		LOG.info("Start of " + this.getClass().getName() + ".updatePatientIdInPatientTable");
		// List<Study> stObject = null;
		// Getting record based on studyUId
		long studyCount = studyExtensionRepo.countByStudyId(studyUID);
		// stObject = em.createNamedQuery(Study.GET_STUDY_BY_STUDY_ID).setParameter(1,
		// studyUID).getResultList();
		if (studyCount > 0) {
			System.out.println("Inside study list record:::::::::::::::::" + studyCount);
			if (null != activeMQFlag && ("on").equalsIgnoreCase(activeMQFlag)) {
				HashMap<String, Object> aiProcessStatus = new HashMap<String, Object>();
				aiProcessStatus.put("Classification", "Not Started");
				aiProcessStatus.put("Qflow", "Not Started");
				aiProcessStatus.put("VentricleAssessment", "Not Started");
				System.out.println("setting default data for study AI Process Status" + aiProcessStatus);
				if(updateAiStatus(studyUID,aiProcessStatus,new Date())) {
					LOG.info("AI process status Updated");
				}
			}
			try {
				System.out.println("before thread sleep");
				Thread.sleep(2000);
				System.out.println("after thread sleep");
			} catch (InterruptedException e) {
				LOG.info(e.getLocalizedMessage());
			}
		}

		LOG.info("End of " + this.getClass().getName() + ".updatePatientIdInPatientTable" + StatusConstants.SUCCESS);
	}

	@Transactional
	public JSONObject mapStudyUser(String orgId, String userId, String studyUID) {
		LOG.info("Start of mapStudyUser");
		LOG.debug("studyUID: {}, orgId: {}, userId: {}", studyUID, orgId, userId);

		Long orgIdLong = Long.parseLong(orgId);
		Long userIdLong = Long.parseLong(userId);

		Optional<OrgConsultantProjection> optionalOrg =
		        orgRepo.findConsultantDetailsById(orgIdLong);

		if (optionalOrg.isPresent() && Boolean.TRUE.equals(optionalOrg.get().getConsultant())) {
		    String consultantEmail = optionalOrg.get().getEmail();
			Optional<User> consultantOpt = userRepo.findByEmail(consultantEmail);

			if (consultantOpt.isPresent()) {
				User consultant = consultantOpt.get();

				boolean userStudiesExists = UserStudiesRepo.existsByStudyIdAndUserId(studyUID, consultant.getId());
				System.out.println("userStudyExists::"+userStudiesExists);
				if (!userStudiesExists) {
					UserStudies us = new UserStudies();
					us.setCreatedBy(userIdLong);
					us.setCreatedDt(new Date());
					us.setActive(true);
					us.setLastUpdatedBy(userIdLong);
					us.setLastUpdatedDt(new Date());
					us.setStudyId(studyUID);
					us.setUser(consultant);

					UserStudiesRepo.save(us);
					updateStatusByStudyInstanceUID("Assigned", studyUID);
					LOG.info("Consultant Doctor assigned to study");
				}
			}
		}

		JSONObject response = new JSONObject();
		try {
			LOG.info("====updateUserAndOrg called=========");

			updateOrgAndCreatedByByStudyInstanceUID(Long.valueOf(orgId), String.valueOf(userIdLong), studyUID);

			// Optional<Study> studyOpt = studyRepository.findByStudyInstanceUID(studyUID);
			Optional<String> status = studyExtensionRepo.findStatusByStudyInstanceUID(studyUID);
			if (status.isPresent()) {
				updateStatusByStudyInstanceUID("Assigned", studyUID);
			} else {
				updateStatusByStudyInstanceUID("Unassigned", studyUID);
			}

		} catch (Exception e) {
			LOG.error("Exception occurred: ", e);
			return commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
		}

		LOG.info("End of mapStudyUser: SUCCESS");
		return response;
	}
	
	private boolean updateStatusByStudyInstanceUID(String status, String studyId) {
		int maxRetries = 3;
		int attempts = 0;
		boolean updatedSuccessfully = false;

		while (attempts < maxRetries && !updatedSuccessfully) {
		    attempts++;
		    Optional<Long> lockVersion = studyExtensionRepo.findLockVersionByStudyId(studyId);
			if(lockVersion.isEmpty()) {
				LOG.info("No row exists to update");
				return true;
			}
		    int updated = studyExtensionRepo.updateStatusByStudyInstanceUID(status, studyId,lockVersion.get());
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
		    LOG.info("End of " + this.getClass().getName() + ".updateStatusByStudyInstanceUID"
		             + StatusConstants.OPERATION_FAILED);
		}
		return updatedSuccessfully;
		
	}
	
	private boolean updateOrgAndCreatedByByStudyInstanceUID(Long orgId, String userId, String studyId) {
		int maxRetries = 3;
		int attempts = 0;
		boolean updatedSuccessfully = false;

		while (attempts < maxRetries && !updatedSuccessfully) {
		    attempts++;
		    Optional<Long> lockVersion = studyExtensionRepo.findLockVersionByStudyId(studyId);
			if(lockVersion.isEmpty()) {
				LOG.info("No row exists to update");
				return true;
			}
		    int updated = studyExtensionRepo.updateOrgAndCreatedByByStudyInstanceUID(orgId, userId,studyId,lockVersion.get());
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
		    LOG.info("End of " + this.getClass().getName() + ".updateStatusByStudyInstanceUID"
		             + StatusConstants.OPERATION_FAILED);
		}
		return updatedSuccessfully;
		
	}

	// Used at weasis
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject saveSummary(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + ".saveSummary");
		LOG.debug("Request : " + jsonRequest);

		JSONObject response = new JSONObject();
		try {

			if (jsonRequest != null && jsonRequest.get(CommonConstants.ACCESS_KEY) != null
					&& !jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals("")
					&& jsonRequest.get(CommonConstants.STUDY_ID) != null
					&& !jsonRequest.get(CommonConstants.STUDY_ID).toString().equals("")
					&& jsonRequest.containsKey(CommonConstants.BOOKMARK_ID)) { // Bookmark id can be null in case of
																				// preprocess

				// Validating security token
				if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

					List<StudyParameter> smObject = null;

					String bookmarkId = jsonRequest.containsKey(CommonConstants.BOOKMARK_ID)
							? jsonRequest.get(CommonConstants.BOOKMARK_ID) != null
									&& jsonRequest.get(CommonConstants.BOOKMARK_ID) != ""
											? (String) jsonRequest.get(CommonConstants.BOOKMARK_ID)
											: null
							: null;

					// Getting bookmark details
					Long bookmark = bookmarkService
							.getBookmarkId(jsonRequest.get(CommonConstants.STUDY_ID).toString(), bookmarkId, false);

					// Getting record based on bookmarkId
//					smObject = em.createNamedQuery(StudyParameter.FIND_BY_BOOKMARK_ID)
//							.setParameter(CommonConstants.BOOKMARK_ID, bookmark.getId()).getResultList();

					smObject = studyParameterRepo.findByBookmarkId(bookmark);

					String radialStrain = null;

					if (jsonRequest.containsKey(CommonConstants.RADIAL_STRAIN)
							&& jsonRequest.get(CommonConstants.RADIAL_STRAIN) != null
							&& !jsonRequest.get(CommonConstants.RADIAL_STRAIN).toString().equals("")) {
						HashMap<String, String> radialStrainMap = (HashMap<String, String>) jsonRequest
								.get(CommonConstants.RADIAL_STRAIN);
						JSONObject radialStrainJson = new JSONObject(radialStrainMap);
						radialStrain = radialStrainJson.toJSONString();
					}
					if (smObject != null && smObject.size() > 0) {

						for (StudyParameter spObj : smObject) {
							spObj.setUpdatedTime(new Date());

							if (radialStrain != null && !radialStrain.equals("")) {
								spObj.setRadialStrainJson(radialStrain);
							}
							if (jsonRequest.get(CommonConstants.SUMMARY) != null
									&& !jsonRequest.get(CommonConstants.SUMMARY).toString().equals("")) {
								spObj.setSummary(jsonRequest.get(CommonConstants.SUMMARY).toString());
							}
							// em.merge(spObj);
							studyParameterRepo.save(spObj);
						}
					} else {
						response = commonMethod.createResponse(StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE,
								StatusConstants.INVALID_STUDY_ID);
						LOG.info("End of " + this.getClass().getName() + ".saveSummary"
								+ StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
						return response;
					}
					LOG.info("saveSummary API End");

					response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SAVED);
					LOG.info("End of " + this.getClass().getName() + ".saveSummary" + StatusConstants.SUCCESS);
					return response;
				} else {

					response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
					LOG.info("End of " + this.getClass().getName() + ".saveSummary" + StatusConstants.UNAUTHORIZED);
					return response;
				}

			} else {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".saveSummary" + StatusConstants.BAD_REQUEST_CODE);
				return response;
			}
		} catch (Exception e) {
			LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".saveSummary" + StatusConstants.OPERATION_FAILED);
			return response;
		}
	}

	private boolean hasValue(JSONObject obj, String key) {
		return obj.containsKey(key) && obj.get(key) != null && !obj.get(key).toString().trim().isEmpty();
	}

	private JSONObject unauthorizedResponse() {
		return commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
	}

	@Override
	@Transactional
	public JSONObject saveAnnotation(JSONObject jsonRequest) {
		LOG.info("Start of saveAnnotation");
		LOG.debug("Request: {}", jsonRequest);

		JSONObject response = new JSONObject();

		try {
			// Validate request fields
			if (jsonRequest != null && hasValue(jsonRequest, CommonConstants.ACCESS_KEY)
					&& hasValue(jsonRequest, CommonConstants.STUDY_ID)
					&& hasValue(jsonRequest, CommonConstants.STUDY_ANNOTATION)
					&& hasValue(jsonRequest, CommonConstants.SOP_INSTANCE_UID)
					&& hasValue(jsonRequest, CommonConstants.INSTANCE_ID.toString())) {

				String accessKey = jsonRequest.get(CommonConstants.ACCESS_KEY).toString();

				if (!accessKey.equals(CommonConstants.SECURITY_TOKEN)) {
					return unauthorizedResponse();
				}

				String studyId = jsonRequest.get(CommonConstants.STUDY_ID).toString();
				String sopInstanceUID = jsonRequest.get(CommonConstants.SOP_INSTANCE_UID).toString();
				String instanceId = jsonRequest.get(CommonConstants.INSTANCE_ID).toString();

				@SuppressWarnings("unchecked")
				HashMap<String, String> annotationMap = (HashMap<String, String>) jsonRequest
						.get(CommonConstants.STUDY_ANNOTATION);

				JSONObject annotationJson = new JSONObject(annotationMap);
				HashMap<String, Object> newAnnotation = annotationJson;
				List<Map<String, Object>> annotationList = new ArrayList<>();

//	            List<StudyAnnotation> existingAnnotations = em.createNamedQuery(StudyAnnotation.STUDY_ANNOTATION_BY_STUDY_ID, StudyAnnotation.class)
//	                    .setParameter(CommonConstants.STUDY_ID, studyId)
//	                    .getResultList();

				List<StudyAnnotation> existingAnnotations = studyAnnotationRepo.findByStudyId(studyId);

				if (!existingAnnotations.isEmpty()) {
					for (StudyAnnotation spObj : existingAnnotations) {
						spObj.setLastUpdatedDt(new Date());
						annotationList = spObj.getAnnotationData();
						boolean updated = false;

						if (annotationList != null && !annotationList.isEmpty()) {
							for (int i = 0; i < annotationList.size(); i++) {
								Map<String, Object> existing = annotationList.get(i);
								if (sopInstanceUID.equals(existing.get(CommonConstants.SOP_INSTANCE_UID))
										&& instanceId.equals(existing.get(CommonConstants.INSTANCE_ID))) {
									annotationList.set(i, newAnnotation);
									updated = true;
									break;
								}
							}
						}

						if (!updated) {
							annotationList.add(newAnnotation);
						}

						spObj.setAnnotationData(annotationList);
						// em.merge(spObj);
						studyAnnotationRepo.save(spObj);
					}

				} else {
					StudyAnnotation sa = new StudyAnnotation();
					sa.setCreatedDt(new Date());
					sa.setLastUpdatedDt(new Date());
					sa.setStudyId(studyId);
					annotationList.add(newAnnotation);
					sa.setAnnotationData(annotationList);
					// em.persist(sa);
					studyAnnotationRepo.save(sa);
				}

				response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SAVED);
				LOG.info("End of saveAnnotation - SUCCESS");
				return response;

			} else {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of saveAnnotation - BAD REQUEST");
				return response;
			}

		} catch (Exception e) {
			LOG.error("Exception in saveAnnotation", e);
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of saveAnnotation - ERROR");
			return response;
		}
	}

	private boolean isValidDeleteRequest(JSONObject json) {
		return hasValue(json, CommonConstants.ACCESS_KEY) && hasValue(json, CommonConstants.STUDY_ID)
				&& hasValue(json, CommonConstants.SOP_INSTANCE_UID)
				&& hasValue(json, CommonConstants.INSTANCE_ID.toString())
				&& CommonConstants.SECURITY_TOKEN.equals(json.get(CommonConstants.ACCESS_KEY).toString());
	}

	@Override
	@Transactional
	public JSONObject deleteAnnotation(JSONObject jsonRequest) {

		if (!isValidDeleteRequest(jsonRequest)) {
			return commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
		}

		String studyId = jsonRequest.get(CommonConstants.STUDY_ID).toString();
		String sopUID = jsonRequest.get(CommonConstants.SOP_INSTANCE_UID).toString();
		String instanceId = jsonRequest.get(CommonConstants.INSTANCE_ID).toString();

		List<StudyAnnotation> annotations = studyAnnotationRepo.findByStudyId(studyId);

		if (annotations.isEmpty()) {
			return commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.INVALIS_STUDY_ID);
		}

		for (StudyAnnotation annotation : annotations) {
			List<Map<String, Object>> data = annotation.getAnnotationData();
			if (data != null) {
				boolean removed = data.removeIf(item -> sopUID.equals(item.get(CommonConstants.SOP_INSTANCE_UID))
						&& instanceId.equals(item.get(CommonConstants.INSTANCE_ID)));

				if (removed) {
					if (data.isEmpty()) {
						studyAnnotationRepo.delete(annotation);
					} else {
						annotation.setAnnotationData(data);
						annotation.setLastUpdatedDt(new Date());
						studyAnnotationRepo.save(annotation);
					}

					return commonMethod.createResponse(StatusConstants.SUCCESS_CODE,
							StatusConstants.ANNOTATION_DELETED);
				}
			}
		}

		return commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.ANNOTATION_DOES_NOTE_EXIST);
	}

	private boolean isValidClassificationRequest(JSONObject jsonRequest) {
		return jsonRequest != null && CommonConstants.SECURITY_TOKEN.equals(jsonRequest.get(CommonConstants.ACCESS_KEY))
				&& hasValue(jsonRequest, CommonConstants.STUDY_INSTANCE_UID)
				&& hasValue(jsonRequest, CommonConstants.STUDY_CLASSIFICATION_DATA)
				&& hasValue(jsonRequest, CommonConstants.IMG_COUNT)
				&& hasValue(jsonRequest, CommonConstants.DICOM_COUNT)
				&& hasValue(jsonRequest, CommonConstants.PATIENT_HEIGHT)
				&& hasValue(jsonRequest, CommonConstants.PATIENT_WEIGHT);
	}

	@Override
	@Transactional
	@SuppressWarnings("unchecked")
	public JSONObject saveClassification(JSONObject jsonRequest) {
		LOG.info("Start of saveClassification");

		try {
			// Validate access key and input
			if (!isValidClassificationRequest(jsonRequest)) {
				return commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
			}

			String studyUID = jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString();

			//JSONArray classificationArray = (JSONArray) jsonRequest.get(CommonConstants.STUDY_CLASSIFICATION_DATA);
			Object classificationObj = jsonRequest.get(CommonConstants.STUDY_CLASSIFICATION_DATA);

			JSONArray classificationArray = new JSONArray();
			if (classificationObj instanceof List) {
			    classificationArray.addAll((List<?>) classificationObj);
			}


			if (classificationArray.isEmpty()) {
				return commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
			}
			
			// Delete existing rows
		    studyClassificationRepo.deleteByStudyId(studyUID);

		    Date now = new Date();
		    StringBuilder sb = new StringBuilder();
		    sb.append("INSERT INTO harmonycvi.study_classification ")
		      .append("(study_id, series_id, sequence_type, image_plane, created_time, last_updated_time) VALUES ");

		    List<String> values = new ArrayList<>();

		    for (Object obj : classificationArray) {
		    	 if (obj instanceof Map) {
				        Map<String, Object> jsonObj = (Map<String, Object>) obj;
				        String seriesId = jsonObj.get("seriesInstanceUid").toString();
				        String sequenceType = jsonObj.get("SequenceType").toString();
				        String imagePlane = jsonObj.get("ImagePlane").toString();
	
				        // Add each row as a value tuple, escape quotes properly
				        values.add(String.format("('%s', '%s', '%s', '%s', '%s', '%s')",
				        		studyUID, seriesId, sequenceType, imagePlane, now, now));
		    	 }
		    }

		    sb.append(String.join(", ", values));

		    // Execute single insert
		    em.createNativeQuery(sb.toString()).executeUpdate();
		    
			String imgCount = jsonRequest.get(CommonConstants.IMG_COUNT).toString();
		    Long dicomCount = Long.parseLong(jsonRequest.get(CommonConstants.DICOM_COUNT).toString());
		    String height = jsonRequest.get(CommonConstants.PATIENT_HEIGHT).toString();
		    String weight = jsonRequest.get(CommonConstants.PATIENT_WEIGHT).toString();
		    
			int maxRetries = 3;
			int attempts = 0;
			boolean updatedSuccessfully = false;

			while (attempts < maxRetries && !updatedSuccessfully) {
			    attempts++;
			    Optional<Long> lockVersion = studyExtensionRepo.findLockVersionByStudyId(studyUID);
				if(lockVersion.isEmpty()) {
					LOG.info("No row exists to update");
					break;
				}
			    int updated = studyExtensionRepo.updateStudyExtension(studyUID, imgCount, dicomCount, height, weight,lockVersion.get());
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
				LOG.info("No StudyExtension found for studyUID: " + studyUID);
			}
			

			LOG.info("saveClassification API End");
			return commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SAVED);

		} catch (Exception e) {
			LOG.error("Exception in saveClassification", e);
			return commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
		}
	}

	private JSONObject createAndLogResponse(String message, String code) {
		JSONObject resp = commonMethod.createResponse(code, message);
		LOG.info(message);
		return resp;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getClassification(JSONObject jsonRequest) {
		LOG.info("Start of {}.getClassification", this.getClass().getName());
		LOG.debug("Request : {}", jsonRequest);
		JSONObject response = new JSONObject();

		try {
			if (hasValue(jsonRequest, CommonConstants.ACCESS_KEY)
					&& hasValue(jsonRequest, CommonConstants.STUDY_INSTANCE_UID)) {
				if (!jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {
					return createAndLogResponse(StatusConstants.INVALID_TOKEN, StatusConstants.UNAUTHORIZED);
				}


				List<StudyClassification> smObject = studyClassificationRepo
						.findByStudyId(jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString());

				response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE,
						StatusConstants.CLASSIFICATION_DATA);
				response.put("data", smObject);
				LOG.info("End of {}.getClassification {}", this.getClass().getName(), StatusConstants.SUCCESS);
				return response;
			} else {
				return createAndLogResponse(StatusConstants.BAD_REQUEST, StatusConstants.BAD_REQUEST_CODE);
			}

		} catch (Exception e) {
			LOG.error("Exception occurred in getClassification", e);
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of {}.getClassification {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
			return response;
		}
	}

	private boolean isEmpty(Object obj) {
		return obj == null || obj.toString().trim().isEmpty();
	}

	private boolean isAdminOrTechnician(String role) {
		String roleStr = role.toUpperCase();
		return roleStr.contains("ADMIN") || roleStr.equals(UserConstants.TECHNICIAN);
	}

	private String resolveOrgId(JSONObject jsonRequest, String email) {
		if ("0".equals(jsonRequest.get(CommonConstants.ORG_ID).toString()) || "1".equals(jsonRequest.get(CommonConstants.ORG_ID).toString())) {
			List<Organization> orgs = userRepo.findOrgsByEmail(email);
			for (Organization org : orgs) {
				if (org.getConsultant() != null && org.getConsultant()) {
					return String.valueOf(org.getId());
				}
			}
			return null;
		} else {
			return jsonRequest.get(CommonConstants.ORG_ID).toString();
		}
	}

	private JSONObject logAndReturn(JSONObject response, String message) {
		LOG.info(message);
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getStudyList(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + ".getStudyList");

		JSONObject response = new JSONObject();
		
		String email = SecurityUtil.currentUserEmailId();
		

		try {
	        // Step 1: Validate JWT Token
	        
	        Optional<String> roleName = userRepo.findRoleNameByEmail(email);
	        if (!roleName.isPresent()) {
	            return logAndReturn(commonMethod.createResponse(UserConstants.INVALID_USERID, UserConstants.UNAUTHORIZED),
	                    "Invalid user from JWT");
	        }
			// Step 2: Validate request and access key
			if (jsonRequest == null || isEmpty(jsonRequest.get(CommonConstants.ACCESS_KEY))
					|| isEmpty(jsonRequest.get(CommonConstants.ORG_ID))) {
				return logAndReturn(
						commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST),
						"Invalid request parameters");
			}

			if (!CommonConstants.SECURITY_TOKEN.equals(jsonRequest.get(CommonConstants.ACCESS_KEY).toString())) {
				return logAndReturn(
						commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN),
						"Invalid security token");
			}

			// Step 3: Prepare query parameters
			String role = roleName.get();
			Long userId = userRepo.findIdByEmail(email).orElse(0L);
			
			String orgId = resolveOrgId(jsonRequest, email);
			if (orgId == null) {
				return logAndReturn(
						commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN),
						"Unable to resolve organization");
			}
			Map<String, Object> prefs = null;
			String showAll = "false";
			if(!role.equalsIgnoreCase("SUPER_ADMIN"))
			{
			Optional<Organization> organization = orgRepo.findById(Long.parseLong(orgId));
			
			if(organization.isPresent())
			{
				Organization org = organization.get();
				prefs = org.getPreferences();
				showAll = prefs.get("show_all").toString();
			}
			}
			LOG.info("ShowAll:"+showAll+" "+role+" "+orgId);
			String searchQuery = (jsonRequest.containsKey("search") && jsonRequest.get("search") != null) ? jsonRequest.get("search").toString().toLowerCase() : "";
			
			int pageNumber = (jsonRequest.containsKey("pageNumber") && jsonRequest.get("pageNumber") != null)
			        ? Integer.parseInt(jsonRequest.get("pageNumber").toString())
			        : 0;

			int pageSize = (jsonRequest.containsKey("pageSize") && jsonRequest.get("pageSize") != null)
			        ? Integer.parseInt(jsonRequest.get("pageSize").toString())
			        : 10;

			int start = (pageNumber) * pageSize;

			boolean isAdminOrTech = isAdminOrTechnician(role);
			boolean isDoctor = role.contains("DOCTOR");

			List<StudyExtension> studyList = new ArrayList<>();
			long totalCount = 0;
			
			Pageable pageable = null;
			if(jsonRequest.get("pageNumber") != null && jsonRequest.get("pageSize") != null) {
			pageable = PageRequest.of(pageNumber, pageSize);
			}
			//String studyId = PacsProxyStudyServiceImpl.studyId;
			//insertStudyExtension(studyId,Long.parseLong(orgId),loggedUser.getId().toString());

			// Step 4: Query data
			
			if(showAll.equalsIgnoreCase("true"))
			{
				Page<StudyExtension> studyPage = null;
				if(jsonRequest.get("pageNumber") != null && jsonRequest.get("pageSize") != null) 
				{
				studyPage = studyExtensionRepo.findByOrgIdAndOptionalPatientName(Long.parseLong(orgId), searchQuery, pageable);
				
				//Page<StudyExtension> studyPage = studyExtensionRepo.findStudiesNative(Long.parseLong(orgId), pageable);
				
				studyList = studyPage.getContent();
				
				totalCount = studyPage.getTotalElements();
				}
				else if(jsonRequest.get("startDate") != null && jsonRequest.get("endDate") != null)
				{
					String startDate = jsonRequest.get("startDate").toString();
					String endDate = jsonRequest.get("endDate").toString();
					studyList = studyExtensionRepo.findByStudyDate(Long.parseLong(orgId), searchQuery,startDate,endDate);
					
				}

				else
				{
					studyList = studyExtensionRepo.findAllByOrgIdAndOptionalPatientName(Long.parseLong(orgId), searchQuery);
					LOG.info("studyList size:"+studyList.size());
				}
				
			}
			else if (isAdminOrTech) {
				if (Long.parseLong(orgId) == -1) {
					LOG.info("OrgID is -1");
//					studyList = em.createNamedQuery(Study.GET_STUDIES_BY_NAME).setParameter(1, "%" + searchQuery + "%")
//							.setFirstResult(start).setMaxResults(pageSize).getResultList();					
					Page<StudyExtension> studyPage = studyExtensionRepo.findByPatientFullNameLike(searchQuery.toLowerCase(), pageable);
					
					studyList = studyPage.getContent();
					totalCount = studyPage.getTotalElements();
					LOG.info("TotalCount in -1:"+totalCount);
//					totalCount = (long) em.createNamedQuery(Study.COUNT_ALL_BY_NAME)
//							.setParameter(1, "%" + searchQuery + "%").getSingleResult();
				} else {
					LOG.info("OrgID is not -1 But Admin or technician");
//					studyList = em.createNamedQuery(Study.GET_BY_ORG).setParameter(1, Long.parseLong(orgId))
//							.setParameter(2, "%" + searchQuery + "%").setFirstResult(start).setMaxResults(pageSize)
//							.getResultList();
					
					//Page<StudyExtension> studyPage = studyExtensionRepo.findStudiesByPatientNameAndOrgId("%" + searchQuery.trim().toLowerCase() + "%", Long.parseLong(orgId), pageable);
					if(jsonRequest.get("pageNumber") != null && jsonRequest.get("pageSize") != null) 
					{
					
					Page<StudyExtension> studyPage = studyExtensionRepo.findByOrgIdAndOptionalPatientName(Long.parseLong(orgId), searchQuery, pageable);
				
					//Page<StudyExtension> studyPage = studyExtensionRepo.findStudiesNative(Long.parseLong(orgId), pageable);
					
					studyList = studyPage.getContent();
					
					totalCount = studyPage.getTotalElements();
					LOG.info("TotalCount in not -1:"+totalCount);
					}
					else
					{
						studyList = studyExtensionRepo.findAllByOrgIdAndOptionalPatientName(Long.parseLong(orgId), searchQuery);
						LOG.info("studyList size:"+studyList.size());
					}
				}
			} else if (isDoctor) {
				LOG.info("Inside Doctor");
				Page<StudyExtension> studyPage = null;
				if(jsonRequest.get("startDate") != null && jsonRequest.get("endDate") != null)
				{
					String startDate = jsonRequest.get("startDate").toString();
					String endDate = jsonRequest.get("endDate").toString();
					
					studyList = studyExtensionRepo.findByStudyDate(Long.parseLong(orgId), searchQuery,startDate,endDate);
				}
				else {
				studyPage = studyExtensionRepo.findStudiesByOrgIdAndUserIdAndPatientNameLike(Long.parseLong(orgId),userId,searchQuery, pageable);
				
				studyList = studyPage.getContent();
				
				totalCount = studyPage.getTotalElements();
				LOG.info("TotalCount in doctor:"+totalCount);
				}
//				totalCount = (long) em.createNamedQuery(Study.COUNT_BY_ORG_USER).setParameter(1, Long.parseLong(orgId))
//						.setParameter(2, loggedUser.getId()).setParameter(3, "%" + searchQuery + "%").getSingleResult();
			}

			// Step 5: Construct response
			if (studyList.isEmpty()) {
				LOG.info("StudyList is empty");
				response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
				response.put(StatusConstants.RESPONSE_MESSAGE, StatusConstants.EMPTY_RESULT);
				response.put("preferences", prefs);
				return logAndReturn(response, "Study list empty");
			}

			List<Map<String, Object>> resultList = prepareStudyListResponse(role, studyList,userId,orgId,showAll);
			int totalPages = (int) Math.ceil((double) totalCount / pageSize);
			int end = start + resultList.size();

			response.put("data", resultList);
			if(jsonRequest.get("pageNumber") != null && jsonRequest.get("pageSize") != null) {
			response.put("pageSize", pageSize);
			response.put("pageNumber", pageNumber);
			response.put("start", start + 1);
			response.put("end", end);
			response.put("totalCount", totalCount);
			response.put("totalPages", totalPages);
			}
			if(jsonRequest.get("startDate") != null && jsonRequest.get("endDate") != null)
			{
				response.put("totalCount", resultList.size());
			}
			response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
			response.put("preferences", prefs);
			return logAndReturn(response, "getStudyList success");

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Exception in getStudyList", e.getLocalizedMessage());
			return logAndReturn(
					commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR),
					"Exception occurred: " + e.getMessage());
		}
	}   
	
	private List<Map<String, Object>> prepareStudyListResponse(String role, List<StudyExtension> studyList,long userId,String orgId,String showAll) throws Exception {
	    List<Map<String, Object>> resultList = new ArrayList<>();
	    if(showAll.equalsIgnoreCase("true"))
	    	role = "ADMIN";
	    	List<String> studyIdsList = new ArrayList<String>();
	    for (StudyExtension studyExt : studyList) {
	    	studyIdsList.add(studyExt.getStudyInstanceUID());
	    }
	    Map<String,StudyDTO> studyDtoList = new HashMap<String, StudyDTO>();
	    List<JsonNode> studyNodes = getStudyDTOs(studyList);
	    System.out.println("studyNodes:"+studyNodes);
        if (!studyNodes.isEmpty()) {
        	for(JsonNode json : studyNodes) {
        	    StudyDTO study = DicomMapper.mapStudy(json);
        	    if (study == null) {
        	        System.out.println("Skipping unmappable study: " + json);
        	        continue;
        	    }
        	    studyDtoList.put(study.getStudyInstanceUID(), study);
        	}
        }
        
        System.out.println("studyDTOlist:"+studyDtoList);

	    for (StudyExtension studyExt : studyList) {
	        boolean isAdminOrTech = role.contains("ADMIN") ||
	                                role.equalsIgnoreCase(UserConstants.TECHNICIAN);
	        
        String studyId = studyExt.getStudyInstanceUID();
        StudyDTO study =studyDtoList.get(studyId);
        System.out.println("studyId:"+studyId);
        if(study == null) {
        	System.out.println("skipped studyId:"+studyId);
        	continue;
        }
        boolean userStudiesExists = false;
        	if(!showAll.equalsIgnoreCase("true"))
        		userStudiesExists = UserStudiesRepo.existsByStudyIdAndUserId(study.getStudyInstanceUID(), userId);
	        if (userStudiesExists || isAdminOrTech) {
	            Map<String, Object> studyMap = new HashMap<>();

	            // Check clinical details & comments
	            boolean clinicalDetailsComments = studyClinicalDetailsCommentsRepo.existsByStudyIdAndStatusOrderByCreatedTimeDesc(study.getStudyInstanceUID(), "active");

	            if (clinicalDetailsComments) {
					studyMap.put("clinicalDetailsAvailable", true);

				} else {
					studyMap.put("clinicalDetailsAvailable", false);

				}

	            // Basic study + patient info
	            studyMap.put(CommonConstants.ORG_ID, studyExt.getOrgId());
	            studyMap.put("study_desc", study.getStudyDescription());
	            studyMap.put("study_date", studyExt.getStudyDate());
	            studyMap.put("study_iuid", study.getStudyInstanceUID());
	            studyMap.put("pat_birthdate", getOrEmpty(study.getPatientBirthDate()));
	            studyMap.put("pat_sex", getOrEmpty(study.getPatientSex()));
	            studyMap.put("pat_name", getOrEmpty(study.getPatientName()));
	            studyMap.put("patient_weight", getOrEmpty(studyExt.getPatientWeight()));
	            studyMap.put("patient_height", getOrEmpty(studyExt.getPatientHeight()));
	            studyMap.put("status", getOrEmpty(studyExt.getStatus()));
	            studyMap.put("no_of_images", studyExt.getNoOfImages());
	            studyMap.put("dicom_images_count", studyExt.getDicomImagesCount());
	            studyMap.put("ai_process_status", studyExt.getAiProcessStatus());
	            studyMap.put("procedure_codes", new ArrayList<>());
	            studyMap.put("is_deleted", studyExt.getIsDeleted());
	            // First series details (if exists)
	            Map<String,Object> seriesMap = dicomWebClient.getFirstSeriesForStudy(studyId,studyExt.getOrgId());
	            if(seriesMap!=null) {
	            	JsonNode firstSeriesNode = (JsonNode) seriesMap.get("firstSeries");
	            	Integer seriesCount = Integer.valueOf(seriesMap.get("seriesCount").toString());
	            	SeriesDTO firstSeries = DicomMapper.mapSeries(firstSeriesNode, seriesCount);
	                studyMap.put("institution", getOrEmpty(firstSeries.getInstitutionName()));
	                studyMap.put("department", getOrEmpty(firstSeries.getDepartment()));
	                studyMap.put("pps_start_date", getOrEmpty(firstSeries.getPpsStartDate()));
	                studyMap.put("pps_end_date", getOrEmpty(firstSeries.getPpsStartTime()));
	                studyMap.put("physician_name", getOrEmpty(firstSeries.getPhysicianName()));
	                studyMap.put("station_name", getOrEmpty(firstSeries.getStationName()));
	                studyMap.put("modality", getOrEmpty(firstSeries.getModality()));
	                studyMap.put("number_of_series",getOrEmpty(study.getSeriesCount()) );
	            } else {
	                studyMap.put("institution", "");
	                studyMap.put("department", "");
	                studyMap.put("pps_start_date", "");
	                studyMap.put("pps_end_date", "");
	                studyMap.put("physician_name", "");
	                studyMap.put("station_name", "");
	                studyMap.put("modality", "");
	                studyMap.put("number_of_series", 0);
	            }

	            resultList.add(studyMap);
	        }
	    	
	    }

	    return resultList;
	}

	private Object getOrEmpty(Object obj) {
	    return obj != null ? obj : "";
	}
	
	
	private List<JsonNode> getStudyDTOs(List<StudyExtension> studyExtList) {

	    if (studyExtList == null || studyExtList.isEmpty()) {
	        return Collections.emptyList();
	    }

	    // STEP 1: Map orgId -> PACS URL
	    Map<Long, String> orgToPacsUrl = studyExtList.stream()
	            .map(StudyExtension::getOrgId)
	            .distinct()
	            .collect(Collectors.toMap(
	                    orgId -> orgId,
	                    orgId -> commonMethod.getPacsUrl(orgId.toString())
	            ));

	    Map<Long, String> orgToToken = studyExtList.stream()
	            .map(st -> Long.valueOf(st.getOrgId().toString())) // convert ANY type to Long
	            .distinct()
	            .collect(Collectors.toMap(
	                    orgId -> orgId,
	                    orgId -> {
							try {
								return pacsTokenService.getToken(orgId.toString());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								return null;
							}
						}
	                    
	            ));


	    // STEP 3: Create PACS URL -> TOKEN map
	    Map<String, String> pacsUrlToToken = new HashMap<>();
	    for (Long orgId : orgToPacsUrl.keySet()) {
	        pacsUrlToToken.put(orgToPacsUrl.get(orgId), orgToToken.get(orgId));
	    }

	    // STEP 4: Group studies by PACS URL
	    Map<String, List<String>> studiesByPacs = new HashMap<>();
	    for (StudyExtension study : studyExtList) {
	        String pacsUrl = orgToPacsUrl.get(study.getOrgId());
	        studiesByPacs.computeIfAbsent(pacsUrl, k -> new ArrayList<>())
	                     .add(study.getStudyInstanceUID());
	    }

	    System.out.println("Studies grouped by PACS URL: " + studiesByPacs);

	    // STEP 5: Fetch studies per PACS
	    List<JsonNode> allStudies = new ArrayList<>();

	    for (String pacsUrl : studiesByPacs.keySet()) {

	        String token = pacsUrlToToken.get(pacsUrl); // fetch token for this PACS
	        System.out.println("keycloak token-2-------------------"+token);
	        
	        try {
	            List<JsonNode> studyList =
	                    dicomWebClient.getStudyByUID(studiesByPacs.get(pacsUrl), pacsUrl, token);

	            allStudies.addAll(studyList);

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    return allStudies;
	}

	
	@Override
	@Transactional
	public ResponseEntity<JSONObject> updateStatus(JSONObject jsonRequest) {
	    LOG.info("Start of {}.updateStatus", this.getClass().getName());
	    LOG.debug("Request: {}", jsonRequest);

	    JSONObject response = new JSONObject();

	    try {
	    	ValidationResult validationResult = RequestValidator.validateRequestWithDetails(jsonRequest, CommonConstants.ACCESS_KEY,
	    			CommonConstants.STUDY_INSTANCE_UID,CommonConstants.STATUS);
	    	if(!validationResult.isValid()) {
	    		response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	            LOG.info("End of {}.updateStatus - {}", this.getClass().getName(), StatusConstants.BAD_REQUEST_CODE);
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	    	}
	    			

	        LOG.info("updateStatus: {}", jsonRequest);

            // Validating security token
            if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {
                long studyCount = studyExtensionRepo.countByStudyId(jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString());

                if (studyCount == 0) {
                    response = commonMethod.createResponse(StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE, StatusConstants.EMPTY_RESULT);
                    LOG.info("End of {}.updateStatus - {}", this.getClass().getName(), StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                } else {
                    
                	if(!updateStatusByStudyInstanceUID(jsonRequest.get(CommonConstants.STATUS).toString(), jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString())) {
                		LOG.info("Not updated any data");
                    	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                	}else {
	                    response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
	                    response.put(StatusConstants.STATUS_MESSAGE, "Updated Status");

	                    LOG.info("End of {}.updateStatus - {}", this.getClass().getName(), StatusConstants.SUCCESS);
	                    return ResponseEntity.status(HttpStatus.OK).body(response);
                    }
                }

            } else {
                response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
                LOG.info("End of {}.updateStatus - {}", this.getClass().getName(), StatusConstants.UNAUTHORIZED);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

	    } catch (Exception e) {
	        LOG.error("Exception: ", e);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        LOG.info("End of {}.updateStatus - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}
	
	@Override
	@Transactional
	public ResponseEntity<JSONObject> updateStudyPatientInfo(JSONObject jsonRequest) {
	    LOG.info("Start of {}.updateStudyPatientInfo", this.getClass().getName());
	    LOG.debug("Request: {}", jsonRequest);

	    JSONObject response = new JSONObject();

	    try {
	    	
	    	ValidationResult validationResult = RequestValidator.validateRequestWithDetails(jsonRequest, CommonConstants.ACCESS_KEY,
	    			CommonConstants.STUDY_INSTANCE_UID,CommonConstants.PATIENT_WEIGHT,CommonConstants.PATIENT_HEIGHT);
	    	if(!validationResult.isValid()) {
	    		response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	            LOG.info("End of {}.updateStudyPatientInfo - {}", this.getClass().getName(), StatusConstants.BAD_REQUEST_CODE);
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	    	}

            // Validating security token
            if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {
                long studyCount = studyExtensionRepo.countByStudyId(jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString());

                if (studyCount <= 0) {
                    response = commonMethod.createResponse(StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE, StatusConstants.EMPTY_RESULT);
                    LOG.info("End of {}.updateStudyPatientInfo - {}", this.getClass().getName(), StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                } else {
        			int maxRetries = 3;
        			int attempts = 0;
        			boolean updatedSuccessfully = false;
        	
        			while (attempts < maxRetries && !updatedSuccessfully) {
        			    attempts++;
        			    Optional<Long> lockVersion = studyExtensionRepo.findLockVersionByStudyId(jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString());
                		if(lockVersion.isEmpty()) {
                			response = commonMethod.createResponse(StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE, StatusConstants.EMPTY_RESULT);
                    	    LOG.info("End of {}.updateStudyPatientInfo - {}", this.getClass().getName(), StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
                    	    return ResponseEntity.status(HttpStatus.OK).body(response);
                		}
        			    int updated = studyExtensionRepo.updatePatientInfo(
                    	        jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(),
                    	        jsonRequest.get(CommonConstants.PATIENT_HEIGHT).toString(),
                    	        jsonRequest.get(CommonConstants.PATIENT_WEIGHT).toString(),
                    	        lockVersion.get()
                    	);
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
        			    LOG.info("End of " + this.getClass().getName() + ".updateStudyPatientInfo "
        			             + StatusConstants.OPERATION_FAILED);
        			    response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
        		        LOG.info("End of {}.updateStudyPatientInfo - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
        		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        			}else {
        				response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
                	    response.put(StatusConstants.STATUS_MESSAGE, "Updated Patient Info");
                	    LOG.info("End of {}.updateStudyPatientInfo - {}", this.getClass().getName(), StatusConstants.SUCCESS);
                	    return ResponseEntity.status(HttpStatus.OK).body(response);
        			}
            		
                }
            } else {
                response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
                LOG.info("End of {}.updateStudyPatientInfo - {}", this.getClass().getName(), StatusConstants.UNAUTHORIZED);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

	    } catch (Exception e) {
	        LOG.error("Exception: ", e);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        LOG.info("End of {}.updateStudyPatientInfo - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}
	
	@Override
	@Transactional
	public ResponseEntity<JSONObject> saveStudyVolumeInfo(JSONObject jsonRequest) {
	    LOG.info("Start of {}.saveStudyVolumeInfo", this.getClass().getName());
	    LOG.debug("Request: {}", jsonRequest);

	    JSONObject response = new JSONObject();

	    try {
	    	
	    	ValidationResult validationResults = RequestValidator.validateRequestWithDetails(
	                jsonRequest,CommonConstants.ACCESS_KEY,CommonConstants.STUDY_INSTANCE_UID,CommonConstants.INFO);

	        if (!validationResults.isValid()) {
	            return ResponseEntity.badRequest()
	                    .body(commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST));
	        }
	     // Security token validation
	        if (!CommonConstants.SECURITY_TOKEN.equals(jsonRequest.get(CommonConstants.ACCESS_KEY).toString())) {
	            response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
	            LOG.info("End of {}.saveStudyVolumeInfo - {}", this.getClass().getName(), StatusConstants.UNAUTHORIZED);
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	        }

	        // Extract info array safely
	        List<Object> volInfo = Optional.ofNullable((List<Object>) jsonRequest.get(CommonConstants.INFO))
	                                       .orElse(Collections.emptyList());

	        String bookmarkIdStr = Optional.ofNullable(jsonRequest.get(CommonConstants.BOOKMARK_ID))
	                                       .map(Object::toString)
	                                       .filter(s -> !s.isEmpty())
	                                       .orElse(null);

	        Bookmarks bookmark = bookmarkService.getBookmark(
	                jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(),
	                bookmarkIdStr,
	                true
	        );

	        Long bookmarkId = bookmark.getId();
	        long count = studyVolumeInfoRepo.countByBookmarkId(bookmarkId);

	        if (count > 0) {
	            int updated = studyVolumeInfoRepo.updateByBookmarkId(
	                    bookmarkId,
	                    jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(),
	                    volInfo,
	                    bookmark.getVersion()
	            );
	            LOG.info("Updated {} StudyVolumeInfo records for bookmarkId={}", updated, bookmarkId);
	        } else {
	            StudyVolumeInfo svi = new StudyVolumeInfo();
	            svi.setBookmark(bookmark);
	            svi.setStudyId(jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString());
	            svi.setEndVolume(volInfo);
	            svi.setVersion(bookmark.getVersion());
	            studyVolumeInfoRepo.save(svi);
	            LOG.info("Inserted new StudyVolumeInfo for bookmarkId={}", bookmarkId);
	        }

	        response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
	        response.put(StatusConstants.STATUS_MESSAGE, "Saved Successfully");
	        LOG.info("End of {}.saveStudyVolumeInfo - {}", this.getClass().getName(), StatusConstants.SUCCESS);
	        return ResponseEntity.ok(response);
	    } catch (Exception e) {
	        LOG.error("Exception: ", e);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        LOG.info("End of {}.saveStudyVolumeInfo - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}

	@Override
	@Transactional
	public ResponseEntity<JSONObject> updateClassification(JSONObject jsonRequest) {
	    LOG.info("Start of {}.updateClassification", this.getClass().getName());
	    LOG.debug("Request: {}", jsonRequest);

	    JSONObject response = new JSONObject();

	    try {
	    	
	    	ValidationResult validationResult = RequestValidator.validateRequestWithDetails(jsonRequest,CommonConstants.ACCESS_KEY,
	    			CommonConstants.STUDY_INSTANCE_UID,CommonConstants.TAG_INFO,CommonConstants.REPROCESS);
	    	if(!validationResult.isValid()) {
	    		response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	            LOG.info("End of {}.updateClassification - {}", this.getClass().getName(), StatusConstants.BAD_REQUEST_CODE);
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	    	}

            // Validating security token
            if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

                JSONParser jsonParser = new JSONParser();
                jsonRequest = (JSONObject) jsonParser.parse(jsonRequest.toString());
                JSONArray array = (JSONArray) jsonRequest.get(CommonConstants.TAG_INFO);

                if (array.size() == 0) {
                    response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
                    LOG.info("End of {}.updateClassification - {}", this.getClass().getName(), StatusConstants.BAD_REQUEST_CODE);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

                for (int j = 0; j < array.size(); j++) {
                    JSONObject jsonObj = (JSONObject) array.get(j);

                    // Getting record based on seriesId && studyId
                    if (jsonRequest.get("saveToDb").toString().equalsIgnoreCase("yes")) {

//	                        List<StudyClassification> smObject = em.createNamedQuery(StudyClassification.STUDY_CLASSIFICATION_BY_SERIES_ID)
//	                                .setParameter(CommonConstants.SERIES_ID, jsonObj.get(CommonConstants.SERIES_INSTANCE_UID).toString())
//	                                .getResultList();
                        
                        List<StudyClassification> smObject = studyClassificationRepo.findBySeriesId(jsonObj.get(CommonConstants.SERIES_INSTANCE_UID).toString());
                        		
                        if (!smObject.isEmpty()) {
                            for (StudyClassification sc : smObject) {
                                //em.remove(sc);
                            	studyClassificationRepo.delete(sc);
                            }
                        }

                        StudyClassification scObj = new StudyClassification();
                        scObj.setLastUpdatedDt(new Date());
                        scObj.setCreatedDt(new Date());
                        scObj.setSeriesId(jsonObj.get(CommonConstants.SERIES_INSTANCE_UID).toString());
                        scObj.setSequenceType(jsonObj.get(CommonConstants.SEQUENCE_TYPE).toString());
                        scObj.setImagePlane(jsonObj.get(CommonConstants.IMAGE_PLANE).toString());
                        scObj.setStudyId(jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString());
                        //em.persist(scObj);
                        studyClassificationRepo.save(scObj);
                    }
                }

                LOG.info("Updated records");

                if (jsonRequest.get("sendToAI").toString().equalsIgnoreCase("yes")) {

                    String aiUrl = aiStudyUrl + "save_new_tags";

                    // Dev AI Url
                    // String aiUrl = "http://192.168.0.40:5020/save_new_tags";

                    // QA AI Url
                    // String aiUrl = "http://192.168.2.39:5021/save_new_tags";
                    LOG.info("Hitting AI API");

                    LOG.info("aiUrl:::::::::::" + aiUrl);

                    if (activemqSwitch.equalsIgnoreCase("true")) {

                        LOG.info("Running in queue async cl ack");

                        JSONObject request = new JSONObject();
                        request.put("url", "alert");

                        queueProducer.addToQueue(request);
                        LOG.info("Queuing API hit");
                    }

                    // Use RestTemplate to make the API call
                    RestTemplate restTemplate = new RestTemplate();
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<String> entity = new HttpEntity<>(jsonRequest.toString(), headers);

                    ResponseEntity<String> clientResponse = restTemplate.exchange(aiUrl, HttpMethod.POST, entity, String.class);
                    LOG.debug("status code===>{}", clientResponse.getStatusCode());

                    if (clientResponse.getStatusCode() != HttpStatus.OK) {
                        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
                        throw new RuntimeException("Failed: HTTP error code: " + clientResponse.getStatusCode());
                    }

                    String output = clientResponse.getBody();
                    JSONParser parserObj = new JSONParser();
                    JSONObject json = (JSONObject) parserObj.parse(output);

                    if (json.get(CommonConstants.STATUS_CODE) != null && json.get(CommonConstants.STATUS_CODE).toString().equals("1000")) {
                        if (jsonRequest.get(CommonConstants.REPROCESS).toString().equalsIgnoreCase("yes")) {
                            LOG.info("Hitting reprocess API");

                            JSONObject resp = reprocessAI(jsonRequest);
                            LOG.info("resp: {}", resp);

                            if (resp.get(CommonConstants.STATUS) != null && resp.get(CommonConstants.STATUS).toString().equals("1000")) {
                                response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, json.get("status").toString());
                                LOG.info("End of {}.updateClassification - {}", this.getClass().getName(), StatusConstants.SUCCESS);
                                return ResponseEntity.status(HttpStatus.OK).body(response);
                            } else {
                                response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, json.get("status").toString());
                                LOG.info("End of {}.updateClassification - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                            }
                        }
                    }
                }

                LOG.info("updateClassification API End");

                response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SAVED);
                LOG.info("End of {}.updateClassification - {}", this.getClass().getName(), StatusConstants.SUCCESS);
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {

                response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
                LOG.info("End of {}.updateClassification - {}", this.getClass().getName(), StatusConstants.UNAUTHORIZED);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
	        
	    } catch (Exception e) {
	        LOG.error("Exception: ", e);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        LOG.info("End of {}.updateClassification - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}


	@Override
	@Transactional
	public ResponseEntity<JSONObject> saveRadialStrain(JSONObject jsonRequest) {
	    LOG.info("Start of {}.saveRadialStrain", this.getClass().getName());
	    LOG.debug("Request: {}", jsonRequest);

	    JSONObject response = new JSONObject();

	    try {
	        if (jsonRequest != null && jsonRequest.get(CommonConstants.ACCESS_KEY) != null
	                && !jsonRequest.get(CommonConstants.ACCESS_KEY).toString().isEmpty()
	                && jsonRequest.get(CommonConstants.STUDY_ID) != null
	                && !jsonRequest.get(CommonConstants.STUDY_ID).toString().isEmpty()) {

	            // Validating security token
	            if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

	                String radialStrain = null;
	                String graph = null;

	                // Processing radial strain
	                if (jsonRequest.containsKey(CommonConstants.RADIAL_STRAIN) && jsonRequest.get(CommonConstants.RADIAL_STRAIN) != null
	                        && !jsonRequest.get(CommonConstants.RADIAL_STRAIN).toString().isEmpty()) {
	                    HashMap<String, String> radialStrainMap = (HashMap<String, String>) jsonRequest.get(CommonConstants.RADIAL_STRAIN);
	                    JSONObject radialStrainJson = new JSONObject(radialStrainMap);
	                    radialStrain = radialStrainJson.toJSONString();
	                }

	                // Processing graph
	                if (jsonRequest.containsKey(CommonConstants.GRAPH) && jsonRequest.get(CommonConstants.GRAPH) != null
	                        && !jsonRequest.get(CommonConstants.GRAPH).toString().isEmpty()) {
	                    HashMap<String, String> graphObjMap = (HashMap<String, String>) jsonRequest.get(CommonConstants.GRAPH);
	                    JSONObject graphObj = new JSONObject(graphObjMap);
	                    graph = graphObj.toJSONString();
	                }
	                
	                String computedSeriesIds = "";
					if(jsonRequest.containsKey(CommonConstants.COMPUTED_SERIES) && jsonRequest.get(CommonConstants.COMPUTED_SERIES)!=null) {
						HashMap computedSeries = (HashMap) jsonRequest.get(CommonConstants.COMPUTED_SERIES);
						JSONObject computedJson = new JSONObject(computedSeries);
						computedSeriesIds = computedJson.toString(); // serialize to string
						System.out.println("Got computedSeriesds");

					}

	                StudyParameter sp = null;
	                List<StudyParameter> smObject = null;

	                String bookmarkId = jsonRequest.containsKey(CommonConstants.BOOKMARK_ID) ?
	                        jsonRequest.get(CommonConstants.BOOKMARK_ID) != null && !jsonRequest.get(CommonConstants.BOOKMARK_ID).toString().isEmpty() ?
	                                (String) jsonRequest.get(CommonConstants.BOOKMARK_ID) : null : null;

	                // Getting bookmark details
	                Bookmarks bookmark = bookmarkService.getBookmark(jsonRequest.get(CommonConstants.STUDY_ID).toString(),
	                        bookmarkId,
	                        true);

	                // Getting record based on bookmark ID
//	                smObject = em.createNamedQuery(StudyParameter.FIND_BY_BOOKMARK_ID)
//	                        .setParameter(CommonConstants.BOOKMARK_ID, bookmark.getId())
//	                        .getResultList();
	                
	                smObject = studyParameterRepo.findByBookmarkId(bookmark.getId());

	                if (smObject != null && !smObject.isEmpty()) {
	                    for (StudyParameter spObj : smObject) {
	                        spObj.setUpdatedTime(new Date());
	                        if (radialStrain != null) {
	                            spObj.setRadialStrainJson(radialStrain);
	                        }
	                        if (graph != null) {
	                            spObj.setGraph(graph);
	                        }
	                        spObj.setComputedSeries(computedSeriesIds);
	                        //em.merge(spObj);
	                        studyParameterRepo.save(spObj);
	                    }
	                } else {
	                    sp = new StudyParameter();
	                    sp.setCreatedTime(new Date());
	                    sp.setUpdatedTime(new Date());
	                    sp.setStudyId(jsonRequest.get(CommonConstants.STUDY_ID).toString());
	                    if (radialStrain != null) {
	                        sp.setRadialStrainJson(radialStrain);
	                    }
	                    if (graph != null) {
	                        sp.setGraph(graph);
	                    }
	                    sp.setBookmark(bookmark);
	                    sp.setVersion(bookmark.getVersion());
	                    sp.setComputedSeries(computedSeriesIds);
	                    //em.persist(sp);
	                    studyParameterRepo.save(sp);
	                }

	                LOG.info("saveRadialStrain API End");

	                response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SAVED);
	                LOG.info("End of {}.saveRadialStrain - {}", this.getClass().getName(), StatusConstants.SUCCESS);
	                return ResponseEntity.status(HttpStatus.OK).body(response);

	            } else {
	                response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
	                LOG.info("End of {}.saveRadialStrain - {}", this.getClass().getName(), StatusConstants.UNAUTHORIZED);
	                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	            }

	        } else {
	            response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	            LOG.info("End of {}.saveRadialStrain - {}", this.getClass().getName(), StatusConstants.BAD_REQUEST_CODE);
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	        }
	    } catch (Exception e) {
	        LOG.error("Exception: ", e);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        LOG.info("End of {}.saveRadialStrain - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}

	
	@Override
	public JSONObject saveAIProcessStatus(JSONObject jsonRequest) {
	    LOG.info("Start of {}.saveAIProcessStatus", this.getClass().getName());
	    LOG.info("saveAIProcessStatus Request: {}", jsonRequest);

	    JSONObject response = new JSONObject();

	    try {
	        if (jsonRequest == null || jsonRequest.get(CommonConstants.ACCESS_KEY) == null ||
	            jsonRequest.get(CommonConstants.STUDY_ID) == null || jsonRequest.get(CommonConstants.STATUS) == null) {
	            
	            response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	            return response;
	        }

	        String accessKey = jsonRequest.get(CommonConstants.ACCESS_KEY).toString();
	        String studyId = jsonRequest.get(CommonConstants.STUDY_ID).toString();

	        if (!CommonConstants.SECURITY_TOKEN.equals(accessKey)) {
	            response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
	            return response;
	        }

	        // Assuming you have StudyRepository with method findByStudyInstanceUID
	        long optionalStudy = studyExtensionRepo.countByStudyId(studyId);

	        if (optionalStudy <=0) {
	            response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	            return response;
	        }

	        @SuppressWarnings("unchecked")
	        HashMap<String, Object> newStatus = (HashMap<String, Object>) jsonRequest.get(CommonConstants.STATUS);
	        
	        Map<String,Object> existingStatus = studyExtensionRepo.findAIProcessStatusByStudyInstanceUID(studyId)
	        		.map(HashMap::new)
	        		.orElseGet(HashMap::new);

	        existingStatus.putAll(newStatus);
	        LOG.info("saveAIProcessStatus newStatus: {}", newStatus);
	        if (!updateAiStatus(studyId,existingStatus,new Date())) {
	            return commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	        }
	        response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SAVED);
	        LOG.info("Successfully saved AI process status for Study ID: {}", studyId);
	        return response;

	    } catch (Exception e) {
	        LOG.error("Exception while saving AI process status", e);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        return response;
	    }
	}
	
	private boolean updateAiStatus(String studyId, Map<String,Object> existingStatus,Date date) {
		int maxRetries = 3;
		int attempts = 0;
		boolean updatedSuccessfully = false;

		while (attempts < maxRetries && !updatedSuccessfully) {
		    attempts++;
		    Optional<Long> lockVersion = studyExtensionRepo.findLockVersionByStudyId(studyId);
			if(lockVersion.isEmpty()) {
				LOG.info("No row exists to update");
				return true;
			}
		    int updated = studyExtensionRepo.updateAiStatus(studyId, existingStatus, date, lockVersion.get());
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
		    LOG.info("End of " + this.getClass().getName() + ".updateAiStatus "
		             + StatusConstants.OPERATION_FAILED);
		}
		return updatedSuccessfully;
		
	}
	
	@Override
	public JSONObject getAIProcessStatus(JSONObject jsonRequest) {
	    LOG.info("Start of {}.getAIProcessStatus", this.getClass().getSimpleName());
	    JSONObject response = new JSONObject();

	    try {
	    	ValidationResult validationResults = RequestValidator.validateRequestWithDetails(
	                jsonRequest,CommonConstants.ACCESS_KEY,CommonConstants.STUDY_ID);

	        if (!validationResults.isValid()) {
	        	response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	            LOG.info("End of {}.getAIProcessStatus - {}", this.getClass().getSimpleName(), StatusConstants.BAD_REQUEST_CODE);
	            return response;
	        }

	        String accessKey = jsonRequest.get(CommonConstants.ACCESS_KEY).toString();
	        String studyId = jsonRequest.get(CommonConstants.STUDY_ID).toString();

	        if (!CommonConstants.SECURITY_TOKEN.equals(accessKey)) {
	            response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
	            LOG.info("End of {}.getAIProcessStatus - {}", this.getClass().getSimpleName(), StatusConstants.UNAUTHORIZED);
	            return response;
	        }

	        Optional<Map<String, Object>> aiProcessStatus = studyExtensionRepo.findAIProcessStatusByStudyInstanceUID(studyId);
	        if (aiProcessStatus.isPresent()) {
	            response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SUCCESS);
	            response.put("aiProcessStatus", aiProcessStatus.isPresent() ? aiProcessStatus.get() : new HashMap<>());
	            LOG.info("End of {}.getAIProcessStatus - {}", this.getClass().getSimpleName(), StatusConstants.SUCCESS);
	            return response;
	        } else {
	            response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	            LOG.info("End of {}.getAIProcessStatus - Study Not Found", this.getClass().getSimpleName());
	            return response;
	        }

	    } catch (Exception e) {
	        LOG.error("Exception in getAIProcessStatus: ", e);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        LOG.info("End of {}.getAIProcessStatus - {}", this.getClass().getSimpleName(), StatusConstants.OPERATION_FAILED);
	        return response;
	    }
	}
	
	@Override
	public JSONObject reprocessAI(JSONObject jsonRequest) {
	    LOG.info("Start of {}.reprocessAI", this.getClass().getSimpleName());
	    LOG.debug("Request : {}", jsonRequest);
	    JSONObject response = new JSONObject();

	    try {
	        String aiUrlBase = aiStudyUrl;
	        if (aiUrlBase == null || aiUrlBase.isEmpty()) {
	            LOG.error("AI_STUDY_URL not configured in environment");
	            return commonMethod.createResponse(StatusConstants.OPERATION_FAILED, "AI URL not configured");
	        }

	        if (CommonConstants.SECURITY_TOKEN.equalsIgnoreCase("on")) {
	            JSONObject request = new JSONObject();
	            request.put("url", "alert");

	            try {
	            	queueProducer.addToQueue(request);
	                LOG.info("Queued alert to ActiveMQ");
	            } catch (Exception e) {
	                LOG.error("Failed to queue to ActiveMQ: {}", e.getMessage());
	            }
	        }

	        // Construct full AI URL
	        String studyId = jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString();
	        String orgId = jsonRequest.get(CommonConstants.ORG_ID).toString();
	        String userId = jsonRequest.get(CommonConstants.USER_ID).toString();

	        String fullUrl = String.format("%s%s/%s/%s/no", aiUrlBase, studyId, orgId, userId);
	        LOG.info("Final AI URL: {}", fullUrl);

	        // Call AI Service
	        ResponseEntity<String> apiResponse = restTemplate.exchange(
	                fullUrl,
	                HttpMethod.GET,
	                null,
	                String.class
	        );

	        if (apiResponse.getStatusCode() == HttpStatus.OK) {
	            JSONParser parser = new JSONParser();
	            response = (JSONObject) parser.parse(apiResponse.getBody());
	            LOG.info("AI API Response: {}", response);
	        } else {
	            LOG.error("AI API Error: HTTP {}", apiResponse.getStatusCode());
	            response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        }

	    } catch (Exception e) {
	        LOG.error("Exception in reprocessAI: {}", e.getMessage(), e);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	    }

	    LOG.info("End of {}.reprocessAI", this.getClass().getSimpleName());
	    return response;
	}


	
	@Override
	@Transactional
	public ResponseEntity<JSONObject> updateTags(JSONObject jsonRequest) {
	    LOG.info("Start of {}.updateTags", this.getClass().getName());
	    LOG.debug("Request: {}", jsonRequest);

	    JSONObject response = new JSONObject();

	    try {
//	        if (jwtToken == null || jwtToken.isEmpty()) {
//	            response = commonMethod.createResponse(UserConstants.MISSING_JWT_TOKKEN, UserConstants.BAD_REQUEST_CODE);
//	            LOG.info("End of {}.updateTags - {}", this.getClass().getName(), UserConstants.MISSING_JWT_TOKKEN);
//	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//	        }
//
//	        Map<String, Object> claims = jwtUtil.decodeJWT(jwtToken);
//	        User loggedUser = userDao.findById(Long.parseLong(claims.get(UserConstants.ID).toString()));
//
//	        if (loggedUser == null) {
//	            response = commonMethod.createResponse(UserConstants.INVALID_USERID, UserConstants.UNAUTHORIZED);
//	            LOG.info("End of {}.updateTags - {}", this.getClass().getName(), UserConstants.INVALID_USERID);
//	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
//	        }
//
//	        // Check if JWT token matches
//	        if (loggedUser.getJwtToken() == null || !loggedUser.getJwtToken().equals(jwtToken)) {
//	            response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
//	            LOG.info("End of {}.updateTags - {}", this.getClass().getName(), StatusConstants.UNAUTHORIZED);
//	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
//	        }

	        if (jsonRequest != null && jsonRequest.get(CommonConstants.ACCESS_KEY) != null
	                && !jsonRequest.get(CommonConstants.ACCESS_KEY).toString().isEmpty()
	                && jsonRequest.get(CommonConstants.SA_TAGS) != null
	                && !jsonRequest.get(CommonConstants.SA_TAGS).toString().isEmpty()
	                && jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID) != null
	                && !jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString().isEmpty()
	                && jsonRequest.get(CommonConstants.ORG_ID) != null
	                && !jsonRequest.get(CommonConstants.ORG_ID).toString().isEmpty()
	                && jsonRequest.get(CommonConstants.USER_ID) != null
	                && !jsonRequest.get(CommonConstants.USER_ID).toString().isEmpty()
	                && jsonRequest.get(CommonConstants.QFLOW_TAGS) != null
	                && !jsonRequest.get(CommonConstants.QFLOW_TAGS).toString().isEmpty()) {

	            String aiUrl = aiStudyUrl + "save_new_tags";

	            LOG.info("Hitting AI API");
	            LOG.info("aiUrl:::::::::::" + aiUrl);

	            if (activemqSwitch.equalsIgnoreCase("true")) {
	                LOG.info("Running in queue async cl ack");

	                JSONObject request = new JSONObject();
	                request.put("url", "alert");

	                queueProducer.addToQueue(request);
	                LOG.info("Queuing API hit");
	            }

	            // Use RestTemplate to make the API call
	            RestTemplate restTemplate = new RestTemplate();
	            HttpHeaders headers = new HttpHeaders();
	            headers.setContentType(MediaType.APPLICATION_JSON);
	            HttpEntity<String> entity = new HttpEntity<>(jsonRequest.toString(), headers);

	            ResponseEntity<String> clientResponse = restTemplate.exchange(aiUrl, HttpMethod.POST, entity, String.class);
	            LOG.debug("Status code: {}", clientResponse.getStatusCode());

	            if (clientResponse.getStatusCode() != HttpStatus.OK) {
	                response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	                throw new RuntimeException("Failed: HTTP error code: " + clientResponse.getStatusCode());
	            }

	            String output = clientResponse.getBody();
	            JSONParser parserObj = new JSONParser();
	            JSONObject json = (JSONObject) parserObj.parse(output);

	            LOG.info("json:::" + json);
	            LOG.info("json:::" + json.get(CommonConstants.STATUS_CODE).toString());

	            if (json.get(CommonConstants.STATUS_CODE) != null && json.get(CommonConstants.STATUS_CODE).toString().equals("1000")) {

	                LOG.info("Hitting reprocess API");

	                JSONObject resp = reprocessAI(jsonRequest);
	                LOG.info("resp: {}", resp);

	                if (resp.get(CommonConstants.STATUS) != null && resp.get(CommonConstants.STATUS).toString().equals("1000")) {
	                    response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, json.get("status").toString());
	                    LOG.info("End of {}.updateTags - {}", this.getClass().getName(), StatusConstants.SUCCESS);
	                    return ResponseEntity.status(HttpStatus.OK).body(response);
	                } else {
	                    response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, json.get("status").toString());
	                    LOG.info("End of {}.updateTags - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
	                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	                }

	            } else {
	                response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, json.get("status").toString());
	                LOG.info("End of {}.updateTags - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
	                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	            }

	        } else {
	            response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	            LOG.info("End of {}.updateTags - {}", this.getClass().getName(), StatusConstants.BAD_REQUEST_CODE);
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	        }

	    } catch (Exception e) {
	        LOG.error("Exception: ", e);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        LOG.info("End of {}.updateTags - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}
	
	@Override
	public ResponseEntity<JSONObject> getEndVolumeInfo(JSONObject jsonRequest) {
	    LOG.info("Start of {}.getEndVolumeInfo", this.getClass().getName());
	    LOG.debug("Request: {}", jsonRequest);

	    JSONObject response = new JSONObject();

	    try {
	    	
	    	ValidationResult validationResults = RequestValidator.validateRequestWithDetails(
	                jsonRequest,CommonConstants.ACCESS_KEY,CommonConstants.STUDY_ID);

	        if (!validationResults.isValid()) {
	        	 return ResponseEntity.badRequest()
		                    .body(commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST));
	        }

            // Validating security token
            if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {
                Optional<List<Object>> studyVolumeList = null;

                String bookmarkId = jsonRequest.containsKey(CommonConstants.BOOKMARK_ID) ?
                        jsonRequest.get(CommonConstants.BOOKMARK_ID) != null && !jsonRequest.get(CommonConstants.BOOKMARK_ID).toString().isEmpty() ?
                                (String) jsonRequest.get(CommonConstants.BOOKMARK_ID) : null : null;

                // Getting bookmark details
                Long bookmark = bookmarkService.getBookmarkId(jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(),
                        bookmarkId,
                        false);
                List<PatientHeightWeightProjection> patientHeightWeight = studyExtensionRepo.findHeightWeightByStudyId(jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString());
                studyVolumeList = studyVolumeInfoRepo.findEndVolumeByBookmarkId(bookmark);

                if (patientHeightWeight.isEmpty() || studyVolumeList.isEmpty()) {
                    response = commonMethod.createResponse(StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE, StatusConstants.EMPTY_RESULT);
                    LOG.info("End of {}.getEndVolumeInfo - {}", this.getClass().getName(), StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                } else {
                    List<Object> studyVolumeInfo = studyVolumeList.get();

                    response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
                    response.put(CommonConstants.PATIENT_HEIGHT, patientHeightWeight.get(0).getPatientHeight());
                    response.put(CommonConstants.PATIENT_WEIGHT, patientHeightWeight.get(0).getPatientWeight());
                    response.put("end_volume_info", studyVolumeInfo.get(0));

                    LOG.info("End of {}.getEndVolumeInfo - {}", this.getClass().getName(), StatusConstants.SUCCESS);
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }

            } else {
                response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
                LOG.info("End of {}.getEndVolumeInfo - {}", this.getClass().getName(), StatusConstants.UNAUTHORIZED);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
	    } catch (Exception e) {
	        LOG.error("Exception: ", e);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        LOG.info("End of {}.getEndVolumeInfo - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}

	
	//save to somewhere
	private void writeFile(byte[] content, String filename) throws IOException {

	    File file = new File(filename);

	    if (!file.exists() && !file.createNewFile()) {
	        throw new IOException("Failed to create file: " + filename);
	    }

	    try (FileOutputStream fop = new FileOutputStream(file)) {
	        fop.write(content);
	        fop.flush();
	    }
	}
	
	@Transactional
	@Override 
	public JSONObject upload(MultipartFile[] files, String studyId) {
	    JSONObject response = new JSONObject();
	    try {
	        if (files == null || files.length == 0) {
	            return commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, "No file uploaded.");
	        }

	        for (MultipartFile file : files) {
	            String fileName = file.getOriginalFilename();
	            if (fileName == null || fileName.isBlank()) {
	                return commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, "Invalid filename.");
	            }

	            List<StudyClinicalDetails> existingFiles =
	                studyClinicalDetailsRepo.findByStudyIdAndFileNameAndStatus(studyId, fileName, "active");

	            if (!existingFiles.isEmpty()) {
	                return commonMethod.createResponse(StatusConstants.OPERATION_FAILED, "File already exists");
	            }

	            String fileLocation = uploadRetriveDir + studyId + "/" + fileName;
	            String fullPath = uploadStorageDir + studyId + "/" + fileName;

	            File dir = new File(uploadStorageDir + studyId);
	            if (!dir.exists()) dir.mkdirs();

	            // Save file bytes
	            byte[] bytes = file.getBytes();
	            writeFile(bytes, fullPath);

	            // Save DB record
	            StudyClinicalDetails scd = new StudyClinicalDetails();
	            scd.setFileLocation(fileLocation);
	            scd.setFileName(fileName);
	            scd.setStatus("active");
	            scd.setStudyId(studyId);
	            scd.setCreatedTime(new Date());
	            studyClinicalDetailsRepo.save(scd);

	            LOG.debug("Saved file metadata for " + fileName);
	        }

	        response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SAVED);
	        LOG.info("End of upload: " + StatusConstants.SUCCESS_CODE);
	        return response;

	    } catch (Exception e) {
	        LOG.error("Upload Exception", e);
	        return commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	    }
	}

	@Override
	public JSONObject getClinicalDetailsFiles(JSONObject jsonRequest) {
	    LOG.info("Start of " + this.getClass().getName() + ".getClinicalDetailsFiles");
	    JSONObject response = new JSONObject();

	    try {
	        // Basic input validations
	        if (jsonRequest == null ||
	            jsonRequest.get(CommonConstants.ACCESS_KEY) == null ||
	            jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID) == null) {
	            return commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	        }

	        String accessKey = jsonRequest.get(CommonConstants.ACCESS_KEY).toString();
	        String studyInstanceUID = jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString();

	        if (!CommonConstants.SECURITY_TOKEN.equals(accessKey)) {
	            return commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
	        }
	        
	        List<StudyClinicalDetails> scdList = studyClinicalDetailsRepo.findByStudyIdAndStatusOrderByCreatedTimeDesc(studyInstanceUID,"active");

	        if (scdList.isEmpty()) {
	            return commonMethod.createResponse(StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE, StatusConstants.EMPTY_RESULT);
	        }

	        List<Map<String, Object>> resultList = new ArrayList<>();

	        for (StudyClinicalDetails scd : scdList) {
	            Map<String, Object> fileData = new HashMap<>();
	            fileData.put("id", scd.getId());
	            fileData.put("url", uploadRetriveDir + scd.getStudyId() + "/" + scd.getFileName());
	            fileData.put("fileName", scd.getFileName());
	            fileData.put("studyId", scd.getStudyId());
	            fileData.put("createdTime", scd.getCreatedTime());
	            resultList.add(fileData);
	        }

	        response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
	        response.put(CommonConstants.DATA, resultList);
	        LOG.info("End of getClinicalDetailsFiles - SUCCESS");
	        return response;

	    } catch (Exception e) {
	        LOG.error("Exception in getClinicalDetailsFiles", e);
	        return commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	    }
	}


	@Override
	public JSONObject saveClinicalDetailsComments(JSONObject jsonRequest) {
	    LOG.info("Start of " + this.getClass().getName() + ".saveClinicalDetailsComments");

	    JSONObject response = new JSONObject();
	    try {

	        // Input validation
	        if (jsonRequest == null ||
	            !jsonRequest.containsKey(CommonConstants.ACCESS_KEY) ||
	            !jsonRequest.containsKey(CommonConstants.STUDY_INSTANCE_UID) ||
	            !jsonRequest.containsKey(CommonConstants.COMMENT) ||
	            jsonRequest.get(CommonConstants.ACCESS_KEY).toString().isBlank() ||
	            jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString().isBlank() ||
	            jsonRequest.get(CommonConstants.COMMENT).toString().isBlank()) {

	            return commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	        }

	        // Security token check
	        if (!CommonConstants.SECURITY_TOKEN.equals(jsonRequest.get(CommonConstants.ACCESS_KEY).toString())) {
	            return commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
	        }

	        // Save comment
	        StudyClinicalDetailsComments scdc = new StudyClinicalDetailsComments();
	        scdc.setStatus("active");
	        scdc.setStudyId(jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString());
	        scdc.setComment(jsonRequest.get(CommonConstants.COMMENT).toString());
	        scdc.setCreatedTime(new Date());

	        //em.persist(scdc);
	        
	        studyClinicalDetailsCommentsRepo.save(scdc);

	        response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
	        response.put(CommonConstants.DATA, "Saved successfully");
	        LOG.info("End of " + this.getClass().getName() + ".saveClinicalDetailsComments - SUCCESS");
	        return response;

	    } catch (Exception e) {
	        LOG.error("Exception in saveClinicalDetailsComments", e);
	        return commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	    }
	}
	
	@Override
	public JSONObject getClinicalDetailsComments(JSONObject jsonRequest) {
	    LOG.info("Start of " + this.getClass().getName() + ".getClinicalDetailsComments");

	    JSONObject response = new JSONObject();

	    try {
	        // Validate input
	        if (jsonRequest == null ||
	            !jsonRequest.containsKey(CommonConstants.ACCESS_KEY) ||
	            !jsonRequest.containsKey(CommonConstants.STUDY_INSTANCE_UID) ||
	            jsonRequest.get(CommonConstants.ACCESS_KEY).toString().isBlank() ||
	            jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString().isBlank()) {

	            return commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	        }

	        // Validate access key
	        if (!CommonConstants.SECURITY_TOKEN.equals(jsonRequest.get(CommonConstants.ACCESS_KEY).toString())) {
	            return commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
	        }

	        String studyId = jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString();
	        
	        List<StudyClinicalDetailsComments> scdList = studyClinicalDetailsCommentsRepo.findByStudyIdAndStatusOrderByCreatedTimeDesc(studyId,"active");

	        if (scdList.isEmpty()) {
	            return commonMethod.createResponse(StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE, StatusConstants.EMPTY_RESULT);
	        }

	        List<Map<String, Object>> resultList = scdList.stream().map(scd -> {
	            Map<String, Object> resMap = new HashMap<>();
	            resMap.put("id", scd.getId());
	            resMap.put("comment", scd.getComment());
	            resMap.put("studyId", scd.getStudyId());
	            resMap.put("createdTime", scd.getCreatedTime());
	            return resMap;
	        }).collect(Collectors.toList());

	        response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
	        response.put(CommonConstants.DATA, resultList);

	        LOG.info("End of " + this.getClass().getName() + ".getClinicalDetailsComments - SUCCESS");
	        return response;

	    } catch (Exception e) {
	        LOG.error("Exception in getClinicalDetailsComments", e);
	        return commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	    }
	}
	
	@Override
	public JSONObject deleteClinicalDetailFile(JSONObject jsonRequest) {
	    LOG.info("Start of " + this.getClass().getName() + ".deleteClinicalDetailFile");
	    LOG.debug("Request : " + jsonRequest);

	    JSONObject response = new JSONObject();

	    try {
	        // --- JWT Token Validation ---
//	        String jwtToken = request.getHeader(UserConstants.JWT_TOKEN);
//	        if (jwtToken == null || jwtToken.isBlank()) {
//	            return commonMethod.createResponse(UserConstants.MISSING_JWT_TOKKEN, UserConstants.BAD_REQUEST_CODE);
//	        }
//
//	        Map<String, Object> claims = jwtUtil.decodeJWT(jwtToken);
//	        User loggedUser = userDao.findById(Long.parseLong(claims.get(UserConstants.ID).toString()));
//
//	        if (loggedUser == null || loggedUser.getJwtToken() == null || !loggedUser.getJwtToken().equals(jwtToken)) {
//	            return commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
//	        }

	        // --- Request & Access Key Validation ---
	        if (jsonRequest == null ||
	            !jsonRequest.containsKey(CommonConstants.ACCESS_KEY) ||
	            !jsonRequest.containsKey(CommonConstants.ID) ||
	            jsonRequest.get(CommonConstants.ACCESS_KEY).toString().isBlank() ||
	            jsonRequest.get(CommonConstants.ID).toString().isBlank()) {

	            return commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	        }

	        String accessKey = jsonRequest.get(CommonConstants.ACCESS_KEY).toString();
	        if (!CommonConstants.SECURITY_TOKEN.equals(accessKey)) {
	            return commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
	        }

	        // --- Fetch File by ID ---
	        int fileId = Integer.parseInt(jsonRequest.get(CommonConstants.ID).toString());
//	        List<StudyClinicalDetails> scdList = em.createNamedQuery("StudyClinicalDetails.getStudyClinicalDetailsById", StudyClinicalDetails.class)
//	                .setParameter("id", fileId)
//	                .getResultList();
	        
	        StudyClinicalDetails scdList = studyClinicalDetailsRepo.findByIdAndStatus(Long.valueOf(fileId),"active");

	        if (scdList == null) {
	            return commonMethod.createResponse(StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE, StatusConstants.EMPTY_RESULT);
	        }

	        // --- Mark File as Inactive ---
	        //StudyClinicalDetails fileToDelete = scdList.get(0);
	        scdList.setStatus("inactive");
	        //em.merge(fileToDelete); // Use merge for existing record update
	        studyClinicalDetailsRepo.save(scdList);

	        response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
	        response.put(CommonConstants.DATA, "Deleted successfully");
	        LOG.info("End of " + this.getClass().getName() + ".deleteClinicalDetailFile - SUCCESS");

	        return response;

	    } catch (Exception e) {
	        LOG.error("Exception in deleteClinicalDetailFile", e);
	        return commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	    }
	}
	
	@Override
	public JSONObject deleteClinicalDetailComment(JSONObject jsonRequest) {
	    LOG.info("Start of " + this.getClass().getName() + ".deleteClinicalDetailComment");
	    LOG.debug("Request : " + jsonRequest);

	    JSONObject response = new JSONObject();

	    try {
	        // --- JWT Token Validation ---
//	        String jwtToken = request.getHeader(UserConstants.JWT_TOKEN);
//	        if (jwtToken == null || jwtToken.isBlank()) {
//	            return commonMethod.createResponse(UserConstants.MISSING_JWT_TOKKEN, UserConstants.BAD_REQUEST_CODE);
//	        }
//
//	        Map<String, Object> claims = jwtUtil.decodeJWT(jwtToken);
//	        User loggedUser = userDao.findById(Long.parseLong(claims.get(UserConstants.ID).toString()));
//
//	        if (loggedUser == null || loggedUser.getJwtToken() == null || !loggedUser.getJwtToken().equals(jwtToken)) {
//	            return commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
//	        }

	        // --- Request & Access Key Validation ---
	        if (jsonRequest == null ||
	            !jsonRequest.containsKey(CommonConstants.ACCESS_KEY) ||
	            !jsonRequest.containsKey(CommonConstants.ID) ||
	            jsonRequest.get(CommonConstants.ACCESS_KEY).toString().isBlank() ||
	            jsonRequest.get(CommonConstants.ID).toString().isBlank()) {

	            return commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	        }

	        String accessKey = jsonRequest.get(CommonConstants.ACCESS_KEY).toString();
	        if (!CommonConstants.SECURITY_TOKEN.equals(accessKey)) {
	            return commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
	        }

	        // --- Fetch Comment by ID ---
	        int commentId = Integer.parseInt(jsonRequest.get(CommonConstants.ID).toString());
//	        List<StudyClinicalDetailsComments> comments = em.createNamedQuery("StudyClinicalDetailsComments.getStudyClinicalDetailsCommentsById", StudyClinicalDetailsComments.class)
//	                .setParameter("id", commentId)
//	                .getResultList();
	        
	       StudyClinicalDetailsComments comments = studyClinicalDetailsCommentsRepo.findByIdAndStatus(Long.valueOf(commentId),"active");

	        if (comments == null) {
	            return commonMethod.createResponse(StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE, StatusConstants.EMPTY_RESULT);
	        }

	        // --- Mark Comment as Inactive ---
	        //StudyClinicalDetailsComments comment = comments.get(0);
	        comments.setStatus("inactive");
	        //em.merge(comment);  // Use merge instead of persist for existing entities
	        studyClinicalDetailsCommentsRepo.save(comments);

	        response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
	        response.put(CommonConstants.DATA, "Deleted successfully");

	        LOG.info("End of " + this.getClass().getName() + ".deleteClinicalDetailComment - SUCCESS");
	        return response;

	    } catch (Exception e) {
	        LOG.error("Exception in deleteClinicalDetailComment", e);
	        return commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	    }
	}

	@Transactional
	@Override
	public ResponseEntity<JSONObject> aiSaveOrgTags(SaveAIOrgTagsDto jsonRequest) {
	    LOG.info("Start of " + this.getClass().getName() + ".aiSaveOrgTags");
	    LOG.debug("Request : " + jsonRequest);

		JSONObject response = new JSONObject();

		try {
			// Check if at least one of the optional fields is present
			if (!hasRequiredData(jsonRequest)) {
				LOG.warn("No tags data or image data provided");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST));
			}
			Long orgId = Long.parseLong(jsonRequest.getOrgId().toString());

			// Use modern repository pattern with Optional
			Optional<AIOrgTags> existingTags = aIOrgTagsRepository.findByOrgId(orgId);

			AIOrgTags aiOrgTags = existingTags.isEmpty()
				? createNewAIOrgTags(orgId)
				: existingTags.get();

			// Process data using modern Java features
			Object tagsData = jsonRequest.getTagsData();
			if (tagsData!=null && tagsData instanceof Map<?, ?> map && !map.isEmpty()) {
				aiOrgTags.setTagsData((HashMap<String, Object>) tagsData);
			}
			
			Object imageData = jsonRequest.getImageData();
			if (imageData!=null && imageData instanceof Map<?, ?> map && !map.isEmpty()) {
				aiOrgTags.setImageData((HashMap<String, Object>) imageData);
			}

			// Save entity
			aIOrgTagsRepository.save(aiOrgTags);

			response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
			response.put(CommonConstants.STATUS, "Saved successfully");

			LOG.info("End of {}.aiSaveOrgTags - {}", this.getClass().getName(), StatusConstants.SUCCESS);
			return ResponseEntity.status(HttpStatus.OK).body(response);

		} catch (NumberFormatException e) {
			LOG.error("Invalid orgId format in aiSaveOrgTags", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, "Invalid orgId format"));
		} catch (Exception e) {
			LOG.error("Exception in aiSaveOrgTags", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR));
		}
	}

	/**
	 * Checks if the request contains at least one of the required data fields
	 */
	private boolean hasRequiredData(SaveAIOrgTagsDto jsonRequest) {
	    return hasValidData(jsonRequest.getTagsData()) || 
	           hasValidData(jsonRequest.getImageData());
	}

	/**
	 * Validates if the specified data object contains valid data
	 */
	private boolean hasValidData(Object data) {
	    if (data == null) return false;
	    
	    if (data instanceof Map<?, ?> map) {
	        return !map.isEmpty();
	    } else if (data instanceof String str) {
	        return !str.trim().isEmpty();
	    } else if (data instanceof byte[] bytes) {
	        return bytes.length > 0;
	    } else if (data instanceof Collection<?> collection) {
	        return !collection.isEmpty();
	    } else if (data instanceof Object[] array) {
	        return array.length > 0;
	    }
	    
	    return true; // Other non-null objects are considered valid
	}

	/**
	 * Creates a new AIOrgTags entity
	 */
	private AIOrgTags createNewAIOrgTags(Long orgId) {
		AIOrgTags aiOrgTags = new AIOrgTags();
		aiOrgTags.setOrgId(orgId);
		LOG.debug("Creating new AIOrgTags for orgId: {}", orgId);
		return aiOrgTags;
	}
	
	@Transactional
	@Override
	public ResponseEntity<JSONObject> getAIOrgTags(SaveAIOrgTagsDto jsonRequest) {
		LOG.info("Start of "+this.getClass().getName()+".getAIOrgTags");
		LOG.debug("Request : " + jsonRequest);

		JSONObject response=new JSONObject();
		try {
				Optional<TagsImageProjection> aiOrgTagsList = aIOrgTagsRepository.findTagsDataAndImageDataByOrgId(Long.parseLong(jsonRequest.getOrgId().toString()));
				
				if(aiOrgTagsList.isPresent()) {
					TagsImageProjection aIOrgTags = aiOrgTagsList.get();
					Map<String, Object> tagsMap = aIOrgTags.getTagsData();
					
					HashMap<String, Object> imageMap = aIOrgTags.getImageData();
					
					System.out.println("got record");
					response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
					response.put(CommonConstants.TAGS_DATA, tagsMap);
					response.put(CommonConstants.IMAGE_DATA, imageMap);
				} else {
					response.put(StatusConstants.STATUS_CODE, StatusConstants.NOT_FOUND);
					response.put(CommonConstants.STATUS, "Tags not found for the orgId");
				}

				LOG.info("End of "+this.getClass().getName()+".getAIOrgTags" + StatusConstants.SUCCESS);

				return ResponseEntity.status(HttpStatus.OK).body(response);
			
		} catch (Exception e) {
			LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			LOG.info("End of "+this.getClass().getName()+".getAIOrgTags" + StatusConstants.OPERATION_FAILED);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR));
		}
	}
	
	@Override
	@Transactional
	public ResponseEntity<JSONObject> getStudyPatientInfoForBookmark(JSONObject jsonRequest) {
	    LOG.info("Start of {}.getStudyPatientInfoForBookmark", this.getClass().getName());
	    LOG.debug("Request: {}", jsonRequest);

	    JSONObject response = new JSONObject();

	    try {

	        BookmarkDetailsProjection bookmark = null;

	        // Get bookmark based on bookmark_id or study instance UID and type
	        if (jsonRequest.containsKey(CommonConstants.BOOKMARK_ID) && jsonRequest.get("bookmark_id") != null && !jsonRequest.get("bookmark_id").toString().isEmpty()) {
	        	Optional<BookmarkDetailsProjection> bookmarkOtp = bookmarksRepo.findBookmarkDetailsById(Long.valueOf(jsonRequest.get("bookmark_id").toString()));
	        	bookmark = bookmarkOtp.isPresent()?bookmarkOtp.get():null;
	        } else if (jsonRequest.containsKey(CommonConstants.TYPE) && jsonRequest.get(CommonConstants.TYPE) != null && !jsonRequest.get(CommonConstants.TYPE).toString().isEmpty()) {
	        	Optional<BookmarkDetailsProjection> bookmarksList = bookmarksRepo.findBookmarkByStudyIdAndVersion(jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(), 0);

	            if (bookmarksList != null && !bookmarksList.isEmpty())
	                bookmark = bookmarksList.get();
	        } else {
	            response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	            LOG.info("End of {}.getStudyPatientInfoForBookmark - {}", this.getClass().getName(), StatusConstants.BAD_REQUEST_CODE);
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	        }

	        // Proceed to fetch the study and volume info based on the bookmark
	        String studyId = bookmark.getStudyInstanceUID();
	        
	        List<PatientHeightWeightProjection> patientHeightWeightList = studyExtensionRepo.findHeightWeightByStudyId(studyId);
	        
	        System.out.println("studyId:"+studyId+" "+bookmark.getId());
	        Optional<List<Object>> studyVolumeList = studyVolumeInfoRepo.findEndVolumeByBookmarkId(bookmark.getId());

	        if (patientHeightWeightList.isEmpty() || studyVolumeList.isEmpty()) {
	            response = commonMethod.createResponse(StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE, StatusConstants.EMPTY_RESULT);
	            LOG.info("End of {}.getStudyPatientInfoForBookmark - {}", this.getClass().getName(), StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
	            return ResponseEntity.status(HttpStatus.OK).body(response);
	        } else {
	            response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
	            response.put(CommonConstants.PATIENT_HEIGHT, patientHeightWeightList.get(0).getPatientHeight());
	            response.put(CommonConstants.PATIENT_WEIGHT, patientHeightWeightList.get(0).getPatientWeight());
	            response.put("end_volume_info", studyVolumeList.get().isEmpty()? new ArrayList<Object>(): studyVolumeList.get().get(0));

	            LOG.info("End of {}.getStudyPatientInfoForBookmark - {}", this.getClass().getName(), StatusConstants.SUCCESS);
	            return ResponseEntity.status(HttpStatus.OK).body(response);
	        }

	    } catch (Exception e) {
	        LOG.error("Exception: ", e);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        LOG.info("End of {}.getStudyPatientInfoForBookmark - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}

	
	public JSONObject prepareResponse(String studyId, String parameterStr) throws ParseException {
	    LOG.info("Start of {}.prepareResponse", this.getClass().getName());

	    JSONObject response = new JSONObject();
	    String sex = "m";
	    List<ParameterReference> parameterReferenceList = null;
	    
	    // Get study details based on the studyId
//	    List<Study> studyList = em.createNamedQuery(Study.GET_STUDY_BY_STUDY_ID)
//	            .setParameter(1, studyId)
//	            .getResultList();
	    String[] sb = parameterStr != null && parameterStr.contains("BREAKFROMHERE") ? parameterStr.split("BREAKFROMHERE") : null;
	    JSONParser parser = new JSONParser();
	    JSONObject lv = new JSONObject();
	    JSONObject rv = new JSONObject();

	    // Process the lv and rv JSON objects if parameterStr is valid
	    if (sb != null && sb.length > 1) {
	    	
	        Optional<String> patSexOpt = studyExtensionRepo.findPatSexByStudyId(studyId);
	        if(patSexOpt==null || patSexOpt.isEmpty()) {
	    		return response;
	    	}
	        
	        if(patSexOpt.get().equalsIgnoreCase("f"))
	        	sex = "f";
	        
	        LOG.info("sex ::: {}", sex);
	        lv = (JSONObject) parser.parse(sb[0]);
	        rv = (JSONObject) parser.parse(sb[1]);
	        
	        parameterReferenceList = parameterReferenceRepo.findBySex(sex);
	    }

	    // Get the response list using commonMethod
	    List<JSONObject> responseList = commonMethod.parameterResponse(lv, rv, parameterReferenceList);
	    response.put(CommonConstants.PARAMETER, responseList);
	    response.put("parameterStr", parameterStr);

	    LOG.debug("Response: {}", response);
	    LOG.info("End of {}.prepareResponse - {}", this.getClass().getName(), StatusConstants.SUCCESS);
	    
	    return response;
	}

	
	@Override
	public JSONObject getStudyImagesCount(String studyId, String orgId) {
	    LOG.info("Start of {}.getStudyImagesCount", this.getClass().getName());
	    JSONObject response = new JSONObject();

	    try {

	        if (studyId == null || studyId.trim().isEmpty()) {
	            response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	            LOG.info("End of {}.getStudyImagesCount - {}", this.getClass().getName(), StatusConstants.BAD_REQUEST_CODE);
	            return response;
	        }

	        Optional<Long> imagesCountOtp = studyExtensionRepo.findDicomImagesCountByStudyInstanceUID(studyId);

	        long count = !imagesCountOtp.isPresent() ? 0 : imagesCountOtp.get();
	        
	        response = commonMethod.createResponse(
	                count > 0 ? StatusConstants.SUCCESS_CODE : StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE,
	                count > 0 ? StatusConstants.FETCHED : StatusConstants.EMPTY_RESULT
	        );
	        response.put("imagesCount", count);

	        LOG.info("response:{}",response);
	        LOG.info("StudyId: {}, Image Count: {}", studyId, count);
	        LOG.info("End of {}.getStudyImagesCount - {}", this.getClass().getName(),
	                count > 0 ? StatusConstants.SUCCESS : StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
	        return response;

	    } catch (Exception e) {
	        LOG.error("Exception in getStudyImagesCount: ", e);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        LOG.info("End of {}.getStudyImagesCount - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
	        return response;
	    }
	}
	
	@Override
	public JSONObject getPreferences(String studyId, String orgId) {
	    LOG.info("Start of {}.getPreferences", this.getClass().getName());
	    JSONObject response = new JSONObject();
	    try {
	        if (studyId == null || studyId.trim().isEmpty()) {
	            response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	            LOG.info("End of {}.getPreferences - {}", this.getClass().getName(), StatusConstants.BAD_REQUEST_CODE);
	            return response;
	        }
	        Map<String, Object> prefs = getOrganizationPreferences(orgId);
	        boolean saveBookmark = extractBooleanPreference(prefs, UserConstants.SAVE_BOOKMARK,true);
	        boolean wordReport = extractBooleanPreference(prefs, UserConstants.WORD_REPORT,false);
	        boolean pdfReport = extractBooleanPreference(prefs, UserConstants.PDF_REPORT,true);
	        response.put("saveBookmark", saveBookmark);
	        response.put("pdfReport", pdfReport);
	        response.put("wordReport", wordReport);

	        String reporttoS3 = commonMethod.getReportToS3();
	        response.put("reportToS3", reporttoS3);
	        LOG.info("response:{}",response);
	        LOG.info("StudyId: {}, Org Id: {}", studyId, orgId);
	        LOG.info("End of {}.getPreferences - {}", this.getClass().getName(),StatusConstants.SUCCESS);
	        return response;

	    } catch (Exception e) {
	        LOG.error("Exception in getPreferences: ", e);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        LOG.info("End of {}.getPreferences - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
	        return response;
	    }
	}

	private Map<String, Object> getOrganizationPreferences(String orgId) {
	    if(orgId == null) {
	        return new HashMap<>();
	    }
	    
	    if(orgId.equalsIgnoreCase("0") || orgId.equalsIgnoreCase("-1")) {
	    	 Map<String, Object> prefs = new HashMap<String, Object>();
	    	 prefs.put(UserConstants.SAVE_BOOKMARK,true);
	    	 prefs.put(UserConstants.PDF_REPORT,true);
	    	 prefs.put(UserConstants.WORD_REPORT,true);
	    	 return prefs;
	    }
	    
	    Optional<Map<String, Object>> organization = orgRepo.findPreferencesById(Long.parseLong(orgId));
	    if(organization.isPresent()) {
	        return organization.get();
	    }
	    return new HashMap<>();
	}

	private boolean extractBooleanPreference(Map<String, Object> prefs, String key,boolean defaultReturn) {
	    if(prefs.containsKey(key) && prefs.get(key) != null) {
	        return (boolean) prefs.get(key);
	    }
	    return defaultReturn;
	}
	
	@Override
	public byte[] getLocalPdf(String relativePath) throws IOException {
		LOG.info("Start of getLocalPDF");
		String fileName = commonMethod.getTargetPath() + relativePath;
		LOG.info("Filename:::"+fileName);
        File file = new File(fileName);
        if (!file.exists()) {
        	LOG.error("Report not found: " + relativePath);
            throw new FileNotFoundException("Report not found: " + relativePath);
        }

        LOG.info("End of getLocalPDF");
        return Files.readAllBytes(file.toPath());
    }
	
	public boolean localPdfExists(String relativePath) throws IOException {
		LOG.info("Start of localPdfExists");
		String fileName = commonMethod.getTargetPath() + relativePath;
		LOG.info("Filename:::"+fileName);
        File file = new File(fileName);
        if (!file.exists()) {
        	LOG.error("Report not found: " + relativePath);
            return false;
        }

        LOG.info("End of localPdfExists");
        return true;
    }
	
	private String createFileNameWithoutUser(JSONObject request) {
		String orgId = (String) request.get("orgId");
		String studyId = (String) request.get("studyId");
		String filename = String.join(
    	        "/",
    	        orgId,
    	        studyId
    	);
		LOG.info("FolderName: {}", filename);
		return filename;
	}
	
	private String createStudyName(JSONObject request) {
		try {
			String fileName = "";
			if(request.containsKey(CommonConstants.STUDY_ID)) {
				String studyId = request.get(CommonConstants.STUDY_ID).toString();
//				StudyUpload studyUpload = em.createNamedQuery(StudyUpload.FIND_BY_STUDY_ID,StudyUpload.class)
//						.setParameter("studyId", studyId)
//						.setMaxResults(1)
//						.getResultList().stream().findFirst().orElse(null);
				List<StudyUploadProjection> studyUploadList = studyUploadRepo.findByStudyIdOrderByCreatedDtDesc(studyId);
				
				if(studyUploadList!=null && studyUploadList.size()>0 && studyUploadList.get(0) !=null) {
					StudyUploadProjection studyUpload = studyUploadList.get(0);
					fileName = studyUpload.getStudyLocation()+"/"+studyUpload.getStudyFileName();
				}
			}
			LOG.info("Study fileName:"+fileName);
			return fileName;
		} catch(Exception e) {
			LOG.error(e.getLocalizedMessage());
			LOG.info("Error while creating study name");
			return "";
		}
	}
	
	@Override
	@Transactional
	public void deleteClassification(String studyUID) {
		LOG.info("Start of {}.deleteClassification - studyUID: {}", this.getClass().getName(), studyUID);

		try {
			if (studyUID == null || studyUID.trim().isEmpty()) {
				LOG.warn("Study UID is null or empty. Aborting delete operation.");
				return;
			}

//			int deletedCount = em.createNamedQuery(StudyClassification.DELETE_STUDY_CLASSIFICATION_BY_STUDY_ID)
//					.setParameter("studyId", studyUID)
//					.executeUpdate();
			
			int deletedCount = studyClassificationRepo.deleteByStudyId(studyUID);

			LOG.info("Deleted {} classifications for studyUID: {}", deletedCount, studyUID);

		} catch (Exception e) {
			LOG.error("Error deleting classifications for studyUID: {}", studyUID, e);
			throw e; // Let Spring handle rollback if needed
		}

		LOG.info("End of {}.deleteClassification", this.getClass().getName());
	}
	
	@Override
	@Transactional
	public ResponseEntity<JSONObject> getPatientDetails(String studyId) {
	    LOG.info("Start of {}.getPatientDetails", this.getClass().getName());
	    JSONObject response = new JSONObject();

	    try {
	        
	        List<PatientHeightWeightProjection> patientHeightWeight = studyExtensionRepo.findHeightWeightByStudyId(studyId);

	        if (patientHeightWeight.isEmpty()) {
	            response = commonMethod.createResponse(StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE, StatusConstants.EMPTY_RESULT);
	            LOG.info("End of {}.getPatientDetails - {}", this.getClass().getName(), StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
	            return ResponseEntity.status(HttpStatus.OK).body(response);
	        } else {

	            response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
	            response.put(CommonConstants.PATIENT_HEIGHT, patientHeightWeight.get(0).getPatientHeight());
	            response.put(CommonConstants.PATIENT_WEIGHT, patientHeightWeight.get(0).getPatientWeight());

	            LOG.info("End of {}.getPatientDetails - {}", this.getClass().getName(), StatusConstants.SUCCESS);
	            return ResponseEntity.status(HttpStatus.OK).body(response);
	        }

	    } catch (Exception e) {
	        LOG.error("Exception: ", e);
	        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        LOG.info("End of {}.getPatientDetails - {}", this.getClass().getName(), StatusConstants.OPERATION_FAILED);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}

	
	private  JSONObject getJson(Map<String, ?> params) {
    	JSONObject jsonObject = new JSONObject();
        if (params != null) {
            for (String eachParam : params.keySet()) {
            	jsonObject.put(eachParam, params.get(eachParam));
            }
        }
        return  jsonObject;
    }
	
	private JSONObject createTagsData(String imagePlane, String sequenceType) {
    	JSONObject tagsData = new JSONObject();
    	tagsData.put("ImagePlane", imagePlane);
    	tagsData.put("SequenceType", sequenceType);
    	tagsData.put("protocol_names", new ArrayList<>());
    	return tagsData;
    }
	
	public void deleteSAParams(String studyId) {
		LOG.info("Start of "+this.getClass().getName()+".deleteSAParams ");
	    try {
	    	
	        Long bookmarkId = bookmarkService.getBookmarkId(studyId, null, false);
	        int deletedCount = studyParameterRepo.deleteByBookmarkId(bookmarkId); 
	        if (deletedCount > 0) {
	            LOG.info("SA Params deleted successfully. Count: {}", deletedCount);
	        } else {
	            LOG.warn("No SA Params found to delete for bookmarkId: {}", bookmarkId);
	        }

	        LOG.info("End of deleteSAParams SUCCESS");

	    } catch (Exception e) {
	        LOG.error("Exception in deleteSAParams", e);
	        LOG.info("End of deleteSAParams FAILED");
	    } 
	}
	
	
	public boolean updateAIProcessStatus(String studyId, String type, String status) {
    	LOG.info("Start of "+this.getClass().getName()+".updateAIProcessStatus");		
		try {
			long studyCount = studyExtensionRepo.countByStudyId(studyId);
			if (studyCount > 0) {
				Map<String, Object> existingStatus = studyExtensionRepo.findAIProcessStatusByStudyInstanceUID(studyId)
		        	    .map(HashMap::new)
		        	    .orElseGet(HashMap::new);
				existingStatus.put(type, status);
				if(!updateAiStatus(studyId,existingStatus,new Date())) {
					return false;
				}
				System.out.println("Saving AI status");
				LOG.info("End of " + this.getClass().getName() + ".updateAIProcessStatus " + StatusConstants.SUCCESS);
				return true;
			}
			return false;
		} catch (Exception e) {
			LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			LOG.info("End of " + this.getClass().getName() + ".updateAIProcessStatus " + StatusConstants.OPERATION_FAILED);
			return false;
		}
    }
	
	public JSONObject reprocessSeries(JSONObject jsonRequest) {
	    LOG.info("Start of " + this.getClass().getName() + ".reprocessSeries");
	    JSONObject response = new JSONObject();

	    try {
	        String aiUrl = aiStudyUrl + "reprocess";

	        try {
				String orgId = jsonRequest.get(CommonConstants.ORG_ID).toString();
				String authorization = pacsTokenService.getToken(orgId);
				jsonRequest.put("Authorization", authorization);
				jsonRequest.put(CommonConstants.SERVER_BASE_URL, commonMethod.getPacsUrl(orgId));
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
	        if ("true".equalsIgnoreCase(activemqSwitch)) {
	            LOG.info("Running in queue async cl ack");
	            JSONObject request = new JSONObject();
	            request.put("url", aiUrl);
	            request.put("request", jsonRequest.toString());
	            request.put("method", "POST");
	            
	            queueProducer.addToQueue(request);
	            response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SUCCESS);
	            response.put("status", "1000");
	            LOG.info("Queued API request");
	            return response;
	        }

	        // Retry logic for HTTP call
	        int connectionRetryCount = 0;
	        while (true) {
	            try {
	                HttpHeaders headers = new HttpHeaders();
	                headers.setContentType(MediaType.APPLICATION_JSON);
	                HttpEntity<String> entity = new HttpEntity<>(jsonRequest.toString(), headers);

	                LOG.info("AI URL: " + aiUrl);
	                ResponseEntity<String> apiResponse = restTemplate.postForEntity(aiUrl, entity, String.class);

	                if (apiResponse.getStatusCode() != HttpStatus.OK) {
	                    response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	                    throw new RuntimeException("Failed: HTTP error code: " + apiResponse.getStatusCode());
	                }

	                String output = apiResponse.getBody();
	                JSONParser parser = new JSONParser();
	                response = (JSONObject) parser.parse(output);
	                break;

	            } catch (ResourceAccessException ex) {
	                Throwable cause = ex.getCause();
	                if (cause instanceof ConnectException || cause instanceof SocketTimeoutException) {
	                    LOG.warn("Connection timeout. Retrying... Attempt " + (connectionRetryCount + 1));
	                    if (++connectionRetryCount > connectionRetryLimit) {
	                        LOG.error("Retry limit reached.", ex);
	                        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	                        return response;
	                    }
	                    try {
	                        Thread.sleep(20000);
	                    } catch (InterruptedException ie) {
	                        Thread.currentThread().interrupt();
	                        return response;
	                    }
	                } else {
	                    LOG.error("Unexpected error during HTTP call", ex);
	                    return response;
	                }
	            } catch (Exception e) {
	                LOG.error("Error calling reprocess API", e);
	                return response;
	            }
	        }

	        // Update AI status
	        String seriesType = (String) jsonRequest.get("seriesType");
	        String type = CommonConstants.AI_PROCESS_STATUS.get(seriesType);
	        String studyId = String.valueOf(jsonRequest.get("studyId"));
	        if (type != null && studyId != null) {
	            updateAIProcessStatus(studyId, type, "inprogress");
	        }

	        LOG.info("End of " + this.getClass().getName() + ".reprocessSeries " + StatusConstants.SUCCESS);
	        return response;

	    } catch (Exception e) {
	        LOG.error("Unhandled exception in reprocessSeries", e);
	        return response;
	    }
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject updateClassificationData(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + ".updateClassificationData");
		LOG.info("Request : " + jsonRequest);
		JSONObject response = new JSONObject();

		try {
			
			ValidationResult validationResult = RequestValidator.validateRequestWithDetails(jsonRequest, CommonConstants.ACCESS_KEY,
					CommonConstants.STUDY_ID,CommonConstants.ORG_ID,CommonConstants.REPROCESS,CommonConstants.IMAGE_PLANE,CommonConstants.SEQUENCE_TYPE,"remember",
					"seriesIdsList");
			
			if(!validationResult.isValid()) {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".updateClassificationData "
						+ StatusConstants.BAD_REQUEST_CODE);
				return response;
			}

			// Validating security token

			if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

				JSONParser jsonParser = new JSONParser();
				jsonRequest = (JSONObject) jsonParser.parse(jsonRequest.toString());
				List<String> seriesIdList = (List<String>) jsonRequest.get("seriesIdsList");
				String studyId = String.valueOf(jsonRequest.get(CommonConstants.STUDY_ID));
				for (String seriesId : seriesIdList) {
					List<StudyClassification> smObject = studyClassificationRepo.findBySeriesId(seriesId);
					try {
						if (!smObject.isEmpty()) {
							StudyClassification scObj = smObject.get(0);
							scObj.setLastUpdatedDt(new Date());
							scObj.setSequenceType(jsonRequest.get(CommonConstants.SEQUENCE_TYPE).toString());
							scObj.setImagePlane(jsonRequest.get(CommonConstants.IMAGE_PLANE).toString());
							//em.merge(scObj);
							studyClassificationRepo.save(scObj);
						}
					} catch (Exception exp) {
						LOG.info("Exception while saving classification data:" + exp.getLocalizedMessage());
					}

				}

				if (jsonRequest.get("remember").toString().equalsIgnoreCase("true")
						&& jsonRequest.get(CommonConstants.TAG_INFO) != null) {
					ArrayList array = (ArrayList) jsonRequest.get(CommonConstants.TAG_INFO);
					
					Optional<HashMap<String, Object>> aiOrgTagsList = aIOrgTagsRepository.findTagsDataByOrgId(Long.parseLong(jsonRequest.get(CommonConstants.ORG_ID).toString()));

					if (aiOrgTagsList.isPresent()) {
						try {
							HashMap<String, Object> tagsMap = aiOrgTagsList.get();
							for (int j = 0; j < array.size(); j++) {
								JSONObject jsonObj = (JSONObject) jsonParser.parse(array.get(j).toString());
								String key = "";
								if (jsonObj.containsKey("imagePlane") && jsonObj.get("imagePlane") != null
										&& jsonObj.containsKey("sequenceType")
										&& jsonObj.get("sequenceType") != null && jsonObj.containsKey("action")
										&& jsonObj.get("action") != null && jsonObj.containsKey("protocols")
										&& jsonObj.get("protocols") != null) {
									String imagePlaneValue = String.valueOf(jsonObj.get("imagePlane"));
									String sequenceTypeValue = String.valueOf(jsonObj.get("sequenceType"));
									String imagePlane = String.valueOf(jsonObj.get("imagePlane")).replace(" ", "");
									String sequenceType = String.valueOf(jsonObj.get("sequenceType")).replace(" ",
											"");
									key = sequenceType.concat(imagePlane);
									String action = String.valueOf(jsonObj.get("action"));
									List<String> protocols = (List<String>) jsonObj.get("protocols");
									for (String protocol : protocols) {
										JSONObject tagData = new JSONObject();
										boolean isTagKeyFound = false;
										if (tagsMap.containsKey(key)) {
										    Map<String, Object> mapData = (Map<String, Object>) tagsMap.get(key);
										    tagData = getJson(mapData);
										    isTagKeyFound = true;
										} else {
										    for (String eachKey : tagsMap.keySet()) {
										        if (eachKey.equalsIgnoreCase(key)) {
										            Map<String, Object> mapData = (Map<String, Object>) tagsMap.get(eachKey);
										            tagData = getJson(mapData);
										            isTagKeyFound = true;
										            key = eachKey; // preserve actual case
										            break;
										        }
										    }
										}
										if (!isTagKeyFound) {
											tagData = createTagsData(imagePlaneValue, sequenceTypeValue);
										}
										if (tagData.containsKey("protocol_names")) {
											List<String> protocolNames = (List<String>) tagData
													.get("protocol_names");
											if (action.equalsIgnoreCase("delete")) {
												if (protocolNames.contains(protocol)) {
													protocolNames.remove(protocol);
												}

											} else if (action.equalsIgnoreCase("add")) {
												if (!protocolNames.contains(protocol)) {
													protocolNames.add(protocol);
												}
											}
											tagData.put("protocol_names", protocolNames);
										}
										tagsMap.put(key, tagData);
									}
								}
							}
							
							int maxRetries = 3;
							int attempts = 0;
							boolean updatedSuccessfully = false;

							while (attempts < maxRetries && !updatedSuccessfully) {
							    attempts++;
							    Optional<Long> lockVersion = aIOrgTagsRepository.findLockVersionByOrgId(Long.parseLong(jsonRequest.get(CommonConstants.ORG_ID).toString()));
								if(lockVersion.isEmpty()) {
									LOG.info("No row exists to update");
									response = commonMethod.createResponse(StatusConstants.EMPTY_RESULT,StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
									LOG.info("End of " + this.getClass().getName() + ".updateClassificationData "
											+ StatusConstants.OPERATION_FAILED);
									return response;
									
								}
							    int updated = aIOrgTagsRepository.updateTagsDataByOrgIdWithVersion(Long.parseLong(jsonRequest.get(CommonConstants.ORG_ID).toString()), tagsMap,lockVersion.get());
								if (updated == 1) {
							        updatedSuccessfully = true;
							    } else {
							        // Optional: wait a bit before retrying
							        Thread.sleep(100); // 100ms backoff
							    }
							}
							if (!updatedSuccessfully) {
							    response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
							    LOG.info("End of " + this.getClass().getName() + ".updateClassificationData "
							             + StatusConstants.OPERATION_FAILED);
							    return response;
							}
							
						} catch (Exception exception) {
							exception.printStackTrace();
						}
					}

				}

				if (jsonRequest.containsKey("deleteSeriesList")
						&& !jsonRequest.get("deleteSeriesList").toString().equals("")) {
					List<String> deleteSeriesIdList = (List<String>) jsonRequest.get("deleteSeriesList");

					for (String seriesId : deleteSeriesIdList) {
						seriesMeasurementsService.deleteContoursBySeries(studyId, seriesId);
						seriesService.deleteSeriesParameters(studyId, seriesId);
						deleteSAParams(studyId);
					}
				}
				
				if (jsonRequest.containsKey("updateStatusList") && jsonRequest.get("updateStatusList") != null) {
					JSONArray updateStatusList = (JSONArray) jsonRequest.get("updateStatusList");
					for (Object obj : updateStatusList) {
						JSONObject entry = (JSONObject) obj;
						String type = entry.containsKey("type") ? (String) entry.get("type") : null;
						String status = entry.containsKey("status") ? (String) entry.get("status") : null;
						updateAIProcessStatus(studyId, type, status);
					}
				}

				// Handle regular reprocess
				if (jsonRequest.get(CommonConstants.REPROCESS).toString().equalsIgnoreCase("yes")) {
				    String seriesType = (String) jsonRequest.get("seriesType");
				    String type = CommonConstants.AI_PROCESS_STATUS.get(seriesType);
				    if (type != null) {
				        updateAIProcessStatus(studyId, type, "inprogress");
				    }

				    LOG.info("hitting reprocess api:::");
				    String orgId = jsonRequest.get(CommonConstants.ORG_ID).toString();
				    jsonRequest.put(CommonConstants.SERVER_BASE_URL, commonMethod.getPacsUrl(orgId));
				    try {
				        String authorization = pacsTokenService.getToken(orgId);
				        jsonRequest.put("Authorization", authorization);
				    } catch (Exception e) {
				        e.printStackTrace();
				    }

				    JSONObject resp = reprocessSeries(jsonRequest);
				    LOG.info("resp :: " + resp);

				    if (resp.get(CommonConstants.STATUS) == null 
				            || !resp.get(CommonConstants.STATUS).toString().equals("1000")) {
				        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED,
				                resp.get("status").toString());
				        LOG.info("End of " + this.getClass().getName() + ".updateClassificationData "
				                + StatusConstants.OPERATION_FAILED);
				        return response;  // only return early on failure
				    }
				    // On success, fall through to check saReprocess below
				}

				// Handle SA reprocess — now reachable whether or not reprocess ran
				if (Boolean.valueOf(jsonRequest.get("saReprocess").toString())) {
				    LOG.info("hitting reprocess api for SA");
				    jsonRequest.put("seriesType", "Short Axis View");
				    String seriesType = (String) jsonRequest.get("seriesType");
				    String type = CommonConstants.AI_PROCESS_STATUS.get(seriesType);
				    if (type != null) {
				        updateAIProcessStatus(studyId, type, "inprogress");
				    }

				    String orgId = jsonRequest.get(CommonConstants.ORG_ID).toString();
				    jsonRequest.put(CommonConstants.SERVER_BASE_URL, commonMethod.getPacsUrl(orgId));
				    try {
				        String authorization = pacsTokenService.getToken(orgId);
				        jsonRequest.put("Authorization", authorization);
				    } catch (Exception e) {
				        e.printStackTrace();
				    }

				    JSONObject resp = reprocessSeries(jsonRequest);
				    LOG.info("resp :: {}", resp);

				    if (resp.get(CommonConstants.STATUS) != null
				            && resp.get(CommonConstants.STATUS).toString().equals("1000")) {
				        response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE,
				                resp.get("status").toString());
				    } else {
				        response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED,
				                resp.get("status").toString());
				    }
				    LOG.info("End of " + this.getClass().getName() + ".updateClassificationData");
				    return response;
				}

				LOG.info("updateClassificationData API End");

				response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SAVED);
				LOG.info("End of " + this.getClass().getName() + ".updateClassificationData "
						+ StatusConstants.SUCCESS);
				return response;
			} else {

				response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
				LOG.info("End of " + this.getClass().getName() + ".updateClassificationData "
						+ StatusConstants.UNAUTHORIZED);
				return response;
			}
		} catch (Exception e) {
			LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".updateClassificationData "
					+ StatusConstants.OPERATION_FAILED);
			return response;
		}
	}
	
	@Override
	public JSONObject getStudyFile(JSONObject request) {
	    JSONObject response = new JSONObject();

	    try {

	        String studyURL = "";
	        String fileName = createStudyName(request);
	        if(fileName.isEmpty()) {
				response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
				response.put(CommonConstants.S3_STUDY_LINK, studyURL);
				return response;
			}
	        studyURL = storagePresignService.generateDownloadUrl(bucketName, fileName, 10);

	        if (studyURL == null || studyURL.isEmpty()) {
	            fileName = createFileNameWithoutUser(request);
	            studyURL = storagePresignService.generateDownloadUrl(bucketName,fileName, 10);
	        }

	        if (!studyURL.isEmpty()) {
	        	long fileSize = storagePresignService.getObjectSize(bucketName, fileName);
	            response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
	            response.put(CommonConstants.S3_STUDY_LINK, studyURL);
	            response.put("fileSize", fileSize);
	        } else {
	            response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
	            response.put(CommonConstants.S3_STUDY_LINK, "");
	        }

	    } catch (Exception e) {
	        LOG.error("Exception in getStudyFile", e);
	        response.put(StatusConstants.STATUS_CODE, StatusConstants.OPERATION_FAILED);
	    }

	    return response;
	}

	@Override
    public JSONObject getStudyExists(String studyId) {
    	LOG.info("Start of isStudyExists");
    	JSONObject response = new JSONObject();
    	if(studyId!=null && !studyId.isEmpty()) {
//			List<Study> stList=em.createNamedQuery(Study.FIND_BY_STUDY_IUID)
//					.setParameter(1, studyId)
//					.getResultList();
			
			long stList = studyExtensionRepo.countByStudyId(studyId);

			if(stList>0){
				response.put("studyExists", true);
			}else {
				response.put("studyExists", false);
			}
    	}else {
    		response=commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE,
					StatusConstants.BAD_REQUEST);
    	}
    	LOG.info("End of isStudyExists");
    	return response;
    }

	@Override
	public JSONObject saveStudy(JSONObject json) {
		LOG.info("Start of saveStudy");
		JSONObject response = new JSONObject();
		try {
			String studyIuid = json.get("studyId").toString();
		    long orgId = Long.parseLong(json.get("orgId").toString());
		    String userId = json.get("userId").toString();
		    String authorization = null;
		    if(json.containsKey(CommonConstants.AUTHORIZATION) && !json.get(CommonConstants.AUTHORIZATION).toString().isEmpty())
		    	authorization = String.valueOf(json.get(CommonConstants.AUTHORIZATION));
		    List<JsonNode> studyNodes = dicomWebClient.getStudyByUID(studyIuid,orgId,authorization);
	        if (studyNodes.isEmpty()) {
	        	throw new RuntimeException("Study not found with study Iuid: " + studyIuid);
	        }
	        StudyDTO study = DicomMapper.mapStudy(studyNodes.get(0));
	    	int maxRetries = 3;
			int attempts = 0;
			boolean updatedSuccessfully = false;
	
			while (attempts < maxRetries && !updatedSuccessfully) {
			    attempts++;
			    Optional<Long> lockVersion = studyExtensionRepo.findLockVersionByStudyId(studyIuid);
			    if(lockVersion.isEmpty()) {
			    	LOG.info("LockVersion is empty for studyId::"+studyIuid);
			    	StudyExtension extension = new StudyExtension();
			        extension.setStudyInstanceUID(studyIuid);
			        extension.setOrgId(orgId);
			        extension.setCreatedBy(userId);
			        extension.setUpdatedBy(userId);
			        extension.setIsAIProcessed(false);
			        extension.setStatus("Pending");
			        extension.setNoOfImages("0");
			        extension.setAiProcessTime(new Date());
			        extension.setStudyDate(study.getStudyDate());
			        extension.setPatientName(study.getPatientName());
			        extension.setStudyTime(study.getStudyTime());
			        extension.setCreatedTime(new Date());
			        extension.setUpdatedTime(new Date());
			        extension.setPatSex(study.getPatientSex());
			        studyExtensionRepo.save(extension);
			        bookmarkService.getBookmark(studyIuid, null, true);
			        return commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SAVED);
			    }
			    
			    int updated = studyExtensionRepo.updateStudyExtensionMeta(studyIuid, studyIuid, orgId, userId,new Date(),lockVersion.get());
				if (updated == 1) {
					LOG.info("Successfully updated");
			        updatedSuccessfully = true;
			    } else {
			        // Optional: wait a bit before retrying
			        try {
			        	LOG.info("Sleeping for 100ms");
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // 100ms backoff
			    }
			}
			if (!updatedSuccessfully) {
				response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			    LOG.info("End of " + this.getClass().getName() + ".saveStudy "
			             + StatusConstants.OPERATION_FAILED);
			    return response;
			}
			return commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SAVED);
		}
		catch (Exception e) {
			LOG.error("Exception: ", e);
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
		}
			return response;
		
	}


	@Override
	public JSONObject processStudy(JSONObject request) {
	    LOG.info("Start of processStudy");

	    JSONObject finalResponse = new JSONObject();
	    JSONObject stepResults = new JSONObject();

	    try {
	        // ----------------------------
	        // 1. Validate User
	        // ----------------------------
	        String email = SecurityUtil.currentUserEmailId();
	        Optional<Long> userIdOpt = userRepo.findIdByEmail(email);

	        if (userIdOpt.isEmpty()) {
	            return commonMethod.createResponse(
	                UserConstants.INVALID_USERID,
	                UserConstants.UNAUTHORIZED
	            );
	        }
	        
	        request.put(CommonConstants.ACCESS_KEY, CommonConstants.SECURITY_TOKEN);
	        
	        JSONObject aiProcessStatus = getAIProcessStatus(request);
	        

		     // =====================================================================
		    //  STEP 0.5: Check AI Process Status
		    // =====================================================================
		    JSONObject aiProcessStatusResult = new JSONObject();
		    try {
		        JSONObject aiStatusResponse = getAIProcessStatus(request);

		        aiProcessStatusResult.put("aiProcessStatus", aiStatusResponse.get("aiProcessStatus"));
		        aiProcessStatusResult.put("responseMessage", aiStatusResponse.get("responseMessage"));
		        aiProcessStatusResult.put("statusCode", aiStatusResponse.get("statusCode"));

		        stepResults.put("aiProcessStatus", aiProcessStatusResult);

		        // FIX: Cast to Map instead of JSONObject
		        Map<String, Object> models = (Map<String, Object>) aiStatusResponse.get("aiProcessStatus");

		        boolean anyInProgress = models.values().stream()
		                .anyMatch(status -> "InProgress".equalsIgnoreCase(status.toString()));

		        if (anyInProgress) {
		            LOG.info("AI models are InProgress. Skipping further processing.");
		            finalResponse.put("steps", stepResults);
		            finalResponse.put("overallStatus", "AI_IN_PROGRESS");
		            return finalResponse;
		        }

		    } catch (Exception e) {
		        LOG.error("getAIProcessStatus failed", e);

		        aiProcessStatusResult.put("statusCode", "500");
		        aiProcessStatusResult.put("responseMessage", "Failed to get AI process status");
		        stepResults.put("aiProcessStatus", aiProcessStatusResult);

//		        finalResponse.put("steps", stepResults);
//		        finalResponse.put("overallStatus", "AI_STATUS_CHECK_FAILED");
//		        return finalResponse;
		    }
	        Long userId = userIdOpt.get();
	        request.put("userId", userId);

		    // =====================================================================
		    //  STEP 1: Save Study (independent)
		    // =====================================================================
		    JSONObject saveStudyResult;
		    boolean saveStudySuccess = false;
	
		    try {
		        saveStudyResult = saveStudy(request);
		        stepResults.put("saveStudy", saveStudyResult);
	
		        // Check success
		        String statusCode = String.valueOf(saveStudyResult.get("statusCode"));
		        if ("200".equals(statusCode)) {
		            saveStudySuccess = true;
		        }
	
		    } catch (Exception e) {
		        LOG.error("saveStudy failed", e);
		        saveStudyResult = commonMethod.createResponse("FAIL", "saveStudy failed");
		        stepResults.put("saveStudy", saveStudyResult);
		    }
	
		    // =====================================================================
		    //  Only proceed if saveStudy succeeded
		    // =====================================================================
		    if (!saveStudySuccess) {
		        LOG.warn("saveStudy failed → Skipping processAI and assignPatient");
//	
//		        finalResponse.put("steps", stepResults);
//		        finalResponse.put("overallStatus", "SAVE_STUDY_FAILED");
		        return commonMethod.createResponse(
			            StatusConstants.OPERATION_FAILED,
			            StatusConstants.SERVER_ERROR
			        );
//		        return finalResponse;
		    }

	        // =====================================================================
	        //  STEP 2: Process AI (independent)
	        // =====================================================================
	        JSONObject processAIResult = new JSONObject();
	        try {
	            String studyId = String.valueOf(request.get(CommonConstants.STUDY_ID));
	            String orgId   = String.valueOf(request.get(CommonConstants.ORG_ID));
	            String authorization = 
	                request.containsKey(CommonConstants.AUTHORIZATION) ?
	                String.valueOf(request.get(CommonConstants.AUTHORIZATION)) : null;

	            ResponseEntity<JSONObject> aiResponse =
	                proccesseAI(studyId, orgId, "no", authorization);

	            processAIResult = aiResponse.getBody();
	            stepResults.put("processAI", processAIResult);
	            finalResponse.put(StatusConstants.RESPONSE_MESSAGE, aiResponse.getBody().get("responseMessage"));
	            finalResponse.put(StatusConstants.STATUS_CODE,String.valueOf(aiResponse.getStatusCodeValue()));
	        } catch (Exception e) {
	            LOG.error("processAI failed", e);
	            processAIResult = commonMethod.createResponse("FAIL", "processAI failed");
	            stepResults.put("processAI", processAIResult);
	        }

	        // =====================================================================
	        //  STEP 3: Assign Patient (independent)
	        // =====================================================================
	        JSONObject assignResult = new JSONObject();
	        try {
	            String studyId = String.valueOf(request.get(CommonConstants.STUDY_ID));
	            request.put("studyList", List.of(studyId));

	            assignResult = userService.assignPatient(request);
	            stepResults.put("assignPatient", assignResult);
	        } catch (Exception e) {
	            LOG.error("assignPatient failed", e);
	            assignResult = commonMethod.createResponse("FAIL", "assignPatient failed");
	            stepResults.put("assignPatient", assignResult);
	        }

	        // =====================================================================
	        // Final Response Summary
	        // =====================================================================
	        finalResponse.put("steps", stepResults);
	        finalResponse.put("overallStatus", "COMPLETED_WITH_RESULTS");

	        LOG.info("End of processStudy (loosely coupled)");
	        return finalResponse;

	    } catch (Exception e) {
	        LOG.error("Fatal exception in processStudy", e);
	        return commonMethod.createResponse(
	            StatusConstants.OPERATION_FAILED,
	            StatusConstants.SERVER_ERROR
	        );
	    }
	}

	@Override
	public JSONObject processStudyExt(JSONObject request) {
		LOG.info("Start of processStudyExt");
		try {
			String studyId = String.valueOf(request.get(CommonConstants.STUDY_ID));
			String orgId = String.valueOf(request.get(CommonConstants.ORG_ID));
			
			String email = SecurityUtil.currentUserEmailId();
			Optional<Long> userIdOpt = userRepo.findIdByEmail(email);

			if (userIdOpt.isEmpty()) {
				return commonMethod.createResponse(UserConstants.INVALID_USERID, UserConstants.UNAUTHORIZED);
			}
			
			String userId = String.valueOf(userIdOpt.get());
			request.put("userId", userIdOpt.get());

			// Check License
			JSONObject licenseRequest = new JSONObject();
			licenseRequest.put("userId", userId);
			licenseRequest.put("orgId", orgId);
			licenseRequest.put("studyId", studyId);

			String checkLicenseUrl = licenseServerUrl + "/license/user/getLicenseDetails";
			LOG.info("Checking license at: " + checkLicenseUrl);
			
			ResponseEntity<JSONObject> licenseResponse = restTemplate.postForEntity(checkLicenseUrl, licenseRequest, JSONObject.class);
			JSONObject licenseBody = licenseResponse.getBody();

			if (licenseBody != null && licenseBody.get("data") != null && licenseBody.get("error") == null) {
				LOG.info("License is valid. Updating license count.");
				String updateLicenseUrl = licenseServerUrl + "/license/user/updateLicenseCount";
				restTemplate.postForEntity(updateLicenseUrl, licenseRequest, JSONObject.class);

				return processStudy(request);
			} else {
				LOG.warn("License not valid. Deleting study: " + studyId);
				// Delete study from dcm4chee
				try {
					String pacsUrl = commonMethod.getPacsUrl(orgId);
					String deleteUrl = pacsUrl + "/studies/" + studyId;
					LOG.info("Deleting study from PACS: " + deleteUrl);
					
					HttpHeaders headers = new HttpHeaders();
					String token = pacsTokenService.getToken(orgId);
					headers.setBearerAuth(token);
					HttpEntity<Void> entity = new HttpEntity<>(headers);
					
					restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, String.class);
					LOG.info("Study deleted successfully from PACS.");
				} catch (Exception e) {
					LOG.error("Error deleting study from PACS: " + e.getMessage());
				}

				return commonMethod.createResponse(StatusConstants.OPERATION_FAILED, "License not valid");
			}
		} catch (Exception e) {
			LOG.error("Fatal exception in processStudyExt", e);
			return commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
		}
	}

}
