package com.vikson.projects.api.resources;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "Project Stats Resource")
@AllArgsConstructor
public class ProjectStatsDTO {
    @ApiModelProperty(value = "Amount of active projects")
    int noTotalProjects;
    @ApiModelProperty(value = "Total amount of projects")
    int noActiveProjects;
    @ApiModelProperty(value = "Amount of active plans")
    int noTotalPlans;
    @ApiModelProperty(value = "Total amount of plans")
    int noActivePlans;
}
