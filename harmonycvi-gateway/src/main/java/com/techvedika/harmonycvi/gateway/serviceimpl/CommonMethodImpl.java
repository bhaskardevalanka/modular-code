package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.techvedika.harmonycvi.gateway.cloud.AiServerController;
import com.techvedika.harmonycvi.gateway.constant.CommonConstants;
import com.techvedika.harmonycvi.gateway.constant.StatusConstants;
import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.dto.KeycloakToken;
import com.techvedika.harmonycvi.gateway.entity.ParameterReference;
import com.techvedika.harmonycvi.gateway.entity.SeriesMeasurements;
import com.techvedika.harmonycvi.gateway.entity.SeriesSegments;
import com.techvedika.harmonycvi.gateway.entity.StudyAnnotation;
import com.techvedika.harmonycvi.gateway.projection.OrgPacsValidationUrlProjection;
import com.techvedika.harmonycvi.gateway.repository.OrganizationRepository;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;
import com.techvedika.harmonycvi.gateway.security.SecurityUtil;
import com.techvedika.harmonycvi.gateway.service.CommonMethod;
import com.techvedika.harmonycvi.gateway.util.ApplicationPropertyReader;

@Service
@Transactional
public class CommonMethodImpl implements CommonMethod {

	private static final Logger LOG = LoggerFactory.getLogger(CommonMethodImpl.class);

	String viewerVersion;
	@Value("${ai.study-url}")
	String aiStudyUrl;

	@Value("${organization.orgs.default.dicomDownloadUrl}")
    String defaultPacsUrl;

	@Value("${dcm4cheeBaseUrl}")
	String dcm4cheeBaseUrl;
	
	@Value("${keycloak.enabled}")
	String keyCloakSwitch;

	@Value("${keycloak.user-password}")
	String keyCloakuserPassword;

	@Value("${keycloak.admin-password}")
    String keyCloakAdminPassword;

	@Value("${org.no-header-id}")
	String NoHeaderOrg;

	@Value("${org.header-footer-proportions}")
    String headerFooterProportions;
	
	@Autowired
    private OrganizationRepository orgRepo;
	
	@Autowired
    private ExternalTokenCallService extToken;
	
	@Autowired
	KeycloakTokenService keycloakTokenService ;
	
	@Autowired
    private UserRepository userRepo;
	
	private AiServerController aiServerController;

	private JmsTemplate jmsTemplate;

	public CommonMethodImpl(AiServerController aiServerController, JmsTemplate jmsTemplate) {
		this.aiServerController = aiServerController;
		this.jmsTemplate = jmsTemplate;
	}
	
	@Override
	public String getReportToS3() {
		return reportToS3;
	}

	@Override
	public void setReportToS3(String reportToS3) {
		this.reportToS3 = reportToS3;
	}

	@Override
	public String getTargetPath() {
		return targetPath;
	}

	@Override
	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}

	@Override
	public String getAccessPath() {
		return accessPath;
	}

	@Override
	public void setAccessPath(String accessPath) {
		this.accessPath = accessPath;
	}

	@Value("${report.to-s3}")
	String reportToS3;

	@Value("#{environment['app.data-dir'] + '/report/'}")
	String targetPath;

	@Value("#{environment['server.gateway-url'] + '/report/'}")
	String accessPath;
//	@Value("${PARAMETER_KEY}")
//	String paramKey;
//	@Value("${PARAMETER_KEY_VALUE}")
//	static String paramKeyValue;
//	@Value("${PARAMETER_KEY_UNITS}")
//	static String paramKeyUnits;
//	@Value("${DE_PARAMETER_KEY}")
//	String deParamsKey;
//	@Value("${DE_PARAMETER_KEY_UNITS}")
//	String deUnitsKey;

	
	public static Properties parameterProperties = new Properties();
//	public static Properties env = new Properties();
	
//	private static final String APPLICATION_FILE = "application.properties";
    private static final String PARAMETER_FILE = "parameter.properties";
    
	
//	static {
//		parameterProperties = new Properties();
//		env = new Properties();	
//		try {
//			String fileName = System.getProperty("jboss.server.config.dir") + "/application.properties";
//			try(FileInputStream fis = new FileInputStream(fileName)) {
//				env.load(fis);
//			}
//			LOG.info("Application properties file path======>"+fileName);
//			LOG.info("Application properties file path======>"+env.size());
//			fileName = System.getProperty("jboss.server.config.dir") + "/parameter.properties";
//			LOG.info("parameter properties file path======>"+fileName);
//			LOG.info("parameter properties file path======>"+ parameterProperties.size());
//			try(FileInputStream fis = new FileInputStream(fileName)) {
//				parameterProperties.load(fis);
//			}
//		} catch (IOException e) {
//			LOG.info(e.getLocalizedMessage());
//		}  
//	}
	
	static {
//        loadProperties(APPLICATION_FILE, env);
        loadProperties(PARAMETER_FILE, parameterProperties);
    }
	
	private static void loadProperties(String fileName, Properties props) {
        try (InputStream input = ApplicationPropertyReader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                System.err.println("Could not find " + fileName + " in classpath");
                return;
            }
            props.load(input);
            LOG.info("Loaded properties from " + fileName);
        } catch (IOException e) {
            System.err.println("Could not load " + fileName + ": " + e.getMessage());
        }
    }

	@Override
	public JSONObject createResponse(String statusCode, String responseMessage) {
		JSONObject response = new JSONObject();
		try {
			response.put(StatusConstants.STATUS_CODE, statusCode);
			response.put(StatusConstants.RESPONSE_MESSAGE, responseMessage);
		} catch (Exception e) {
			LOG.error("Error creating response JSON: {}", e.getMessage());
		}
		return response;
	}

	@Override
	public JSONObject createJsonObject(SeriesMeasurements data) {
		JSONObject response = new JSONObject();
		try {
			response.put(CommonConstants.INSTANCE_ARRAY, data != null ? data.getInstanceArray() : "");
			response.put(CommonConstants.COMMON_DATA, data != null ? data.getCommonData() : "");
		} catch (Exception e) {
			LOG.error("Error creating JSON object: {}", e.getMessage());
		}
		return response;
	}

	@Override
	public JSONObject createJsonSSObject(SeriesSegments data) {
		JSONObject response = new JSONObject();
		try {
			response.put(CommonConstants.INSTANCE_ARRAY, data != null ? data.getInstanceArray() : "");
			response.put(CommonConstants.SERIES_INSTANCE_UID, data != null ? data.getSeriesId() : "");
			response.put(CommonConstants.STUDY_INSTANCE_UID, data != null ? data.getStudyId() : "");
			response.put(CommonConstants.CREATION_DATE, data != null ? data.getCreationDate() : "");
			response.put(CommonConstants.TYPE, data != null ? data.getType() : "");
			response.put(CommonConstants.SEGMENT_TYPE, data != null ? data.getSegment_type() : "");
		} catch (Exception e) {
			LOG.error("Error creating segment JSON: {}", e.getMessage());
		}
		return response;
	}

	@Override
	public SeriesMeasurements setSeriesMeasurementsObject(SeriesMeasurements smObject, ArrayList<JSONObject> request,
			String seriesId, JSONObject jsonRequest) {
		try {
			if (smObject == null) {
				smObject = new SeriesMeasurements();
				smObject.setSeriesId(seriesId);
				smObject.setStudyId(jsonRequest.get(CommonConstants.STUDY_ID).toString());
				smObject.setPatientId(jsonRequest.get(CommonConstants.PATIENT_ID).toString());
			}
		} catch (Exception e) {
			LOG.error("Error setting SeriesMeasurements: {}", e.getMessage());
		}
		return smObject;
	}

	@Override
	public SeriesMeasurements setSeriesMeasurementsObjectForFreeHand(SeriesMeasurements smObject,
			ArrayList<JSONObject> request, String seriesId, JSONObject jsonRequest,
			LinkedHashMap<String, Object> commonData) {
		try {
			smObject.setCommonData(commonData);
			smObject.setSeriesId(seriesId);
			smObject.setStudyId(commonData.get(CommonConstants.STUDY_INSTANCE_UID).toString());
			smObject.setPatientId(commonData.get(CommonConstants.PATIENT_ID).toString());
		} catch (Exception e) {
			LOG.error("Error setting FreeHand SeriesMeasurements: {}", e.getMessage());
		}
		return smObject;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject processFreeHandData(JSONObject data) {
		LOG.info("Start of " + this.getClass().getName() + ".processFreeHandData");

		try {
			if (data == null) {
				return null;
			}

			JSONArray updatedRequestArray = new JSONArray();
			JSONArray newMeasurementArray;
			JSONObject newMeasurementObject;

			List<LinkedHashMap<String, Object>> seriesList = (List<LinkedHashMap<String, Object>>) data
					.get(CommonConstants.REQUEST);

			if (seriesList != null && !seriesList.isEmpty()) {
				for (LinkedHashMap<String, Object> seriesObj : seriesList) {

					List<Map<String, Object>> measurementList = (List<Map<String, Object>>) seriesObj
							.get(CommonConstants.MESUREMENT);

					newMeasurementArray = new JSONArray();
					newMeasurementObject = new JSONObject();

					for (int j = 0; j < measurementList.size(); j++) {
						//Map<String, Object> measurement = measurementList.get(j);
						HashMap<String, Object> measurement = new HashMap<>(measurementList.get(j));

						if (CommonConstants.FREE_TEXT.equals(seriesObj.get(CommonConstants.TOOL_TYPE))) {
							List<LinkedHashMap<String, Object>> coordinateList = new ArrayList<>();

							Object leftCounter = measurement.get(CommonConstants.LEFT_COUNTER);
							Object rightCounter = measurement.get(CommonConstants.RIGHT_COUNTER);
							Object wall = measurement.get(CommonConstants.WALL);

							if (leftCounter instanceof List<?>) {
								coordinateList.addAll((List<LinkedHashMap<String, Object>>) leftCounter);
							}
							if (rightCounter instanceof List<?>) {
								coordinateList.addAll((List<LinkedHashMap<String, Object>>) rightCounter);
							}
							if (wall instanceof List<?>) {
								coordinateList.addAll((List<LinkedHashMap<String, Object>>) wall);
							}

							if (!coordinateList.isEmpty()) {
								for (int k = 0; k < coordinateList.size(); k++) {
									LinkedHashMap<String, Object> coordinate = coordinateList.get(k);
									JSONObject measurementJson = createMesuremetJson(coordinate, measurement, k + 1);
									newMeasurementArray.add(measurementJson);
								}
							}

							newMeasurementObject.put(CommonConstants.SERIES_ID,
									seriesObj.get(CommonConstants.SERIES_ID));
							newMeasurementObject.put(CommonConstants.MESUREMENT_JSON, newMeasurementArray);
						}
					}

					updatedRequestArray.add(newMeasurementObject);
				}

				data.put(CommonConstants.REQUEST, updatedRequestArray);
			}

		} catch (Exception e) {
			LOG.info("Exception while processing Free hand data: {}", e.getLocalizedMessage());
		}

		LOG.info("End of " + this.getClass().getName() + ".processFreeHandData");
		return data;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject createMesuremetJson(LinkedHashMap<String, Object> coordinateObj, HashMap<String, Object> s,
			Integer measurementNo) {

		LOG.info("Start of " + this.getClass().getName() + ".createMesuremetJson");

		JSONObject response = new JSONObject();
		try {
			// Initialize nested JSON objects
			JSONObject handles = new JSONObject();
			JSONObject start = buildHandlePoint(coordinateObj, CommonConstants.STSRT_X, CommonConstants.STSRT_Y, true,
					false);
			JSONObject end = buildHandlePoint(coordinateObj, CommonConstants.END_X, CommonConstants.END_Y, true, true);

			JSONObject boundingBox = buildBoundingBox();
			JSONObject textBox = buildTextBox(coordinateObj, boundingBox);
			JSONObject translation = buildTranslation();
			JSONObject voi = buildVOI();
			JSONObject viewPort = buildViewPort(translation, voi);

			handles.put(CommonConstants.START, start);
			handles.put(CommonConstants.END, end);
			handles.put(CommonConstants.TEXT_BOX, textBox);

			// Construct main response object
			response.put(CommonConstants._ID, s.get(CommonConstants._ID));
			response.put(CommonConstants.VISIBLE, true);
			response.put(CommonConstants.ACTIVE, true);
			response.put(CommonConstants.HANDLES, handles);
			response.put(CommonConstants.PATIENT_ID, s.get(CommonConstants.PATIENT_ID));
			response.put(CommonConstants.STUDY_INSTANCE_UID, s.get(CommonConstants.STUDY_INSTANCE_UID));
			response.put(CommonConstants.SERIES_INSTANCE_UID, s.get(CommonConstants.SERIES_INSTANCE_UID));
			response.put(CommonConstants.SOP_INSTANCE_UID, s.get(CommonConstants.SOP_INSTANCE_UID));
			response.put(CommonConstants.FRAME_INDEX, s.get(CommonConstants.FRAME_INDEX));
			response.put(CommonConstants.IMAGE_PATH, s.get(CommonConstants.IMAGE_PATH));
			response.put(CommonConstants.USER_ID, s.get(CommonConstants.USER_ID));
			response.put(CommonConstants.TOOL_TYPE, CommonConstants.LENGTH);
			response.put(CommonConstants.ADDITIONAL_DATA, new JSONObject());
			response.put(CommonConstants.INVALIDATE, false);
			response.put(CommonConstants.CREATED_AT, s.get(CommonConstants.CREATED_AT));
			response.put(CommonConstants.TIME_POINT_ID, s.get(CommonConstants.TIME_POINT_ID));
			response.put(CommonConstants.MESUREMENT_NUMBER, measurementNo);
			response.put(CommonConstants.LENGTH, s.get(CommonConstants.LENGTH));
			response.put(CommonConstants.VIEW_PORT, viewPort);

		} catch (Exception e) {
			LOG.info("Error in createMesuremetJson: {}", e.getLocalizedMessage());
			LOG.info("End of " + this.getClass().getName() + ".createMesuremetJson " + UserConstants.FAILURE);
		}

		LOG.info("End of " + this.getClass().getName() + ".createMesuremetJson");
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject createStraightLineFormatForFreeHand(LinkedHashMap<String, Object> singleInstanceObject,
			LinkedHashMap<String, Object> coordinateObj, HashMap<String, Object> s, Integer measurementNo) {
		LOG.info("Start of " + this.getClass().getName() + ".createStraightLineFormatForFreeHand");

		JSONObject response = new JSONObject();
		try {
			// Build handles
			JSONObject handles = new JSONObject();
			JSONObject start = buildHandlePoint(coordinateObj, CommonConstants.STSRT_X, CommonConstants.STSRT_Y, true,
					false);
			JSONObject end = buildHandlePoint(coordinateObj, CommonConstants.END_X, CommonConstants.END_Y, true, true);
			JSONObject boundingBox = buildBoundingBox();
			JSONObject textBox = buildTextBox(coordinateObj, boundingBox);

			handles.put(CommonConstants.START, start);
			handles.put(CommonConstants.END, end);
			handles.put(CommonConstants.TEXT_BOX, textBox);

			// Build final response
			response.put(CommonConstants.HANDLES, handles);
			response.put(CommonConstants.SOP_INSTANCE_UID, singleInstanceObject.get(CommonConstants.SOP_INSTANCE_UID));
			response.put(CommonConstants.IMAGE_PATH, singleInstanceObject.get(CommonConstants.IMAGE_PATH));
			response.put(CommonConstants.MESUREMENT_NUMBER, measurementNo);
			response.put(CommonConstants.TOOL_TYPE, CommonConstants.FREE_HAND_LENGTH);
			response.put(CommonConstants.COORDINATES_TYPE, CommonConstants.SEGMENTS);
			response.put(CommonConstants.IS_SEGMENTS, true);

		} catch (Exception e) {
			LOG.info("Error in createStraightLineFormatForFreeHand: {}", e.getLocalizedMessage());
			LOG.info("End of " + this.getClass().getName() + ".createStraightLineFormatForFreeHand "
					+ UserConstants.FAILURE);
		}

		LOG.info("End of " + this.getClass().getName() + ".createStraightLineFormatForFreeHand");
		return response;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public JSONObject createFreeHandMesuremetJson(LinkedHashMap<String, Object> singleInstanceObject,
			LinkedHashMap<String, Object> commonData, String coordinateType, Integer measurementNumber) {
		LOG.info("Start of " + this.getClass().getName() + ".createFreeHandMesuremetJson");

		JSONObject response = new JSONObject();
		List<JSONObject> handleList = new ArrayList<>();

		try {
			List<LinkedHashMap<String, Object>> coordinateList = (List<LinkedHashMap<String, Object>>) singleInstanceObject
					.get(coordinateType);

			if (coordinateList != null && coordinateList.size() >= 2) {
				for (int i = 0; i < coordinateList.size(); i++) {
					LinkedHashMap<String, Object> current = coordinateList.get(i);
					LinkedHashMap<String, Object> next = (i + 1 < coordinateList.size()) ? coordinateList.get(i + 1)
							: coordinateList.get(0); // loop back to the first

					JSONObject handle = buildFreehandHandle(current, next, i + 1, i == coordinateList.size() - 1);
					handleList.add(handle);
				}
			}
		} catch (Exception e) {
			LOG.info("Exception while creating free hand measurement json: {}", e.getLocalizedMessage());
		}

		response.put(CommonConstants.IMAGE_PATH, singleInstanceObject.get(CommonConstants.IMAGE_PATH));
		response.put(CommonConstants.SOP_INSTANCE_UID, singleInstanceObject.get(CommonConstants.SOP_INSTANCE_UID));

		if (singleInstanceObject.containsKey(CommonConstants.PHASE_SOP_INSTANCE_UID)) {
			Object phaseUid = singleInstanceObject.get(CommonConstants.PHASE_SOP_INSTANCE_UID);
			if (phaseUid != null) {
				response.put(CommonConstants.PHASE_SOP_INSTANCE_UID, phaseUid);
			}
		}

		response.put(CommonConstants.HANDLES, handleList);
		response.put(CommonConstants.TOOL_TYPE, CommonConstants.FREE_HAND_MOUSE);
		response.put(CommonConstants.COORDINATES_TYPE, coordinateType);
		response.put(CommonConstants.IS_SEGMENTS, false);
		response.put(CommonConstants.MESUREMENT_NUMBER, null); // left null intentionally

		if (handleList.isEmpty()) {
			LOG.info("End of " + this.getClass().getName() + ".createFreeHandMesuremetJson "
					+ StatusConstants.SUCCESS_WITH_EMPTY_RESULT_CODE);
			return null;
		}

		LOG.info("End of " + this.getClass().getName() + ".createFreeHandMesuremetJson");
		return response;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public LinkedHashMap<String, Object> createCommonDataJson(LinkedHashMap<String, Object> commonData) {
	    LOG.info("Start of " + this.getClass().getName() + ".createCommonDataJson");

	    try {
	        if (commonData != null) {
	            commonData.put(CommonConstants.TEXT_BOX, buildTextBoxJson());
	            commonData.put(CommonConstants.VIEW_PORT, buildViewPortJson());
	        }
	    } catch (Exception e) {
	        LOG.info("Exception in createCommonDataJson: {}", e.getLocalizedMessage());
	        LOG.info("End of " + this.getClass().getName() + ".createCommonDataJson " + UserConstants.FAILURE);
	    }

	    LOG.info("End of " + this.getClass().getName() + ".createCommonDataJson");
	    return commonData;
	}
	
	@Override
	public String getSHA(String input) {
	    LOG.info("Start of " + this.getClass().getName() + ".getSHA");

	    try {
	        MessageDigest md = MessageDigest.getInstance(CommonConstants.SHA_256);
	        byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
	        BigInteger no = new BigInteger(1, messageDigest);
	        String hashtext = no.toString(16);

	        // Pad with leading zeros to ensure 64-character length for SHA-256
	        while (hashtext.length() < 64) {
	            hashtext = "0" + hashtext;
	        }

	        LOG.info("End of " + this.getClass().getName() + ".getSHA");
	        return hashtext;

	    } catch (NoSuchAlgorithmException e) {
	        LOG.info("End of " + this.getClass().getName() + ".getSHA " + UserConstants.FAILURE + " " + e.getMessage());
	        return null;
	    }
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<JSONObject> parameterResponse(JSONObject lv, JSONObject rv, List<ParameterReference> parameterReferenceList) {
	    LOG.info("Start of " + this.getClass().getName() + ".parameterResponse");
	    List<JSONObject> response = new ArrayList<>();

	    try {
	        String paramKey = parameterProperties.get(CommonConstants.PARAMETER_KEY).toString();
	        String[] keyArray = paramKey != null && !paramKey.isEmpty() ? paramKey.split(",") : null;
	        JSONParser parser = new JSONParser();
	        JSONObject longNameJson = (JSONObject) parser.parse(parameterProperties.get(CommonConstants.PARAMETER_KEY_VALUE).toString());
	        JSONObject unitsJson = (JSONObject) parser.parse(parameterProperties.get(CommonConstants.PARAMETER_KEY_UNITS).toString());
	        for (String key : keyArray) {
	            JSONObject lvrvObj = new JSONObject();

	            for (ParameterReference prObj : parameterReferenceList) {
	                if (prObj == null || !key.equals(prObj.getParameter())) continue;

	                JSONObject parameterObj = new JSONObject();
	                parameterObj.put(CommonConstants.MAX, parseNumber(prObj.getMax()));
	                parameterObj.put(CommonConstants.MIN, parseNumber(prObj.getMin()));

	                JSONObject sourceJson = CommonConstants.LV.equals(prObj.getType()) ? lv : rv;
	                parameterObj.put(CommonConstants.VALUE, parseValue(key, sourceJson));

	                lvrvObj.put(prObj.getType(), parameterObj);
	            }

	            lvrvObj.put(CommonConstants.SHORT_NAME, key);
	            lvrvObj.put(CommonConstants.LONG_NAME, longNameJson.get(key));
	            lvrvObj.put(CommonConstants.UNITS, unitsJson.get(key));
	            
	            response.add(lvrvObj);
	        }

	    } catch (Exception e) {
	        LOG.info("Exception in parameterResponse: {}", e.getLocalizedMessage());
	        LOG.info("End of " + this.getClass().getName() + ".parameterResponse " + UserConstants.FAILURE);
	    }

	    LOG.info("End of " + this.getClass().getName() + ".parameterResponse");
	    return response;
	}
	
	@Override
	public JSONObject createStudyAnnotationObj(StudyAnnotation data) {
	    LOG.info("Start of " + this.getClass().getName() + ".createStudyAnnotationObj");
	    JSONObject response = new JSONObject();
	    try {
	        response.put(CommonConstants.STUDY_ID, Objects.toString(data != null ? data.getStudyId() : "", ""));
	        response.put(CommonConstants.STUDY_ANNOTATION, Objects.toString(data != null ? data.getAnnotationData() : "", ""));
	    } catch (Exception e) {
	        LOG.info("Exception in createStudyAnnotationObj: {}", e.getLocalizedMessage());
	        LOG.info("End of " + this.getClass().getName() + ".createStudyAnnotationObj " + UserConstants.FAILURE);
	    }
	    LOG.info("End of " + this.getClass().getName() + ".createStudyAnnotationObj");
	    return response;
	}
	
	@Override
	public JSONObject deParameterResponse(JSONObject deParamData) {
	    LOG.info("Start of " + this.getClass().getName() + ".deParameterResponse");
	    new JSONObject();
	    try {
	        String paramKey = parameterProperties.get(CommonConstants.DE_PARAMETER_KEY).toString();
	        String[] keyArray = (paramKey != null && !paramKey.isEmpty()) ? paramKey.split(",") : null;
	        JSONParser parser = new JSONParser();
	        JSONObject unitsJson = (JSONObject) parser.parse(parameterProperties.get(CommonConstants.DE_PARAMETER_KEY_UNITS).toString());

	        for (String key : keyArray) {
	            if (deParamData.containsKey(key)) {
	            	deParamData.put(key, deParamData.get(key) + " " + unitsJson.get(key));
	               // Object value = deParamData.get(key);
	                //response.put(key, value + " " + unitsJson.getOrDefault(key, ""));
	            }
	        }
	    } catch (Exception e) {
	        LOG.info("Exception in deParameterResponse: {}", e.getLocalizedMessage());
	        LOG.info("End of " + this.getClass().getName() + ".deParameterResponse " + UserConstants.FAILURE);
	        return deParamData; // fallback
	    }
	    LOG.info("End of " + this.getClass().getName() + ".deParameterResponse");
	    return deParamData;
	}


	@Override
	public boolean isAIServiceRunning() {
		boolean isServiceRunning = false;
		   boolean statusRecieved = false;
		   while(!statusRecieved) {
			   LOG.info("Checking for server status");
			   requestTimeStampUpdate();
		       String aiServerStatus = aiServerController.getAIServerStatus();
		       LOG.info("AI status:"+aiServerStatus);
		       if("running".equalsIgnoreCase(aiServerStatus)) {	
		    	   statusRecieved = true;
		    	   isServiceRunning = isLoadBalancerActive();
		       }else if("stopped".equalsIgnoreCase(aiServerStatus)){	    	  
		    	   aiServerController.restartAIServer();
					statusRecieved = true;
					isServiceRunning =  isLoadBalancerActive();			  
		       }else if("".equalsIgnoreCase(aiServerStatus)){
		    	   statusRecieved = true;
		    	   isServiceRunning = false;
		       }else {
		    	   try {
		    		   Thread.sleep(10000);
		    		   LOG.info("Waiting for 10 seconds");
					} catch (InterruptedException e) {
						LOG.info("Interruption to thread while waiting");
					}
		       }
	       }
	       return isServiceRunning;
	}

    /**
     * Check Load Balancer status
     */
    private boolean isLoadBalancerActive() {
        boolean isActive = false;
        int hitCount = 0;

        RestTemplate restTemplate = new RestTemplate();
        while (!isActive && hitCount < 4) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        aiStudyUrl + "checkStatus", HttpMethod.GET, entity, String.class);

                LOG.info("status code for load balancer: {}", response.getStatusCodeValue());

                if (response.getStatusCode() == HttpStatus.OK) {
                    String output = response.getBody();
                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject) parser.parse(output);

                    LOG.info("Load balancer response: {}", json.toJSONString());

                    if (json.containsKey("statusCode") &&
                            "1000".equals(json.get("statusCode").toString())) {
                        isActive = true;
                        return true;
                    }
                }

                hitCount++;
            } catch (Exception e) {
                LOG.error("Unable to get Load balancer status: {}", e.getLocalizedMessage());
            }

            try {
                LOG.info("Waiting for 10 more seconds...");
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return false;
    }
	
    private void requestTimeStampUpdate() {

        try {
            String payload = String.valueOf(System.currentTimeMillis());

            LOG.info("Sending timestamp to queue: {}", payload);

            jmsTemplate.convertAndSend(
                    "request_timestamps_queue",
                    payload
            );

        } catch (Exception e) {
            LOG.error("requestTimeStampUpdate exception", e);
        }
    }
	
	private static JSONObject parsePropertyJson(String propertyKey) {
	    try {
	        JSONParser parser = new JSONParser();
	        return (JSONObject) parser.parse(parameterProperties.get(propertyKey).toString());
	    } catch (ParseException e) {
	        LOG.info("Failed to parse JSON for key {}: {}", propertyKey, e.getLocalizedMessage());
	        return new JSONObject();
	    }
	}

	public static String getLongName(String shortName) {
	    LOG.info("Start of CommonMethodImpl.getLongName");
	    JSONObject json = parsePropertyJson(CommonConstants.PARAMETER_KEY_VALUE);
	    String value = (String) json.getOrDefault(shortName, "");
	    LOG.info("End of CommonMethodImpl.getLongName");
	    return value;
	}

	public static String getUnits(String shortName) {
	    LOG.info("Start of CommonMethodImpl.getUnits");
	    JSONObject json = parsePropertyJson(CommonConstants.PARAMETER_KEY_UNITS);
	    String value = (String) json.getOrDefault(shortName, "");
	    LOG.info("End of CommonMethodImpl.getUnits");
	    return value;
	}


	private JSONObject buildHandlePoint(Map<String, Object> coordinateObj, String xKey, String yKey, boolean highlight,
			boolean active) {
		JSONObject point = new JSONObject();
		point.put(CommonConstants.X, coordinateObj.get(xKey));
		point.put(CommonConstants.Y, coordinateObj.get(yKey));
		point.put(CommonConstants.HIGHLIGHT, highlight);
		point.put(CommonConstants.ACTIVE, active);
		point.put(CommonConstants.DRAW_INDEPENDENTLY, false);
		point.put(CommonConstants.MOVES_INDEPENDENTLY, false);
		point.put(CommonConstants.ALLOW_OUTSIDE_IMAGE, false);
		point.put(CommonConstants.HAS_MOVED, false);
		point.put(CommonConstants.HAS_BOUNDING_BOX, false);
		point.put(CommonConstants.LOCKED, false);
		return point;
	}

	private JSONObject buildBoundingBox() {
		JSONObject box = new JSONObject();
		box.put(CommonConstants.WIDTH, 86.0693359375);
		box.put(CommonConstants.HEIGHT, 25);
		box.put(CommonConstants.LEFT, 656);
		box.put(CommonConstants.TOP, 337.5);
		return box;
	}

	private JSONObject buildTextBox(Map<String, Object> coordinateObj, JSONObject boundingBox) {
		JSONObject textBox = new JSONObject();
		textBox.put(CommonConstants.ACTIVE, false);
		textBox.put(CommonConstants.HAS_MOVED, false);
		textBox.put(CommonConstants.MOVES_INDEPENDENTLY, false);
		textBox.put(CommonConstants.DRAW_INDEPENDENTLY, true);
		textBox.put(CommonConstants.ALLOW_OUTSIDE_IMAGE, true);
		textBox.put(CommonConstants.HAS_BOUNDING_BOX, true);
		textBox.put(CommonConstants.HIGHLIGHT, false);
		textBox.put(CommonConstants.LOCKED, false);
		textBox.put(CommonConstants.X, coordinateObj.get(CommonConstants.END_X));
		textBox.put(CommonConstants.Y, coordinateObj.get(CommonConstants.END_Y));
		textBox.put(CommonConstants.BOUNDING_BOX, boundingBox);
		return textBox;
	}

	private JSONObject buildTranslation() {
		JSONObject translation = new JSONObject();
		translation.put(CommonConstants.X, 0);
		translation.put(CommonConstants.Y, 0);
		return translation;
	}

	private JSONObject buildVOI() {
		JSONObject voi = new JSONObject();
		voi.put(CommonConstants.WINDOW_WIDTH, 693);
		voi.put(CommonConstants.WINDOW_CENTER, 288);
		return voi;
	}

	private JSONObject buildViewPort(JSONObject translation, JSONObject voi) {
		JSONObject viewPort = new JSONObject();
		viewPort.put(CommonConstants.SCALE, "");
		viewPort.put(CommonConstants.TRANSLATION, translation);
		viewPort.put(CommonConstants.VOI, voi);
		viewPort.put(CommonConstants.INVERT, false);
		viewPort.put(CommonConstants.PIXEL_REPLICATION, false);
		viewPort.put(CommonConstants.ROTATION, 0);
		return viewPort;
	}

	private JSONObject buildFreehandHandle(Map<String, Object> current, Map<String, Object> next, int measurementNumber,
			boolean isLastSegment) {
		JSONObject handle = new JSONObject();
		JSONObject lineCoord = new JSONObject();
		List<JSONObject> lines = new ArrayList<>();

		handle.put(CommonConstants.X, current.get(CommonConstants.X));
		handle.put(CommonConstants.Y, current.get(CommonConstants.Y));
		handle.put(CommonConstants.HEIGHT, true);
		handle.put(CommonConstants.ACTIVE, false);

		lineCoord.put(CommonConstants.X, next.get(CommonConstants.X));
		lineCoord.put(CommonConstants.Y, next.get(CommonConstants.Y));
		
		// Only include the second point in "LINES" if it's the last coordinate (for wrapping around)
		if (isLastSegment) {
			JSONObject midPoint = new JSONObject();
			midPoint.put(CommonConstants.X, next.get(CommonConstants.X));
			midPoint.put(CommonConstants.Y, next.get(CommonConstants.Y));
			lines.add(midPoint);
			lineCoord.put(CommonConstants.LINES, lines);
		}

		lines.clear();
		lines.add(lineCoord);
		handle.put(CommonConstants.LINES, lines);
		handle.put(CommonConstants.MESUREMENT_NUMBER, measurementNumber);

		return handle;
	}

	private JSONObject buildTextBoxJson() {
	    JSONObject textBox = new JSONObject();
	    textBox.put(CommonConstants.ACTIVE, false);
	    textBox.put(CommonConstants.HAS_MOVED, false);
	    textBox.put(CommonConstants.MOVES_INDEPENDENTLY, false);
	    textBox.put(CommonConstants.DRAW_INDEPENDENTLY, true);
	    textBox.put(CommonConstants.ALLOW_OUTSIDE_IMAGE, true);
	    textBox.put(CommonConstants.HAS_BOUNDING_BOX, true);
	    textBox.put(CommonConstants.HIGHLIGHT, false);
	    textBox.put(CommonConstants.LOCKED, false);
	    return textBox;
	}

	private JSONObject buildViewPortJson() {
	    JSONObject translation = new JSONObject();
	    translation.put(CommonConstants.X, 0);
	    translation.put(CommonConstants.Y, 0);

	    JSONObject voi = new JSONObject();
	    voi.put(CommonConstants.WINDOW_WIDTH, 876);
	    voi.put(CommonConstants.WINDOW_CENTER, 382);

	    JSONObject viewPort = new JSONObject();
	    viewPort.put(CommonConstants.SCALE, 0.9471153846153846);
	    viewPort.put(CommonConstants.TRANSLATION, translation);
	    viewPort.put(CommonConstants.VOI, voi);
	    viewPort.put(CommonConstants.INVERT, false);
	    viewPort.put(CommonConstants.PIXEL_REPLICATION, false);
	    viewPort.put(CommonConstants.ROTATION, 0);
	    return viewPort;
	}
	
	private Object parseNumber(String value) {
	    if (value == null || value.trim().isEmpty()) return null;
	    if ("N/A".equalsIgnoreCase(value)) return value;
	    try {
	        return Double.parseDouble(value);
	    } catch (NumberFormatException e) {
	        return null;
	    }
	}

	private Object parseValue(String key, JSONObject jsonObject) {
	    if (jsonObject == null || jsonObject.get(key) == null || jsonObject.get(key).toString().isEmpty()) return null;

	    try {
	        if (CommonConstants.ED_FRAME.equalsIgnoreCase(key) || CommonConstants.ES_FRAME.equalsIgnoreCase(key)) {
	            return Integer.parseInt(jsonObject.get(key).toString());
	        } else {
	            return Double.parseDouble(jsonObject.get(key).toString());
	        }
	    } catch (NumberFormatException e) {
	        return null;
	    }
	}

	@Override
	public String getPacsUrl(String orgId) {
		Optional<OrgPacsValidationUrlProjection> orgOpt = orgRepo.findPacsValidationUrlById(Long.valueOf(orgId));
        if(orgOpt.isEmpty()) {
        	LOG.debug("orgopt is empty:"+defaultPacsUrl);
        	return defaultPacsUrl;
        }
    	String pacsUrl = orgOpt.get().getPacsUrl();
    	LOG.debug("pacsUrl::"+pacsUrl);
    	if(pacsUrl == null || pacsUrl.contains("null") || pacsUrl.isEmpty() || pacsUrl.isBlank()) {
    		LOG.debug("returning defaultpacsUrl::"+defaultPacsUrl);
        	return defaultPacsUrl;
    	}else {
        	return pacsUrl;
    	}
	}
	
	@Override
	public String getPacsUrlForViewer(String orgId) {
		String pacsUrlForViewer = dcm4cheeBaseUrl + "/aets/DCM4CHEE/rs";
		Optional<OrgPacsValidationUrlProjection> orgOpt = orgRepo.findPacsValidationUrlById(Long.valueOf(orgId));
        if(orgOpt.isEmpty()) {
        	LOG.debug("orgopt is empty:"+pacsUrlForViewer);
        	return pacsUrlForViewer;
        }
    	String pacsUrl = orgOpt.get().getPacsUrl();
    	LOG.debug("pacsUrl::"+pacsUrl);
    	if(pacsUrl == null || pacsUrl.contains("null") || pacsUrl.isEmpty() || pacsUrl.isBlank()) {
    		LOG.debug("returning defaultpacsUrl::"+pacsUrlForViewer);
        	return pacsUrlForViewer;
    	}else {
        	return pacsUrl;
    	}
	}

	@Override
	public String getKeyClokaSwitch() {
		return keyCloakSwitch;
	}

	@Override
	public String getKeyCloakuserPassword() {
		return keyCloakuserPassword;
	}

	@Override
	public String getKeyCloakAdminPassword() {
		return keyCloakAdminPassword;
	}

	@Override
	public KeycloakToken getPacsToken(String orgId,String userEmail) throws Exception {
	    try {
	        Long org = Long.valueOf(orgId);
	        return extToken.callExternalApiAndGetToken(org);
	    } catch (Exception ex) {
	        LOG.debug("Error while fetching external token: {}", ex.getMessage());
	        return getFallbackKeycloakToken(userEmail);
	    }
	}

	private KeycloakToken getFallbackKeycloakToken(String userEmail) throws Exception {
	    String role = userRepo.findRoleNameByEmail(userEmail)
	            .orElse(UserConstants.SUPER_ADMIN);

	    LOG.info("UserEmail:{}",userEmail);
	    boolean isSuperAdmin = UserConstants.SUPER_ADMIN.equalsIgnoreCase(role);
	    String password = isSuperAdmin ? keyCloakAdminPassword : keyCloakuserPassword;

	    return keycloakTokenService.getUserAccessToken(userEmail, password);
	}


	public String getNoHeaderOrg() {
		return NoHeaderOrg;
	}

	public String getHeaderFooterProportions() {
		return headerFooterProportions;
	}
}
