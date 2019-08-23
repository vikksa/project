package com.vikson.projects.model;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "organisation_root_folder")
public class OrganisationRootFolder {

    @Id
    @Type(type = "pg-uuid")
    private UUID id;
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "folder_id", updatable = false)
    private ProjectFolder folder;

    protected OrganisationRootFolder() {
    }

    public OrganisationRootFolder(UUID id, ProjectFolder folder) {
        this.id = id;
        this.folder = folder;
    }

    public UUID getId() {
        return id;
    }

    public ProjectFolder getFolder() {
        return folder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrganisationRootFolder that = (OrganisationRootFolder) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return folder != null ? folder.equals(that.folder) : that.folder == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (folder != null ? folder.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OrganisationRootFolder{" +
                "id=" + id +
                ", folder=" + folder +
                '}';
    }
}
