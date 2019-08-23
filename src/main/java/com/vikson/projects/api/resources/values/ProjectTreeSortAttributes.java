package com.vikson.projects.api.resources.values;

public enum ProjectTreeSortAttributes {
    Title, LastActivity;

    public String column() {
        switch (this) {
            case Title:
                return "name";
            case LastActivity:
                return "actual_last_activity";
            default:
                    return "name";
        }
    }

}
