package com.vikson.projects.api.resources;

import com.vikson.projects.model.ProjectFolderRestriction;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.UUID;

@ApiModel(value = "Project Folder Restriction Resource")
public class ProjectFolderRestrictionDTO {

    @ApiModelProperty(value = "Id of the user to restrict")
    private UUID userId;
    @ApiModelProperty(value = "Id of the folder to restrict to")
    private UUID folderId;
    @ApiModelProperty(value = "Name of the restricted folder")
    private String folderName;

    @JsonCreator
    public ProjectFolderRestrictionDTO(@JsonProperty(value = "userId", required = true) UUID userId,
                                       @JsonProperty("folderId") UUID folderId) {
        this.userId = userId;
        this.folderId = folderId;
    }

    public ProjectFolderRestrictionDTO(ProjectFolderRestriction restriction) {
        this.userId = restriction.getUserId();
        this.folderId = restriction.getFolder().getId();
        this.folderName = restriction.getFolder().getName();
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getFolderId() {
        return folderId;
    }

    public String getFolderName() {
        return folderName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectFolderRestrictionDTO that = (ProjectFolderRestrictionDTO) o;

        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (folderId != null ? !folderId.equals(that.folderId) : that.folderId != null) return false;
        return folderName != null ? folderName.equals(that.folderName) : that.folderName == null;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (folderId != null ? folderId.hashCode() : 0);
        result = 31 * result + (folderName != null ? folderName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProjectFolderRestrictionDTO{" +
                "userId=" + userId +
                ", folderId=" + folderId +
                ", folderName='" + folderName + '\'' +
                '}';
    }
}
