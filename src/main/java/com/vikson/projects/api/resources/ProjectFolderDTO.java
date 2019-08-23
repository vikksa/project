package com.vikson.projects.api.resources;

import com.vikson.projects.api.resources.values.ProjectFolderExclude;
import com.vikson.projects.api.resources.values.StateFilter;
import com.vikson.projects.model.Project;
import com.vikson.projects.model.ProjectFolder;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApiModel(value = "Project Folder Resource")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectFolderDTO extends AuditedDTO {

    @ApiModelProperty(value = "Id of the project folder")
    private UUID id;
    @ApiModelProperty(value = "Name of the project folder")
    private String name;
    @ApiModelProperty(value = "Id of the organisation")
    private UUID organisationId;
    @ApiModelProperty(value = "Id of the parent folder")
    private UUID parentId;
    @ApiModelProperty(value = "List of all the subfolders in this folder")
    private List<ProjectFolderDTO> subFolder;
    @ApiModelProperty(value = "List of all the projects in this folder")
    private List<ProjectDTO> children;
    @ApiModelProperty(value = "Whether this project folder is active or not")
    private Boolean active;


    public ProjectFolderDTO() {
    }

    public ProjectFolderDTO(ProjectFolder folder, Function<Project, ProjectDTO> projectTranslator, Boolean state) {
        this(folder, projectTranslator, state, null);
    }

    public ProjectFolderDTO(ProjectFolder folder, Function<Project, ProjectDTO> projectTranslator, Boolean state, ProjectFolderExclude[] excludes) {
        super(folder);
        this.id = folder.getId();
        this.name = folder.getName();
        this.organisationId = folder.getOrganisationId();
        if (folder.getParent() != null)
            this.parentId = folder.getParent().getId();
        this.active = folder.isActive();
        if (state != null) {
            this.active = state;
        }
        setSubFolderAndChildren(folder, projectTranslator, state, excludes);
    }

    private void setSubFolderAndChildren(ProjectFolder folder, Function<Project, ProjectDTO> projectTranslator, Boolean state, ProjectFolderExclude[] excludes){
        Set<ProjectFolderExclude> excludeSet = excludes != null ?
                Arrays.stream(excludes).collect(Collectors.toSet()) : Collections.emptyNavigableSet();
        if(!excludeSet.contains(ProjectFolderExclude.SubFolders)) {
            this.subFolder = folder.getSubFolders()
                    .stream()
                    .map(sf -> new ProjectFolderDTO(sf, projectTranslator, folder.isActive() ? null : false))
                    .collect(Collectors.toList());
        } else {
            this.subFolder = Collections.emptyList();
        }
        if(!excludeSet.contains(ProjectFolderExclude.Projects)) {
            this.children = folder.getChildren()
                    .stream()
                    .map(projectTranslator)
                    .peek(projectDTO -> {
                        if (state != null) {
                            projectDTO.setActive(state);
                        }
                    })
                    .collect(Collectors.toList());
        } else {
            this.children = Collections.emptyList();
        }
    }

    public ProjectFolderDTO(ProjectFolder folder, Function<Project, ProjectDTO> projectTranslator, StateFilter stateFilter, String search, Boolean state) {
        super(folder);
        this.id = folder.getId();
        this.name = folder.getName();
        this.organisationId = folder.getOrganisationId();
        if (folder.getParent() != null)
            this.parentId = folder.getParent().getId();
        this.subFolder = folder.getSubFolders()
            .stream()
            .filter(projectFolder -> projectFolder.getName().toLowerCase().contains(search))
            .peek(projectFolder -> {
                if (state != null) projectFolder.setActive(state);
            })
            .filter(projectFolder -> {
                if (stateFilter == StateFilter.Active) return projectFolder.isActive();
                if (stateFilter == StateFilter.Inactive) return !projectFolder.isActive();
                return true;
            })
            .map(sf -> new ProjectFolderDTO(sf, projectTranslator, stateFilter, search, folder.isActive() ? null : false))
            .collect(Collectors.toList());
        this.children = folder.getChildren()
            .stream()
            .filter(project -> project.getName().getName().toLowerCase().contains(search))
            .peek(project -> {
                if (state != null) project.setActive(state);
            })
            .filter(project -> {
                if (stateFilter == StateFilter.Active) return project.isActive();
                if (stateFilter == StateFilter.Inactive) return !project.isActive();
                return true;
            })
            .map(projectTranslator)
            .collect(Collectors.toList());
        this.active = folder.isActive();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(UUID organisationId) {
        this.organisationId = organisationId;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public List<ProjectFolderDTO> getSubFolder() {
        return subFolder;
    }

    public void setSubFolder(List<ProjectFolderDTO> subFolder) {
        this.subFolder = subFolder;
    }

    public List<ProjectDTO> getChildren() {
        return children;
    }

    public void setChildren(List<ProjectDTO> children) {
        this.children = children;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectFolderDTO that = (ProjectFolderDTO) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (active != null ? !active.equals(that.active) : that.active != null) return false;
        if (organisationId != null ? !organisationId.equals(that.organisationId) : that.organisationId != null)
            return false;
        return parentId != null ? parentId.equals(that.parentId) : that.parentId == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (organisationId != null ? organisationId.hashCode() : 0);
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        result = 31 * result + (active != null ? active.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProjectFolderDTO{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", organisationId=" + organisationId +
            ", parentId=" + parentId +
            ", subFolder=" + subFolder +
            ", children=" + children +
            '}';
    }
}
