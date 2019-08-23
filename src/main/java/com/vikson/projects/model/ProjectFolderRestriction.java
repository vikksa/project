package com.vikson.projects.model;

import com.vikson.services.users.resources.UserProfile;
import org.hibernate.annotations.Type;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "project_folder_restrictions")
public class ProjectFolderRestriction {

    @Id
    @Type(type = "pg-uuid")
    private UUID userId;
    @ManyToOne(optional = false)
    private ProjectFolder folder;

    private ProjectFolderRestriction() {
    }

    public ProjectFolderRestriction(UserProfile user, ProjectFolder folder) {
        Assert.notNull(user, "user is required - must not be NULL!");
        Assert.notNull(folder, "folder is required - must not be NULL!");
        Assert.isTrue(user.getOrganisationId().equals(folder.getOrganisationId()), "user and folder must belong to same organisation!");
        this.userId = user.getUserId();
        this.folder = folder;
    }

    public UUID getUserId() {
        return userId;
    }

    public ProjectFolder getFolder() {
        return folder;
    }

    public void setFolder(ProjectFolder folder) {
        Assert.notNull(folder, "folder is required - must not be NULL!");
        this.folder = folder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectFolderRestriction that = (ProjectFolderRestriction) o;

        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        return folder != null ? folder.equals(that.folder) : that.folder == null;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (folder != null ? folder.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProjectFolderRestriction{" +
                "userId=" + userId +
                ", folder=" + folder +
                '}';
    }
}
