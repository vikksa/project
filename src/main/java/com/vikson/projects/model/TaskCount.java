package com.vikson.projects.model;

import com.vikson.projects.model.values.TaskCountPk;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "task_count")
public class TaskCount {

    @EmbeddedId
    private TaskCountPk id;
    @Column(name = "open_count")
    private int openCount;
    @Column(name = "done_count")
    private int doneCount;
    @LastModifiedDate
    private ZonedDateTime lastModified;

    protected TaskCount() {
    }

    public TaskCount(String username, UUID planId, int openCount, int doneCount) {
        this.id = new TaskCountPk(username, planId);
        this.openCount = openCount;
    }

    public UUID getPlanId() {
        return id.getPlanId();
    }

    public int getOpenCount() {
        return openCount;
    }

    public void setOpenCount(int openCount) {
        this.openCount = openCount;
    }

    public int getDoneCount() {
        return doneCount;
    }

    public void setDoneCount(int doneCount) {
        this.doneCount = doneCount;
    }

    public int getTotal() {
        return openCount + doneCount;
    }

    public ZonedDateTime getLastModified() {
        return lastModified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskCount taskCount = (TaskCount) o;
        return openCount == taskCount.openCount &&
                Objects.equals(id, taskCount.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, openCount);
    }

    @Override
    public String toString() {
        return "TaskCount{" +
                "id=" + id +
                ", openCount=" + openCount +
                '}';
    }
}
