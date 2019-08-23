package com.vikson.projects.model;

import com.vikson.projects.model.values.RevisionLocationDifference;
import com.vikson.projects.model.values.PlanType;
import com.vikson.projects.model.values.RevisionMetadata;
import org.hibernate.annotations.Type;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.*;

@Entity
@Table(name = "plan_revisions")
public class PlanRevision extends AuditedEntity {

    @Id
    @Type(type = "pg-uuid")
    private UUID id = UUID.randomUUID();
    @ManyToOne(optional = false)
    private Plan plan;
    @Column(nullable = false)
    private String version;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType type;
    @Embedded
    private RevisionMetadata metadata;
    @OneToMany(mappedBy = "revision", cascade = CascadeType.ALL)
    private List<PlanLevel> levels = new ArrayList<>();
    @Embedded
    private RevisionLocationDifference revisionLocationDifference;

    protected PlanRevision() {
    }

    public PlanRevision(UUID id, Plan plan, String version, PlanType type, RevisionMetadata metadata,  ZonedDateTime created, RevisionLocationDifference revisionLocationDifference) {
        super(created);
        this.id = id != null? id : UUID.randomUUID();
        Assert.notNull(plan, "PlanRevision.plan must not be NULL!");
        this.plan = plan;
        Assert.isTrue(!StringUtils.isEmpty(version), "PlanRevision.version must not be NULL or empty!");
        this.version = version;
        Assert.notNull(type, "PlanRevision.type must not be NULL!");
        this.type = type;
        Assert.notNull(metadata, "PlanRevision.metadata must not be NULL!");
        this.metadata = metadata;
        this.revisionLocationDifference = revisionLocationDifference;
    }

    public UUID getId() {
        return id;
    }

    public Plan getPlan() {
        return plan;
    }

    public String getVersion() {
        return version;
    }

    public PlanType getType() {
        return type;
    }

    public RevisionMetadata getMetadata() {
        return metadata;
    }

    public List<PlanLevel> getLevels() {
        return Collections.unmodifiableList(levels);
    }

    public void setLevels(List<PlanLevel> levels) {
        this.levels = levels;
    }

    public boolean isFilePlan() {
        return type == PlanType.File;
    }

    public RevisionLocationDifference getRevisionLocationDifference() {
        return revisionLocationDifference;
    }

    public Optional<String> getOriginalFileName() {
        if(!isFilePlan())
            return Optional.empty();
        return Optional.ofNullable(metadata.getFileName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanRevision that = (PlanRevision) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(version, that.version) &&
                type == that.type &&
                Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, version, type, metadata);
    }
}
