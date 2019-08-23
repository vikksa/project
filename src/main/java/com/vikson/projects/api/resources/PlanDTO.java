package com.vikson.projects.api.resources;

import com.vikson.projects.api.resources.values.ResourcePermission;
import com.vikson.projects.model.Plan;
import com.vikson.projects.model.values.PlanType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@ApiModel(value = "Plan Resource")
public class PlanDTO extends AuditedDTO {

    @ApiModelProperty(value = "Id of the plan")
    private UUID id;
    @ApiModelProperty(value = "Id of the project")
    private UUID projectId;
    @ApiModelProperty(value = "Name of the plan")
    private String name;
    private PlanRevisionDTO currentRevision;
    @ApiModelProperty(value = "List of all the revisions of this plan")
    private List<PlanRevisionDTO> revisions;
    private PlanRevisionDTO revision;
    @ApiModelProperty(value = "Whether the plan is active or not")
    private Boolean active;
    @ApiModelProperty(value = "The s3 folder id of the plan")
    private String s3FolderId;
    private RevisionMetadataDTO metadata;
    @ApiModelProperty(value = "The type of the plan")
    private PlanType planType;
    @ApiModelProperty(value = "Id of the parent folder of the plan")
    private UUID parentId;
    @ApiModelProperty(value = "Name of the parent folder of the plan")
    private String parentName;
    @ApiModelProperty(value = "List of the permission the user has on this plan")
    private List<ResourcePermission> permissions = new ArrayList<>();

    @ApiModelProperty(value = "Id of the current revision id")
    private UUID currentRevisionId;
    @ApiModelProperty(value = "Number of the open tasks in this plan")
    private Integer openTaskCount;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    @ApiModelProperty(value = "Timestamp of the last activity in this plan")
    private Instant lastActivity;
    @ApiModelProperty(value = "Indicates if a revision is in process")
    private Boolean lock;
    private BimplusPlanReferenceDto bimplusPlanReferenceDto;

    public PlanDTO() {
    }

    public PlanDTO(Plan plan, List<PlanRevisionDTO> revisions, PlanRevisionDTO currentRevision) {
        super(plan);
        this.id = plan.getId();
        this.projectId = plan.getProject().getId();
        this.name = plan.getName();
        this.revisions = revisions;
        this.currentRevision = currentRevision;
        this.active = plan.isActive();
        this.s3FolderId = plan.getId().toString();
        this.metadata = new RevisionMetadataDTO(plan.getCurrentRevision().getType(), plan.getCurrentRevision().getMetadata());
        this.planType = plan.getCurrentRevision().getType();
        this.parentId = plan.getParent().getId();
        this.parentName = plan.getParent().getName();
        this.lock = plan.isLock();
        if(plan.getLastActivity() != null) {
            this.lastActivity = plan.getLastActivity().toInstant();
            super.setLastModified(this.lastActivity);
        }
        this.bimplusPlanReferenceDto = plan.getBimplusPlanReference() != null ? new BimplusPlanReferenceDto(plan.getBimplusPlanReference()) : new BimplusPlanReferenceDto();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlanRevisionDTO getCurrentRevision() {
        return currentRevision;
    }

    public void setCurrentRevision(PlanRevisionDTO currentRevision) {
        this.currentRevision = currentRevision;
    }

    public List<PlanRevisionDTO> getRevisions() {
        return revisions;
    }

    public void setRevisions(List<PlanRevisionDTO> revisions) {
        this.revisions = revisions;
    }

    public PlanRevisionDTO getRevision() {
        return revision;
    }

    public void setRevision(PlanRevisionDTO revision) {
        this.revision = revision;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getS3FolderId() {
        return s3FolderId;
    }

    public void setS3FolderId(String s3FolderId) {
        this.s3FolderId = s3FolderId;
    }

    public RevisionMetadataDTO getMetadata() {
        return metadata;
    }

    public void setMetadata(RevisionMetadataDTO metadata) {
        this.metadata = metadata;
    }

    public PlanType getPlanType() {
        return planType;
    }

    public void setPlanType(PlanType planType) {
        this.planType = planType;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public List<ResourcePermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<ResourcePermission> permissions) {
        this.permissions = permissions;
    }

    public UUID getCurrentRevisionId() {
        return currentRevisionId;
    }

    public void setCurrentRevisionId(UUID currentRevisionId) {
        this.currentRevisionId = currentRevisionId;
    }

    public String getParentName() {
        return parentName;
    }

    public Instant getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Instant lastActivity) {
        this.lastActivity = lastActivity;
    }

    public Boolean getLock() {
        return lock;
    }

    public void setLock(Boolean lock) {
        this.lock = lock;
    }

    public BimplusPlanReferenceDto getBimplusPlanReferenceDto() {
        return bimplusPlanReferenceDto;
    }

    public void setBimplusPlanReferenceDto(BimplusPlanReferenceDto bimplusPlanReferenceDto) {
        this.bimplusPlanReferenceDto = bimplusPlanReferenceDto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanDTO planDTO = (PlanDTO) o;
        return Objects.equals(id, planDTO.id) &&
                Objects.equals(projectId, planDTO.projectId) &&
                Objects.equals(name, planDTO.name) &&
                Objects.equals(currentRevision, planDTO.currentRevision) &&
                Objects.equals(revisions, planDTO.revisions) &&
                Objects.equals(revision, planDTO.revision) &&
                Objects.equals(active, planDTO.active) &&
                Objects.equals(s3FolderId, planDTO.s3FolderId) &&
                Objects.equals(metadata, planDTO.metadata) &&
                planType == planDTO.planType &&
                Objects.equals(parentId, planDTO.parentId) &&
                Objects.equals(parentName, planDTO.parentName) &&
                Objects.equals(permissions, planDTO.permissions) &&
                Objects.equals(currentRevisionId, planDTO.currentRevisionId) &&
                Objects.equals(openTaskCount, planDTO.openTaskCount) &&
                Objects.equals(lastActivity, planDTO.lastActivity) &&
                Objects.equals(lock, planDTO.lock) &&
                Objects.equals(bimplusPlanReferenceDto, planDTO.bimplusPlanReferenceDto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, projectId, name, currentRevision, revisions, revision, active, s3FolderId, metadata, planType, parentId, parentName, permissions, currentRevisionId, openTaskCount, lastActivity, lock, bimplusPlanReferenceDto);
    }

    public Integer getOpenTaskCount() {
        return openTaskCount;
    }

    public void setOpenTaskCount(Integer openTaskCount) {
        this.openTaskCount = openTaskCount;
    }
}
