package com.vikson.projects.api.resources.values;

import com.vikson.projects.api.resources.ProjectTreeChildItem;

public enum ProjectTreeTypeFilter {
    All,
    Folders;


    public ProjectTreeChildItem.Type[] allowedTypes() {
        if (this == ProjectTreeTypeFilter.Folders) {
            return new ProjectTreeChildItem.Type[]{ProjectTreeChildItem.Type.Folder};
        }
        return ProjectTreeChildItem.Type.values();
    }
}
