package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.techvedika.harmonycvi.gateway.constant.CommonConstants;
import com.techvedika.harmonycvi.gateway.constant.StatusConstants;
import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.dicomweb.DicomMapper;
import com.techvedika.harmonycvi.gateway.dicomweb.DicomWebClient;
import com.techvedika.harmonycvi.gateway.dicomweb.StudyDTO;
import com.techvedika.harmonycvi.gateway.entity.Bookmarks;
import com.techvedika.harmonycvi.gateway.entity.ParameterReference;
import com.techvedika.harmonycvi.gateway.entity.SeriesMeasurements;
import com.techvedika.harmonycvi.gateway.entity.SeriesSegments;
import com.techvedika.harmonycvi.gateway.entity.StudyAnnotation;
import com.techvedika.harmonycvi.gateway.exception.RequestValidator;
import com.techvedika.harmonycvi.gateway.exception.ValidationResult;
import com.techvedika.harmonycvi.gateway.repository.BookmarksRepository;
import com.techvedika.harmonycvi.gateway.repository.ParameterReferenceRepository;
import com.techvedika.harmonycvi.gateway.repository.SeriesMeasurementsRepository;
import com.techvedika.harmonycvi.gateway.repository.SeriesSegmentsRepository;
import com.techvedika.harmonycvi.gateway.repository.StudyAnnotationRepository;
import com.techvedika.harmonycvi.gateway.repository.StudyExtensionRepository;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;
import com.techvedika.harmonycvi.gateway.security.SecurityUtil;
import com.techvedika.harmonycvi.gateway.service.BookmarkService;
import com.techvedika.harmonycvi.gateway.service.CommonMethod;
import com.techvedika.harmonycvi.gateway.service.SeriesMeasurementsService;

@Service
public class SeriesMeasurementsServiceImpl implements SeriesMeasurementsService {

	private static final Logger LOG = LoggerFactory.getLogger(SeriesMeasurementsServiceImpl.class);

	@Value("${ai.study-url}")
	String aiStudyUrl;

	@Value("${messaging.activemq.enabled:false}")
	String activemqSwitch;

	
	@Autowired
	private CommonMethod commonMethod;
	

	@Autowired
	PacsTokenService pacsTokenService;


	@Autowired
	private UserRepository userDao;

	@Autowired
	private BookmarkService bookmarkService;

	@Autowired
	private BookmarksRepository bookmarkRepo;

	@Autowired
	private SeriesMeasurementsRepository seriesMeasurementsRepository;

	@Autowired
	SeriesSegmentsRepository seriesSegmentsRepository;

	@Autowired
	private StudyAnnotationRepository studyAnnotationRepository;
	
	@Autowired
	private StudyExtensionRepository studyExtensionRepo;
	
	@Autowired
	private ParameterReferenceRepository parameterReferenceRepo;
	
	@Autowired
	SecurityUtil securityUtil;

	public int connectionRetryLimit = 6;

	// API is used at weasis, Can actually eliminate the API by using
	// getBookmarkDetails in case of preprocess. Didnt make any changes
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getSeriesMeasurementsData(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + ".getSeriesMeasurementsData");
		LOG.debug("Request : " + jsonRequest);

		JSONObject response = new JSONObject();
		try {
			//Long userId = securityUtil.currentUserId();
			String userEmailId = SecurityUtil.currentUserEmailId();
			Optional<String> roleName = userDao.findRoleNameByEmail(userEmailId);
			if(roleName==null || roleName.isEmpty()) {
				response = commonMethod.createResponse(UserConstants.INVALID_USERID, UserConstants.UNAUTHORIZED);
				LOG.info("End of " + this.getClass().getName() + ".getSeriesMeasurementsData "
						+ UserConstants.INVALID_USERID);
				return response;
			}
			String role = roleName.get();

			if (jsonRequest != null && jsonRequest.get(CommonConstants.ACCESS_KEY) != null
					&& !jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals("")
					&& jsonRequest.get(CommonConstants.STUDY_ID) != null
					&& !jsonRequest.get(CommonConstants.STUDY_ID).toString().equals("")) {

				// Validating security token

				if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

					if (jsonRequest.get(CommonConstants.TYPE).toString().equalsIgnoreCase("preprocess")) {

						List<SeriesMeasurements> smObject = null;

						// Getting record based studyId
						smObject = seriesMeasurementsRepository
								.findByStudyId(jsonRequest.get(CommonConstants.STUDY_ID).toString());

						if (smObject == null) {
							response = commonMethod.createResponse(StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE,
									StatusConstants.EMPTY_RESULT);
							LOG.info("End of " + this.getClass().getName() + ".getSeriesMeasurementsData "
									+ StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
							return response;

						} else {
							List<JSONObject> resultList = new ArrayList<JSONObject>();

							// getting study annotation based on study id
							List<StudyAnnotation> studyAnnotation = studyAnnotationRepository
									.findByStudyId(jsonRequest.get(CommonConstants.STUDY_ID).toString());

							for (SeriesMeasurements ob : smObject) {
								JSONObject createJsonObject = commonMethod.createJsonObject(ob);
								if (createJsonObject != null) {
									resultList.add(createJsonObject);
								}
							}
//							List<Study> stObject = null;
//							// Getting record based on seriesId && studyId
							response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE,
									StatusConstants.FETCHED);
							response.put(StatusConstants.DATA, resultList);
							response.put(CommonConstants.STUDY_ANNOTATION,
									studyAnnotation != null && studyAnnotation.size() > 0
											? studyAnnotation.get(0).getAnnotationData()
											: null);
							response.put("userRole", role);
							System.gc();
							LOG.info("End of " + this.getClass().getName() + ".getSeriesMeasurementsData "
									+ StatusConstants.SUCCESS);
							return response;
						}
					} else if (jsonRequest.get(CommonConstants.TYPE).toString().equalsIgnoreCase("edit")) {
						// Getting record based on seriesId && studyId
						List<SeriesMeasurements> smObject = seriesMeasurementsRepository
								.findByStudyId(jsonRequest.get(CommonConstants.STUDY_ID).toString());

						System.gc();
						List<SeriesMeasurements> smObjectEdit = seriesMeasurementsRepository
								.findByStudyId(jsonRequest.get(CommonConstants.STUDY_ID).toString());

						if (smObject == null && smObjectEdit == null) {
							response = commonMethod.createResponse(StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE,
									StatusConstants.EMPTY_RESULT);
							LOG.info("End of " + this.getClass().getName() + ".getSeriesMeasurementsData "
									+ StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
							return response;

						} else {
							List<JSONObject> resultList = new ArrayList<JSONObject>();

							// getting study annotation based on study id
							List<StudyAnnotation> studyAnnotation = studyAnnotationRepository
									.findByStudyId(jsonRequest.get(CommonConstants.STUDY_ID).toString());

							String seriesId = "";
							for (SeriesMeasurements ob : smObjectEdit) {
								seriesId = seriesId + "," + ob.getSeriesId();
								JSONObject createJsonObject = commonMethod.createJsonObject(ob);
								if (createJsonObject != null) {
									resultList.add(createJsonObject);
								}
							}

							for (SeriesMeasurements ob : smObject) {
								if (!seriesId.contains(ob.getSeriesId())) {
									JSONObject createJsonObject = commonMethod.createJsonObject(ob);
									if (createJsonObject != null) {
										resultList.add(createJsonObject);
									}
								}

							}

							response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE,
									StatusConstants.FETCHED);
							if (smObjectEdit != null && !smObjectEdit.isEmpty()) {
								response.put("isEdited", true);
							} else {
								response.put("isEdited", false);
							}
							response.put(StatusConstants.DATA, resultList);
							response.put(CommonConstants.STUDY_ANNOTATION,
									studyAnnotation != null && studyAnnotation.size() > 0
											? studyAnnotation.get(0).getAnnotationData()
											: null);
							System.gc();
							response.put("userRole", role);

							LOG.info("End of " + this.getClass().getName() + ".getSeriesMeasurementsData "
									+ StatusConstants.SUCCESS);

							return response;
						}

					} else if (jsonRequest.get(CommonConstants.TYPE).toString().equalsIgnoreCase("bookmark")) {
						// Getting record based on seriesId && studyId
						List<SeriesMeasurements> smObject = seriesMeasurementsRepository
								.findByStudyId(jsonRequest.get(CommonConstants.STUDY_ID).toString());
						System.gc();
						List<SeriesMeasurements> smObjectBookmark = seriesMeasurementsRepository
								.findByStudyId(jsonRequest.get(CommonConstants.STUDY_ID).toString());

						if (smObject == null && smObjectBookmark == null) {
							response = commonMethod.createResponse(StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE,
									StatusConstants.EMPTY_RESULT);
							LOG.info("End of " + this.getClass().getName() + ".getSeriesMeasurementsData "
									+ StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
							return response;

						} else {
							List<JSONObject> resultList = new ArrayList<JSONObject>();

							// getting study annotation based on study id
							List<StudyAnnotation> studyAnnotation = studyAnnotationRepository
									.findByStudyId(jsonRequest.get(CommonConstants.STUDY_ID).toString());

							String seriesId = "";
							for (SeriesMeasurements ob : smObjectBookmark) {
								seriesId = seriesId + "," + ob.getSeriesId();
								JSONObject createJsonObject = commonMethod.createJsonObject(ob);
								if (createJsonObject != null) {
									resultList.add(createJsonObject);
								}
							}
							for (SeriesMeasurements ob : smObject) {
								if (!seriesId.contains(ob.getSeriesId())) {
									JSONObject createJsonObject = commonMethod.createJsonObject(ob);
									if (createJsonObject != null) {
										resultList.add(createJsonObject);
									}
								}

							}

							response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE,
									StatusConstants.FETCHED);

							response.put(StatusConstants.DATA, resultList);
							response.put(CommonConstants.STUDY_ANNOTATION,
									studyAnnotation != null && studyAnnotation.size() > 0
											? studyAnnotation.get(0).getAnnotationData()
											: null);
							System.gc();
							response.put("userRole", role);

							LOG.info("End of " + this.getClass().getName() + ".getSeriesMeasurementsData "
									+ StatusConstants.SUCCESS);

							return response;
						}
					} else {
						LOG.info("End of " + this.getClass().getName() + ".getSeriesMeasurementsData "
								+ StatusConstants.SUCCESS);
						return response;
					}

				} else {

					response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
					LOG.info("End of " + this.getClass().getName() + ".getSeriesMeasurementsData "
							+ StatusConstants.UNAUTHORIZED);
					return response;
				}

			} else {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".getSeriesMeasurementsData "
						+ StatusConstants.BAD_REQUEST_CODE);
				return response;
			}
		} catch (Exception e) {
			LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".getSeriesMeasurementsData "
					+ StatusConstants.OPERATION_FAILED);
			return response;
		}
	}
	
	@Override
	@Transactional
	public JSONObject saveSeriesFreeHandMeasurementsInfo(JSONObject jsonRequest) {
	    LOG.info("Start of {}.saveSeriesFreeHandMeasurementsInfo", this.getClass().getName());
	    JSONObject response = new JSONObject();

	    try {
	        // Validate request
	        ValidationResult validationResults = RequestValidator.validateRequestWithDetails(
	                jsonRequest, CommonConstants.ACCESS_KEY, CommonConstants.COMMON_DATA, CommonConstants.INSTANCE_ARRAY);
	        if (!validationResults.isValid()) {
	            LOG.info("End of {}.saveSeriesFreeHandMeasurementsInfo - Bad Request", this.getClass().getName());
	            return commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	        }

	        // Validate security token
	        String accessKey = jsonRequest.get(CommonConstants.ACCESS_KEY).toString();
	        if (!CommonConstants.SECURITY_TOKEN.equals(accessKey)) {
	            LOG.info("End of {}.saveSeriesFreeHandMeasurementsInfo - Unauthorized", this.getClass().getName());
	            return commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
	        }

	        LinkedHashMap<String, Object> commonData = (LinkedHashMap<String, Object>) jsonRequest.get(CommonConstants.COMMON_DATA);
	        if (commonData == null || !commonData.containsKey(CommonConstants.STUDY_INSTANCE_UID)
	                || !commonData.containsKey(CommonConstants.SERIES_INSTANCE_UID)) {
	            LOG.info("End of {}.saveSeriesFreeHandMeasurementsInfo - Bad Request", this.getClass().getName());
	            return commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	        }

	        String studyId = commonData.get(CommonConstants.STUDY_INSTANCE_UID).toString();
	        String seriesId = commonData.get(CommonConstants.SERIES_INSTANCE_UID).toString();
	        LOG.info("saveSeriesFreeHandMeasurementsInfo for:"+studyId+" "+seriesId);
	        ArrayList<?> instanceArray = (ArrayList<?>) jsonRequest.get(CommonConstants.INSTANCE_ARRAY);
	        if (instanceArray == null || instanceArray.isEmpty()) {
	            LOG.info("End of {}.saveSeriesFreeHandMeasurementsInfo - Bad Request", this.getClass().getName());
	            return commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
	        }

	        // Get or create bookmark
	        String bookmarkIdStr = (String) jsonRequest.getOrDefault(CommonConstants.BOOKMARK_ID, null);
	        LOG.info("BookmarkId in saveSeriesFreeHandMeasurementsInfo:"+bookmarkIdStr);
	        Bookmarks bookmark = bookmarkService.getBookmark(studyId, bookmarkIdStr, true);
	        LOG.info("Got Bookmark in saveSeriesFreeHandMeasurementsInfo:"+bookmarkIdStr);

	        // Fetch existing SeriesMeasurements or create new
	        Optional<SeriesMeasurements> optionalSM = seriesMeasurementsRepository
	                .findFirstByStudyIdAndSeriesIdAndBookmarkId(studyId, seriesId, bookmark.getId());

	        if(optionalSM.isEmpty()) {
	        	LOG.info("Series Measurement is empty in saveSeriesFreeHandMeasurementsInfo");
	        }else {
	        	LOG.info("Series Measurement is not empty in saveSeriesFreeHandMeasurementsInfo");
	        }
	        SeriesMeasurements sm = optionalSM.orElseGet(SeriesMeasurements::new);

	        // Prepare commonData for storage
	        commonData = commonMethod.createCommonDataJson(commonData);

	        LOG.info("After creating common data in saveSeriesFreeHandMeasurementsInfo");
	        // Update fields
	        sm = commonMethod.setSeriesMeasurementsObjectForFreeHand(sm, null, seriesId, jsonRequest, commonData);
	        LOG.info("After setting object for saveSeriesFreeHandMeasurementsInfo");
	        LocalDateTime now = LocalDateTime.now();
	        sm.setCreationDate(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
	        sm.setLastUpdatedDate(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
	        sm.setInstanceArray(instanceArray);
	        sm.setBookmark(bookmark);
	        sm.setVersion(bookmark.getVersion());

	        // Save in DB
	        seriesMeasurementsRepository.save(sm);

	        response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
	        response.put(StatusConstants.STATUS_MESSAGE, "Saved Successfully");
	        LOG.info("End of {}.saveSeriesFreeHandMeasurementsInfo - Success", this.getClass().getName());
	        return response;

	    } catch (Exception e) {
	        LOG.error("Exception in {}.saveSeriesFreeHandMeasurementsInfo: ", this.getClass().getName(), e);
	        return commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	    }
	}


	@Override
	public void deleteMeasurement(String patientId) {

		try {
			seriesMeasurementsRepository.deleteMeasurementByPatientId(patientId);

		} catch (Exception e) {
			LOG.info(e.getLocalizedMessage());
		}

	}
	
	public JSONObject getSeriesMeasurementsDataBySeriesId(JSONObject jsonRequest) {

		LOG.info("Start of " + this.getClass().getName() + ".getSeriesMeasurementsDataBySeriesId");
		JSONObject response = new JSONObject();
		try {

			if (jsonRequest != null && jsonRequest.get(CommonConstants.ACCESS_KEY) != null
					&& !jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals("")
					&& jsonRequest.get(CommonConstants.SERIES_ID) != null
					&& !jsonRequest.get(CommonConstants.SERIES_ID).toString().equals("")
					&& jsonRequest.get("magnitudeSeriesInstanceUid") != null
					&& !jsonRequest.get("magnitudeSeriesInstanceUid").toString().equals("")) {

				// Validating security token

				if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

					List<SeriesMeasurements> smObject = null;
					List<SeriesMeasurements> smObject2 = null;

					String bookmarkId = jsonRequest.containsKey(CommonConstants.BOOKMARK_ID)
							? jsonRequest.get(CommonConstants.BOOKMARK_ID) != null
									&& jsonRequest.get(CommonConstants.BOOKMARK_ID) != ""
											? (String) jsonRequest.get(CommonConstants.BOOKMARK_ID)
											: null
							: null;
					// Getting bookmark details
					Long bookmark = bookmarkService
							.getBookmarkId(jsonRequest.get(CommonConstants.STUDY_ID).toString(), bookmarkId, false);

					// Getting record based on seriesId && studyId
					smObject = seriesMeasurementsRepository.findByStudyIdAndSeriesIdAndBookmarkId(
							jsonRequest.get(CommonConstants.STUDY_ID).toString(),
							jsonRequest.get(CommonConstants.SERIES_ID).toString(), bookmark);

					smObject2 = seriesMeasurementsRepository.findByStudyIdAndSeriesIdAndBookmarkId(
							jsonRequest.get(CommonConstants.STUDY_ID).toString(),
							jsonRequest.get("magnitudeSeriesInstanceUid").toString(), bookmark);

					if (smObject == null) {
						response = commonMethod.createResponse(StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE,
								StatusConstants.EMPTY_RESULT);
						LOG.info("End of " + this.getClass().getName() + ".getSeriesMeasurementsDataBySeriesId "
								+ StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
						return response;

					} else {
						List<JSONObject> resultList = new ArrayList<JSONObject>();

						for (SeriesMeasurements ob : smObject) {

							JSONObject createJsonObject = commonMethod.createJsonObject(ob);
							if (createJsonObject != null) {
								resultList.add(createJsonObject);
							}

						}

						for (SeriesMeasurements ob2 : smObject2) {

							JSONObject createJsonObject2 = commonMethod.createJsonObject(ob2);
							if (createJsonObject2 != null) {
								resultList.add(createJsonObject2);
							}

						}

						response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.FETCHED);
						response.put(StatusConstants.DATA, resultList);

						LOG.info("End of " + this.getClass().getName() + ".getSeriesMeasurementsDataBySeriesId "
								+ StatusConstants.SUCCESS);
						System.gc();
						return response;
					}
				} else {

					response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
					LOG.info("End of " + this.getClass().getName() + ".getSeriesMeasurementsDataBySeriesId "
							+ StatusConstants.UNAUTHORIZED);
					return response;
				}

			} else {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".getSeriesMeasurementsDataBySeriesId "
						+ StatusConstants.BAD_REQUEST_CODE);
				return response;
			}
		} catch (Exception e) {
			LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".getSeriesMeasurementsDataBySeriesId "
					+ StatusConstants.OPERATION_FAILED);
			return response;
		}

	}

	@Override
	@Transactional
	public JSONObject updateSeriesMeasurement(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + ".updateSeriesMeasurement");
		JSONObject response = new JSONObject();
		try {
			if (jsonRequest != null && jsonRequest.get(CommonConstants.ACCESS_KEY) != null
					&& !jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals("")
					&& jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID) != null
					&& !jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString().equals("")
					&& jsonRequest.get("data") != null && !jsonRequest.get("data").toString().equals("")
					&& jsonRequest.get(CommonConstants.PATIENT_WEIGHT) != null
					&& !jsonRequest.get(CommonConstants.PATIENT_WEIGHT).toString().equals("")
					&& jsonRequest.get(CommonConstants.PATIENT_HEIGHT) != null
					&& !jsonRequest.get(CommonConstants.PATIENT_HEIGHT).toString().equals("")
					&& jsonRequest.get(CommonConstants.SEND_MULTIPLE) != null
					&& !jsonRequest.get(CommonConstants.SEND_MULTIPLE).toString().equals("")) {
				String patientWeight = jsonRequest.get(CommonConstants.PATIENT_WEIGHT).toString();
				String patientHeight = jsonRequest.get(CommonConstants.PATIENT_HEIGHT).toString();

				List<JSONObject> resultList = new ArrayList<JSONObject>();
				JSONObject requestObj = new JSONObject();

				// Validating security token
				System.gc();
				if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {
					ArrayList data = (ArrayList) jsonRequest.get("data");
					if (data != null) {
						for (Object instanceObj : data) {

							LinkedHashMap instance = (LinkedHashMap) instanceObj;
							if (instance.get(CommonConstants.SERIES_INSTANCE_UID) != null
									&& !instance.get(CommonConstants.SERIES_INSTANCE_UID).toString().equals("")
									&& instance.get(CommonConstants.INSTANCE_ARRAY) != null
									&& !instance.get(CommonConstants.INSTANCE_ARRAY).toString().equals("")) {

//								Bookmarks preprocessBookmark = (Bookmarks) em
//										.createNamedQuery(Bookmarks.FINDID_BY_STUDY_AND_VERSION)
//										.setParameter(CommonConstants.STUDY_ID,
//												jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString())
//										.setParameter("version", 0).getResultList().get(0);

								Long preprocessBookmark = bookmarkRepo.findBookmarkIdByStudyIdAndVersion(
										jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(), 0);

//								List<SeriesMeasurements> smObject = em
//										.createNamedQuery(SeriesMeasurements.FIND_BY_STUDY_SERIES_BOOKMARK)
//										.setParameter(CommonConstants.STUDY_ID,
//												jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString())
//										.setParameter(CommonConstants.SERIES_ID,
//												instance.get(CommonConstants.SERIES_INSTANCE_UID).toString())
//										.setParameter(CommonConstants.BOOKMARK_ID, preprocessBookmark.getId())
//										.getResultList();

								List<SeriesMeasurements> smObject = seriesMeasurementsRepository
										.findByStudyIdAndSeriesIdAndBookmarkId(
												jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(),
												instance.get(CommonConstants.SERIES_INSTANCE_UID).toString(),
												preprocessBookmark);

								ArrayList instanceArray = (ArrayList) instance.get(CommonConstants.INSTANCE_ARRAY);

								SeriesMeasurements sm = new SeriesMeasurements();
								sm.setSeriesId(instance.get(CommonConstants.SERIES_INSTANCE_UID).toString());
								sm.setStudyId(jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString());
								sm.setInstanceArray(instanceArray);
								if (smObject.size() > 0) {
									sm.setCommonData(smObject.get(smObject.size() - 1).getCommonData());
								}
								sm.setPatientHeight(patientHeight);
								sm.setPatientWeight(patientWeight);
								// em.persist(sm);

								JSONObject createJsonObject = commonMethod.createJsonObject(sm);
								resultList.add(createJsonObject);

							}

						}
						requestObj.put("type", "edit"); // in case of bookmark, using getParameters API to get analysis
														// results
						requestObj.put("patient_width", "");
						requestObj.put("patient_weight", jsonRequest.get(CommonConstants.PATIENT_WEIGHT).toString());
						requestObj.put("patient_height", jsonRequest.get(CommonConstants.PATIENT_HEIGHT).toString());
						requestObj.put("data", resultList);
						// bookmark id is not necessary as this API gets called only incase of edit
//	                    requestObj.put("bookmark_id", (jsonRequest.containsKey(CommonConstants.BOOKMARK_ID) && jsonRequest.get(CommonConstants.BOOKMARK_ID)!=null) 
//	                    		?jsonRequest.get(CommonConstants.BOOKMARK_ID).toString():null);
						requestObj.put("volumeInfo", jsonRequest.get("volumeInfo"));


						String aiUrl = aiStudyUrl+ "params_calculation";

						// Dev AI URL for params
						// String aiUrl = "http://192.168.0.40:5020/params_calculation";

						// QA AI URL for params
						// String aiUrl = "http://192.168.2.39:5021/params_calculation";
						// Chandana local
						// String aiUrl = "http://192.168.3.104:5020/params_calculation";

						LOG.info("aiUrl:::::::::::" + aiUrl);
						if (activemqSwitch.equalsIgnoreCase("true")) {
							if (!commonMethod.isAIServiceRunning()) {
								response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED,
										StatusConstants.SERVER_ERROR);
								return response;
							}
						}
						RestTemplate restTemplate = new RestTemplate();

						HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.APPLICATION_JSON);
						headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

						// Request entity
						HttpEntity<String> entity = new HttpEntity<>(jsonRequest.toJSONString(), headers);

						// Send POST request
						ResponseEntity<String> res = restTemplate.exchange(
								aiUrl,
								HttpMethod.POST,
								entity,
								String.class
						);

						LOG.info("status code===> " + res.getStatusCode());

						if (res.getStatusCode() != HttpStatus.OK) {
							response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
							throw new RuntimeException("Failed : HTTP error code : " + res.getStatusCode());
						}


						JSONParser parser = new JSONParser();
						JSONObject json = (JSONObject) parser.parse(res.getBody());
						LOG.debug("json:::" + json);
						JSONObject lv_rv_data = prepareResponse(
								jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(), json);
						LOG.info("updateSeriesMeasurement API End");
						lv_rv_data.put(CommonConstants.VOLUME_INFO, json.get("volumeInfo"));
						response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, "Contours Updated");
						response.put("data", lv_rv_data);
						// response.putAll(lv_rv_data);

						System.gc();
						LOG.info("End of " + this.getClass().getName() + ".updateSeriesMeasurement "
								+ StatusConstants.SUCCESS);
						return response;
					}

				} else {

					response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
					LOG.info("End of " + this.getClass().getName() + ".updateSeriesMeasurement "
							+ StatusConstants.UNAUTHORIZED);
					return response;
				}

			} else {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".updateSeriesMeasurement "
						+ StatusConstants.BAD_REQUEST_CODE);
				return response;
			}
		} catch (Exception e) {
			LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".updateSeriesMeasurement "
					+ StatusConstants.OPERATION_FAILED);
			return response;
		}
		return response;
	}

	private JSONObject prepareResponse(String study_id, JSONObject aiResponse) {
		LOG.info("Start of " + this.getClass().getName() + ".prepareResponse");
		JSONObject response = new JSONObject();
		String sex = "m";
		List<ParameterReference> parameterReferenceList = null;

//		List<Study> stObject = em.createNamedQuery(Study.GET_STUDY_BY_STUDY_ID).setParameter(1, study_id)
//				.getResultList();

		//List<Study> stObject = studyRepo.findAllByStudyInstanceUID(study_id);
	        

		JSONObject lv = new JSONObject();
		JSONObject rv = new JSONObject();
		String patrameter = null;
		if (aiResponse != null && aiResponse.containsKey("saParams")) {	      
	        Optional<String> patSexOpt = studyExtensionRepo.findPatSexByStudyId(study_id);
	        if(patSexOpt==null || patSexOpt.isEmpty()) {
	    		return response;
	    	}
	        
	        if(patSexOpt.get().equalsIgnoreCase("f"))
	        	sex = "f";
			
			JSONObject saParams = (JSONObject) aiResponse.get("saParams");
			System.out.println("sex :::" + sex);
			lv = (JSONObject) saParams.get(CommonConstants.LV);
			rv = (JSONObject) saParams.get(CommonConstants.RV);
			patrameter = lv.toJSONString() + "BREAKFROMHERE" + rv.toJSONString();

//			parameterReferenceList = em.createNamedQuery("ParameterReference.getStudyParameterReferenceBySex")
//					.setParameter("sex", sex).getResultList();

			parameterReferenceList = parameterReferenceRepo.findBySex(sex);
		}

		List<JSONObject> responseList = commonMethod.parameterResponse(lv, rv, parameterReferenceList);
		response.put(CommonConstants.PARAMETER, responseList);
		response.put("parameterStr", patrameter);
		LOG.info("End of " + this.getClass().getName() + ".prepareResponse " + StatusConstants.SUCCESS);
		LOG.debug("Response : " + response);

		return response;

	}

	// Table has to get updated with bookmark_id column
	@Override
	public JSONObject saveSegments(JSONObject jsonRequest) {

		LOG.info("Start of " + this.getClass().getName() + ".saveSegments");
		LOG.debug("Request : " + jsonRequest);

		JSONObject response = new JSONObject();
		try {

			if (jsonRequest != null && jsonRequest.get(CommonConstants.ACCESS_KEY) != null
					&& !jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals("")
					&& jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID) != null
					&& !jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString().equals("")
					&& jsonRequest.get(CommonConstants.SEG_INFORMATION) != null
					&& !jsonRequest.get(CommonConstants.SEG_INFORMATION).toString().equals("")
					&& jsonRequest.get(CommonConstants.TYPE) != null
					&& !jsonRequest.get(CommonConstants.TYPE).toString().equals("")) {

				// Validating security token

				if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

					JSONParser jsonParser = new JSONParser();
					jsonRequest = (JSONObject) jsonParser.parse(jsonRequest.toString());
					JSONArray array = (JSONArray) jsonRequest.get(CommonConstants.SEG_INFORMATION);
					if (array.size() == 0) {
						response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE,
								StatusConstants.BAD_REQUEST);
						LOG.info("End of " + this.getClass().getName() + ".saveSegments "
								+ StatusConstants.BAD_REQUEST_CODE);
						return response;
					}

//					List<SeriesSegments> ssList = em.createNamedQuery("SeriesSegments.getByStudyId")
//							.setParameter(CommonConstants.STUDY_ID,
//									jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString())
//							.setParameter(CommonConstants.TYPE, jsonRequest.get(CommonConstants.TYPE).toString())
//							.getResultList();

					List<SeriesSegments> ssList = seriesSegmentsRepository.findByStudyIdAndType(
							jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(),
							jsonRequest.get(CommonConstants.TYPE).toString());

					if (ssList.size() > 0) {
						for (SeriesSegments ss : ssList) {
							seriesSegmentsRepository.delete(ss);

						}
					}

					for (int j = 0; j < array.size(); j++) {
						JSONObject jsonObj = (JSONObject) array.get(j);
						ArrayList instanceArray = (ArrayList) jsonObj.get(CommonConstants.INSTANCE_ARRAY);

						SeriesSegments ss = new SeriesSegments();

						ss.setInstanceArray(instanceArray);
						ss.setType(jsonRequest.get(CommonConstants.TYPE).toString());
						ss.setSeriesId(jsonObj.get(CommonConstants.SERIES_INSTANCE_UID).toString());
						ss.setStudyId(jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString());
						ss.setSegment_type(jsonObj.get("slice").toString());
						ss.setCreationDate(new Date());
						ss.setLastUpdatedDate(new Date());

						seriesSegmentsRepository.save(ss);

					}
					LOG.info("End of " + this.getClass().getName() + ".saveSegments " + StatusConstants.SUCCESS);

					/*
					 * List<Study> studyList=null;
					 * 
					 * 
					 * 
					 * //Getting record based on seriesId && studyId
					 * studyList=em.createNamedQuery(Study.GET_STUDY_BY_STUDY_ID) .setParameter(1,
					 * jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString())
					 * .getResultList(); if(studyList.size() > 0){ Study st = studyList.get(0);
					 * HashMap<String, Object> aiProcessStatus = new HashMap<String, Object>();
					 * if(st.getAiProcessStatus() != null && st.getAiProcessStatus().size() > 0){
					 * aiProcessStatus = st.getAiProcessStatus();
					 * 
					 * int size = aiProcessStatus.size();
					 * if(!aiProcessStatus.containsValue("Save Segments Done")){
					 * aiProcessStatus.put(String.valueOf(size + 1), "Save Segments Done");
					 * 
					 * } } else{
					 * 
					 * aiProcessStatus.put("1", "Save Segments Done");
					 * 
					 * } st.setAiProcessStatus(aiProcessStatus); em.merge(st);
					 * 
					 * }
					 */

					response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SAVED);
					System.gc();
					LOG.info("End of " + this.getClass().getName() + ".saveSegments " + StatusConstants.SUCCESS);
					return response;

				} else {

					response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
					LOG.info("End of " + this.getClass().getName() + ".saveSegments " + StatusConstants.UNAUTHORIZED);
					return response;
				}

			} else {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".saveSegments " + StatusConstants.BAD_REQUEST_CODE);
				return response;
			}
		} catch (Exception e) {
			LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".saveSegments " + StatusConstants.OPERATION_FAILED);
			return response;
		}
	}

	// Table has to get updated with bookmark_id column
	@Override
	public JSONObject getSeriesSegments(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + ".getSeriesSegments");
		LOG.debug("Request : " + jsonRequest);
		JSONObject response = new JSONObject();
		try {

			if (jsonRequest != null && jsonRequest.get(CommonConstants.ACCESS_KEY) != null
					&& !jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals("")
					&& jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID) != null
					&& !jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString().equals("")
					&& jsonRequest.get(CommonConstants.TYPE) != null
					&& !jsonRequest.get(CommonConstants.TYPE).toString().equals("")) {

				// Validating security token

				if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

//					List<SeriesSegments> ssList = em.createNamedQuery("SeriesSegments.getByStudyId")
//							.setParameter(CommonConstants.STUDY_ID,
//									jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString())
//							.setParameter(CommonConstants.TYPE, jsonRequest.get(CommonConstants.TYPE).toString())
//							.getResultList();

					List<SeriesSegments> ssList = seriesSegmentsRepository.findByStudyIdAndType(
							jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(),
							jsonRequest.get(CommonConstants.TYPE).toString());

					List<JSONObject> resultList = new ArrayList<JSONObject>();
					if (ssList.size() > 0) {
						for (SeriesSegments ss : ssList) {

							JSONObject createJsonObject = commonMethod.createJsonSSObject(ss);
							if (createJsonObject != null) {
								resultList.add(createJsonObject);
							}
						}
						LOG.info("End of " + this.getClass().getName() + ".getSeriesSegments "
								+ StatusConstants.SUCCESS);

						response.put(StatusConstants.STATUS_CODE, StatusConstants.SUCCESS_CODE);
						response.put(CommonConstants.DATA, resultList);
						System.gc();
						return response;

					} else {
						response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE,
								StatusConstants.BAD_REQUEST);
						LOG.info("End of " + this.getClass().getName() + ".getSeriesSegments "
								+ StatusConstants.BAD_REQUEST_CODE);
						return response;
					}
				} else {

					response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
					LOG.info("End of " + this.getClass().getName() + ".getSeriesSegments "
							+ StatusConstants.UNAUTHORIZED);
					return response;
				}

			} else {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".getSeriesSegments "
						+ StatusConstants.BAD_REQUEST_CODE);
				return response;
			}
		} catch (Exception e) {
			LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".getSeriesSegments " + StatusConstants.OPERATION_FAILED);
			return response;
		}
	}

	@Override
	@Transactional
	public JSONObject updateQflowContour(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + ".updateQflowContour");

		JSONObject response = new JSONObject();
		try {

//			if (request.getHeader(UserConstants.JWT_TOKEN) == null) {
//				response = commonMethod.createResponse(UserConstants.MISSING_JWT_TOKKEN,
//						UserConstants.BAD_REQUEST_CODE);
//				LOG.info("End of " + this.getClass().getName() + ".updateQflowContour "
//						+ UserConstants.MISSING_JWT_TOKKEN);
//				return response;
//			}
//			String jwtToken = request.getHeader(UserConstants.JWT_TOKEN).toString();
//			Map<String, Object> claims = jwtUtil.decodeJWT(jwtToken);
//			User logedUser = userDao.findById(Long.parseLong(claims.get(UserConstants.ID).toString()));
//
//			if (logedUser == null) {
//				response = commonMethod.createResponse(UserConstants.INVALID_USERID, UserConstants.UNAUTHORIZED);
//				LOG.info("End of " + this.getClass().getName() + ".updateQflowContour " + UserConstants.INVALID_USERID);
//				return response;
//			}
//
//			if (logedUser.getJwtToken() == null || !logedUser.getJwtToken().equals(jwtToken)) {
//				response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
//				LOG.info("End of " + this.getClass().getName() + ".updateQflowContour " + StatusConstants.UNAUTHORIZED);
//				return response;
//			}

			if (jsonRequest != null && jsonRequest.get(CommonConstants.ACCESS_KEY) != null
					&& !jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals("")
					&& jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID) != null
					&& !jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString().equals("")
					&& jsonRequest.get("phaseSeriesInstanceUid") != null
					&& !jsonRequest.get("phaseSeriesInstanceUid").toString().equals("")
					&& jsonRequest.get("magnitudeSeriesInstanceUid") != null
					&& !jsonRequest.get("magnitudeSeriesInstanceUid").toString().equals("")
					&& jsonRequest.get(CommonConstants.INSTANCE_ARRAY) != null
					&& !jsonRequest.get(CommonConstants.INSTANCE_ARRAY).toString().equals("")) {

				// Validating security token
				System.gc();
				if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

					ArrayList instanceArray = (ArrayList) jsonRequest.get(CommonConstants.INSTANCE_ARRAY);

//					Bookmarks preprocessBookmark = (Bookmarks) em
//							.createNamedQuery(Bookmarks.FINDID_BY_STUDY_AND_VERSION)
//							.setParameter(CommonConstants.STUDY_ID,
//									jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString())
//							.setParameter("version", 0).getResultList().get(0);

					Long preprocessBookmark = bookmarkRepo.findBookmarkIdByStudyIdAndVersion(
							jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(), 0);

//					List<SeriesMeasurements> smObjectPreprocess = em
//							.createNamedQuery(SeriesMeasurements.FIND_BY_STUDY_SERIES_BOOKMARK)
//							.setParameter(CommonConstants.STUDY_ID,
//									jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString())
//							.setParameter(CommonConstants.SERIES_ID,
//									jsonRequest.get("phaseSeriesInstanceUid").toString())
//							.setParameter(CommonConstants.BOOKMARK_ID, preprocessBookmark.getId()).getResultList();

					List<SeriesMeasurements> smObjectPreprocess = seriesMeasurementsRepository
							.findByStudyIdAndSeriesIdAndBookmarkId(
									jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(),
									jsonRequest.get("phaseSeriesInstanceUid").toString(), preprocessBookmark);

//					List<SeriesMeasurements> smObjectPreprocess2 = em
//							.createNamedQuery(SeriesMeasurements.FIND_BY_STUDY_SERIES_BOOKMARK)
//							.setParameter(CommonConstants.STUDY_ID,
//									jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString())
//							.setParameter(CommonConstants.SERIES_ID,
//									jsonRequest.get("magnitudeSeriesInstanceUid").toString())
//							.setParameter(CommonConstants.BOOKMARK_ID, preprocessBookmark.getId()).getResultList();

					List<SeriesMeasurements> smObjectPreprocess2 = seriesMeasurementsRepository
							.findByStudyIdAndSeriesIdAndBookmarkId(
									jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(),
									jsonRequest.get("magnitudeSeriesInstanceUid").toString(),
									preprocessBookmark);

					SeriesMeasurements sm = new SeriesMeasurements();
					sm.setSeriesId(jsonRequest.get("phaseSeriesInstanceUid").toString());
					sm.setStudyId(jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString());
					sm.setInstanceArray(instanceArray);
					if (smObjectPreprocess.size() > 0) {
						sm.setCommonData(smObjectPreprocess.get(smObjectPreprocess.size() - 1).getCommonData());
					}
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

					sm.setCreationDate(dateFormat.format(new Date()));
					sm.setLastUpdatedDate(dateFormat.format(new Date()));

					SeriesMeasurements sm2 = new SeriesMeasurements();
					sm2.setSeriesId(jsonRequest.get("magnitudeSeriesInstanceUid").toString());
					sm2.setStudyId(jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString());
					sm2.setInstanceArray(instanceArray);
					if (smObjectPreprocess2.size() > 0) {
						sm2.setCommonData(smObjectPreprocess2.get(smObjectPreprocess2.size() - 1).getCommonData());
					}
					SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

					sm2.setCreationDate(dateFormat2.format(new Date()));
					sm2.setLastUpdatedDate(dateFormat2.format(new Date()));

					// em.persist(sm2);

					LOG.info("Hitting AI API");

					String aiUrl = aiStudyUrl + "QflowCustomParams";

					// Dev AI URL for params
					// String aiUrl = "http://192.168.0.40:5020/QflowCustomParams";

					// QA AI URL for params
					// String aiUrl = "http://192.168.2.39:5021/QflowCustomParams";
					// Chandana local
					// String aiUrl = "http://192.168.3.104:5020/QflowCustomParams";

					response.put("type", "edit");
					response.put("isAll", "yes");

					if (activemqSwitch.equalsIgnoreCase("true")) {
						if (!commonMethod.isAIServiceRunning()) {
							response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED,
									StatusConstants.SERVER_ERROR);
							return response;
						}
					}
					RestTemplate restTemplate = new RestTemplate();

					// String
					// aiUrl=CommonMethodImpl.env.getProperty(CommonConstants.AI_STUDY_URL)+studyUID+"/"+orgId+"/"+userId+"/"+isAll;
					LOG.info("AI url=============>" + aiUrl);
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

					// Request entity
					HttpEntity<String> entity = new HttpEntity<>(jsonRequest.toJSONString(), headers);

					// Send POST request
					ResponseEntity<String> res = restTemplate.exchange(
							aiUrl,
							HttpMethod.POST,
							entity,
							String.class
					);

					LOG.info("status code===> " + res.getStatusCode());

					if (res.getStatusCode() != HttpStatus.OK) {
						response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
						throw new RuntimeException("Failed : HTTP error code : " + res.getStatusCode());
					}


					LOG.debug("output::::" + res.getBody());
					JSONParser parser = new JSONParser();
					JSONObject json = (JSONObject) parser.parse(res.getBody());
					LOG.debug("json:::" + json);
					response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, "Contours Updated");

					response.put(CommonConstants.DATA, json);
					System.gc();

					LOG.info("End of " + this.getClass().getName() + ".updateQflowContour " + StatusConstants.SUCCESS);

					return response;

				} else {

					response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
					LOG.info("End of " + this.getClass().getName() + ".updateQflowContour "
							+ StatusConstants.UNAUTHORIZED);
					return response;
				}

			} else {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".updateQflowContour "
						+ StatusConstants.BAD_REQUEST_CODE);
				return response;
			}
		} catch (Exception e) {
			LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".updateQflowContour " + StatusConstants.OPERATION_FAILED);
			return response;
		}
	}

	@Transactional
	@Override
	public JSONObject deleteContours(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + ".deleteContours");

		JSONObject response = new JSONObject();
		try {

			ValidationResult validationResult = RequestValidator.validateRequestWithDetails(jsonRequest, CommonConstants.ACCESS_KEY,
					CommonConstants.STUDY_INSTANCE_UID);
			if(!validationResult.isValid()) {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".deleteContours " + StatusConstants.BAD_REQUEST_CODE);
				return response;
			}
			if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {
				System.gc();
				// Considering that contours are deleted only for preprocess

				Long preprocessBookmark = bookmarkRepo.findBookmarkIdByStudyIdAndVersion(
						jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(), 0);

				int deletedCount = seriesMeasurementsRepository.deleteByStudyIdAndBookmarkId(
				        jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(),
				        preprocessBookmark
				);

				LOG.info("Deleted {} SeriesMeasurements for studyId={} and bookmarkId={}", 
				         deletedCount, 
				         jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(), 
				         preprocessBookmark);
				
				LOG.info("End of " + this.getClass().getName() + ".deleteContours " + StatusConstants.SUCCESS);

				response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, "Contours Deleted");
				return response;
			} else {
				response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
				LOG.info("End of " + this.getClass().getName() + ".deleteContours " + StatusConstants.UNAUTHORIZED);
				return response;
			}
			
		} catch (Exception e) {
			LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".deleteContours " + StatusConstants.OPERATION_FAILED);
			return response;
		}
	}

	@Override
	public JSONObject updateGLSSeriesMeasurement(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + ".updateGLSSeriesMeasurement");
		LOG.debug("Request : " + jsonRequest);

		JSONObject response = new JSONObject();
		try {
			
			ValidationResult validationResult = RequestValidator.validateRequestWithDetails(jsonRequest, CommonConstants.ACCESS_KEY,CommonConstants.ORG_ID,
					CommonConstants.STUDY_INSTANCE_UID,CommonConstants.TWOCH_SERIES_ID,CommonConstants.FOURCH_SERIES_ID,"twoChInstanceArray",
					"fourChInstanceArray","paramsType",CommonConstants.ORG_ID,CommonConstants.USER_ID,"edited_info");
			
			if(!validationResult.isValid()) {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".updateGLSSeriesMeasurement "
						+ StatusConstants.BAD_REQUEST_CODE);
				return response;
			}

			// Validating security token
			System.gc();
			if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

				ArrayList twoChInstanceArray = (ArrayList) jsonRequest.get("twoChInstanceArray");
				ArrayList fourChInstanceArray = (ArrayList) jsonRequest.get("fourChInstanceArray");
				
				Long preprocessBookmark = bookmarkRepo.findBookmarkIdByStudyIdAndVersion(
						jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(), 0);

				List<HashMap<String, Object>> smObjectPreprocess = seriesMeasurementsRepository
						.findCommonDataByStudyIdAndSeriesIdAndBookmarkId(
								jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(),
								jsonRequest.get(CommonConstants.TWOCH_SERIES_ID).toString(),
								preprocessBookmark);

				List<HashMap<String, Object>> smObjectPreprocess2 = seriesMeasurementsRepository
						.findCommonDataByStudyIdAndSeriesIdAndBookmarkId(
								jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(),
								jsonRequest.get(CommonConstants.FOURCH_SERIES_ID).toString(),
								preprocessBookmark);

				List<JSONObject> resultList = new ArrayList<JSONObject>();

				LinkedHashMap twoChJson = null;
				ArrayList<HashMap<String, Object>> twoChArray = new ArrayList<>();
				if (twoChInstanceArray.size() > 0) {
					twoChJson = (LinkedHashMap) twoChInstanceArray.get(0);
					twoChArray = (ArrayList<HashMap<String, Object>>) twoChJson.get(CommonConstants.INSTANCE_ARRAY);
				}
				JSONObject createJsonObject = new JSONObject();
				
				createJsonObject.put(CommonConstants.INSTANCE_ARRAY, twoChArray != null ? twoChArray : "");
				createJsonObject.put(CommonConstants.COMMON_DATA, smObjectPreprocess.get(smObjectPreprocess.size() - 1) != null ? smObjectPreprocess.get(smObjectPreprocess.size() - 1) : "");
				if (createJsonObject != null) {
					createJsonObject.put(CommonConstants.STUDY_INSTANCE_UID, jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString());
					createJsonObject.put(CommonConstants.SERIES_INSTANCE_UID, jsonRequest.get(CommonConstants.TWOCH_SERIES_ID).toString());
					createJsonObject.put(CommonConstants.IMAGE_TYPE, "GLS_2CH");
					resultList.add(createJsonObject);
				}
				LinkedHashMap fourChJson = null;
				ArrayList<HashMap<String, Object>> fourChArray = new ArrayList<>();
				if (fourChInstanceArray.size() > 0) {
					fourChJson = (LinkedHashMap) fourChInstanceArray.get(0);
					fourChArray = (ArrayList<HashMap<String, Object>>) fourChJson
							.get(CommonConstants.INSTANCE_ARRAY);
				}
				JSONObject createJsonObject2 = new JSONObject();
				createJsonObject2.put(CommonConstants.INSTANCE_ARRAY, fourChArray != null ? fourChArray : "");
				createJsonObject2.put(CommonConstants.COMMON_DATA, smObjectPreprocess2.get(smObjectPreprocess2.size() - 1) != null ? smObjectPreprocess2.get(smObjectPreprocess2.size() - 1) : "");
				if (createJsonObject2 != null) {
					createJsonObject2.put(CommonConstants.STUDY_INSTANCE_UID, jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString());
					createJsonObject2.put(CommonConstants.SERIES_INSTANCE_UID, jsonRequest.get(CommonConstants.FOURCH_SERIES_ID).toString());
					createJsonObject2.put(CommonConstants.IMAGE_TYPE, "GLS_4CH");
					resultList.add(createJsonObject2);
				}

				LOG.info("Hitting AI API");

				String aiUrl = aiStudyUrl + "GLSCustomParams";
				System.out.println("aiUrl:"+aiUrl);
				JSONObject requestObj = new JSONObject();
				requestObj.put(CommonConstants.TYPE, "edit");
				requestObj.put(CommonConstants.DATA, resultList);
				requestObj.put(CommonConstants.STUDY_INSTANCE_UID,
						jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID));
				requestObj.put("paramsType", jsonRequest.get("paramsType"));
				requestObj.put(CommonConstants.ORG_ID, jsonRequest.get(CommonConstants.ORG_ID));
				requestObj.put(CommonConstants.USER_ID, jsonRequest.get(CommonConstants.USER_ID));
				requestObj.put("isAll", "no");
				requestObj.put("edited_info", jsonRequest.get("edited_info"));
				if (jsonRequest.containsKey("reference_info")) {
					requestObj.put("reference_info", jsonRequest.get("reference_info"));
				}
				 
				try {
					String orgId = jsonRequest.get(CommonConstants.ORG_ID).toString();
		        	String authorization = pacsTokenService.getToken(orgId);
		        	requestObj.put("Authorization", authorization);
		        	requestObj.put(CommonConstants.SERVER_BASE_URL, commonMethod.getPacsUrl(orgId));
	    		} catch (Exception e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
				LOG.info("glsrequest:"+requestObj);

				if (activemqSwitch.equalsIgnoreCase("true")) {
					if (!commonMethod.isAIServiceRunning()) {
						response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED,
								StatusConstants.SERVER_ERROR);
						return response;
					}
				}
				int connectionRetryCount = 0;
				RestTemplate restTemplate = new RestTemplate();
				while (true) {
					try {
						// String
						// aiUrl=CommonMethodImpl.env.getProperty(CommonConstants.AI_STUDY_URL)+studyUID+"/"+orgId+"/"+userId+"/"+isAll;
						LOG.debug("updateGLSSeriesMeasurement : AI url=============>" + aiUrl);
						HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.APPLICATION_JSON);
						headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

						// Request entity
						HttpEntity<String> entity = new HttpEntity<>(requestObj.toJSONString(), headers);

						// Send POST request
						ResponseEntity<String> res = restTemplate.exchange(
								aiUrl,
								HttpMethod.POST,
								entity,
								String.class
						);

						LOG.info("status code===> " + res.getStatusCode());

						if (res.getStatusCode() != HttpStatus.OK) {
							response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
							throw new RuntimeException("Failed : HTTP error code : " + res.getStatusCode());
						}

						LOG.debug(" updateGLSSeriesMeasurement::: output::::" + res.getBody());
						JSONParser parser = new JSONParser();
						JSONObject json = (JSONObject) parser.parse(res.getBody());
						LOG.debug("updateGLSSeriesMeasurement:: json reponse:::" + json);
						response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, "Success");
						response.put(CommonConstants.DATA, json);
						LOG.debug("GLS response:"+response);
						System.gc();

						LOG.info("End of " + this.getClass().getName() + ".updateGLSSeriesMeasurement "
								+ StatusConstants.SUCCESS);

						return response;
//                			 break; // Exit loop if call is successful
					} catch (Exception e) {
						if (e.getCause() instanceof ConnectException
								|| e.getCause() instanceof SocketTimeoutException) {
							LOG.info("cause  ConnectException or SocketTimeoutException:::"
									+ e.getLocalizedMessage());
							LOG.info("Waiting for 20 more seconds after connection Exception");
							try {
								if (connectionRetryCount > connectionRetryLimit) {
									LOG.info("Connection retry limit reached");
									LOG.error("Exception : " + e);
									LOG.info(e.getLocalizedMessage());
									response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED,
											StatusConstants.SERVER_ERROR);
									LOG.info("End of " + this.getClass().getName() + ".updateGLSSeriesMeasurement "
											+ StatusConstants.OPERATION_FAILED);
									return response;
								}
								Thread.sleep(20000);
								connectionRetryCount++;
							} catch (InterruptedException ie) {
								Thread.currentThread().interrupt();
								LOG.error("Retry interrupted, stopping retries.");
								LOG.error("Exception : " + e);
								LOG.info(e.getLocalizedMessage());
								response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED,
										StatusConstants.SERVER_ERROR);
								LOG.info("End of " + this.getClass().getName() + ".updateGLSSeriesMeasurement "
										+ StatusConstants.OPERATION_FAILED);
								return response;
							}
						} else {
							System.out.println("Unexpected Exception :::" + e.getLocalizedMessage());
							LOG.error("Exception : " + e);
							LOG.info(e.getLocalizedMessage());
							response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED,
									StatusConstants.SERVER_ERROR);
							LOG.info("End of " + this.getClass().getName() + ".updateGLSSeriesMeasurement "
									+ StatusConstants.OPERATION_FAILED);
							return response;
						}

					}
				}
			} else {

				response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
				LOG.info("End of " + this.getClass().getName() + ".updateGLSSeriesMeasurement "
						+ StatusConstants.UNAUTHORIZED);
				return response;
			}
		} catch (Exception e) {
			LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".updateGLSSeriesMeasurement "
					+ StatusConstants.OPERATION_FAILED);
			return response;
		}
	}

	// API was written for weasis, but not in use now. This API can be used by AI to
	// save all short axis data at once. Updated API
	@Override
	public JSONObject saveStudyMeasurementsInfo(JSONObject jsonRequest) {

		LOG.info("Start of " + this.getClass().getName() + ".saveStudyMeasurementsInfo");
		LOG.debug("Request : " + jsonRequest);

		JSONObject response = new JSONObject();
		ArrayList<JSONObject> mesurementArrayList = new ArrayList<JSONObject>();
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

			if (jsonRequest != null && jsonRequest.get(CommonConstants.ACCESS_KEY) != null
					&& !jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals("")
					&& jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID) != null
					&& !jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString().equals("")
					&& jsonRequest.get(CommonConstants.DATA) != null
					&& !jsonRequest.get(CommonConstants.DATA).toString().equals("")) {

				// Validating security token

				if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

					ArrayList data = (ArrayList) jsonRequest.get("data");
					if (data != null) {
						for (Object instanceObj : data) {
							LinkedHashMap seriesInstance = (LinkedHashMap) instanceObj;
							LinkedHashMap<String, Object> commonData = (LinkedHashMap<String, Object>) seriesInstance
									.get(CommonConstants.COMMON_DATA);

							if (commonData != null && commonData.containsKey(CommonConstants.STUDY_INSTANCE_UID)
									&& seriesInstance.containsKey(CommonConstants.SERIES_INSTANCE_UID)
									&& commonData.get(CommonConstants.STUDY_INSTANCE_UID) != null
									&& !commonData.get(CommonConstants.STUDY_INSTANCE_UID).toString().equals("")
									&& seriesInstance.get(CommonConstants.SERIES_INSTANCE_UID) != null
									&& !seriesInstance.get(CommonConstants.SERIES_INSTANCE_UID).toString().equals("")) {

								String bookmarkId = jsonRequest.containsKey(CommonConstants.BOOKMARK_ID)
										? jsonRequest.get(CommonConstants.BOOKMARK_ID) != null
												&& jsonRequest.get(CommonConstants.BOOKMARK_ID) != ""
														? (String) jsonRequest.get(CommonConstants.BOOKMARK_ID)
														: null
										: null;

								// Getting bookmark details
								// Creating new bookmark if doesnt exist
								Bookmarks bookmark = bookmarkService.getBookmark(
										commonData.get(CommonConstants.STUDY_INSTANCE_UID).toString(), bookmarkId,
										true);

//								List<SeriesMeasurements> smObjectDelete = em
//										.createNamedQuery(SeriesMeasurements.FIND_BY_STUDY_SERIES_BOOKMARK)
//										.setParameter(CommonConstants.STUDY_ID,
//												commonData.get(CommonConstants.STUDY_INSTANCE_UID).toString())
//										.setParameter(CommonConstants.SERIES_ID,
//												seriesInstance.get(CommonConstants.SERIES_INSTANCE_UID).toString())
//										.setParameter(CommonConstants.BOOKMARK_ID, bookmark.getId()).getResultList();


								// At AI, before saving updated contours, calling delete API to delete existing
								// records
//								for(SeriesMeasurements sm: smObjectDelete){
//									em.remove(sm);
//
//								}
								// getting array list of series data
								ArrayList instanceArray = (ArrayList) seriesInstance
										.get(CommonConstants.INSTANCE_ARRAY);
								LOG.info("Series id:"
										+ seriesInstance.get(CommonConstants.SERIES_INSTANCE_UID).toString()
										+ " InstanceArray size:" + instanceArray.size());
								if (instanceArray != null && instanceArray.size() > 0) {

									// Preparing common data
									commonData = commonMethod.createCommonDataJson(commonData);

									SeriesMeasurements smObject = new SeriesMeasurements();
									smObject = commonMethod.setSeriesMeasurementsObjectForFreeHand(smObject,
											mesurementArrayList,
											seriesInstance.get(CommonConstants.SERIES_INSTANCE_UID).toString(),
											jsonRequest, commonData);
									smObject.setCreationDate(dateFormat.format(new Date()));
									smObject.setLastUpdatedDate(dateFormat.format(new Date()));
									smObject.setInstanceArray(instanceArray);
									smObject.setBookmark(bookmark);
									smObject.setVersion(bookmark.getVersion());
									seriesMeasurementsRepository.save(smObject);
								}

								// return response;
							} else {
								response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE,
										StatusConstants.BAD_REQUEST);
								// return response;
							}
						}
						response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SAVED);
						System.gc();
						LOG.info("End of " + this.getClass().getName() + ".saveStudyMeasurementsInfo "
								+ StatusConstants.SUCCESS);
						return response;
					} else {
						response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE,
								StatusConstants.BAD_REQUEST);
						LOG.info("End of " + this.getClass().getName() + ".saveStudyMeasurementsInfo "
								+ StatusConstants.BAD_REQUEST_CODE);
						return response;
					}
				} else {

					response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
					LOG.info("End of " + this.getClass().getName() + ".saveStudyMeasurementsInfo "
							+ StatusConstants.UNAUTHORIZED);
					return response;
				}

			} else {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".saveStudyMeasurementsInfo "
						+ StatusConstants.BAD_REQUEST_CODE);
				return response;
			}
		} catch (Exception e) {
			LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".saveStudyMeasurementsInfo "
					+ StatusConstants.OPERATION_FAILED);
			return response;
		}
	}

	// Didnt update
	@Override
	public JSONObject updateDESeriesMeasurement(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + "updateDESeriesMeasurement");
		LOG.debug("Request : " + jsonRequest);

		JSONObject response = new JSONObject();
		try {

//			if (request.getHeader(UserConstants.JWT_TOKEN) == null) {
//
//				response = commonMethod.createResponse(UserConstants.MISSING_JWT_TOKKEN,
//						UserConstants.BAD_REQUEST_CODE);
//				LOG.info("End of " + this.getClass().getName() + ".updateDESeriesMeasurement "
//						+ UserConstants.MISSING_JWT_TOKKEN);
//				return response;
//			}
//			String jwtToken = request.getHeader(UserConstants.JWT_TOKEN).toString();
//			Map<String, Object> claims = jwtUtil.decodeJWT(jwtToken);
//			User logedUser = userDao.findById(Long.parseLong(claims.get(UserConstants.ID).toString()));
//
//			if (logedUser == null) {
//				response = commonMethod.createResponse(UserConstants.INVALID_USERID, UserConstants.UNAUTHORIZED);
//				LOG.info("End of " + this.getClass().getName() + ".updateDESeriesMeasurement "
//						+ UserConstants.INVALID_USERID);
//				return response;
//			}
//
//			if (logedUser.getJwtToken() == null || !logedUser.getJwtToken().equals(jwtToken)) {
//				response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
//				LOG.info("End of " + this.getClass().getName() + ".updateDESeriesMeasurement "
//						+ StatusConstants.UNAUTHORIZED);
//				return response;
//			}

			if (jsonRequest != null && jsonRequest.get(CommonConstants.ACCESS_KEY) != null
					&& !jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals("")
					&& jsonRequest.get(CommonConstants.STUDY_ID) != null
					&& !jsonRequest.get(CommonConstants.STUDY_ID).toString().equals("")
					&& jsonRequest.get(CommonConstants.DE_SERIES_ID) != null
					&& !jsonRequest.get(CommonConstants.DE_SERIES_ID).toString().equals("")
					&& jsonRequest.get(CommonConstants.SHORT_AXIS_CONTOUR) != null
					&& !jsonRequest.get(CommonConstants.SHORT_AXIS_CONTOUR).toString().equals("")) {

				// Validating security token
				System.gc();

				if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

					LinkedHashMap<String, Object> shortAxisData = (LinkedHashMap<String, Object>) jsonRequest
							.get(CommonConstants.SHORT_AXIS_CONTOUR);
					ArrayList shortAxisContour = (ArrayList) shortAxisData.get("data");

					List<JSONObject> resultList = new ArrayList<JSONObject>();
					if (shortAxisContour != null && shortAxisContour.size() > 0) {

//						Bookmarks preprocessBookmark = (Bookmarks) em
//								.createNamedQuery(Bookmarks.FINDID_BY_STUDY_AND_VERSION)
//								.setParameter(CommonConstants.STUDY_ID,
//										jsonRequest.get(CommonConstants.STUDY_ID).toString())
//								.setParameter("version", 0).getResultList().get(0);

						Long preprocessBookmark = bookmarkRepo.findBookmarkIdByStudyIdAndVersion(
								jsonRequest.get(CommonConstants.STUDY_ID).toString(), 0);

						// Looping through list
						for (int i = 0; i < shortAxisContour.size(); i++) {
							HashMap seriesObj = (HashMap) shortAxisContour.get(i);
							ArrayList<JSONObject> instanceArrayList = (ArrayList<JSONObject>) seriesObj
									.get(CommonConstants.INSTANCE_ARRAY);
							if (seriesObj.get(CommonConstants.SERIES_INSTANCE_UID) != null
									&& !seriesObj.get(CommonConstants.SERIES_INSTANCE_UID).toString().equals("")
									&& seriesObj.get(CommonConstants.INSTANCE_ARRAY) != null
									&& !seriesObj.get(CommonConstants.INSTANCE_ARRAY).toString().equals("")) {

								if (instanceArrayList == null || instanceArrayList.size() == 0) {
									response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE,
											StatusConstants.EMPTY_MESUREMENT);
									LOG.info("End of " + this.getClass().getName() + ".updateDESeriesMeasurement "
											+ StatusConstants.BAD_REQUEST_CODE);
									return response;
								}

								// Getting record based on seriesId && studyId
								List<SeriesMeasurements> smObjectList = null;
//								smObjectList = em.createNamedQuery(SeriesMeasurements.FIND_BY_STUDY_SERIES)
//										.setParameter(CommonConstants.STUDY_ID,
//												jsonRequest.get(CommonConstants.STUDY_ID).toString())
//										.setParameter(CommonConstants.SERIES_ID,
//												seriesObj.get(CommonConstants.SERIES_INSTANCE_UID).toString())
//										.setParameter(CommonConstants.BOOKMARK_ID, preprocessBookmark.getId())
//										.getResultList();

								smObjectList = seriesMeasurementsRepository.findByStudyIdAndSeriesIdAndBookmarkId(
										jsonRequest.get(CommonConstants.STUDY_ID).toString(),
										seriesObj.get(CommonConstants.SERIES_INSTANCE_UID).toString(),
										preprocessBookmark);

								if (smObjectList != null && smObjectList.size() > 0) {
									SeriesMeasurements sm = new SeriesMeasurements();
									sm.setCommonData(smObjectList.get(0).getCommonData());
									sm.setInstanceArray((ArrayList) seriesObj.get(CommonConstants.INSTANCE_ARRAY));
									JSONObject createJsonObject = commonMethod.createJsonObject(sm);
									if (createJsonObject != null) {
										resultList.add(createJsonObject);
									}
								}
							}
						}
					}

					shortAxisData.put("data", resultList);
					jsonRequest.put(CommonConstants.SHORT_AXIS_CONTOUR, shortAxisData);

					String aiUrl = aiStudyUrl + "DEPreprocessAPI";
					// String aiUrl = "http://192.168.3.155:5020/DEPreprocessAll";
					System.out.println("updateDESeriesMeasurement ::: AI Call request :: ");
					if (activemqSwitch.equalsIgnoreCase("true")) {
						if (!commonMethod.isAIServiceRunning()) {
							response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED,
									StatusConstants.SERVER_ERROR);
							return response;
						}
					}


					RestTemplate restTemplate = new RestTemplate();
					// String
					// aiUrl=CommonMethodImpl.env.getProperty(CommonConstants.AI_STUDY_URL)+studyUID+"/"+orgId+"/"+userId+"/"+isAll;
					LOG.info("updateDESeriesMeasurement : AI url=============>" + aiUrl);
					// Prepare headers
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

					// Request entity
					HttpEntity<String> entity = new HttpEntity<>(jsonRequest.toJSONString(), headers);

					// Send POST request
					ResponseEntity<String> res = restTemplate.exchange(
							aiUrl,
							HttpMethod.POST,
							entity,
							String.class
					);

					LOG.info("status code===> " + res.getStatusCode());

					if (res.getStatusCode() != HttpStatus.OK) {
						response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
						throw new RuntimeException("Failed : HTTP error code : " + res.getStatusCode());
					}



//					LOG.info("End of " + this.getClass().getName() + ".qflowPropagation " + StatusConstants.SUCCESS);
//					return response;
					LOG.debug(" updateDESeriesMeasurement::: output::::" + res.getBody());
					JSONParser parser = new JSONParser();
					JSONObject json = (JSONObject) parser.parse(res.getBody());
					LOG.info("json reponse:::" + json.get("parameters"));
					JSONObject deParams = commonMethod.deParameterResponse((JSONObject) (json.get("parameters")));
					LOG.info("updateDESeriesMeasurement API End");
					json.put("parameters", deParams);
					response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, "Contours Updated");
					response.put("data", json);
					System.gc();

					LOG.info("End of " + this.getClass().getName() + ".updateDESeriesMeasurement "
							+ StatusConstants.SUCCESS);

					return response;

				} else {

					response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
					LOG.info("End of " + this.getClass().getName() + ".updateDESeriesMeasurement "
							+ StatusConstants.UNAUTHORIZED);
					return response;
				}

			} else {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".updateDESeriesMeasurement "
						+ StatusConstants.BAD_REQUEST_CODE);
				return response;
			}
		} catch (Exception e) {
			LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".updateDESeriesMeasurement "
					+ StatusConstants.OPERATION_FAILED);
			return response;
		}
	}

	@Override
	public JSONObject shortAxisPropagation(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + ".shortAxisPropagation");
		LOG.debug("Request : " + jsonRequest);

		JSONObject response = new JSONObject();
		try {
			
			ValidationResult validationResult = RequestValidator.validateRequestWithDetails(jsonRequest,CommonConstants.ACCESS_KEY,
					CommonConstants.STUDY_ID,CommonConstants.PROPGATION_INFO,CommonConstants.SHORT_AXIS_DATA,CommonConstants.ORG_ID);
			if(!validationResult.isValid()) {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".shortAxisPropagation "
						+ StatusConstants.BAD_REQUEST_CODE);
				return response;
			}

			// Validating security token
			System.gc();

			if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

				ArrayList shortAxisData = (ArrayList) jsonRequest.get(CommonConstants.SHORT_AXIS_DATA);
				// ArrayList shortAxisContour = (ArrayList) shortAxisData.get("data");
				List shortData = new ArrayList();
				LOG.info("serieseArrayList size:::::::" + shortAxisData.size());
				if (shortAxisData != null && shortAxisData.size() > 0) {

					// Looping through list
					for (int i = 0; i < shortAxisData.size(); i++) {
						HashMap seriesObj = (HashMap) shortAxisData.get(i);
						ArrayList<JSONObject> instanceArrayList = (ArrayList<JSONObject>) seriesObj
								.get(CommonConstants.INSTANCE_ARRAY);
						if (seriesObj.get(CommonConstants.SERIES_INSTANCE_UID) != null
								&& !seriesObj.get(CommonConstants.SERIES_INSTANCE_UID).toString().equals("")
								&& seriesObj.get(CommonConstants.INSTANCE_ARRAY) != null
								&& !seriesObj.get(CommonConstants.INSTANCE_ARRAY).toString().equals("")) {

							if (instanceArrayList == null || instanceArrayList.size() == 0) {
								response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE,
										StatusConstants.EMPTY_MESUREMENT);
								LOG.info("End of " + this.getClass().getName() + ".shortAxisPropagation "
										+ StatusConstants.BAD_REQUEST_CODE);
								return response;
							}
							
							Long preprocessBookmark = bookmarkRepo.findBookmarkIdByStudyIdAndVersion(jsonRequest.get(CommonConstants.STUDY_ID).toString(), 0);

							// Getting record based on seriesId && studyId
							// Getting preprocess record, to get common data
							List<HashMap<String, Object>> smObjectList = null;

							smObjectList = seriesMeasurementsRepository.findCommonDataByStudyIdAndSeriesIdAndBookmarkId(
									jsonRequest.get(CommonConstants.STUDY_ID).toString(),
									seriesObj.get(CommonConstants.SERIES_INSTANCE_UID).toString(),
									preprocessBookmark);

							if (smObjectList != null && smObjectList.size() > 0) {
								seriesObj.put("commonData", smObjectList.get(0) != null ? smObjectList.get(0) : "");
								shortData.add(seriesObj);
							}
						}
					}
				}

				jsonRequest.put(CommonConstants.SHORT_AXIS_DATA, shortData);
				String orgId = jsonRequest.get(CommonConstants.ORG_ID).toString();
				jsonRequest.put(CommonConstants.SERVER_BASE_URL, commonMethod.getPacsUrl(orgId));
				try {
	            	String keyCloaktoken = pacsTokenService.getToken(orgId);
	    			jsonRequest.put("Authorization", keyCloaktoken);
	    			jsonRequest.put("keycloak_switch", commonMethod.getKeyClokaSwitch());
	    		} catch (Exception e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
				String aiUrl = aiStudyUrl + "shortAxisPropagation";
				LOG.info("shortAxisPropagation : AI url=============>" + aiUrl);

				if (activemqSwitch.equalsIgnoreCase("true")) {
					if (!commonMethod.isAIServiceRunning()) {
						response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
						return response;
					}
				}

				int connectionRetryCount = 0;
				RestTemplate restTemplate = new RestTemplate();

				while (true) {
					try {
						// Prepare headers
						HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.APPLICATION_JSON);
						headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

						// Request entity
						HttpEntity<String> entity = new HttpEntity<>(jsonRequest.toJSONString(), headers);

						// Send POST request
						ResponseEntity<String> res = restTemplate.exchange(
								aiUrl,
								HttpMethod.POST,
								entity,
								String.class
						);

						LOG.info("status code===> " + res.getStatusCodeValue());

						if (res.getStatusCode() != HttpStatus.OK) {
							response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
							throw new RuntimeException("Failed : HTTP error code : " + res.getStatusCodeValue());
						}

						// Parse response
						JSONParser parser = new JSONParser();
						JSONObject json = (JSONObject) parser.parse(res.getBody());

						response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, "Contours Updated");
						response.put("data", json);

						LOG.info("End of " + this.getClass().getName() + ".shortAxisPropagation " + StatusConstants.SUCCESS);
						return response;

					} catch (Exception ex) {
						Throwable cause = ex.getCause();

						if (cause instanceof ConnectException || cause instanceof SocketTimeoutException) {
							LOG.warn("Connection issue: " + ex.getLocalizedMessage());
							if (connectionRetryCount >= connectionRetryLimit) {
								LOG.error("Connection retry limit reached");
								response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
								return response;
							}
							try {
								Thread.sleep(20000);
								connectionRetryCount++;
								LOG.info("Retrying connection attempt #" + connectionRetryCount);
							} catch (InterruptedException ie) {
								Thread.currentThread().interrupt();
								LOG.error("Retry interrupted");
								response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
								return response;
							}
						} else {
							LOG.error("Unexpected Exception: " + ex.getLocalizedMessage(), ex);
							response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
							return response;
						}

					}
				}
			} else {

				response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
				LOG.info("End of " + this.getClass().getName() + ".shortAxisPropagation "
						+ StatusConstants.UNAUTHORIZED);
				return response;
			}
		} catch (Exception e) {
			LOG.error("Exception : " + e.getLocalizedMessage());
			LOG.info(e.getLocalizedMessage());
			e.printStackTrace();
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".shortAxisPropagation "
					+ StatusConstants.OPERATION_FAILED);
			return response;
		}
	}

	@Override
	public JSONObject qFlowPropagation(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + ".qFlowPropagation");
		LOG.debug("Request : " + jsonRequest);

		JSONObject response = new JSONObject();
		try {
			
			ValidationResult validationResult = RequestValidator.validateRequestWithDetails(jsonRequest, CommonConstants.ACCESS_KEY,
					CommonConstants.STUDY_INSTANCE_UID,CommonConstants.PROPGATION_INFO,CommonConstants.INSTANCE_ARRAY,CommonConstants.ORG_ID);
			if(!validationResult.isValid()) {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".qFlowPropagation "
						+ StatusConstants.BAD_REQUEST_CODE);
				return response;
			}

			// Validating security token
			System.gc();

			if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

				LOG.info("Hitting AI API :::  QFlowPropagation:::");
				ArrayList propagationData = (ArrayList) jsonRequest.get(CommonConstants.PROPGATION_INFO);
				if (propagationData != null && propagationData.size() > 0) {

					HashMap seriesObj = (HashMap) propagationData.get(0);
					if (seriesObj.get(CommonConstants.SERIES_INSTANCE_UID) != null
							&& !seriesObj.get(CommonConstants.SERIES_INSTANCE_UID).toString().equals("")) {
						
						Long preprocessBookmark = bookmarkRepo.findBookmarkIdByStudyIdAndVersion(jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(), 0);

						// Getting record based on seriesId && studyId
						List<HashMap<String, Object>> smObjectList = null;

						smObjectList = seriesMeasurementsRepository.findCommonDataByStudyIdAndSeriesIdAndBookmarkId(
								jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(),
								seriesObj.get(CommonConstants.SERIES_INSTANCE_UID).toString(), preprocessBookmark);

						if (smObjectList != null && smObjectList.size() > 0) {
							jsonRequest.put("commonData", smObjectList.get(0) != null ? smObjectList.get(0) : "");
						}
					}

				}
				
				String orgId = jsonRequest.get(CommonConstants.ORG_ID).toString();
				jsonRequest.put(CommonConstants.SERVER_BASE_URL, commonMethod.getPacsUrl(orgId));
				try {
	            	String keyCloaktoken = pacsTokenService.getToken(orgId);
		    		jsonRequest.put("Authorization", keyCloaktoken);
	    			jsonRequest.put("keycloak_switch", commonMethod.getKeyClokaSwitch());
	    		} catch (Exception e) { 
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
				String aiUrl = aiStudyUrl + "qflowPropagation";
				// String aiUrl = "http://192.168.0.40:5020/qflowPropagation";
				// System.out.println("shortAxisPropagation ::: AI Call request :: " +
				// jsonRequest);
				if (activemqSwitch.equalsIgnoreCase("true")) {
					if (!commonMethod.isAIServiceRunning()) {
						response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED,
								StatusConstants.SERVER_ERROR);
						return response;
					}
				}
				int connectionRetryCount = 0;
				RestTemplate restTemplate = new RestTemplate();
				while (true) {
					try {
						// Prepare headers
						HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.APPLICATION_JSON);
						headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

						// Request entity
						HttpEntity<String> entity = new HttpEntity<>(jsonRequest.toJSONString(), headers);

						// Send POST request
						ResponseEntity<String> res = restTemplate.exchange(
								aiUrl,
								HttpMethod.POST,
								entity,
								String.class
						);

						LOG.info("status code===> " + res.getStatusCodeValue());

						if (res.getStatusCode() != HttpStatus.OK) {
							response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
							throw new RuntimeException("Failed : HTTP error code : " + res.getStatusCodeValue());
						}

						// Parse response
						JSONParser parser = new JSONParser();
						JSONObject json = (JSONObject) parser.parse(res.getBody());

						response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, "Contours Updated");
						response.put("data", json);

						LOG.info("End of " + this.getClass().getName() + ".qflowPropagation " + StatusConstants.SUCCESS);
						return response;

					} catch (Exception ex) {
						Throwable cause = ex.getCause();

						if (cause instanceof ConnectException || cause instanceof SocketTimeoutException) {
							LOG.warn("Connection issue: " + ex.getLocalizedMessage());
							if (connectionRetryCount >= connectionRetryLimit) {
								LOG.error("Connection retry limit reached");
								response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
								return response;
							}
							try {
								Thread.sleep(20000);
								connectionRetryCount++;
								LOG.info("Retrying connection attempt #" + connectionRetryCount);
							} catch (InterruptedException ie) {
								Thread.currentThread().interrupt();
								LOG.error("Retry interrupted");
								response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
								return response;
							}
						} else {
							LOG.error("Unexpected Exception: " + ex.getLocalizedMessage(), ex);
							response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
							return response;
						}

					}
				}
			} else {

				response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
				LOG.info("End of " + this.getClass().getName() + ".qFlowPropagation "
						+ StatusConstants.UNAUTHORIZED);
				return response;
			}

		} catch (Exception e) {
			LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".qFlowPropagation " + StatusConstants.OPERATION_FAILED);
			return response;
		}
	}
	
	@Override
	public JSONObject glsPropagation(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + ".glsPropagation");
		LOG.debug("Request : " + jsonRequest);

		JSONObject response = new JSONObject();
		try {
			
			ValidationResult validationResult = RequestValidator.validateRequestWithDetails(jsonRequest, CommonConstants.ACCESS_KEY,
					CommonConstants.STUDY_INSTANCE_UID,CommonConstants.PROPGATION_INFO,CommonConstants.DATA,CommonConstants.ORG_ID);
			if(!validationResult.isValid()) {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".glsPropagation"
						+ StatusConstants.BAD_REQUEST_CODE);
				return response;
			}

			// Validating security token
			System.gc();

			if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

				LOG.info("Hitting AI API :::  glsPropagation:::");
				ArrayList propagationData = (ArrayList) jsonRequest.get(CommonConstants.PROPGATION_INFO);
				if (propagationData != null && propagationData.size() > 0) {

					HashMap seriesObj = (HashMap) propagationData.get(0);
					if (seriesObj.get(CommonConstants.SERIES_INSTANCE_UID) != null
							&& !seriesObj.get(CommonConstants.SERIES_INSTANCE_UID).toString().equals("")) {
						
						Long preprocessBookmark = bookmarkRepo.findBookmarkIdByStudyIdAndVersion(jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(), 0);

						// Getting record based on seriesId && studyId
						List<HashMap<String, Object>> smObjectList = null;

						smObjectList = seriesMeasurementsRepository.findCommonDataByStudyIdAndSeriesIdAndBookmarkId(
								jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(),
								seriesObj.get(CommonConstants.SERIES_INSTANCE_UID).toString(), preprocessBookmark);

						if (smObjectList != null && smObjectList.size() > 0) {
							jsonRequest.put("commonData", smObjectList.get(0) != null ? smObjectList.get(0) : "");
						}
					}

				}
				
				String orgId = jsonRequest.get(CommonConstants.ORG_ID).toString();
				jsonRequest.put(CommonConstants.SERVER_BASE_URL, commonMethod.getPacsUrl(orgId));
				try {
	            	String keyCloaktoken = pacsTokenService.getToken(orgId);
		    		jsonRequest.put("Authorization", keyCloaktoken);
	    			jsonRequest.put("keycloak_switch", commonMethod.getKeyClokaSwitch());
	    		} catch (Exception e) { 
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
				String aiUrl = aiStudyUrl + "GLSPropagation";
				// String aiUrl = "http://192.168.0.40:5020/qflowPropagation";
				 LOG.debug("glsPropagation ::: AI Call request :: " +
				 jsonRequest);
				if (activemqSwitch.equalsIgnoreCase("true")) {
					if (!commonMethod.isAIServiceRunning()) {
						response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED,
								StatusConstants.SERVER_ERROR);
						return response;
					}
				}
				int connectionRetryCount = 0;
				RestTemplate restTemplate = new RestTemplate();
				while (true) {
					try {
						// Prepare headers
						HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.APPLICATION_JSON);
						headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

						// Request entity
						HttpEntity<String> entity = new HttpEntity<>(jsonRequest.toJSONString(), headers);

						// Send POST request
						ResponseEntity<String> res = restTemplate.exchange(
								aiUrl,
								HttpMethod.POST,
								entity,
								String.class
						);

						LOG.info("status code===> " + res.getStatusCodeValue());

						if (res.getStatusCode() != HttpStatus.OK) {
							response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
							throw new RuntimeException("Failed : HTTP error code : " + res.getStatusCodeValue());
						}

						// Parse response
						JSONParser parser = new JSONParser();
						JSONObject json = (JSONObject) parser.parse(res.getBody());

						response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, "Contours Updated");
						response.put("data", json);

						LOG.info("End of " + this.getClass().getName() + ".glsPropagation " + StatusConstants.SUCCESS);
						return response;

					} catch (Exception ex) {
						ex.printStackTrace();
						Throwable cause = ex.getCause();

						if (cause instanceof ConnectException || cause instanceof SocketTimeoutException) {
							LOG.warn("Connection issue: " + ex.getLocalizedMessage());
							if (connectionRetryCount >= connectionRetryLimit) {
								LOG.error("Connection retry limit reached");
								response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
								return response;
							}
							try {
								Thread.sleep(20000);
								connectionRetryCount++;
								LOG.info("Retrying connection attempt #" + connectionRetryCount);
							} catch (InterruptedException ie) {
								Thread.currentThread().interrupt();
								LOG.error("Retry interrupted");
								response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
								return response;
							}
						} else {
							LOG.error("Unexpected Exception: " + ex.getLocalizedMessage());
							response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
							return response;
						}

					}
				}
			} else {

				response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
				LOG.info("End of " + this.getClass().getName() + ".glsPropagation "
						+ StatusConstants.UNAUTHORIZED);
				return response;
			}

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".glsPropagation " + StatusConstants.OPERATION_FAILED);
			return response;
		}
	}
	
	@Override
	public JSONObject atrialPropagation(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + ".atrialPropagation");
		LOG.debug("Request : " + jsonRequest);

		JSONObject response = new JSONObject();
		try {
			
			ValidationResult validationResult = RequestValidator.validateRequestWithDetails(jsonRequest, CommonConstants.ACCESS_KEY,
					CommonConstants.STUDY_INSTANCE_UID,CommonConstants.PROPGATION_INFO,CommonConstants.DATA,CommonConstants.ORG_ID);
			if(!validationResult.isValid()) {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".atrialPropagation"
						+ StatusConstants.BAD_REQUEST_CODE);
				return response;
			}

			// Validating security token
			System.gc();

			if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

				LOG.info("Hitting AI API :::  atrialPropagation:::");
				ArrayList propagationData = (ArrayList) jsonRequest.get(CommonConstants.PROPGATION_INFO);
				if (propagationData != null && propagationData.size() > 0) {

					HashMap seriesObj = (HashMap) propagationData.get(0);
					if (seriesObj.get(CommonConstants.SERIES_INSTANCE_UID) != null
							&& !seriesObj.get(CommonConstants.SERIES_INSTANCE_UID).toString().equals("")) {
						
						Long preprocessBookmark = bookmarkRepo.findBookmarkIdByStudyIdAndVersion(jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(), 0);

						// Getting record based on seriesId && studyId
						List<HashMap<String, Object>> smObjectList = null;

						smObjectList = seriesMeasurementsRepository.findCommonDataByStudyIdAndSeriesIdAndBookmarkId(
								jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(),
								seriesObj.get(CommonConstants.SERIES_INSTANCE_UID).toString(), preprocessBookmark);

						if (smObjectList != null && smObjectList.size() > 0) {
							jsonRequest.put("commonData", smObjectList.get(0) != null ? smObjectList.get(0) : "");
						}
					}

				}
				
				String orgId = jsonRequest.get(CommonConstants.ORG_ID).toString();
				jsonRequest.put(CommonConstants.SERVER_BASE_URL, commonMethod.getPacsUrl(orgId));
				try {
	            	String keyCloaktoken = pacsTokenService.getToken(orgId);
		    		jsonRequest.put("Authorization", keyCloaktoken);
	    			jsonRequest.put("keycloak_switch", commonMethod.getKeyClokaSwitch());
	    		} catch (Exception e) { 
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
				String aiUrl = aiStudyUrl + "atrialPropagation";
				// String aiUrl = "http://192.168.0.40:5020/qflowPropagation";
				 LOG.debug("atrialPropagation ::: AI Call request :: " +
				 jsonRequest);
				if (activemqSwitch.equalsIgnoreCase("true")) {
					if (!commonMethod.isAIServiceRunning()) {
						response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED,
								StatusConstants.SERVER_ERROR);
						return response;
					}
				}
				int connectionRetryCount = 0;
				RestTemplate restTemplate = new RestTemplate();
				while (true) {
					try {
						// Prepare headers
						HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.APPLICATION_JSON);
						headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

						// Request entity
						HttpEntity<String> entity = new HttpEntity<>(jsonRequest.toJSONString(), headers);

						// Send POST request
						ResponseEntity<String> res = restTemplate.exchange(
								aiUrl,
								HttpMethod.POST,
								entity,
								String.class
						);

						LOG.info("status code===> " + res.getStatusCodeValue());

						if (res.getStatusCode() != HttpStatus.OK) {
							response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
							throw new RuntimeException("Failed : HTTP error code : " + res.getStatusCodeValue());
						}

						// Parse response
						JSONParser parser = new JSONParser();
						JSONObject json = (JSONObject) parser.parse(res.getBody());

						response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, "Contours Updated");
						response.put("data", json);

						LOG.info("End of " + this.getClass().getName() + ".atrialPropagation " + StatusConstants.SUCCESS);
						return response;

					} catch (Exception ex) {
						ex.printStackTrace();
						Throwable cause = ex.getCause();

						if (cause instanceof ConnectException || cause instanceof SocketTimeoutException) {
							LOG.warn("Connection issue: " + ex.getLocalizedMessage());
							if (connectionRetryCount >= connectionRetryLimit) {
								LOG.error("Connection retry limit reached");
								response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
								return response;
							}
							try {
								Thread.sleep(20000);
								connectionRetryCount++;
								LOG.info("Retrying connection attempt #" + connectionRetryCount);
							} catch (InterruptedException ie) {
								Thread.currentThread().interrupt();
								LOG.error("Retry interrupted");
								response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
								return response;
							}
						} else {
							LOG.error("Unexpected Exception: " + ex.getLocalizedMessage());
							response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
							return response;
						}

					}
				}
			} else {

				response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
				LOG.info("End of " + this.getClass().getName() + ".atrialPropagation "
						+ StatusConstants.UNAUTHORIZED);
				return response;
			}

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".atrialPropagation " + StatusConstants.OPERATION_FAILED);
			return response;
		}
	}

	@Override
	@Transactional
	public boolean deleteContoursBySeries(String studyId, String seriesId) {

		LOG.info("Start of " + this.getClass().getName() + ".deleteContoursBySeries");

	    try {
	        Long bookmarkId = bookmarkService.getBookmarkId(studyId, null, false);

	        int deletedCount = seriesMeasurementsRepository.deleteByStudyIdAndSeriesIdAndBookmarkId(studyId, seriesId, bookmarkId);
	                

	        if (deletedCount > 0) {
	            LOG.info("Contours deleted successfully. Count: {}", deletedCount);
	        } else {
	            LOG.warn("No contours found to delete for StudyId: {} SeriesId: {}", studyId, seriesId);
	        }
	        
			LOG.info("End of " + this.getClass().getName() + ".deleteContoursBySeries " + StatusConstants.SUCCESS);
	        return true;

	    } catch (Exception e) {
	        LOG.error("Exception while deleting contours", e);
	        commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
	        LOG.info("End of deleteContoursBySeries FAILED"); 
	        return false;
	    }
	}
}
