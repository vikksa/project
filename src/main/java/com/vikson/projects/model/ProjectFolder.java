package com.vikson.projects.model;

import org.hibernate.annotations.Type;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "project_folders")
public class ProjectFolder extends AuditedEntity{

    @Id
    @Type(type = "pg-uuid")
    private UUID id;
    @Column(nullable = false)
    private UUID organisationId;
    @Column(nullable = false)
    private String name;
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Project> children = new ArrayList<>();
    @ManyToOne
    private ProjectFolder parent;
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<ProjectFolder> subFolders = new ArrayList<>();

    private boolean active = true;

    protected ProjectFolder() {
    }

    public ProjectFolder(UUID id, String name, UUID organisationId, ProjectFolder parent) {
        this.id = id == null? UUID.randomUUID() : id;
        Assert.isTrue(!StringUtils.isEmpty(name), "ProjectFolder.name must not be NULL or empty!");
        this.name = name;
        Assert.notNull(organisationId, "ProjectFolder.organisationId must not be NULL!");
        this.organisationId = organisationId;
        this.parent = parent;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public List<Project> getChildren() {
        return children;
    }

    public ProjectFolder getParent() {
        return parent;
    }

    public void setParent(ProjectFolder parent) {
        this.parent = parent;
    }

    public List<ProjectFolder> getSubFolders() {
        return subFolders;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setChildren(List<Project> children) {
        this.children = children;
    }

    public void setSubFolders(List<ProjectFolder> subFolders) {
        this.subFolders = subFolders;
    }

    public boolean isParentOf(UUID childId){
        for(ProjectFolder folder : subFolders){
            if(folder.getId().equals(childId) || folder.isParentOf(childId)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectFolder that = (ProjectFolder) o;

        if (active != that.active) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (organisationId != null ? !organisationId.equals(that.organisationId) : that.organisationId != null)
            return false;
        if (children != null ? !children.equals(that.children) : that.children != null) return false;
        return subFolders != null ? subFolders.equals(that.subFolders) : that.subFolders == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (organisationId != null ? organisationId.hashCode() : 0);
        result = 31 * result + (children != null ? children.hashCode() : 0);
        result = 31 * result + (subFolders != null ? subFolders.hashCode() : 0);
        result = 31 * result + (active ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProjectFolder{" +
                "id=" + id +
                ", organisationId=" + organisationId +
                ", children=" + children +
                ", subFolders=" + subFolders +
                '}';
    }
}
