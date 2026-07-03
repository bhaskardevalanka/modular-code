package com.techvedika.harmonycvi.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techvedika.harmonycvi.gateway.dto.OrgApiConfigRequest;
import com.techvedika.harmonycvi.gateway.service.OrgApiConfigService;

@RestController
@RequestMapping("/org/api-config")
public class OrgApiConfigController {

    @Autowired
    private OrgApiConfigService service;

    @PostMapping("/save")
    public String saveConfig(@RequestBody OrgApiConfigRequest request) throws Exception {
        service.saveOrgApiConfig(request);
        return "API configuration saved successfully!";
    }
}
