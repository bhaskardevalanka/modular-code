package com.techvedika.harmonycvi.gateway.controller;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techvedika.harmonycvi.gateway.service.BookmarkService;

@RestController
@RequestMapping("/bookmark")
public class BookmarkController {
	
	private static final Logger LOG = LoggerFactory.getLogger(BookmarkController.class);

    @Autowired
    private BookmarkService bookmarkService;

    // Get bookmark list by study ID
    @GetMapping(value = "/getBookmarkList/{userID}/{studyUID}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject getBookmarkListByStudyId(@PathVariable("userID") Long userID, @PathVariable("studyUID") String studyUID) {
        System.out.println("Start of " + this.getClass().getName() + ".getBookmarkListByStudyId");
        JSONObject response = new JSONObject();
        response = bookmarkService.getList(userID, studyUID);
        System.out.println("End of " + this.getClass().getName() + ".getBookmarkListByStudyId");
        return response;
    }

    // Save bookmark
    @PostMapping(value = "/saveBookmark", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public JSONObject saveBookmark(@RequestBody JSONObject json) {
        System.out.println("Start of " + this.getClass().getName() + ".saveBookmark");
        JSONObject response = new JSONObject();
        response = bookmarkService.save(json);
        System.out.println("End of " + this.getClass().getName() + ".saveBookmark");
        return response;
    }

    // Get bookmark details by ID
    @GetMapping(value = "/getBookmarkDetails/{bookmark_id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<JSONObject> getBookmarkDetailsById(@PathVariable("bookmark_id") Long bookmark_id) {
        System.out.println("Start of " + this.getClass().getName() + ".getBookmarkDetailsById");
        //JSONObject response = new JSONObject();
        ResponseEntity<JSONObject> response = bookmarkService.getBookmarkDetailById(bookmark_id);
        System.out.println("End of " + this.getClass().getName() + ".getBookmarkDetailsById");
        return response;
    }

    // Delete bookmark
    @DeleteMapping(value = "/deleteBookmark/{user_id}/{bookmark_id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<JSONObject> deleteBookmark(@PathVariable("user_id") Long user_id, @PathVariable("bookmark_id") Long bookmark_id) {
        System.out.println("Start of " + this.getClass().getName() + ".deleteBookmark");
        //JSONObject response = new JSONObject();
        ResponseEntity<JSONObject> response = bookmarkService.deleteBookmarkById(user_id, bookmark_id);
        System.out.println("End of " + this.getClass().getName() + ".deleteBookmark");
        return response;
    }

    // Get latest bookmark by version
    @GetMapping(value = "/getLatestBookmark/{study_id}/{is_preprocess}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<JSONObject> getBookmarkByVersion(@PathVariable("study_id") String studyId, @PathVariable("is_preprocess") boolean isPreprocess) {
        System.out.println("Start of " + this.getClass().getName() + ".getBookmarkByVersion");
        //JSONObject response = new JSONObject();
        ResponseEntity<JSONObject> response = bookmarkService.getBookmarkByVersion(studyId, isPreprocess);
        System.out.println("End of " + this.getClass().getName() + ".getBookmarkByVersion");
        return response;
    }

    // Convert bookmark to preprocess
    @PostMapping(value = "/convertToPreprocess/{bookmark_id}/{study_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONObject> updateTransferStatus(@PathVariable("study_id") String studyId, @PathVariable("bookmark_id") Long bookmarkId) {
        return bookmarkService.converttoPreprocessBookmark(studyId, bookmarkId);
    }
}