package com.techvedika.harmonycvi.gateway.controller;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techvedika.harmonycvi.gateway.service.ContourCommentService;

@RestController
@RequestMapping("/contourComment")
public class ContourCommentController {

	@Autowired
	private ContourCommentService ccService;
	
	@PostMapping(value = "/saveComment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public JSONObject saveComment(@RequestBody JSONObject request) {
		System.out.println("Start of "+this.getClass().getName()+".saveComment");
		JSONObject response= new JSONObject();
		response=ccService.saveComment(request);
		System.out.println("End of "+this.getClass().getName()+".saveComment");
		return response;
	}
}

