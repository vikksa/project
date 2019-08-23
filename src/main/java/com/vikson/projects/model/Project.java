package com.vikson.projects.model;

import com.vikson.projects.model.values.*;
import com.vikson.projects.model.values.AutoDeskConfig;
import com.vikson.projects.model.values.BimplusProjectConfig;
import com.vikson.projects.model.values.ProjectAddress;
import com.vikson.projects.model.values.ProjectClient;
import com.vikson.projects.model.values.ProjectDuration;
import com.vikson.projects.model.values.ProjectName;
import org.hibernate.annotations.Type;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class Project extends AuditedEntity {

    @Id
    @Type(type = "pg-uuid")
    private UUID id;
    @Column(nullable = false)
    private UUID organisationId;
    @Column
    private String organisationName;
    private boolean active;

    @Embedded
    private ProjectName name;
    @Column(nullable = false)
    private String description;
    @Embedded
    private ProjectDuration duration;
    @Embedded
    private ProjectAddress address;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_logo_id")
    private ProjectLogo logo;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_logo_id")
    private ProjectLogo reportLogo;
    @Column(nullable = false)
    private String constructionType;
    @Embedded
    private ProjectClient client;
    @Embedded
    private BimplusProjectConfig bimplusConfig;
    @Embedded
    private AutoDeskConfig autoDeskConfig = new AutoDeskConfig();

    @ManyToOne(optional = false)
    private ProjectFolder parent;
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<ScaleDefinition> scaleDefinitions;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "root_folder_id")
    private PlanFolder rootFolder;

    private String notes;

    private String manager;
    private String managerPhone;

    private ZonedDateTime lastActivity;

    public Project() {
    }

    public Project(UUID id, String name, UUID organisationId, ProjectFolder parent, ZonedDateTime created) {
        super(created);
        this.id = id != null? id : UUID.randomUUID();
        this.name = new ProjectName(name);
        Assert.notNull(organisationId, "Project.organisationId must not be NULL!");
        this.organisationId = organisationId;
        this.rootFolder = new PlanFolder(id, name, this, null);
        Assert.notNull(parent, "Project.parent must not be NULL!");
        this.parent = parent;

        this.active = true;
        this.description = "";
        this.constructionType = "";
        this.duration = new ProjectDuration();
        this.address = new ProjectAddress();
        this.client = new ProjectClient();
        this.bimplusConfig = new BimplusProjectConfig();
        this.scaleDefinitions = new ArrayList<>();
        this.notes = "";
        this.manager = "";
        this.managerPhone = "";
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(UUID organisationId) {
        this.organisationId = organisationId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ProjectName getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if(description == null)
            description = "";
        this.description = description;
    }

    public ProjectDuration getDuration() {
        if(duration == null)
            duration = new ProjectDuration();
        return duration;
    }

    public ProjectAddress getAddress() {
        if(address == null)
            address = new ProjectAddress();
        return address;
    }

    public ProjectLogo getProjectLogo() {
        return logo;
    }

    public void setProjectLogo(ProjectLogo logo) {
        this.logo = logo;
    }

    public ProjectLogo getReportLogo() {
        return reportLogo;
    }

    public void setReportLogo(ProjectLogo reportLogo) {
        this.reportLogo = reportLogo;
    }

    public String getConstructionType() {
        return constructionType;
    }

    public void setConstructionType(String constructionType) {
        if(StringUtils.isEmpty(constructionType))
            constructionType = "";
        this.constructionType = constructionType;
    }

    public ProjectClient getClient() {
        if(client == null)
            client = new ProjectClient();
        return client;
    }

    public BimplusProjectConfig getBimplusConfig() {
        if(bimplusConfig == null) {
            bimplusConfig = new BimplusProjectConfig();
        }
        return bimplusConfig;
    }

    public Project withBimplusConfig(BimplusProjectConfig bimplusConfig) {
        this.bimplusConfig = bimplusConfig;
        return this;
    }

    public ProjectFolder getParent() {
        return parent;
    }

    public void setParent(ProjectFolder parent) {
        this.parent = parent;
    }

    public List<ScaleDefinition> getScaleDefinitions() {
        return scaleDefinitions;
    }

    public PlanFolder getRootFolder() {
        return rootFolder;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        if (StringUtils.isEmpty(notes)) notes = "";
        this.notes = notes;
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

    /**
     * Timestamp of the last change in this project (including plans, pins, tasks and media).
     *
     * @return timestamp, maybe {@code NULL}
     */
    public ZonedDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(ZonedDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    /**
     * Sets the given {@link ZonedDateTime} to {@link this#lastActivity} if the current value is {@code null} or
     * the given value is after the current value.
     *
     * @param newLastActivity {@link ZonedDateTime}
     * @return {@code true} when actually changed.
     */
    public boolean swapLastActivityIfNewer(ZonedDateTime newLastActivity) {
        if(this.lastActivity == null || newLastActivity.isAfter(this.lastActivity)) {
            this.lastActivity = newLastActivity;
            return true;
        }
        return false;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
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
        Project project = (Project) o;
        return active == project.active &&
                Objects.equals(id, project.id) &&
                Objects.equals(organisationId, project.organisationId) &&
                Objects.equals(organisationName, project.organisationName) &&
                Objects.equals(name, project.name) &&
                Objects.equals(description, project.description) &&
                Objects.equals(duration, project.duration) &&
                Objects.equals(address, project.address) &&
                Objects.equals(logo, project.logo) &&
                Objects.equals(reportLogo, project.reportLogo) &&
                Objects.equals(constructionType, project.constructionType) &&
                Objects.equals(client, project.client) &&
                Objects.equals(bimplusConfig, project.bimplusConfig) &&
                Objects.equals(parent, project.parent) &&
                Objects.equals(scaleDefinitions, project.scaleDefinitions) &&
                Objects.equals(rootFolder, project.rootFolder) &&
                Objects.equals(notes, project.notes) &&
                Objects.equals(manager, project.manager) &&
                Objects.equals(managerPhone, project.managerPhone) &&
                Objects.equals(lastActivity, project.lastActivity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, organisationId, organisationName, active, name, description, duration, address, logo, reportLogo, constructionType, client, bimplusConfig, parent, scaleDefinitions, rootFolder, notes, manager, managerPhone, lastActivity);
    }

    @Override
    public String toString() {
        return String.format("Project (id: <%s>, name: %s, organisation: <%s>, active: <%b>)",
                id, StringUtils.quote(name.toString()), organisationId, active);
    }
}
