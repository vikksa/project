package com.vikson.projects.api.resources;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.UUID;

@ApiModel(value = "Move Object Resource")
public class MoveDto {
    @ApiModelProperty(value = "List of the ids of the entities to move")
    private List<UUID> entities;
    @ApiModelProperty(value = "List of the ids of the folders to move")
    private List<UUID> folders;
    @ApiModelProperty(value = "Id of the project")
    private UUID projectId;

    public List<UUID> getEntities() {
        return entities;
    }

    public void setEntities(List<UUID> entities) {
        this.entities = entities;
    }

    public List<UUID> getFolders() {
        return folders;
    }

    public void setFolders(List<UUID> folders) {
        this.folders = folders;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }
}
