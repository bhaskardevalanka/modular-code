package com.techvedika.harmonycvi.gateway.service;

import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;

import com.techvedika.harmonycvi.gateway.entity.Bookmarks;

public interface BookmarkService {
	public JSONObject save(JSONObject request);

	JSONObject getList(Long userID, String study_id);

	public ResponseEntity<JSONObject> getBookmarkDetailById(Long bookmark_id);

	public ResponseEntity<JSONObject> deleteBookmarkById(Long user_id, Long bookmark_id);
	
	public void deleteBookmarkByStudyId(String studyId);
	
	public ResponseEntity<JSONObject> getBookmarkByVersion(String studyId, boolean isPreprocess);
	
	public Bookmarks getBookmark(String studyId,String bookmarkId,boolean shouldCreate); 
	
	public Long getBookmarkId(String studyId,String bookmarkId,boolean shouldCreate); 
	
	public ResponseEntity<JSONObject> converttoPreprocessBookmark(String studyId,Long bookmarkId);
}
