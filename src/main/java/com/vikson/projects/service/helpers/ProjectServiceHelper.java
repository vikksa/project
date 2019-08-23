package com.vikson.projects.service.helpers;

import com.vikson.projects.api.resources.ProjectDTO;
import com.vikson.projects.model.Project;
import com.vikson.projects.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectServiceHelper {

    @Autowired
    private ProjectService projectService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Project createNew(ProjectDTO newProject) {
        return projectService.createProject(newProject);
    }

}
