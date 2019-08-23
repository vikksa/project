package com.vikson.projects.api.resources;

import com.vikson.projects.model.PlanRevision;
import com.vikson.projects.model.values.PlanType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@ApiModel(value = "Plan Revision Resource")
public class PlanRevisionDTO extends AuditedDTO {

    @ApiModelProperty(value = "Id of the plan revision")
    private UUID id;
    @ApiModelProperty(value = "List of all the plan levels")
    private List<PlanLevelDTO> level;
    @ApiModelProperty(value = "Id of the s3 folder")
    private String s3FolderId;
    @ApiModelProperty(value = "The version of the plan revision")
    private String version;
    private RevisionMetadataDTO metadata;
    @ApiModelProperty(value = "Currently unused")
    private String username;
    @ApiModelProperty(value = "A HTTP Download URL for the revision archive, valid for 1 hour.", readOnly = true)
    private URL archiveDownloadUrl;
    @ApiModelProperty(value = "A HTTP Download URL for the original revision file, valid for 1 hours.", readOnly = true)
    private URL originalDownloadUrl;
    @ApiModelProperty(value = "Plan revision location difference")
    private PlanRevisionDifference revisionLocationDifference;
    @ApiModelProperty(value = "Indicates if this revision can be set as active. False when dimensions differ with current revision")
    private boolean allowSetActive = true;
    @ApiModelProperty(value = "Whether the plan name should be overwritten with the new revisions file name")
    private boolean overwritePlanname = false;

    public PlanRevisionDTO() {
    }

    public PlanRevisionDTO(PlanRevision revision, List<PlanLevelDTO> levels, String username) {
        super(revision);
        this.id = revision.getId();
        this.level = levels;
        this.s3FolderId = this.id.toString();
        this.version = revision.getVersion();
        this.metadata = new RevisionMetadataDTO(revision.getType(), revision.getMetadata());
        if (metadata.getPlanType() == PlanType.Map && !levels.isEmpty()) {
            PlanLevelDTO biggestLevel = level.stream().max(Comparator.naturalOrder())
                    .orElseThrow(RuntimeException::new);
            metadata.setImageWidth(biggestLevel.getWidth());
            metadata.setImageHeight(biggestLevel.getHeight());
        }
        this.username = username;
        if(revision.getRevisionLocationDifference()!=null) {
            this.revisionLocationDifference = new PlanRevisionDifference()
                    .withMoveX(revision.getRevisionLocationDifference().getMoveX())
                    .withMoveY(revision.getRevisionLocationDifference().getMoveY())
                    .withFactor(revision.getRevisionLocationDifference().getFactor());
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<PlanLevelDTO> getLevel() {
        return level;
    }

    public void setLevel(List<PlanLevelDTO> level) {
        this.level = level;
    }

    public String getS3FolderId() {
        return s3FolderId;
    }

    public void setS3FolderId(String s3FolderId) {
        this.s3FolderId = s3FolderId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public RevisionMetadataDTO getMetadata() {
        return metadata;
    }

    public void setMetadata(RevisionMetadataDTO metadata) {
        this.metadata = metadata;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public URL getArchiveDownloadUrl() {
        return archiveDownloadUrl;
    }

    public void setArchiveDownloadUrl(URL archiveDownloadUrl) {
        this.archiveDownloadUrl = archiveDownloadUrl;
    }

    public PlanRevisionDifference getRevisionLocationDifference() {
        return revisionLocationDifference;
    }

    public void setRevisionLocationDifference(PlanRevisionDifference revisionLocationDifference) {
        this.revisionLocationDifference = revisionLocationDifference;
    }

    public URL getOriginalDownloadUrl() {
        return originalDownloadUrl;
    }

    public void setOriginalDownloadUrl(URL originalDownloadUrl) {
        this.originalDownloadUrl = originalDownloadUrl;
    }

    public boolean isAllowSetActive() {
        return allowSetActive;
    }

    public void setAllowSetActive(boolean allowSetActive) {
        this.allowSetActive = allowSetActive;
    }

    public PlanRevisionDTO withAllowSetActive(boolean allowSetActive) {
        this.allowSetActive = allowSetActive;
        return this;
    }

    public boolean isOverwritePlanname() {
        return overwritePlanname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanRevisionDTO that = (PlanRevisionDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(level, that.level) &&
                Objects.equals(s3FolderId, that.s3FolderId) &&
                Objects.equals(version, that.version) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(username, that.username) &&
                Objects.equals(archiveDownloadUrl, that.archiveDownloadUrl) &&
                Objects.equals(revisionLocationDifference, that.revisionLocationDifference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, level, s3FolderId, version, metadata, username, archiveDownloadUrl, revisionLocationDifference);
    }
}
