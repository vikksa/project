package com.vikson.projects.model;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class AuditedEntity {

    @CreatedBy
    private UUID createdBy;
    @Column(nullable = false)
    private ZonedDateTime created;
    @LastModifiedBy
    private UUID lastModifiedBy;
    @LastModifiedDate
    private ZonedDateTime lastModified;

    protected AuditedEntity() {
        this(null);
    }

    public AuditedEntity(ZonedDateTime created) {
        this.created = created != null? created : ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public UUID getLastModifiedBy() {
        return lastModifiedBy;
    }

    public ZonedDateTime getLastModified() {
        return lastModified;
    }
}
