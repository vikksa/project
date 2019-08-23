package com.vikson.projects.model.values;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class BimplusProjectConfig {

    @Column(name = "bimplus_id")
    private UUID id;
    @Column(name = "bimplus_project_name")
    private String projectName;
    @Column(name = "bimplus_project_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private BimplusProjectState state = BimplusProjectState.NOT_LINKED;
    @Column(name = "team_slug")
    private String teamSlug;

    public BimplusProjectConfig() {}

    public BimplusProjectConfig(UUID id, String projectName, BimplusProjectState state, String teamSlug) {
        this.id = id;
        this.projectName = projectName;
        this.state = state;
        this.teamSlug = teamSlug;
    }

    public static BimplusProjectConfig newConfiguration(UUID id, String projectName, String teamSlug) {
        return new BimplusProjectConfig(id, projectName, BimplusProjectState.LINKED, teamSlug);
    }

    public String getTeamSlug() {
        return teamSlug;
    }

    public void setTeamSlug(String teamSlug) {
        this.teamSlug = teamSlug;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public BimplusProjectState getState() {
        return state;
    }

    public void setState(BimplusProjectState state) {
        this.state = state;
    }

    public BimplusProjectConfig unlink() {
        this.state = BimplusProjectState.UNLINKED;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BimplusProjectConfig that = (BimplusProjectConfig) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(projectName, that.projectName) &&
                state == that.state &&
                Objects.equals(teamSlug, that.teamSlug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, projectName, state, teamSlug);
    }

    @Override
    public String toString() {
        return "BimplusProjectConfig{" +
                "id=" + id +
                ", projectName='" + projectName + '\'' +
                ", state=" + state +
                ", teamSlug='" + teamSlug + '\'' +
                '}';
    }
}
