package com.vikson.projects.api.resources;

import com.vikson.projects.api.resources.values.StateFilter;
import com.vikson.projects.model.Project;
import com.vikson.projects.model.ProjectFolder;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProjectTreeDTO extends ProjectFolderDTO {

    private List<ProjectDTO> externalProjects;

    public ProjectTreeDTO() {
    }

    public ProjectTreeDTO(ProjectFolder rootFolder, List<ProjectDTO> externalProjects, Function<Project, ProjectDTO> projectTranslator, StateFilter stateFilter, String search) {
        super(rootFolder, projectTranslator, stateFilter, search, null);
        if (externalProjects != null) {
            externalProjects = externalProjects.stream()
                .filter(projectDTO -> projectDTO.getName().toLowerCase().contains(search))
                .filter(projectDTO -> {
                    if (stateFilter == StateFilter.Active) return projectDTO.getActive();
                    if (stateFilter == StateFilter.Inactive) return !projectDTO.getActive();
                    return true;
                }).collect(Collectors.toList());

            this.getChildren().addAll(externalProjects);
        }
        this.externalProjects = externalProjects;
    }

    public List<ProjectDTO> getExternalProjects() {
        return externalProjects;
    }

    public void setExternalProjects(List<ProjectDTO> externalProjects) {
        this.externalProjects = externalProjects;
    }
}
