package com.vikson.projects.api.resources;

import com.vikson.projects.model.PlanFolder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(value = "Plan Tree Resource")
public class PlanTreeDTO extends PlanFolderDTO {

    @ApiModelProperty(value = "Name of the project")
    private String projectName;

    public PlanTreeDTO() {
    }

    public PlanTreeDTO(String projectName, PlanFolder rootFolder, List<PlanFolderDTO> subFolder, List<PlanDTO> children) {
        super(rootFolder, subFolder, children);
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }
}
