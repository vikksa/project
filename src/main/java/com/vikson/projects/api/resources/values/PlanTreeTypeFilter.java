package com.vikson.projects.api.resources.values;

import com.vikson.projects.api.resources.PlanTreeChildItem;

public enum PlanTreeTypeFilter {
    All, Folders, Plans;

    public PlanTreeChildItem.Type[] allowedTypes() {
        switch (this) {
            case Folders:
                return new PlanTreeChildItem.Type[]{PlanTreeChildItem.Type.Folder};
            case Plans:
                return new PlanTreeChildItem.Type[]{PlanTreeChildItem.Type.Plan};
            default:
                return PlanTreeChildItem.Type.values();
        }
    }

}
