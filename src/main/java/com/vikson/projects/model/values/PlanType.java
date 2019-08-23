package com.vikson.projects.model.values;

import org.springframework.util.StringUtils;

public enum PlanType {

    File,
    Map;

    public static PlanType valueOfNullable(String name) {
        if(StringUtils.isEmpty(name))
            return null;
        return PlanType.valueOf(name);
    }

}
