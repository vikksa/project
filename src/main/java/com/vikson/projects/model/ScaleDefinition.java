package com.vikson.projects.model;

import org.hibernate.annotations.Type;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "scale_definitions")
public class ScaleDefinition {

    @Id
    @Type(type = "pg-uuid")
    private UUID id = UUID.randomUUID();
    @Column(name = "name", nullable = false)
    private String name = "";
    @Column(name = "one_star_label", nullable = false)
    private String oneStarLabel = "";
    @Column(name = "two_star_label", nullable = false)
    private String twoStarLabel = "";
    @Column(name = "three_star_label", nullable = false)
    private String threeStarLabel = "";
    @Column(name = "four_star_label", nullable = false)
    private String fourStarLabel = "";
    @Column(name = "five_star_label", nullable = false)
    private String fiveStarLabel = "";
    private boolean active = true;
    private boolean costs = false;
    private boolean priority = false;
    @ManyToOne(optional = false)
    private Project project;

    protected ScaleDefinition() {
    }

    public ScaleDefinition(Project project) {
        Assert.notNull(project, "ScaleDefinition.project must not be NULL!");
        this.project = project;
    }

    public ScaleDefinition(Project project, ScaleDefinition scaleDefinition) {
        this(project);
        this.name = scaleDefinition.getName();
        this.oneStarLabel = scaleDefinition.getOneStarLabel();
        this.twoStarLabel = scaleDefinition.getTwoStarLabel();
        this.threeStarLabel = scaleDefinition.getThreeStarLabel();
        this.fourStarLabel = scaleDefinition.getFourStarLabel();
        this.fiveStarLabel = scaleDefinition.getFiveStarLabel();
        this.active = scaleDefinition.isActive();
        this.costs = scaleDefinition.isCosts();
        this.priority = scaleDefinition.isPriority();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(StringUtils.isEmpty(name))
            name = "";
        this.name = name;
    }

    public String getOneStarLabel() {
        return oneStarLabel;
    }

    public void setOneStarLabel(String oneStarLabel) {
        if(StringUtils.isEmpty(oneStarLabel))
            oneStarLabel = "";
        this.oneStarLabel = oneStarLabel;
    }

    public String getTwoStarLabel() {
        return twoStarLabel;
    }

    public void setTwoStarLabel(String twoStarLabel) {
        if(StringUtils.isEmpty(twoStarLabel))
            twoStarLabel = "";
        this.twoStarLabel = twoStarLabel;
    }

    public String getThreeStarLabel() {
        return threeStarLabel;
    }

    public void setThreeStarLabel(String threeStarLabel) {
        if(StringUtils.isEmpty(threeStarLabel))
            threeStarLabel = "";
        this.threeStarLabel = threeStarLabel;
    }

    public String getFourStarLabel() {
        return fourStarLabel;
    }

    public void setFourStarLabel(String fourStarLabel) {
        if(StringUtils.isEmpty(fourStarLabel))
            fourStarLabel = "";
        this.fourStarLabel = fourStarLabel;
    }

    public String getFiveStarLabel() {
        return fiveStarLabel;
    }

    public void setFiveStarLabel(String fiveStarLabel) {
        if(StringUtils.isEmpty(fiveStarLabel))
            fiveStarLabel = "";
        this.fiveStarLabel = fiveStarLabel;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isCosts() {
        return costs;
    }

    public void setCosts(boolean costs) {
        this.costs = costs;
    }

    public boolean isPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    public Project getProject() {
        return project;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScaleDefinition)) return false;
        ScaleDefinition that = (ScaleDefinition) o;
        return active == that.active &&
            costs == that.costs &&
            priority == that.priority &&
            Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(oneStarLabel, that.oneStarLabel) &&
            Objects.equals(twoStarLabel, that.twoStarLabel) &&
            Objects.equals(threeStarLabel, that.threeStarLabel) &&
            Objects.equals(fourStarLabel, that.fourStarLabel) &&
            Objects.equals(fiveStarLabel, that.fiveStarLabel) &&
            Objects.equals(project, that.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, oneStarLabel, twoStarLabel, threeStarLabel, fourStarLabel, fiveStarLabel, active, costs, priority, project);
    }

    @Override
    public String toString() {
        return "ScaleDefinition{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", oneStarLabel='" + oneStarLabel + '\'' +
                ", twoStarLabel='" + twoStarLabel + '\'' +
                ", threeStarLabel='" + threeStarLabel + '\'' +
                ", fourStarLabel='" + fourStarLabel + '\'' +
                ", fiveStarLabel='" + fiveStarLabel + '\'' +
                ", active=" + active +
                ", costs=" + costs +
                ", priority=" + priority +
                '}';
    }
}
