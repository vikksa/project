package com.vikson.projects.api.resources;

import com.vikson.projects.api.resources.values.*;
import com.vikson.projects.api.resources.BimplusProjectConfigDto;
import com.vikson.projects.api.resources.values.ProjectPermissions;
import com.vikson.projects.model.Project;
import com.vikson.projects.model.values.AutoDeskConfig;
import com.vikson.projects.model.ScaleDefinition;
import com.vikson.services.issues.resources.PinCategory;
import com.vikson.services.users.resources.Role;
import com.vikson.services.users.resources.TeamMember;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@ApiModel(value = "Project Resource")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectDTO extends AuditedDTO {

    @ApiModelProperty(value = "Id of the project")
    private UUID id;
    @ApiModelProperty(value = "Name of the project")
    private String name;
    @ApiModelProperty(value = "Initials of the project")
    private String initials;
    @ApiModelProperty(value = "Id of the organisation")
    private UUID organisationId;
    @ApiModelProperty(value = "Name of the organisation")
    private String organisationName;
    @ApiModelProperty(value = "Whether the project is active or not")
    private Boolean active;
    private ProjectDurationDTO duration;
    private ProjectAddressDTO address;
    private ScaleDefinitionDTO costDefinition;
    private ScaleDefinitionDTO priorityDefinition;
    @ApiModelProperty(value = "Id of the parent folder")
    private UUID parentId;
    @ApiModelProperty(value = "Id of the project owner")
    private UUID ownerId;
    @ApiModelProperty(value = "Description of the project")
    private String description;
    @ApiModelProperty(value = "The client")
    private String client;
    @ApiModelProperty(value = "The client assistant")
    private String clientAssistant;
    private ConstructionTypeDTO constructionType;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    @ApiModelProperty(value = "When this change occurred")
    private Instant occurred;
    private ProjectFolderDTO parent;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    @ApiModelProperty(value = "Timestamp of the last activity in this project")
    private Instant lastActivity;

    @ApiModelProperty(value = "The permissions the user has in this project")
    private List<ProjectPermissions> permissions;

    @ApiModelProperty(value = "The membership in this project")
    private TeamMember membership;
    @ApiModelProperty(value = "Users role in this project")
    private Role role;

    @ApiModelProperty(value = "List of all the pin categories")
    private List<PinCategory> categoryList;

    @ApiModelProperty(value = "Project notes")
    private String notes;
    @ApiModelProperty(value = "Whether this is an external project")
    private Boolean external = false;

    @ApiModelProperty(value = "Manager of the project")
    private String manager;
    @ApiModelProperty(value = "Phone of the manager of the project")
    private String managerPhone;
    @ApiModelProperty(value = "Number of plans in the project")
    private int numberOfPlans;
    private BimplusProjectConfigDto bimplusProjectConfigDto;
    private AutoDeskConfig autoDeskConfig;

    public ProjectDTO() {
    }

    public ProjectDTO(Project project) {
        super(project);
        this.id = project.getId();
        this.name = project.getName().getName();
        this.initials = project.getName().getInitials();
        this.organisationId = project.getOrganisationId();
        this.organisationName = project.getOrganisationName();
        this.active = project.isActive();
        this.duration = new ProjectDurationDTO(project.getDuration());
        this.address = new ProjectAddressDTO(project.getAddress());
        this.costDefinition = project.getScaleDefinitions()
                .stream()
                .filter(ScaleDefinition::isCosts)
                .findFirst()
                .map(ScaleDefinitionDTO::new)
                .orElse(null);
        this.priorityDefinition = project.getScaleDefinitions()
                .stream()
                .filter(ScaleDefinition::isPriority)
                .findFirst()
                .map(ScaleDefinitionDTO::new)
                .orElse(null);
        this.parentId = project.getParent().getId();
        this.ownerId = project.getCreatedBy();
        this.description = project.getDescription();
        this.client = project.getClient().getClientName();
        this.clientAssistant = project.getClient().getAssistantName();
        this.constructionType = new ConstructionTypeDTO(project.getConstructionType());
        this.occurred = project.getCreated().toInstant();
        this.notes= project.getNotes();
        this.manager = project.getManager();
        this.managerPhone = project.getManagerPhone();
        if(project.getLastActivity() != null) {
            this.lastActivity = project.getLastActivity().toInstant();
            super.setLastModified(this.lastActivity);
        }
        this.bimplusProjectConfigDto = project.getBimplusConfig() != null ? new BimplusProjectConfigDto(project.getBimplusConfig()) : new BimplusProjectConfigDto();
        this.autoDeskConfig = project.getAutoDeskConfig();
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

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(UUID organisationId) {
        this.organisationId = organisationId;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public ProjectDurationDTO getDuration() {
        return duration;
    }

    public void setDuration(ProjectDurationDTO duration) {
        this.duration = duration;
    }

    public ProjectAddressDTO getAddress() {
        return address;
    }

    public void setAddress(ProjectAddressDTO address) {
        this.address = address;
    }

    public ScaleDefinitionDTO getCostDefinition() {
        return costDefinition;
    }

    public void setCostDefinition(ScaleDefinitionDTO costDefinition) {
        this.costDefinition = costDefinition;
    }

    public ScaleDefinitionDTO getPriorityDefinition() {
        return priorityDefinition;
    }

    public void setPriorityDefinition(ScaleDefinitionDTO priorityDefinition) {
        this.priorityDefinition = priorityDefinition;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getClientAssistant() {
        return clientAssistant;
    }

    public void setClientAssistant(String clientAssistant) {
        this.clientAssistant = clientAssistant;
    }

    public ConstructionTypeDTO getConstructionType() {
        return constructionType;
    }

    public void setConstructionType(ConstructionTypeDTO constructionType) {
        this.constructionType = constructionType;
    }

    public Instant getOccurred() {
        return occurred;
    }

    public void setOccurred(Instant occurred) {
        this.occurred = occurred;
    }

    public ProjectFolderDTO getParent() {
        return parent;
    }

    public void setParent(ProjectFolderDTO parent) {
        this.parent = parent;
    }

    public TeamMember getMembership() {
        return membership;
    }

    public void setMembership(TeamMember membership) {
        this.membership = membership;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<ProjectPermissions> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<ProjectPermissions> permissions) {
        this.permissions = permissions;
    }

    public List<PinCategory> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<PinCategory> categoryList) {
        this.categoryList = categoryList;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getExternal() {
        return external;
    }

    public void setExternal(Boolean external) {
        this.external = external;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getManagerPhone() {
        return managerPhone;
    }

    public void setManagerPhone(String managerPhone) {
        this.managerPhone = managerPhone;
    }

    public Instant getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Instant lastActivity) {
        this.lastActivity = lastActivity;
    }

    public int getNumberOfPlans() {
        return numberOfPlans;
    }

    public void setNumberOfPlans(int numberOfPlans) {
        this.numberOfPlans = numberOfPlans;
    }

    public BimplusProjectConfigDto getBimplusProjectConfigDto() {
        return bimplusProjectConfigDto;
    }

    public void setBimplusProjectConfigDto(BimplusProjectConfigDto bimplusProjectConfigDto) {
        this.bimplusProjectConfigDto = bimplusProjectConfigDto;
    }

    public AutoDeskConfig getAutoDeskConfig() {
        return autoDeskConfig;
    }

    public void setAutoDeskConfig(AutoDeskConfig autoDeskConfig) {
        this.autoDeskConfig = autoDeskConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectDTO that = (ProjectDTO) o;
        return numberOfPlans == that.numberOfPlans &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(initials, that.initials) &&
                Objects.equals(organisationId, that.organisationId) &&
                Objects.equals(organisationName, that.organisationName) &&
                Objects.equals(active, that.active) &&
                Objects.equals(duration, that.duration) &&
                Objects.equals(address, that.address) &&
                Objects.equals(costDefinition, that.costDefinition) &&
                Objects.equals(priorityDefinition, that.priorityDefinition) &&
                Objects.equals(parentId, that.parentId) &&
                Objects.equals(ownerId, that.ownerId) &&
                Objects.equals(description, that.description) &&
                Objects.equals(client, that.client) &&
                Objects.equals(clientAssistant, that.clientAssistant) &&
                Objects.equals(constructionType, that.constructionType) &&
                Objects.equals(occurred, that.occurred) &&
                Objects.equals(parent, that.parent) &&
                Objects.equals(lastActivity, that.lastActivity) &&
                Objects.equals(permissions, that.permissions) &&
                Objects.equals(membership, that.membership) &&
                Objects.equals(role, that.role) &&
                Objects.equals(categoryList, that.categoryList) &&
                Objects.equals(notes, that.notes) &&
                Objects.equals(external, that.external) &&
                Objects.equals(manager, that.manager) &&
                Objects.equals(managerPhone, that.managerPhone) &&
                Objects.equals(bimplusProjectConfigDto, that.bimplusProjectConfigDto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, initials, organisationId, organisationName, active, duration, address, costDefinition, priorityDefinition, parentId, ownerId, description, client, clientAssistant, constructionType, occurred, parent, lastActivity, permissions, membership, role, categoryList, notes, external, manager, managerPhone, numberOfPlans, bimplusProjectConfigDto);
    }

}
