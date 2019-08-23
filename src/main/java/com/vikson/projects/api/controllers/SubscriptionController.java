package com.vikson.projects.api.controllers;

import com.vikson.projects.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(path = "/api/v2")
@RestController
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping(path = "/plans/limit")
    public boolean canCreatePlans() {
        return subscriptionService.canCreatePlans();
    }

    @GetMapping(path = "/projects/limit")
    public boolean canCreateProjects() {
        return subscriptionService.canCreateProjects();
    }

}
