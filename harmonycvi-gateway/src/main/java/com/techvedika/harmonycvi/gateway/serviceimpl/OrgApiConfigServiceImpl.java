package com.techvedika.harmonycvi.gateway.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvedika.harmonycvi.gateway.dto.OrgApiConfigRequest;
import com.techvedika.harmonycvi.gateway.entity.OrgApiConfig;
import com.techvedika.harmonycvi.gateway.entity.Organization;
import com.techvedika.harmonycvi.gateway.repository.OrgApiConfigRepository;
import com.techvedika.harmonycvi.gateway.repository.OrganizationRepository;
import com.techvedika.harmonycvi.gateway.service.OrgApiConfigService;

@Service
public class OrgApiConfigServiceImpl implements OrgApiConfigService {

    @Autowired
    private OrgApiConfigRepository repo;

    @Autowired
    private OrganizationRepository orgRepo;

    @Autowired
    private ObjectMapper mapper;

    public void saveOrgApiConfig(OrgApiConfigRequest req) throws Exception {

        Organization org = orgRepo.findById(req.getOrgId())
                .orElseThrow(() -> new Exception("Invalid org_id"));

        OrgApiConfig config = new OrgApiConfig();

        config.setOrg(org);
        config.setApiUrl(req.getApiUrl());
        config.setMethod(req.getMethod());
        config.setRequestParamsType(req.getRequestParamsType());
        config.setResponseTokenField(req.getResponseTokenField());
        
        config.setUserApiUrl(req.getUserApiUrl());
        config.setUserMethod(req.getUserMethod());
        config.setUserRequestParamsType(req.getUserRequestParamsType());
        config.setUserResponseTokenField(req.getUserResponseTokenField());

        // Convert map to JSON string
        if (req.getRequestParams() != null)
            config.setRequestParams(mapper.writeValueAsString(req.getRequestParams()));

        if (req.getHeaders() != null)
            config.setHeaders(mapper.writeValueAsString(req.getHeaders()));
        
        if (req.getUserRequestParams() != null)
            config.setUserRequestParams(mapper.writeValueAsString(req.getUserRequestParams()));

        if (req.getUserHeaders() != null)
            config.setUserHeaders(mapper.writeValueAsString(req.getUserHeaders()));

        repo.save(config);
    }
}
