package com.techvedika.harmonycvi.gateway.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.simple.JSONObject;

import com.techvedika.harmonycvi.gateway.dto.KeycloakToken;
import com.techvedika.harmonycvi.gateway.entity.ParameterReference;
import com.techvedika.harmonycvi.gateway.entity.SeriesMeasurements;
import com.techvedika.harmonycvi.gateway.entity.SeriesSegments;
import com.techvedika.harmonycvi.gateway.entity.StudyAnnotation;

import jakarta.servlet.http.HttpServletRequest;

public interface CommonMethod {

	public JSONObject createResponse(String statusCode, String responseMessage);

	public SeriesMeasurements setSeriesMeasurementsObject(SeriesMeasurements smObject, ArrayList<JSONObject> request,
			String seriesId, JSONObject jsonRequest);

	JSONObject createJsonObject(SeriesMeasurements data);

	JSONObject processFreeHandData(JSONObject data);

	JSONObject createMesuremetJson(LinkedHashMap<String, Object> cordinateObj, HashMap<String, Object> s,
			Integer mesurementNo);

	JSONObject createFreeHandMesuremetJson(LinkedHashMap<String, Object> singleInstanceObject,
			LinkedHashMap<String, Object> commonData, String coordinateType, Integer mesuremenNumber);

	SeriesMeasurements setSeriesMeasurementsObjectForFreeHand(SeriesMeasurements smObject,
			ArrayList<JSONObject> request, String seriesId, JSONObject jsonRequest,
			LinkedHashMap<String, Object> commonData);

	LinkedHashMap<String, Object> createCommonDataJson(LinkedHashMap<String, Object> commonData);

	String getSHA(String input);

	List parameterResponse(JSONObject lv, JSONObject rv, List<ParameterReference> parameterReferenceList);

	JSONObject createStraightLineFormatForFreeHand(LinkedHashMap<String, Object> singleInstanceObject,
			LinkedHashMap<String, Object> cordinateObj, HashMap<String, Object> commonData, Integer mesurementNo);

	JSONObject createStudyAnnotationObj(StudyAnnotation data);

	JSONObject createJsonSSObject(SeriesSegments data);

	JSONObject deParameterResponse(JSONObject deParamData);

	boolean isAIServiceRunning();

	public String getReportToS3();

	public void setReportToS3(String reportToS3);
	public String getTargetPath();

	public void setTargetPath(String targetPath);
	public String getAccessPath();

	public void setAccessPath(String accessPath);
	
	public String getPacsUrl(String orgId);
	
	public String getPacsUrlForViewer(String orgId);
	
	public String getKeyClokaSwitch();
	
	public String getKeyCloakuserPassword();
	
	public String getKeyCloakAdminPassword();
	
	public KeycloakToken getPacsToken(String orgId,String userEmail) throws Exception;
	
	public String getNoHeaderOrg();
	
	public String getHeaderFooterProportions();
}
