package com.techvedika.harmonycvi.gateway.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techvedika.harmonycvi.gateway.repository.OrganizationRepository;
import com.techvedika.harmonycvi.gateway.repository.UserOrganizationRepository;
import com.techvedika.harmonycvi.gateway.repository.UserRepository;
import com.techvedika.harmonycvi.gateway.service.UserOrganizationService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserOrganizationServiceImpl implements UserOrganizationService {

	@Autowired
    private UserOrganizationRepository userOrgRepo;
	
	@Autowired
    private UserRepository userRepo;
	
	@Autowired
    private OrganizationRepository orgRepo;

    @Override
    public void addMapping(Long userId, Long orgId) {
//        UserOrganizationId id = new UserOrganizationId(userId, orgId);
//        if (userOrgRepo.existsById(id)) return;
//
//        User user = userRepo.findById(userId)
//                .orElseThrow(() -> new EntityNotFoundException("User not found"));
//        Organization org = orgRepo.findById(orgId)
//                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));
//
//        userOrgRepo.save(new UserOrganization(user, org));
    }

    @Override
    public void removeMapping(Long userId, Long orgId) {
//        userOrgRepo.deleteById(new UserOrganizationId(userId, orgId));
    }

    @Override
    public List<Long> getUserIdsForOrg(Long orgId) {
        return userOrgRepo.findUserIdsByOrgId(orgId);
    }

    @Override
    public List<Long> getOrgIdsForUser(Long userId) {
        return userOrgRepo.findOrgIdsByUserId(userId);
    }
}