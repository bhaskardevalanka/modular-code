package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.util.List;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.entity.User;
import com.techvedika.harmonycvi.gateway.repository.CenterRepository;
import com.techvedika.harmonycvi.gateway.repository.DeviceDetailsRepository;
import com.techvedika.harmonycvi.gateway.repository.OrgApiConfigRepository;
import com.techvedika.harmonycvi.gateway.repository.OrganizationRepository;
import com.techvedika.harmonycvi.gateway.repository.PrivilegesRepository;
import com.techvedika.harmonycvi.gateway.repository.RoleRepository;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsyncDeleteOrgServiceImpl {

	private static final Logger LOG = LoggerFactory.getLogger(AsyncDeleteOrgServiceImpl.class);

    private final UserRepository userRepo;
    private final UserServiceImpl userServiceImpl;
    private final CenterRepository centerRepo;
    private final DeviceDetailsRepository deviceRepo;
    private final OrganizationServiceImpl orgServiceImpl;
    private final OrganizationRepository orgRepo;
    private final PrivilegesRepository privilegesRepo;
    private final OrgApiConfigRepository orgApiConfigRepo;
    private final RoleRepository roleRepo;

    public AsyncDeleteOrgServiceImpl(
            UserRepository userRepo,
            UserServiceImpl userServiceImpl,
            CenterRepository centerRepo,
            DeviceDetailsRepository deviceRepo,
            OrganizationServiceImpl orgServiceImpl,
            OrganizationRepository orgRepo,
            PrivilegesRepository privilegesRepo,
            OrgApiConfigRepository orgApiConfigRepo,
            RoleRepository roleRepo) {

        this.userRepo = userRepo;
        this.userServiceImpl = userServiceImpl;
        this.centerRepo = centerRepo;
        this.deviceRepo = deviceRepo;
        this.orgServiceImpl = orgServiceImpl;
        this.orgRepo = orgRepo;
        this.privilegesRepo = privilegesRepo;
        this.orgApiConfigRepo = orgApiConfigRepo;
        this.roleRepo = roleRepo;
    }

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void performAsyncDeleteOrg(JSONObject json, String userEmailId) {


		Long orgId = Long.valueOf(json.get("orgId").toString());

		Optional<User> orgUserOtp = userRepo.findByEmail(userEmailId);
		if(orgUserOtp.isEmpty())
			return;
		User user = orgUserOtp.get();

		if (!json.get("userId").equals("")) {
			Long userId = Long.valueOf(json.get("userId").toString());
			Optional<User> orgUserOpt = userRepo.findById(userId);
			if(orgUserOpt.isEmpty())
				return;
			User orgUser = orgUserOpt.get();
			orgServiceImpl.deleteOrUpdateUser(orgUser, orgId);
		} else {
			List<User> users = userServiceImpl.getOrgUser(orgId, user);
			LOG.info("users size : {}", users.size());
			for (User usr : users) {
				orgServiceImpl.deleteOrUpdateUser(usr, orgId);
			}
			Optional<Long> deviceId = deviceRepo.findIdByOrgId(orgId);
			if (deviceId.isPresent()) {
				deviceRepo.deleteUserDevicesByOrgId(orgId);
				deviceRepo.flush();
			}

			Optional<Long> centerId = centerRepo.findIdByOrgId(orgId);
			if (centerId.isPresent()) {
				centerRepo.deleteUserCentersByOrgId(centerId.get());
				deviceRepo.deleteByCenterId(centerId.get());
				centerRepo.deleteByOrgId(orgId);
				centerRepo.flush();
			}
			privilegesRepo.deleteByOrgId(orgId);
			orgApiConfigRepo.deleteByOrgId(orgId);
			roleRepo.deleteByOrgId(orgId);
			orgRepo.deleteByOrgId(orgId);
			LOG.info("Org deleted Successfully");
			orgRepo.flush();

		}
	}

}
