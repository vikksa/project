package com.vikson.projects.api.resources;

import com.vikson.projects.model.AuditedEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.UUID;

public class AuditedDTO {

    private UUID createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant created;
    private UUID lastModifiedBy;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant lastModified;

    public AuditedDTO() {
    }

    public AuditedDTO(AuditedEntity entity) {
        this.createdBy = entity.getCreatedBy();
        if (entity.getCreated() != null)
            this.created = entity.getCreated().toInstant();
        this.lastModifiedBy = entity.getLastModifiedBy();
        if (entity.getLastModified() != null)
            this.lastModified = entity.getLastModified().toInstant();
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public UUID getLastModifiedBy() {
        return lastModifiedBy;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }
}
