package com.techvedika.harmonycvi.gateway.service;

import java.util.List;

public interface UserOrganizationService {
    void addMapping(Long userId, Long orgId);
    void removeMapping(Long userId, Long orgId);

    List<Long> getUserIdsForOrg(Long orgId);
    List<Long> getOrgIdsForUser(Long userId);
}