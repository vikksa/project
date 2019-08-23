package com.vikson.projects.service.helpers;

import com.vikson.projects.repositories.PlanRepository;
import com.vikson.services.issues.IssueRequestApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class PlanServiceHelper {

    @Autowired
    private IssueRequestApiClient issueRequestApiClient;
    @Autowired
    private PlanRepository planRepository;

    @Async
    @Transactional
    public void doFetchLastActivity(UUID planId, Instant lastModified, SecurityContext securityContext) {
        SecurityContextHolder.setContext(securityContext);
        ZonedDateTime lastActivity = issueRequestApiClient.getLastActivityForPlan(planId);
        if (lastActivity != null && lastActivity.toInstant().isAfter(lastModified)) {
            planRepository.setLastModified(planId, lastActivity);
        }
    }
}

