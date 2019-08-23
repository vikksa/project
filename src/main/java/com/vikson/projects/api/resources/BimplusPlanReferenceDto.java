package com.vikson.projects.api.resources;

import com.vikson.projects.model.values.BimplusPlanReference;
import com.vikson.projects.model.values.BimplusPlanState;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import java.util.UUID;

/**
 * Contains information regarding Bimplus attachment integration with vikson plan
 * {@link BimplusPlanState} is set as NOT_LINKED by default
 * Name and Revision can be changed during a revision update.
 * State can be only changed to UNLINKED if first LINKED.
 */
@ApiModel(value = "Bimplus plan resource")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BimplusPlanReferenceDto {

    @ApiModelProperty(value = "Id of the bimplus attachment")
    private UUID id;
    @ApiModelProperty(value = "Bimplus attachment name")
    private String name;
    @ApiModelProperty(value = "Bimplus revision name")
    private int revision;
    @ApiModelProperty(value = "Bimplus plan state")
    private BimplusPlanState state;
    @ApiModelProperty(value = "Bimplus error message")
    private String errorMessage;

    public BimplusPlanReferenceDto() {
    }

    @JsonCreator
    public BimplusPlanReferenceDto(@JsonProperty(value = "id") UUID id,
                                   @JsonProperty(value = "name") String name,
                                   @JsonProperty(value = "revision") int revision,
                                   @JsonProperty(value = "state") BimplusPlanState state) {
        this.id = id;
        this.name = name;
        this.revision = revision;
        this.state = state;
    }

    public BimplusPlanReferenceDto(BimplusPlanReference bimplusPlanReference) {
        this.id = bimplusPlanReference.getId();
        this.name = bimplusPlanReference.getName();
        this.revision = bimplusPlanReference.getRevision();
        this.state = bimplusPlanReference.getState();
        this.errorMessage = bimplusPlanReference.getErrorMessage();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BimplusPlanReferenceDto withId(UUID id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BimplusPlanReferenceDto withName(String name) {
        this.name = name;
        return this;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public BimplusPlanReferenceDto withRevision(int revision) {
        this.revision = revision;
        return this;
    }

    public BimplusPlanState getState() {
        return state;
    }

    public void setState(BimplusPlanState state) {
        this.state = state;
    }

    public BimplusPlanReferenceDto withState(BimplusPlanState state) {
        this.state = state;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public BimplusPlanReferenceDto withErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BimplusPlanReferenceDto that = (BimplusPlanReferenceDto) o;
        return revision == that.revision &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                state == that.state &&
                Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, revision, state, errorMessage);
    }

    @Override
    public String toString() {
        return "BimplusPlanReferenceDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", revision=" + revision +
                ", state=" + state +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
