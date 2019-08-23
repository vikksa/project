package com.vikson.projects.model.values;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class BimplusPlanReference {

    @Column(name = "bimplus_attachment_id")
    private UUID id;
    @Column(name = "bimplus_attachment_name")
    private String name;
    @Column(name = "bimplus_revision")
    private int revision;
    @Enumerated(EnumType.STRING)
    @Column(name = "bimplus_plan_state", nullable = false)
    private BimplusPlanState state = BimplusPlanState.NOT_LINKED;
    @Column(name = "bimplus_error_message")
    private String errorMessage;

    public BimplusPlanReference() {
    }

    public BimplusPlanReference(UUID id, String name, int revision, BimplusPlanState state) {
        this.id = id;
        this.name = name;
        this.revision = revision;
        this.state = state;
    }

    public static BimplusPlanReference newConfiguration(UUID id, String name, int revision) {
        return new BimplusPlanReference(id, name, revision, BimplusPlanState.LINKED);
    }

    public static BimplusPlanReference errorOccurred(UUID id, String name, String errorMessage) {
        return new BimplusPlanReference().withState(BimplusPlanState.ERROR)
                .withId(id)
                .withName(name)
                .withErrorMessage(errorMessage);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BimplusPlanReference withId(UUID id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BimplusPlanReference withName(String name) {
        this.name = name;
        return this;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public BimplusPlanReference withRevision(int revision) {
        this.revision = revision;
        return this;
    }

    public BimplusPlanState getState() {
        return state;
    }

    public void setState(BimplusPlanState state) {
        this.state = state;
    }

    public BimplusPlanReference withState(BimplusPlanState state) {
        this.state = state;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public BimplusPlanReference withErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BimplusPlanReference that = (BimplusPlanReference) o;
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
