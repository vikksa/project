package com.vikson.projects.api.resources;

import com.vikson.projects.model.Plan;
import com.vikson.projects.model.PlanFolder;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Minimum required info of a plan for sync apis
 */
public class PlanQuickInfo implements Serializable {
    private UUID planId;
    private UUID projectId;
    private List<UUID> parentFolders;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant lastActivity;
    private long planSize = 0;

    public PlanQuickInfo() {
    }

    public PlanQuickInfo(Plan plan, ZonedDateTime since) {
        this.planId = plan.getId();
        this.projectId = plan.getProject().getId();
        this.parentFolders = getParentFolderIds(plan);
        this.lastActivity = plan.getLastActivity().toInstant();
        if (since == null || !plan.getCurrentRevision().getCreated().isBefore(since)) {
            this.planSize = plan.getCurrentRevision().getMetadata().getArchiveSize();
        }
    }

    //Parent folderIds ordered by proximity
    private List<UUID> getParentFolderIds(Plan plan) {
        return getParentFolderIds(plan.getParent(), new ArrayList<>());
    }

    private List<UUID> getParentFolderIds(PlanFolder folder, List<UUID> ids) {
        if (folder != null) {
            ids.add(folder.getId());
            return getParentFolderIds(folder.getParent(), ids);
        }
        return ids;
    }

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public List<UUID> getParentFolders() {
        return parentFolders;
    }

    public void setParentFolders(List<UUID> parentFolders) {
        this.parentFolders = parentFolders;
    }

    public Instant getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Instant lastActivity) {
        this.lastActivity = lastActivity;
    }

    public Long getPlanSize() {
        return planSize;
    }

    public void setPlanSize(Long planSize) {
        this.planSize = planSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanQuickInfo)) return false;
        PlanQuickInfo that = (PlanQuickInfo) o;
        return Objects.equals(planId, that.planId) &&
            Objects.equals(projectId, that.projectId) &&
            Objects.equals(parentFolders, that.parentFolders) &&
            Objects.equals(lastActivity, that.lastActivity) &&
            Objects.equals(planSize, that.planSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId, projectId, parentFolders, lastActivity, planSize);
    }

    @Override
    public String toString() {
        return "PlanQuickInfo{" +
            "planId=" + planId +
            ", projectId=" + projectId +
            ", parentFolders=" + parentFolders +
            ", lastActivity=" + lastActivity +
            ", planSize=" + planSize +
            '}';
    }
}
