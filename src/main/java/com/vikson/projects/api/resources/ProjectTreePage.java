package com.vikson.projects.api.resources;

import com.vikson.projects.model.ProjectFolder;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProjectTreePage extends PageImpl<ProjectTreeChildItem> {

    private UUID id;
    private UUID parentId;
    private String name;

    public ProjectTreePage(ProjectFolder parent, List<ProjectTreeChildItem> content, Pageable pageable, long total) {
        super(content, pageable, total);
        Assert.notNull(parent, "parent must not be NULL");
        this.id = parent.getId();
        if (parent.getParent() != null)
            this.parentId = parent.getParent().getId();
        this.name = parent.getName();
    }

    public UUID getId() {
        return id;
    }

    public UUID getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProjectTreePage that = (ProjectTreePage) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(parentId, that.parentId) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), id, parentId, name);
    }
}
