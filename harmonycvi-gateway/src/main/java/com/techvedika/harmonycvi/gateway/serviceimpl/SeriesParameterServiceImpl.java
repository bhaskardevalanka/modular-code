package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.techvedika.harmonycvi.gateway.constant.CommonConstants;
import com.techvedika.harmonycvi.gateway.constant.StatusConstants;
import com.techvedika.harmonycvi.gateway.entity.Bookmarks;
import com.techvedika.harmonycvi.gateway.entity.ParameterReference;
import com.techvedika.harmonycvi.gateway.entity.SeriesParameter;
import com.techvedika.harmonycvi.gateway.exception.RequestValidator;
import com.techvedika.harmonycvi.gateway.exception.ValidationResult;
import com.techvedika.harmonycvi.gateway.repository.SeriesParameterRepository;
import com.techvedika.harmonycvi.gateway.service.BookmarkService;
import com.techvedika.harmonycvi.gateway.service.CommonMethod;
import com.techvedika.harmonycvi.gateway.service.SeriesParameterService;

@Service
public class SeriesParameterServiceImpl implements SeriesParameterService {

	private static final Logger LOG = LoggerFactory.getLogger(SeriesParameterServiceImpl.class);

	@Value("${ai.study-url}")
	String aiStudyUrl;

	@Value("${ai.ecv-study-url}")
	String ecvAiStudyUrl;

	@Value("${messaging.activemq.enabled:false}")
	String activemqSwitch;

	
	@Autowired
	private CommonMethod commonMethod;

	@Autowired
	private BookmarkService bookmarkService;

	@Autowired
	SeriesParameterRepository seriesParameterRepository;
	
	@Autowired
	PacsTokenService pacsTokenService;

	public int connectionRetryLimit = 6;

	// Need to include studyId in request
	@Override
	public JSONObject getParameter(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + ".getParameter");
		LOG.debug("Request : " + jsonRequest);

		JSONObject response = new JSONObject();
		try {

			if (jsonRequest != null && jsonRequest.get(CommonConstants.ACCESS_KEY) != null
					&& !jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals("")
					&& jsonRequest.get(CommonConstants.SERIES_ID) != null
					&& !jsonRequest.get(CommonConstants.SERIES_ID).toString().equals("")) {

				// Validating security token

				if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

					List<SeriesParameter> smObject = null;
					List<ParameterReference> parameterReferenceList = null;

					String bookmarkId = jsonRequest.containsKey(CommonConstants.BOOKMARK_ID)
							? jsonRequest.get(CommonConstants.BOOKMARK_ID) != null
									&& jsonRequest.get(CommonConstants.BOOKMARK_ID) != ""
											? (String) jsonRequest.get(CommonConstants.BOOKMARK_ID)
											: null
							: null;

					// Getting bookmark details
					Long bookmark = bookmarkService.getBookmarkId(
							jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(), bookmarkId, false);

//					smObject = em.createNamedQuery(SeriesParameter.FIND_BY_BOOKMARK_ID)
//							.setParameter(CommonConstants.SERIES_ID,
//									jsonRequest.get(CommonConstants.SERIES_ID).toString())
//							.setParameter(CommonConstants.BOOKMARK_ID, bookmark.getId()).getResultList();

					smObject = seriesParameterRepository.findBySeriesIdAndBookmarkId(
							jsonRequest.get(CommonConstants.SERIES_ID).toString(), bookmark);

					if (smObject == null || smObject.size() == 0) {
						LOG.info("getParameter API End");

						response = commonMethod.createResponse(StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE,
								StatusConstants.EMPTY_RESULT);
						LOG.info("End of " + this.getClass().getName() + ".getParameter "
								+ StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
						return response;

					} else {
						LOG.info("getParameter API End");
						response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.FETCHED);
						response.put(CommonConstants.PARAMETER, smObject.get(0).getParameterJson());
						response.put(CommonConstants.GRAPH, smObject.get(0).getGraph());
						response.put(CommonConstants.SUMMARY, smObject.get(0).getSummary());
						response.put(CommonConstants.TYPE, smObject.get(0).getType());
						LOG.info("End of " + this.getClass().getName() + ".getParameter " + StatusConstants.SUCCESS);
						return response;
					}
				} else {

					response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
					LOG.info("End of " + this.getClass().getName() + ".getParameter " + StatusConstants.UNAUTHORIZED);
					return response;
				}

			} else {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".getParameter " + StatusConstants.BAD_REQUEST_CODE);
				return response;
			}
		} catch (Exception e) {
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".getParameter " + StatusConstants.OPERATION_FAILED);
			return response;
		}
	}

	@Override
	public JSONObject saveParameter(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + ".saveParameter");
		LOG.debug("Request : " + jsonRequest);
		JSONObject response = new JSONObject();
		try {
			if (jsonRequest != null && jsonRequest.get(CommonConstants.ACCESS_KEY) != null
					&& !jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals("")
					&& jsonRequest.get(CommonConstants.STUDY_ID) != null
					&& !jsonRequest.get(CommonConstants.STUDY_ID).toString().equals("")
					&& jsonRequest.get(CommonConstants.SERIES_ID) != null
					&& !jsonRequest.get(CommonConstants.SERIES_ID).toString().equals("")
					&& jsonRequest.containsKey(CommonConstants.SERIES_TYPE)
					&& jsonRequest.get(CommonConstants.SERIES_TYPE) != null
					&& !jsonRequest.get(CommonConstants.SERIES_TYPE).toString().equals("")) {

				// Validating security token
				if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

					String graph = null;
					if (jsonRequest.containsKey(CommonConstants.GRAPH) && jsonRequest.get(CommonConstants.GRAPH) != null
							&& !jsonRequest.get(CommonConstants.GRAPH).toString().equals("")) {
						HashMap<String, String> graphObjMap = (HashMap<String, String>) jsonRequest
								.get(CommonConstants.GRAPH);
						JSONObject graphObj = new JSONObject(graphObjMap);
						graph = graphObj.toJSONString();
					}

					// JSONObject remaining_data = (JSONObject)
					// jsonRequest.remove(CommonConstants.GRAPH);

					// System.out.println("JSON string============" + patrameter);
					SeriesParameter sp = null;
					List<SeriesParameter> smObject = null;

					String bookmarkId = jsonRequest.containsKey(CommonConstants.BOOKMARK_ID)
							? jsonRequest.get(CommonConstants.BOOKMARK_ID) != null
									&& jsonRequest.get(CommonConstants.BOOKMARK_ID) != ""
											? (String) jsonRequest.get(CommonConstants.BOOKMARK_ID)
											: null
							: null;

					String type = jsonRequest.get(CommonConstants.SERIES_TYPE).toString();

					// Getting bookmark details
					Bookmarks bookmark = bookmarkService
							.getBookmark(jsonRequest.get(CommonConstants.STUDY_ID).toString(), bookmarkId, true);

					// Getting record based on seriesId && studyId
//					smObject = em.createNamedQuery(SeriesParameter.FIND_BY_BOOKMARK_ID)
//							.setParameter(CommonConstants.SERIES_ID,
//									jsonRequest.get(CommonConstants.SERIES_ID).toString())
//							.setParameter(CommonConstants.BOOKMARK_ID, bookmark.getId()).getResultList();

					smObject = seriesParameterRepository.findBySeriesIdAndBookmarkId(
							jsonRequest.get(CommonConstants.SERIES_ID).toString(), bookmark.getId());

					if (smObject != null && smObject.size() > 0) {
						for (SeriesParameter spObj : smObject) {
							spObj.setUpdatedTime(new Date());
							spObj.setParameterJson(jsonRequest.toString());
							if (graph != null) {
								spObj.setGraph(graph);
							}
							seriesParameterRepository.save(spObj);
						}

					} else {
						sp = new SeriesParameter();
						sp.setCreatedTime(new Date());
						sp.setUpdatedTime(new Date());
						sp.setStudyId(jsonRequest.get(CommonConstants.STUDY_ID).toString());
						sp.setParameterJson(jsonRequest.toJSONString());
						sp.setSeriesId(jsonRequest.get(CommonConstants.SERIES_ID).toString());
						sp.setType(type);
						sp.setBookmark(bookmark);
						sp.setVersion(bookmark.getVersion());
						if (graph != null) {
							sp.setGraph(graph);
						}
						seriesParameterRepository.save(sp);
					}

					LOG.info("saveParameter API End");

					response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SAVED);
					LOG.info("End of " + this.getClass().getName() + ".saveParameter " + StatusConstants.SUCCESS);
					return response;

				} else {

					response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
					LOG.info("End of " + this.getClass().getName() + ".saveParameter " + StatusConstants.UNAUTHORIZED);
					return response;
				}

			} else {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".saveParameter " + StatusConstants.BAD_REQUEST_CODE);
				return response;
			}

		} catch (Exception ex) {
			LOG.error("Exception : " + ex);
			LOG.info(ex.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".saveParameter " + StatusConstants.OPERATION_FAILED);
			return response;
		}
	}

	@Override
	public JSONObject saveGLSSeriesParameter(JSONObject jsonRequest) {LOG.info("Start of " + this.getClass().getName() + ".saveGLSSeriesParameter");
		LOG.debug("Request : " + jsonRequest);
	
		JSONObject response = new JSONObject();
		try {
		    // 1. Validate request
		    ValidationResult validationResult = RequestValidator.validateRequestWithDetails(
		            jsonRequest,
		            CommonConstants.ACCESS_KEY,
		            CommonConstants.STUDY_INSTANCE_UID,
		            CommonConstants.TWOCH_SERIES_ID,
		            CommonConstants.FOURCH_SERIES_ID
		    );
		    if (!validationResult.isValid()) {
		        response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
		        LOG.error("Validation failed: " + jsonRequest.toJSONString());
		        return response;
		    }
	
		    // 2. Token check
		    if (!CommonConstants.SECURITY_TOKEN.equals(jsonRequest.get(CommonConstants.ACCESS_KEY).toString())) {
		        response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
		        LOG.info("End of saveGLSSeriesParameter - Unauthorized");
		        return response;
		    }
	
		    // 3. Precompute common values
		    String studyId = jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString();
		    String jsonString = jsonRequest.toJSONString(); // serialize once
		    String graph = null;
	
		    if (jsonRequest.containsKey(CommonConstants.GRAPH) && jsonRequest.get(CommonConstants.GRAPH) != null) {
		        graph = new JSONObject((HashMap<String, String>) jsonRequest.get(CommonConstants.GRAPH)).toJSONString();
		    }
	
		    String bookmarkId = (jsonRequest.containsKey(CommonConstants.BOOKMARK_ID)
		            && jsonRequest.get(CommonConstants.BOOKMARK_ID) != null
		            && !"".equals(jsonRequest.get(CommonConstants.BOOKMARK_ID)))
		            ? (String) jsonRequest.get(CommonConstants.BOOKMARK_ID)
		            : null;
	
		    String type = (jsonRequest.containsKey(CommonConstants.SERIES_TYPE)
		            && jsonRequest.get(CommonConstants.SERIES_TYPE) != null)
		            ? jsonRequest.get(CommonConstants.SERIES_TYPE).toString()
		            : CommonConstants.GLS;
	
		    // 4. Get bookmark once
		    Bookmarks bookmark = bookmarkService.getBookmark(studyId, bookmarkId, false);
	
		    // 5. Handle both series IDs in one loop
		    List<String> seriesIds = Arrays.asList(
		            jsonRequest.get(CommonConstants.TWOCH_SERIES_ID).toString(),
		            jsonRequest.get(CommonConstants.FOURCH_SERIES_ID).toString()
		    );
	
		    Date now = new Date();
		    for (String seriesId : seriesIds) {
		        SeriesParameter sp = seriesParameterRepository
		                .findFirstBySeriesIdAndBookmarkId(seriesId, bookmark.getId())
		                .orElseGet(SeriesParameter::new); // create if not found
	
		        // Fill fields
		        if (sp.getId() == null) {
		            sp.setCreatedTime(now);
		            sp.setStudyId(studyId);
		            sp.setSeriesId(seriesId);
		            sp.setType(type);
		            sp.setBookmark(bookmark);
		            sp.setVersion(bookmark.getVersion());
		        }
	
		        sp.setUpdatedTime(now);
		        sp.setParameterJson(jsonString);
		        if (graph != null) {
		            sp.setGraph(graph);
		        }
	
		        // Persist
		        seriesParameterRepository.save(sp);
		    }
	
		    LOG.info("saveGLSSeriesParameter API End - Success");
		    response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SAVED);
		    return response;
	
		} catch (Exception ex) {
		    LOG.error("Exception in saveGLSSeriesParameter : ", ex);
		    response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
		    return response;
		}
	}

	@Override
	public JSONObject getGLSParameter(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + ".getGLSParameter");
		LOG.debug("Request : " + jsonRequest);

		JSONObject response = new JSONObject();
		try {

			if (jsonRequest != null && jsonRequest.get(CommonConstants.ACCESS_KEY) != null
					&& !jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals("")
					&& jsonRequest.get(CommonConstants.TWO_CH_SERIESID) != null
					&& !jsonRequest.get(CommonConstants.TWO_CH_SERIESID).toString().equals("")) {

				// Validating security token

				if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {

					List<SeriesParameter> smObject = null;
					List<ParameterReference> parameterReferenceList = null;

					String bookmarkId = jsonRequest.containsKey(CommonConstants.BOOKMARK_ID)
							? jsonRequest.get(CommonConstants.BOOKMARK_ID) != null
									&& jsonRequest.get(CommonConstants.BOOKMARK_ID) != ""
											? (String) jsonRequest.get(CommonConstants.BOOKMARK_ID)
											: null
							: null;

					// Getting bookmark details
					Long bookmark = bookmarkService.getBookmarkId(
							jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString(), bookmarkId, false);

					// Getting record based on seriesId
//					smObject = em.createNamedQuery(SeriesParameter.FIND_BY_BOOKMARK_ID)
//							.setParameter(CommonConstants.SERIES_ID,
//									jsonRequest.get(CommonConstants.TWO_CH_SERIESID).toString())
//							.setParameter(CommonConstants.BOOKMARK_ID, bookmark.getId()).getResultList();

					smObject = seriesParameterRepository.findBySeriesIdAndBookmarkId(
							jsonRequest.get(CommonConstants.TWO_CH_SERIESID).toString(), bookmark);

					if (smObject == null || smObject.size() == 0) {
//						smObject = em.createNamedQuery(SeriesParameter.FIND_BY_BOOKMARK_ID)
//								.setParameter(CommonConstants.SERIES_ID,
//										jsonRequest.get(CommonConstants.FOUR_CH_SERIESID).toString())
//								.setParameter(CommonConstants.BOOKMARK_ID, bookmark.getId()).getResultList();

						smObject = seriesParameterRepository.findBySeriesIdAndBookmarkId(
								jsonRequest.get(CommonConstants.FOUR_CH_SERIESID).toString(), bookmark);

						if (smObject == null || smObject.size() == 0) {
							response = commonMethod.createResponse(StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE,
									StatusConstants.EMPTY_RESULT);
							LOG.info("End of " + this.getClass().getName() + ".getGLSParameter "
									+ StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
							return response;

						} else {
							response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE,
									StatusConstants.FETCHED);
							response.put(CommonConstants.PARAMETER, smObject.get(0).getParameterJson());
							response.put(CommonConstants.GRAPH, smObject.get(0).getGraph());
							response.put(CommonConstants.SUMMARY, smObject.get(0).getSummary());
							response.put(CommonConstants.TYPE, smObject.get(0).getType());
							response.put(CommonConstants.FOUR_CH_SERIESID, smObject.get(0).getSeriesId());
							response.put(CommonConstants.TWO_CH_SERIESID,
									jsonRequest.get(CommonConstants.TWO_CH_SERIESID).toString());
							LOG.info("End of " + this.getClass().getName() + ".getGLSParameter "
									+ StatusConstants.SUCCESS);
							return response;
						}
					} else {
						response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.FETCHED);
						response.put(CommonConstants.PARAMETER, smObject.get(0).getParameterJson());
						response.put(CommonConstants.GRAPH, smObject.get(0).getGraph());
						response.put(CommonConstants.SUMMARY, smObject.get(0).getSummary());
						response.put(CommonConstants.TYPE, smObject.get(0).getType());
						response.put(CommonConstants.TWO_CH_SERIESID, smObject.get(0).getSeriesId());
						response.put(CommonConstants.FOUR_CH_SERIESID,
								jsonRequest.get(CommonConstants.FOUR_CH_SERIESID).toString());
						LOG.info("End of " + this.getClass().getName() + ".getGLSParameter " + StatusConstants.SUCCESS);
						return response;
					}
				} else {

					response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
					LOG.info(
							"End of " + this.getClass().getName() + ".getGLSParameter " + StatusConstants.UNAUTHORIZED);
					return response;
				}

			} else {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info(
						"End of " + this.getClass().getName() + ".getGLSParameter " + StatusConstants.BAD_REQUEST_CODE);
				return response;
			}
		} catch (Exception e) {
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".getGLSParameter " + StatusConstants.OPERATION_FAILED);
			return response;
		}
	}

	@Override
	public JSONObject saveDESeriesParam(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + ".saveDESeriesParam");
		LOG.debug("Request : " + jsonRequest);
		JSONObject response = new JSONObject();
		try {
			if (jsonRequest != null && jsonRequest.get(CommonConstants.ACCESS_KEY) != null
					&& !jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals("")
					&& jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID) != null
					&& !jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString().equals("")
					&& jsonRequest.get(CommonConstants.SERIES_LIST) != null
					&& !jsonRequest.get(CommonConstants.SERIES_LIST).toString().equals("")) {
				// Validating security token

				if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {
					String graph = null;
					if (jsonRequest.containsKey(CommonConstants.GRAPH) && jsonRequest.get(CommonConstants.GRAPH) != null
							&& !jsonRequest.get(CommonConstants.GRAPH).toString().equals("")) {
						HashMap<String, String> graphObjMap = (HashMap<String, String>) jsonRequest
								.get(CommonConstants.GRAPH);
						JSONObject graphObj = new JSONObject(graphObjMap);
						graph = graphObj.toJSONString();
					}

					ArrayList<String> seriesArray = (ArrayList<String>) jsonRequest.get(CommonConstants.SERIES_LIST);

					String type = jsonRequest.containsKey(CommonConstants.SERIES_TYPE)
							&& jsonRequest.get(CommonConstants.SERIES_TYPE) != null
									? jsonRequest.get(CommonConstants.SERIES_TYPE).toString()
									: CommonConstants.DE;

					seriesArray.stream().forEach(series -> {
						SeriesParameter seriesParam = null;
						List<SeriesParameter> twoChSMObject = null;

//						twoChSMObject = em.createNamedQuery(SeriesParameter.FIND_BY_SERIES_ID)
//								.setParameter(CommonConstants.SERIES_ID, series)
//								.setParameter(CommonConstants.TYPE, type).getResultList();

						twoChSMObject = seriesParameterRepository.findBySeriesIdAndType(series, type);

						if (twoChSMObject != null && twoChSMObject.size() > 0) {
							for (SeriesParameter spObj : twoChSMObject) {
								spObj.setUpdatedTime(new Date());
								spObj.setParameterJson(jsonRequest.toString());
								seriesParameterRepository.save(spObj);
							}
						} else {

							seriesParam = new SeriesParameter();
							seriesParam.setCreatedTime(new Date());
							seriesParam.setUpdatedTime(new Date());
							seriesParam.setStudyId(jsonRequest.get(CommonConstants.STUDY_INSTANCE_UID).toString());
							seriesParam.setParameterJson(jsonRequest.toJSONString());
							seriesParam.setSeriesId(series);
							seriesParam.setType(type);
							seriesParameterRepository.save(seriesParam);
						}

					});
					LOG.info("saveDESeriesParam API End");

					response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SAVED);
					LOG.info("End of " + this.getClass().getName() + ".saveDESeriesParam " + StatusConstants.SUCCESS);
					return response;

				} else {
					response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
					LOG.info("End of " + this.getClass().getName() + ".saveDESeriesParam "
							+ StatusConstants.UNAUTHORIZED);
					return response;
				}
			} else {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".saveDESeriesParam "
						+ StatusConstants.BAD_REQUEST_CODE);
				return response;
			}

		} catch (Exception ex) {
			LOG.error("Exception : " + ex);
			LOG.info(ex.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".saveDESeriesParam " + StatusConstants.OPERATION_FAILED);
			return response;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject saveMultipleSeriesParameters(JSONObject jsonRequest) {
		LOG.info("Start of " + this.getClass().getName() + ".saveMultipleSeriesParameters");
		LOG.debug("Request : " + jsonRequest);
		JSONObject response = new JSONObject();
		try {
			
			ValidationResult validationResult = RequestValidator.validateRequestWithDetails(jsonRequest, CommonConstants.ACCESS_KEY,
					CommonConstants.STUDY_ID,"seriesParams",CommonConstants.BOOKMARK_ID,CommonConstants.SERIES_TYPE);
			
			if(!validationResult.isValid()) {
				response = commonMethod.createResponse(StatusConstants.BAD_REQUEST_CODE, StatusConstants.BAD_REQUEST);
				LOG.info("End of " + this.getClass().getName() + ".saveMultipleSeriesParameters "
						+ StatusConstants.UNAUTHORIZED);
				return response;
			}

			if (jsonRequest.get(CommonConstants.ACCESS_KEY).toString().equals(CommonConstants.SECURITY_TOKEN)) {
				ArrayList seriesList = (ArrayList) jsonRequest.get("seriesParams");
				LOG.info("Series params list size:" + seriesList.size());

				String bookmarkId = jsonRequest.containsKey(CommonConstants.BOOKMARK_ID)
						? jsonRequest.get(CommonConstants.BOOKMARK_ID) != null
								&& jsonRequest.get(CommonConstants.BOOKMARK_ID) != ""
										? (String) jsonRequest.get(CommonConstants.BOOKMARK_ID)
										: null
						: null;

				String type = jsonRequest.get(CommonConstants.SERIES_TYPE).toString();

				// Getting bookmark details
				Bookmarks bookmark = bookmarkService
						.getBookmark(jsonRequest.get(CommonConstants.STUDY_ID).toString(), bookmarkId, false);

				seriesList.forEach(series -> {
					String jsonText = JSONValue.toJSONString(series);
					JSONObject seriesJson = (JSONObject) JSONValue.parse(jsonText);
					SeriesParameter sp = new SeriesParameter();
					sp.setCreatedTime(new Date());
					sp.setUpdatedTime(new Date());
					sp.setStudyId(jsonRequest.get(CommonConstants.STUDY_ID).toString());
					sp.setParameterJson(jsonText);
					sp.setSeriesId(seriesJson.get(CommonConstants.SERIES_ID).toString());
					sp.setType(type);
					sp.setBookmark(bookmark);
					sp.setVersion(bookmark.getVersion());
					if (seriesJson.containsKey("graph") && seriesJson.get("graph") != null)
						sp.setGraph(seriesJson.get("graph").toString());
					seriesParameterRepository.save(sp);
				});

				LOG.info("End of " + this.getClass().getName() + ".saveMultipleSeriesParameters "
						+ StatusConstants.UNAUTHORIZED);

				response = commonMethod.createResponse(StatusConstants.SUCCESS_CODE, StatusConstants.SAVED);
				return response;

			} else {

				response = commonMethod.createResponse(StatusConstants.UNAUTHORIZED, StatusConstants.INVALID_TOKEN);
				LOG.info("End of " + this.getClass().getName() + ".saveMultipleSeriesParameters "
						+ StatusConstants.UNAUTHORIZED);
				return response;
			}

		} catch (Exception ex) {
			LOG.error("Exception : " + ex);
			LOG.info(ex.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".saveMultipleSeriesParameters "
					+ StatusConstants.UNAUTHORIZED);
			return response;
		}
	}


	
	@Override 
	public boolean deleteSeriesParameters(String studyId, String seriesId) {

		LOG.info("Start of " + this.getClass().getName() + ".deleteSeriesParameters");

	    try {
	        Long bookmarkId = bookmarkService.getBookmarkId(studyId, null, false);

	        int deletedCount = seriesParameterRepository
	                .deleteByStudyIdAndSeriesIdAndBookmarkId(studyId, seriesId, bookmarkId);

	        if (deletedCount > 0) {
	            LOG.info("Deleted {} series parameters", deletedCount);
	            return true;
	        } else {
	            LOG.warn("No records found to delete for StudyId: {} SeriesId: {}", studyId, seriesId);
	            return false;
	        }
 
	    } catch (Exception e) { 
	        LOG.error("Exception while deleting series params", e);
	        return false;
	    }
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getECVMapDetails(JSONObject requestObj) {
		LOG.info("Start of " + this.getClass().getName() + "getECVMapDetails");
		JSONObject response = new JSONObject();
		try {

			String aiUrl = ecvAiStudyUrl + "ECVMap";
			LOG.info("aiUrl : " + aiUrl);

			if (activemqSwitch.equalsIgnoreCase("true")) {
				if (!commonMethod.isAIServiceRunning()) {
					response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED,
							StatusConstants.SERVER_ERROR);
					return response;
				}
			}
			String orgId = requestObj.get(CommonConstants.ORG_ID).toString();
			requestObj.put(CommonConstants.SERVER_BASE_URL, commonMethod.getPacsUrl(orgId));
			try {
            	String authorization = pacsTokenService.getToken(orgId);
    			requestObj.put("Authorization", authorization);
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
			int connectionRetryCount = 0;
			RestTemplate restTemplate = new RestTemplate();
			while (true) {
				try {
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

					JSONParser parser = new JSONParser();
					response = (JSONObject) parser.parse(res.getBody());
					LOG.info("response from AI API:::" + response);

					LOG.info("End of " + this.getClass().getName() + ".getECVMapDetails " + StatusConstants.SUCCESS);
					return response;
				} catch (Exception e) {
					if (e.getCause() instanceof ConnectException || e.getCause() instanceof SocketTimeoutException) {
						System.out.println(
								"cause  ConnectException or SocketTimeoutException:::" + e.getLocalizedMessage());
						System.out.println("Waiting for 20 more seconds after connection Exception");
						try {
							if (connectionRetryCount > connectionRetryLimit) {
								System.out.println("Connection retry limit reached");
								LOG.error("Exception : " + e);
								LOG.info(e.getLocalizedMessage());
								response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED,
										StatusConstants.SERVER_ERROR);
								LOG.info("End of " + this.getClass().getName() + ".getECVMapDetails "
										+ StatusConstants.OPERATION_FAILED);
								return response;
							}
							Thread.sleep(20000);
							connectionRetryCount++;
						} catch (InterruptedException ie) {
							Thread.currentThread().interrupt();
							System.out.println("Retry interrupted, stopping retries.");
							LOG.error("Exception : " + e);
							LOG.info(e.getLocalizedMessage());
							response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED,
									StatusConstants.SERVER_ERROR);
							LOG.info("End of " + this.getClass().getName() + ".getECVMapDetails "
									+ StatusConstants.SERVER_ERROR);
							return response;
						}
					} else {
						System.out.println("Unexpected Exception :::" + e.getLocalizedMessage());
						LOG.error("Exception : " + e);
						LOG.info(e.getLocalizedMessage());
						response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED,
								StatusConstants.SERVER_ERROR);
						LOG.info("End of " + this.getClass().getName() + ".getECVMapDetails "
								+ StatusConstants.SERVER_ERROR);
						return response;
					}

				}
			}

		} catch (Exception e) {
			LOG.error("Exception : " + e);
			LOG.info(e.getLocalizedMessage());
			response = commonMethod.createResponse(StatusConstants.OPERATION_FAILED, StatusConstants.SERVER_ERROR);
			LOG.info("End of " + this.getClass().getName() + ".getECVMapDetails " + StatusConstants.SERVER_ERROR);
			return response;
		}

	}

}
