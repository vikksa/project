package com.vikson.projects.service;

import com.vikson.projects.repositories.PlanRepository;
import com.vikson.projects.repositories.ProjectRepository;
import com.vikson.services.users.PersonalApiClient;
import com.vikson.services.users.SubscriptionApiClient;
import com.vikson.services.users.resources.Subscription;
import com.vikson.services.users.resources.UserProfile;
import com.vikson.projects.api.resources.SubscriptionLimits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private Map<Subscription.SubscriptionType, SubscriptionLimits> limits;

    @Autowired
    private PersonalApiClient personalApiClient;
    @Autowired
    private SubscriptionApiClient apiClient;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private PlanRepository planRepository;

    public SubscriptionService() {
        limits = new EnumMap<>(Subscription.SubscriptionType.class);
        limits.put(Subscription.SubscriptionType.Test, new SubscriptionLimits(2, 100));
        limits.put(Subscription.SubscriptionType.Pocket, new SubscriptionLimits(2, 100));
        limits.put(Subscription.SubscriptionType.Combo, new SubscriptionLimits(10, 800));
    }

    public boolean canCreateProjects() {
        UserProfile me = personalApiClient.getMe();
        Subscription subscription = apiClient.getSubscription();
        SubscriptionLimits subscriptionLimits = this.limits.get(subscription.getType());
        if(subscriptionLimits != null) {
            log.debug("Project Count Limit: {}.", subscriptionLimits.getProjects());
            long activeProjects = projectRepository.countByOrganisationIdAndActiveIsTrue(me.getOrganisationId());
            log.debug("Currently Active Projects: {}.", activeProjects);
            if(activeProjects >= subscriptionLimits.getProjects()) {
                log.debug("Exceeded Projects Limit (limit: {}, active: {})!", subscriptionLimits.getProjects(), activeProjects);
                return false;
            }
        } else {
            log.debug("No Limits defined for Subscription Type {}.", subscription.getType());
        }
        return true;
    }

    public boolean canCreatePlans() {
        UserProfile me = personalApiClient.getMe();
        Subscription subscription = apiClient.getSubscription();
        SubscriptionLimits subscriptionLimits = this.limits.get(subscription.getType());
        if(subscriptionLimits != null) {
            log.debug("Plan Count Limit: {}.", subscriptionLimits.getPlans());
            long activePlans = planRepository.countRevisionsOfActivePlansInOrganisation(me.getOrganisationId());
            log.debug("Currently Active Plans (including Revisions): {}.", activePlans);
            if(activePlans >= subscriptionLimits.getPlans()) {
                log.debug("Exceeded Plans Limit (limit: {}, active: {})!", subscriptionLimits.getPlans(), activePlans);
                return false;
            }
        } else {
            log.debug("No Limits defined for Subscription type {}.", subscription.getType());
        }
        return true;
    }

}
