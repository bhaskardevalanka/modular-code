package com.techvedika.harmonycvi.gateway.projection;

import java.util.Collection;

import com.techvedika.harmonycvi.gateway.entity.Privileges;

public interface LoginProjection {
    Long getId();
    String getEmail();
    String getFirstName();
    String getLastName();
    String getJwtToken();
    Boolean getActive();
    RoleInfo getRole();

    interface RoleInfo {
        Long getId();
        String getName();
        Collection<Privileges> getPrivileges();
    }
    
    Boolean getMfaEnabled();
    Integer getLoginCount();
    String getDeviceInfo();
}
