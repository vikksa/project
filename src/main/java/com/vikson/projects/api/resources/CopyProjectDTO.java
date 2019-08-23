package com.vikson.projects.api.resources;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.UUID;

@ApiModel(value = "Copy Project Resource")
public class CopyProjectDTO extends ProjectDTO {

    @ApiModelProperty(value = "Whether categories should be copied")
    private boolean categories;
    @ApiModelProperty(value = "Whether scale definitions should be copied")
    private boolean scaleDefinitions;
    @ApiModelProperty(value = "Whether teams should be copied")
    private boolean team;
    @ApiModelProperty(value = "List of team members to be copied or to exclude based on the team flag")
    private List<UUID> teamMembers;
    @ApiModelProperty(value = "Whether contacts should be copied")
    private boolean contacts;
    @ApiModelProperty(value = "List of contacts to be copied or to exclude based on the contacts flag")
    private List<UUID> contactsList;
    @ApiModelProperty(value = "Whether datasets should be copied")
    private boolean datasets;
    @ApiModelProperty(value = "Whether templates should be copied")
    private boolean templates;

    public boolean isCategories() {
        return categories;
    }

    public void setCategories(boolean categories) {
        this.categories = categories;
    }

    public boolean isScaleDefinitions() {
        return scaleDefinitions;
    }

    public void setScaleDefinitions(boolean scaleDefinitions) {
        this.scaleDefinitions = scaleDefinitions;
    }

    public boolean isTeam() {
        return team;
    }

    public void setTeam(boolean team) {
        this.team = team;
    }

    public boolean isContacts() {
        return contacts;
    }

    public void setContacts(boolean contacts) {
        this.contacts = contacts;
    }

    public boolean isDatasets() {
        return datasets;
    }

    public void setDatasets(boolean datasets) {
        this.datasets = datasets;
    }

    public List<UUID> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(List<UUID> teamMembers) {
        this.teamMembers = teamMembers;
    }

    public List<UUID> getContactsList() {
        return contactsList;
    }

    public void setContactsList(List<UUID> contactsList) {
        this.contactsList = contactsList;
    }

    public boolean isTemplates() {
        return templates;
    }

    public void setTemplates(boolean templates) {
        this.templates = templates;
    }
}
