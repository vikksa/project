package com.vikson.projects.model;

import org.hibernate.annotations.Type;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "plan_folders")
public class PlanFolder extends AuditedEntity{

    @Id
    @Type(type = "pg-uuid")
    private UUID id;
    @Column(nullable = false)
    private String name;
    @ManyToOne(optional = false)
    private Project project;
    @ManyToOne
    private PlanFolder parent;
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<PlanFolder> subFolders = new ArrayList<>();
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Plan> plans = new ArrayList<>();

    private boolean active = true;


    protected PlanFolder() {
    }

    public PlanFolder(UUID id, String name, Project project, PlanFolder parent) {
        this.id = id != null? id : UUID.randomUUID();
        Assert.isTrue(!StringUtils.isEmpty(name), "PlanFolder.name must not be NULL or empty!");
        this.name = name;
        Assert.notNull(project, "PlanFolder.project must not be NULL!");
        this.project = project;
        this.parent = parent;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Assert.isTrue(!StringUtils.isEmpty(name), "PlanFolder.name must not be NULL or empty!");
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

    public List<PlanFolder> getSubFolders() {
        return subFolders;
    }

    public List<Plan> getPlans() {
        return plans;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isParentOf(UUID childId){
        for(PlanFolder planfolder : subFolders){
            if(planfolder.getId().equals(childId) || planfolder.isParentOf(childId)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlanFolder that = (PlanFolder) o;

        if (active != that.active) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (subFolders != null ? !subFolders.equals(that.subFolders) : that.subFolders != null) return false;
        return plans != null ? plans.equals(that.plans) : that.plans == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (subFolders != null ? subFolders.hashCode() : 0);
        result = 31 * result + (plans != null ? plans.hashCode() : 0);
        result = 31 * result + (active ? 1 : 0);
        return result;
    }

}
