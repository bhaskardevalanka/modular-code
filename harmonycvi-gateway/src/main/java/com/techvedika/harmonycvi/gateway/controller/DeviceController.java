package com.techvedika.harmonycvi.gateway.controller;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.service.DeviceService;
import com.techvedika.harmonycvi.gateway.service.VersionService;
import com.techvedika.harmonycvi.gateway.util.UserUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Spring Boot replacement for the former Java EE `DeviceController`.
 *
 * <p><b>Notes & conventions</b></p>
 * <ul>
 *   <li>All methods return {@code ResponseEntity<JSONObject>} for clearer HTTP semantics.</li>
 *   <li>Business errors are signalled via {@code userUtils.globalException(...)} which throws
 *       your custom {@code ApiException}; those are handled globally by
 *       {@code @ControllerAdvice ExceptionHandler}s.</li>
 *   <li>No explicit JWT extraction here – Spring Security has already authenticated the user.
 *       If your {@code versionService.checkVersion(...)} still needs the raw
 *       {@link HttpServletRequest}, it is provided as a method parameter.</li>
 * </ul>
 */
@RestController
@RequestMapping(path = "/device", produces = MediaType.APPLICATION_JSON_VALUE)
public class DeviceController {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceController.class);
    
    @Autowired
    private DeviceService deviceService;
    
    @Autowired
    private VersionService versionService;
    
    @Autowired
    private UserUtils userUtils;
    

    /* ---------- GET LIST ---------- */

    @PostMapping(path = "/getList", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONObject> getList(@RequestBody JSONObject json,
                                              HttpServletRequest request) {

        LOG.info("Service hit ==> {}", json);

        // Optional version check
        JSONObject versionResp = versionService.checkVersion(request);
        if (versionResp != null) {
            return ResponseEntity.ok(versionResp);
        }

        JSONObject body = deviceService.getList(json);
        return ResponseEntity.ok(body);
    }

    /* ---------- CREATE ---------- */

    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONObject> create(@RequestBody JSONObject json,
                                             HttpServletRequest request) {

        LOG.info("Service hit ==> {}", json);

        JSONObject versionResp = versionService.checkVersion(request);
        if (versionResp != null) {
            return ResponseEntity.ok(versionResp);
        }

        JSONObject body = deviceService.create(json);
        return ResponseEntity.ok(body);
    }

    /* ---------- UPDATE ---------- */

    @PostMapping(path = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONObject> update(@RequestBody JSONObject json,
                                             HttpServletRequest request) {

        LOG.info("Service hit ==> {}", json);

        JSONObject versionResp = versionService.checkVersion(request);
        if (versionResp != null) {
            return ResponseEntity.ok(versionResp);
        }

        JSONObject body = deviceService.update(json);
        return ResponseEntity.ok(body);
    }

    /* ---------- GET BY ID ---------- */

    @GetMapping(path = "/getById/{id}")
    public ResponseEntity<JSONObject> getById(@PathVariable String id,
                                              HttpServletRequest request) {

        LOG.info("Service hit ==> getById/{}", id);

        JSONObject versionResp = versionService.checkVersion(request);
        if (versionResp != null) {
            return ResponseEntity.ok(versionResp);
        }

        JSONObject body = deviceService.getById(id);
        return ResponseEntity.ok(body);
    }
}