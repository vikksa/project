package com.vikson.projects.api.resources;

import com.vikson.projects.model.values.BimplusProjectConfig;
import com.vikson.projects.model.values.BimplusProjectState;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Contains information regarding Bimplus project integration with vikson project
 * {@link BimplusProjectState} is set as NOT_LINKED by default
 * State can be only changed to UNLINKED if first LINKED.
 */
@ApiModel(value = "Bimplus project resource")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BimplusProjectConfigDto implements Serializable {

    @ApiModelProperty(value = "Id of the bimplus project")
    private UUID id;
    @ApiModelProperty(value = "Bimplus project name")
    private String projectName;
    @ApiModelProperty(value = "Bimplus project state")
    private BimplusProjectState state;
    @ApiModelProperty(value = "Team Slug")
    private String teamSlug;

    public BimplusProjectConfigDto() {
    }

    @JsonCreator
    public BimplusProjectConfigDto(@JsonProperty(value = "id") UUID id,
                                   @JsonProperty(value = "projectName") String projectName,
                                   @JsonProperty(value = "state") BimplusProjectState state,
                                   @JsonProperty(value = "teamSlug") String teamSlug) {
        this.id = id;
        this.projectName = projectName;
        this.state = state;
        this.teamSlug = teamSlug;
    }

    public BimplusProjectConfigDto(BimplusProjectConfig bimplusProjectConfig) {
        this.id = bimplusProjectConfig.getId();
        this.projectName = bimplusProjectConfig.getProjectName();
        this.state = bimplusProjectConfig.getState();
        this.teamSlug = bimplusProjectConfig.getTeamSlug();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BimplusProjectConfigDto withId(UUID id) {
        this.id = id;
        return this;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public BimplusProjectConfigDto withName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public BimplusProjectState getState() {
        return state;
    }

    public void setState(BimplusProjectState state) {
        this.state = state;
    }

    public String getTeamSlug() {
        return teamSlug;
    }

    public void setTeamSlug(String teamSlug) {
        this.teamSlug = teamSlug;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BimplusProjectConfigDto that = (BimplusProjectConfigDto) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(projectName, that.projectName) &&
                state == that.state &&
                Objects.equals(teamSlug, that.teamSlug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, projectName, state, teamSlug);
    }
}
