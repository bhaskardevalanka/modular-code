package com.techvedika.harmonycvi.gateway.service;

import org.json.simple.JSONObject;

public interface SeriesMeasurementsService {
	public JSONObject getSeriesMeasurementsData(JSONObject json);
	public JSONObject saveSeriesFreeHandMeasurementsInfo(JSONObject json);
	public void deleteMeasurement(String patientID);
	public JSONObject getSeriesMeasurementsDataBySeriesId(JSONObject json);
	public JSONObject updateSeriesMeasurement(JSONObject json);
	public JSONObject saveSegments(JSONObject json);
	public JSONObject getSeriesSegments(JSONObject json);
	public JSONObject updateQflowContour(JSONObject json);
	public JSONObject deleteContours(JSONObject json);
	public JSONObject updateGLSSeriesMeasurement(JSONObject json);
	public JSONObject saveStudyMeasurementsInfo(JSONObject json);
	public JSONObject updateDESeriesMeasurement(JSONObject json);
	public JSONObject shortAxisPropagation(JSONObject json);
	public JSONObject qFlowPropagation(JSONObject json);
	public JSONObject glsPropagation(JSONObject json);
	public JSONObject atrialPropagation(JSONObject json);
	boolean deleteContoursBySeries(String studyId, String seriesId);

}
