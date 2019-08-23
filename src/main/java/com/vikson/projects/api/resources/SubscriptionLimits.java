package com.vikson.projects.api.resources;

public class SubscriptionLimits {

    private int projects;
    private int plans;

    public SubscriptionLimits(int projects, int plans) {
        this.projects = projects;
        this.plans = plans;
    }

    public int getProjects() {
        return projects;
    }

    public int getPlans() {
        return plans;
    }
}
