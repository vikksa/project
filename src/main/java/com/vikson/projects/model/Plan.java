package com.vikson.projects.model;

import com.vikson.projects.model.values.BimplusPlanReference;
import org.hibernate.annotations.Type;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;

@Entity
@Table(name = "plans")
public class Plan extends AuditedEntity {

    @Id
    @Type(type = "pg-uuid")
    private UUID id = UUID.randomUUID();
    @Column(nullable = false)
    private String name;
    @ManyToOne(optional = false)
    private Project project;
    @ManyToOne(optional = false)
    private PlanFolder parent;
    @OneToOne(cascade = CascadeType.ALL)
    private PlanRevision currentRevision;
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL)
    private List<PlanRevision> revisions = new ArrayList<>();
    private boolean active = true;
    private ZonedDateTime lastActivity;
    private boolean lock = false;
    @Embedded
    private BimplusPlanReference bimplusPlanReference;

    protected Plan() {
    }

    public Plan(UUID id, String name, Project project, PlanFolder parent, ZonedDateTime created) {
        super(created);
        this.id = id != null? id : UUID.randomUUID();
        Assert.isTrue(!StringUtils.isEmpty(name), "Plan.name must not be NULL or empty!");
        this.name = name;
        Assert.notNull(project, "Plan.project must not be NULL!");
        this.project = project;
        Assert.notNull(parent, "Plan.parent must not be NULL!");
        this.parent = parent;
        this.bimplusPlanReference = new BimplusPlanReference();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Assert.isTrue(!StringUtils.isEmpty(name), "Plan.name must not be NULL or empty!");
        this.name = name;
    }

    public Project getProject() {
        return project;
    }

    public PlanFolder getParent() {
        return parent;
    }

    public void setParent(PlanFolder parent) {
        this.parent = parent;
    }

    public List<PlanRevision> getRevisions() {
        return Collections.unmodifiableList(revisions);
    }

    public PlanRevision getCurrentRevision() {
        return currentRevision;
    }

    public void setCurrentRevision(PlanRevision currentRevision) {
        Assert.notNull(currentRevision, "Plan.currentRevision must not be NULL!");
        this.currentRevision = currentRevision;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void addNewRevision(PlanRevision revision) {
        Assert.notNull(revision, "Cannot add NULL Revision to Plan!");
        revisions.add(revision);
        currentRevision = revision;
    }

    public boolean requiresLastModifiedUpdate() {
        return getLastModified() == null || Instant.now().minusSeconds(5L * 60L).isAfter(getLastModified().toInstant());
    }

    /**
     * Timestamp of the last change on this plan (including any pins, tasks or media).
     *
     * @return timestamp, maybe {@code NULL}
     */
    public ZonedDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(ZonedDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    /**
     * Sets the given {@link ZonedDateTime} to {@link this#lastActivity} if the current value is {@code null} or
     * the given value is after the current value.
     *
     * @param newLastActivity {@link ZonedDateTime}
     * @return {@code true} when it actually changed.
     */
    public boolean swapLastActivityIfNewer(ZonedDateTime newLastActivity) {
        if(this.lastActivity == null || newLastActivity.isAfter(this.lastActivity)) {
            this.lastActivity = newLastActivity;
            return true;
        }
        return false;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public BimplusPlanReference getBimplusPlanReference() {
        if(bimplusPlanReference == null) {
            bimplusPlanReference = new BimplusPlanReference();
        }
        return bimplusPlanReference;
    }

    public void setBimplusPlanReference(BimplusPlanReference bimplusPlanReference) {
        this.bimplusPlanReference = bimplusPlanReference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plan plan = (Plan) o;
        return active == plan.active &&
                lock == plan.lock &&
                Objects.equals(id, plan.id) &&
                Objects.equals(name, plan.name) &&
                Objects.equals(project, plan.project) &&
                Objects.equals(parent, plan.parent) &&
                Objects.equals(currentRevision, plan.currentRevision) &&
                Objects.equals(revisions, plan.revisions) &&
                Objects.equals(lastActivity, plan.lastActivity) &&
                Objects.equals(bimplusPlanReference, plan.bimplusPlanReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, project, parent, currentRevision, revisions, active, lastActivity, lock, bimplusPlanReference);
    }

    @Override
    public String toString() {
        return "Plan{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", parent=" + parent +
                ", currentRevision=" + currentRevision +
                ", revisions=" + revisions +
                ", active=" + active +
                '}';
    }
}
