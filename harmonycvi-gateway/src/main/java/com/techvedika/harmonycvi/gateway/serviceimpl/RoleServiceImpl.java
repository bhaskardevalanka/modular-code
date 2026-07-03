package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.projection.RoleProjection;
import com.techvedika.harmonycvi.gateway.repository.RoleRepository;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;
import com.techvedika.harmonycvi.gateway.security.SecurityUtil;
import com.techvedika.harmonycvi.gateway.service.RoleService;
import com.techvedika.harmonycvi.gateway.util.UserUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private static final Logger LOG = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Autowired
    private RoleRepository roleRepo;


    @Autowired
    private UserUtils userUtils;

    @Autowired
    private UserRepository userRepo;


    @Override
    @Transactional
    public JSONObject getList() {
        LOG.info("Inside getList API");
        JSONObject response = new JSONObject();
        
        String userEmailId = SecurityUtil.currentUserEmailId();

        boolean allowed = userRepo.hasPrivilege(userEmailId, UserConstants.PRIVILEGE_LIST_ROLE);

		if (!allowed) {
		    userUtils.globalException(
		        UserConstants.NO_PRIVILAGES_TO_ACCESS,
		        Integer.parseInt(UserConstants.STATUS_RESTRICTED)
		    );
		}
		
		Long userId = userRepo.findIdByEmail(userEmailId).orElse(0L);
		LOG.info("userId:"+userId);
        boolean isSuperAdmin = userUtils.isSuperAdmin(userId);
        boolean isOrgAdmin   = userUtils.isOrgAdmin(userId);
        boolean isUserAdmin  = userUtils.isUserAdmin(userId);

        LOG.info("after boolean checking::"+isSuperAdmin+" "+isOrgAdmin+" "+isUserAdmin);
        List<RoleProjection> roles = new ArrayList<>();
        if (isSuperAdmin) {
            roles = roleRepo.findByActiveTrueOrderByCreatedDtAsc();
        } else if (isOrgAdmin) {
            roles = roleRepo.findByActiveTrueAndNameNotOrderByCreatedDtAsc("SUPER ADMIN");
        } else if (isUserAdmin) {
            roles = roleRepo.findActiveDoctorsAndConsultantsOrderedByCreatedDate();
        }
        
        LOG.info("roles:"+roles.size());

        if (roles != null && !roles.isEmpty()) {
            List<Map<String, Object>> listMap = new ArrayList<>();
            for (RoleProjection role : roles) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", role.getName());
                map.put("id", role.getId());
                map.put("active", role.getActive());
                listMap.add(map);
            }
            LOG.info("getList API End");
            return userUtils.updateResponse(response,
                    UserConstants.STATUS_SUCCESS, UserConstants.SUCCESS,
                    UserConstants.ROLE_GET_SUCCESS_MESSAGE, listMap);
        } else {
            LOG.info("getList API End");
            userUtils.globalException(UserConstants.EMPTY_RESULT,
                    Integer.parseInt(UserConstants.NO_CONTENT_CODE));
            return null;
        }
    }
}
