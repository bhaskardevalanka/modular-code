package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.entity.VersionInfo;
import com.techvedika.harmonycvi.gateway.repository.VersionInfoRepository;
import com.techvedika.harmonycvi.gateway.service.VersionService;
import com.techvedika.harmonycvi.gateway.util.UserUtils;

import jakarta.servlet.http.HttpServletRequest;

@Service
@Transactional
public class VersionServiceImpl implements VersionService {
	private static final Logger log = LoggerFactory.getLogger(VersionServiceImpl.class);
	
	/** Header names */
    private static final String HDR_TYPE    = "Type";
    private static final String HDR_VERSION = "Version";
    
    @Autowired
    private VersionInfoRepository versionInfoRepo;
    
    @Autowired
    private UserUtils userUtils;

	@Override
	public JSONObject checkVersion(HttpServletRequest request) {
		String type = request.getHeader(HDR_TYPE);
		
        if (type == null) {
        	userUtils.globalException("Type header is missing",Integer.parseInt(UserConstants.BAD_REQUEST_CODE));
        }

        if (type.equalsIgnoreCase("weasis")) {
            return null;
        }

        String uiVersion = request.getHeader(HDR_VERSION);
        
        if (uiVersion == null) {
            userUtils.globalException("Version header is missing",Integer.parseInt(UserConstants.BAD_REQUEST_CODE));
        }
        
        Optional<VersionInfo> result = versionInfoRepo.findByType(type);
        if(result.isPresent()) {
        	VersionInfo serverInfo = result.get();
        	int uiVal = getVersionValue(uiVersion);
            int serverVal = getVersionValue(serverInfo.getVersion());

            if (uiVal > serverVal) {
            	serverInfo.setVersion(uiVersion);
                versionInfoRepo.save(serverInfo);
                return null;
            }
            if (uiVal < serverVal) {
                Map<String,Object> resp = new HashMap<>();
                resp.put("statusCode", "500");
                resp.put("statusMessage", "New version is available!");
                return new JSONObject(resp);
            }
        	
        } else {
        	log.info("No verison details found in server");
        	return null;
        }        
        
        return null; 
	}
	
	private int getVersionValue(String v) {
        try { return Integer.parseInt(v.replaceAll("\\.", "")); }
        catch (NumberFormatException ex) {
        	log.info("Exception while reading version value:"+ex.getLocalizedMessage());
        	return 0;
        }
    }

}
