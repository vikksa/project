package com.vikson.projects.model;

import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "plan_levels")
public class PlanLevel implements Comparable<PlanLevel> {

    @Id
    @Type(type = "pg-uuid")
    private UUID id;
    @Column(name = "tiles_x")
    private int tilesX;
    @Column(name = "tiles_y")
    private int tilesY;
    @ManyToOne(optional = false)
    private PlanRevision revision;

    protected PlanLevel() {
    }

    public PlanLevel(UUID id, int tilesX, int tilesY, PlanRevision revision) {
        this.id = id == null? UUID.randomUUID() : id;
        this.tilesX = tilesX;
        this.tilesY = tilesY;
        this.revision = revision;
    }

    public UUID getId() {
        return id;
    }

    public int getTilesX() {
        return tilesX;
    }

    public int getTilesY() {
        return tilesY;
    }

    public PlanRevision getRevision() {
        return revision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlanLevel planLevel = (PlanLevel) o;

        if (tilesX != planLevel.tilesX) return false;
        if (tilesY != planLevel.tilesY) return false;
        return id != null ? id.equals(planLevel.id) : planLevel.id == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + tilesX;
        result = 31 * result + tilesY;
        return result;
    }

    @Override
    public String toString() {
        return "PlanLevel{" +
                "id=" + id +
                ", tilesX=" + tilesX +
                ", tilesY=" + tilesY +
                '}';
    }

    @Override
    public int compareTo(@NotNull PlanLevel o) {
        return tilesX * tilesY - o.tilesX * o.tilesY;
    }
}
