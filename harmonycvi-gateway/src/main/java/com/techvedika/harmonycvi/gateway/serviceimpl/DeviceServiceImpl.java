package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.constant.UserConstants;
import com.techvedika.harmonycvi.gateway.entity.Center;
import com.techvedika.harmonycvi.gateway.entity.DeviceDetails;
import com.techvedika.harmonycvi.gateway.entity.User;
import com.techvedika.harmonycvi.gateway.repository.CenterRepository;
import com.techvedika.harmonycvi.gateway.repository.DeviceDetailsRepository;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;
import com.techvedika.harmonycvi.gateway.security.SecurityUtil;
import com.techvedika.harmonycvi.gateway.service.DeviceService;
import com.techvedika.harmonycvi.gateway.util.UserUtils;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class DeviceServiceImpl implements DeviceService {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceServiceImpl.class);
    
    @Autowired
    private DeviceDetailsRepository deviceRepo;
    
    @Autowired
    private CenterRepository centerRepo;
    
    @Autowired
    private UserRepository userRepo;
    
    @Autowired
    private UserUtils userUtils;
    

    /* ---------- CREATE ---------- */
    @Override
    public JSONObject create(JSONObject json) {

        // 1) current user (via Spring Security)
    	String userEmailId = SecurityUtil.currentUserEmailId();

        // 2) validate mandatory fields
        String err = userUtils.validateDeviceMandatoryField(json);
        if (err != null && !err.isBlank())
            userUtils.globalException(err, Integer.parseInt(UserConstants.BAD_REQUEST_CODE));
        
        boolean allowed = userRepo.hasPrivilege(userEmailId, UserConstants.PRIVILEGE_ADD_DEVICE);

		if (!allowed) {
		    userUtils.globalException(
		        UserConstants.NO_PRIVILAGES_TO_ACCESS,
		        Integer.parseInt(UserConstants.STATUS_RESTRICTED)
		    );
		}

		Long userId = userRepo.findIdByEmail(userEmailId).orElse(0L);
		
        // 3) uniqueness of device UID
        String devUID = json.get(UserConstants.DEVICE_UID).toString();
        if (deviceRepo.existsByDevUID(devUID)) {
            userUtils.globalException(UserConstants.DEVICE_ALREADY_EXIST_MESSAGE,
                    Integer.parseInt(UserConstants.STATUS_DUPLICATE_CODE));
        }


        // 4) centre & org
        Long centreId = Long.valueOf(json.get(UserConstants.CENTER_ID).toString());
        Optional<Center> centerOpt = centerRepo.findById(centreId);
        if(centerOpt.isEmpty()) {
        	userUtils.globalException(
                    UserConstants.NO_CENTER,
                    Integer.parseInt(UserConstants.NO_CONTENT_CODE));
        }
        Center center = centerOpt.get();
        // 5) persist
        DeviceDetails dd = new DeviceDetails();
        dd.setDevUID(devUID);
        dd.setDeviceType(json.get(UserConstants.DEVICE_TYPE).toString());
        dd.setCenter(center);
        dd.setOrganization(center.getOrganization());
        dd.setCreatedBy(userId);
        dd.setCreatedDt(new Date());
        dd.setActive((Boolean)json.get(UserConstants.ACTIVE));

        deviceRepo.save(dd);

        return userUtils.updateResponse(new JSONObject(),
                                        UserConstants.STATUS_SUCCESS,
                                        UserConstants.SUCCESS,
                                        UserConstants.SAVED,
                                        null);
    }

    /* ---------- UPDATE ---------- */

    @Override
    public JSONObject update(JSONObject json) {

    	String userEmailId = SecurityUtil.currentUserEmailId();
    	Long userId = userRepo.findIdByEmail(userEmailId).orElse(0L);
        Long deviceId = Long.valueOf(json.get(UserConstants.ID).toString());
        Optional<DeviceDetails> deviceDetailOpt = deviceRepo.findById(deviceId);
        if(deviceDetailOpt.isEmpty()) {
        	userUtils.globalException(
                    UserConstants.DEVICE_NOT_EXIST_MESSAGE,
                    Integer.parseInt(UserConstants.NO_CONTENT_CODE));
        }
        
        DeviceDetails deviceDetail = deviceDetailOpt.get();

        String newUID = json.containsKey(UserConstants.DEVICE_UID)
                ? String.valueOf(json.get(UserConstants.DEVICE_UID))
                : deviceDetail.getDevUID();

        if (deviceRepo.existsByDevUIDAndIdNot(newUID, deviceId)) {
            userUtils.globalException(UserConstants.DUPLICAT_DEVICE,
                    Integer.parseInt(UserConstants.STATUS_DUPLICATE_CODE));
        }

        // optional centre update
        if (json.containsKey(UserConstants.CENTER_ID) && json.get(UserConstants.CENTER_ID)!=null) {
            Long centerId = Long.valueOf(json.get(UserConstants.CENTER_ID).toString());
            Optional<Center> centerOpt = centerRepo.findById(centerId);
            if(centerOpt.isEmpty()) {
            	userUtils.globalException(
                        UserConstants.NO_CENTER,
                        Integer.parseInt(UserConstants.NO_CONTENT_CODE));
            }
            Center center = centerOpt.get();
            deviceDetail.setCenter(center);
            deviceDetail.setOrganization(center.getOrganization());
        }

        deviceDetail.setDevUID(newUID);
        deviceDetail.setDeviceType(json.getOrDefault(UserConstants.DEVICE_TYPE, deviceDetail.getDeviceType()).toString());
        deviceDetail.setActive((Boolean)json.getOrDefault(UserConstants.ACTIVE, deviceDetail.getActive()));
        deviceDetail.setLastUpdatedBy(userId);
        deviceDetail.setLastUpdatedDt(new Date());

        deviceRepo.save(deviceDetail);   // merge‑style save

        String msg = "";
        if(json.containsKey(UserConstants.IS_STATUS_UPDATE) && json.get(UserConstants.IS_STATUS_UPDATE)!=null) {
        	if(json.get(UserConstants.IS_STATUS_UPDATE).toString().equalsIgnoreCase("yes")) {
        		msg = (UserConstants.DEVICE +
                           (deviceDetail.getActive() ? UserConstants.ENABLED : UserConstants.DISABLED));
        	}else {
        		msg = UserConstants.DEVICE + UserConstants.UPDATED;
        	}
        	
        }else {
        	msg = UserConstants.DEVICE + UserConstants.UPDATED;
        }
        
        return userUtils.updateResponse(new JSONObject(),
                                        UserConstants.STATUS_SUCCESS,
                                        UserConstants.SUCCESS,
                                        msg,
                                        null);
    }

    /* ---------- LIST ---------- */

    @Override
    public JSONObject getList(JSONObject json) {

        Long centerId = Long.valueOf(json.get(UserConstants.CENTER_ID).toString());
        boolean centerExists = centerRepo.existsById(centerId);
        if(!centerExists) {
        	userUtils.globalException(
                    UserConstants.NO_CENTER,
                    Integer.parseInt(UserConstants.NO_CONTENT_CODE));
        }

        List<DeviceDetails> ddList = deviceRepo.findByCenterId(centerId);
        if (ddList.isEmpty())
            userUtils.globalException(UserConstants.EMPTY_RESULT,
                    Integer.parseInt(UserConstants.NO_CONTENT_CODE));

        List<Map<String, Object>> result = ddList.stream()
                .map(this::toMap)
                .toList();

        return userUtils.updateResponse(new JSONObject(),
                                        UserConstants.STATUS_SUCCESS,
                                        UserConstants.SUCCESS,
                                        UserConstants.FETCHED,
                                        result);
    }

    /* ---------- GET BY ID ---------- */

    @Override
    public JSONObject getById(String deviceId) {

    	String userEmailId = SecurityUtil.currentUserEmailId();
        
        boolean allowed = userRepo.hasPrivilege(userEmailId, UserConstants.PRIVILEGE_LIST_DEVICE);

		if (!allowed) {
		    userUtils.globalException(
		        UserConstants.NO_PRIVILAGES_TO_ACCESS,
		        Integer.parseInt(UserConstants.STATUS_RESTRICTED)
		    );
		}

        Optional<DeviceDetails> deviceDetailOpt = deviceRepo.findById(Long.valueOf(deviceId));
        if(deviceDetailOpt.isEmpty()) {
        	userUtils.globalException(
                    UserConstants.DEVICE_NOT_EXIST_MESSAGE,
                    Integer.parseInt(UserConstants.NO_CONTENT_CODE));
        }
        
        DeviceDetails dd = deviceDetailOpt.get();

        return userUtils.updateResponse(new JSONObject(),
                                        UserConstants.STATUS_SUCCESS,
                                        UserConstants.SUCCESS,
                                        UserConstants.FETCHED,
                                        toMap(dd));
    }

    /* ---------- helper ---------- */

    private Map<String, Object> toMap(DeviceDetails dd) {
        Map<String, Object> m = new HashMap<>();
        m.put(UserConstants.DEVICE_UID , dd.getDevUID());
        m.put(UserConstants.CENTER_ID  , dd.getCenter().getId());
        m.put(UserConstants.CENTER_NAME, dd.getCenter().getName());
        m.put(UserConstants.ORG_ID     , dd.getOrganization().getId());
        m.put(UserConstants.ORG_NAME   , dd.getOrganization().getName());
        m.put(UserConstants.ID         , dd.getId());
        m.put(UserConstants.ACTIVE     , dd.getActive());
        m.put(UserConstants.DEVICE_TYPE, dd.getDeviceType());
        return m;
    }

	@Override
	public DeviceDetails findById(long id) {
		Optional<DeviceDetails> DeviceDetailsOtp = deviceRepo.findById(id);
		if(DeviceDetailsOtp.isPresent()) {
			DeviceDetailsOtp.get();
		}
		return null;
	}

	@Override
	public void save(DeviceDetails device) {
		deviceRepo.save(device);		
	}

	@Override
	public void update(DeviceDetails device) {
		deviceRepo.save(device);		
	}

	@Override
	public void delete(DeviceDetails device) {
		deviceRepo.delete(device);		
	}

	@Override
	public Long getAllActiveDeviceByOrg(Long orgId) {
		return deviceRepo.countActiveByOrg(orgId);
	}

	@Override
	public Long getAllActiveDevice() {
		return deviceRepo.countAllActive();
	}
}