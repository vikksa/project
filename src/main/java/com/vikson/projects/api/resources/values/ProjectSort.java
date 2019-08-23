package com.vikson.projects.api.resources.values;

import com.vikson.projects.api.resources.AuditedDTO;
import com.vikson.projects.api.resources.ProjectDTO;

import java.util.Comparator;

public enum ProjectSort {
    Name((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName())),
    Created(Comparator.comparing(AuditedDTO::getCreated)),
    LastModified((p1,p2) -> {
        if(p2.getLastModified() == null)
            return 1;
        if(p1.getLastModified() == null)
            return -1;
        return p1.getLastModified().compareTo(p2.getLastModified());
    });

    private Comparator<ProjectDTO> comparator;

    ProjectSort(Comparator<ProjectDTO> comparator) {
        this.comparator = comparator;
    }

    public Comparator<ProjectDTO> getComparator() {
        return comparator;
    }
}
