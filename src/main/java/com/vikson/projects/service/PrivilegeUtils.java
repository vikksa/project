package com.vikson.projects.service;

import com.vikson.services.users.UserPrivileges;
import com.vikson.services.users.resources.Privilege;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PrivilegeUtils {

    @Autowired
    private UserPrivileges userPrivileges;

    public boolean canViewAllPlans(UUID projectId) {
        return userPrivileges.hasAnyPrivileges(projectId, Privilege.ManagePins, Privilege.CreatePins, Privilege.ViewPins,
            Privilege.ManagePlans, Privilege.CreatePlans);
    }

    public boolean canManagePlans(UUID projectId) {
        return userPrivileges.hasAnyPrivileges(projectId, Privilege.ManagePlans);
    }
}
