package com.vikson.projects.model;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "project_logo")
public class ProjectLogo {

    @Id
    @Column(name = "logo_storage_id")
    private UUID storageId;
    @Column(name = "logo_media_type")
    private String mediaType;
    @Column(name = "logo_size")
    private Long size;
    @Column(name = "logo_checksum")
    private String checksum;

    protected ProjectLogo() {
    }

    public ProjectLogo(UUID storageId, String mediaType, Long size, String checksum) {
        this.storageId = storageId;
        this.mediaType = mediaType;
        this.size = size;
        this.checksum = checksum;
    }

    public UUID getStorageId() {
        return storageId;
    }

    public String getMediaType() {
        return mediaType;
    }

    public Long getSize() {
        return size;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectLogo logo = (ProjectLogo) o;

        if (storageId != null ? !storageId.equals(logo.storageId) : logo.storageId != null) return false;
        if (mediaType != null ? !mediaType.equals(logo.mediaType) : logo.mediaType != null) return false;
        if (size != null ? !size.equals(logo.size) : logo.size != null) return false;
        return checksum != null ? checksum.equals(logo.checksum) : logo.checksum == null;
    }

    @Override
    public int hashCode() {
        int result = storageId != null ? storageId.hashCode() : 0;
        result = 31 * result + (mediaType != null ? mediaType.hashCode() : 0);
        result = 31 * result + (size != null ? size.hashCode() : 0);
        result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProjectLogo{" +
                "storageId=" + storageId +
                ", mediaType='" + mediaType + '\'' +
                ", size=" + size +
                ", checksum=" + checksum +
                '}';
    }
}
