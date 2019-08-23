package com.vikson.projects.model.values;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class TaskCountPk implements Serializable {

    @Column(name = "username", nullable = false)
    private String username;
    @Type(type = "pg-uuid")
    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    protected TaskCountPk() {
    }

    public TaskCountPk(String username, UUID planId) {
        this.username = username;
        this.planId = planId;
    }

    public String getUsername() {
        return username;
    }

    public UUID getPlanId() {
        return planId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskCountPk that = (TaskCountPk) o;
        return Objects.equals(username, that.username) &&
                Objects.equals(planId, that.planId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(username, planId);
    }

    @Override
    public String toString() {
        return "TaskCountPk{" +
                "username=" + username +
                ", planId=" + planId +
                '}';
    }
}
