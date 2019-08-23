package com.vikson.projects.api.resources.values;

public enum PlanTreeSortAttributes {
    Title("name"),LastActivity("actual_last_activity");

    private final String column;

    PlanTreeSortAttributes(String column) {
        this.column = column;
    }

    public String colum() {
        return column;
    }
}
