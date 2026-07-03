package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.projection.PrivilegeRecord;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;
import com.techvedika.harmonycvi.gateway.security.SecurityUtil;
import com.techvedika.harmonycvi.gateway.service.PrivilegesService;
import com.techvedika.harmonycvi.gateway.util.UserUtils;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PrivilegesServiceImpl implements PrivilegesService {

    private static final Logger LOG = LoggerFactory.getLogger(PrivilegesServiceImpl.class);
    
    @Autowired
    private UserUtils userUtils;
    
    @Autowired
    private UserRepository userRepo;
    
    @Override
    @Transactional
    public JSONObject getList() {
        LOG.info("Started getList");

        JSONObject response = new JSONObject();

        String userEmailId = SecurityUtil.currentUserEmailId();

        boolean allowed = userRepo.hasPrivilege(userEmailId, UserConstants.PRIVILEGE_LIST_ROLE);

		if (!allowed) {
		    userUtils.globalException(
		        UserConstants.NO_PRIVILAGES_TO_ACCESS,
		        Integer.parseInt(UserConstants.STATUS_RESTRICTED)
		    );
		}

		List<PrivilegeRecord> privilegeRecord = userRepo.findPrivilegesByEmail(userEmailId);
		
		if(privilegeRecord!=null && !privilegeRecord.isEmpty()) {
			List<Map<String, Object>> listMap = privilegeRecord.stream()
                    .map(priv -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", priv.id());
                        map.put("name", priv.name());
                        map.put("active", priv.active());
                        return map;
                    }).collect(Collectors.toList());
			 return userUtils.updateResponse(response,
	                    UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
	                    UserConstants.PRIVILEGE_LIST_USER, listMap);
        } else {
            userUtils.globalException(UserConstants.EMPTY_RESULT,
                    Integer.parseInt(UserConstants.NO_CONTENT_CODE));
            return null; // will not be reached because above line throws exception
        }
    }

}