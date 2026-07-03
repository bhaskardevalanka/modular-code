package com.techvedika.harmonycvi.gateway.controller;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techvedika.harmonycvi.gateway.service.UserCommentsService;

@RestController
@RequestMapping("/comments")
public class UserCommentsController {
	private static final Logger LOG = LoggerFactory.getLogger(CenterController.class);

    @Autowired
    private UserCommentsService userCommentsService;

    @PostMapping("/save")
    public ResponseEntity<JSONObject> save(@RequestBody JSONObject json) {
        System.out.println("Service hit===========>" + json);
        JSONObject response = userCommentsService.save(json);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/getByPatientId")
    public ResponseEntity<JSONObject> getByPatientId(@RequestBody JSONObject json) {
        System.out.println("Service hit===========>" + json);
        JSONObject response = userCommentsService.getByPatientId(json);
        return ResponseEntity.ok(response);
    }
}
