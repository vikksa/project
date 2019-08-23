package com.vikson.projects.api.resources;

import com.vikson.projects.model.PlanFolder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.UUID;

@ApiModel(value = "Plan Folder Resource")
public class PlanFolderDTO extends AuditedDTO{

    @ApiModelProperty(value = "Id of the plan folder")
    private UUID id;
    @ApiModelProperty(value = "Name of the folder")
    private String name;
    @ApiModelProperty(value = "Id of the parent folder")
    private UUID parentId;
    @ApiModelProperty(value = "List of sub folders")
    private List<PlanFolderDTO> subFolder;
    @ApiModelProperty(value = "List of plans in this folder")
    private List<PlanDTO> children;
    @ApiModelProperty(value = "Id of the project")
    private UUID projectId;
    @ApiModelProperty(value = "Whether this folder is active or not")
    private Boolean active;


    public PlanFolderDTO() {
    }

    public PlanFolderDTO(PlanFolder folder, List<PlanFolderDTO> subFolder, List<PlanDTO> children) {
        super(folder);
        this.id = folder.getId();
        this.name = folder.getName();
        if(folder.getParent() != null)
            this.parentId = folder.getParent().getId();
        this.subFolder = subFolder;
        this.children = children;
        this.projectId = folder.getProject().getId();
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

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public List<PlanFolderDTO> getSubFolder() {
        return subFolder;
    }

    public void setSubFolder(List<PlanFolderDTO> subFolder) {
        this.subFolder = subFolder;
    }

    public List<PlanDTO> getChildren() {
        return children;
    }

    public void setChildren(List<PlanDTO> children) {
        this.children = children;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
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

        PlanFolderDTO that = (PlanFolderDTO) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (active != null ? !active.equals(that.active) : that.active != null) return false;
        return parentId != null ? parentId.equals(that.parentId) : that.parentId == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        result = 31 * result + (active != null ? active.hashCode() : 0);

        return result;
    }
}
