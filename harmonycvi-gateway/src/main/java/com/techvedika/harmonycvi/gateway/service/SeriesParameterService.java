package com.techvedika.harmonycvi.gateway.service;

import org.json.simple.JSONObject;

public interface SeriesParameterService {

	JSONObject getParameter(JSONObject jsonRequest);

	JSONObject saveParameter(JSONObject json);

	JSONObject saveGLSSeriesParameter(JSONObject json);

	JSONObject getGLSParameter(JSONObject json);

	JSONObject saveDESeriesParam(JSONObject json);

	JSONObject saveMultipleSeriesParameters(JSONObject json);

	boolean deleteSeriesParameters(String studyId, String seriesId);

	JSONObject getECVMapDetails(JSONObject json);
}
