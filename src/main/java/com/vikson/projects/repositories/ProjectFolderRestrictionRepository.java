package com.vikson.projects.repositories;

import com.vikson.projects.model.ProjectFolderRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectFolderRestrictionRepository extends JpaRepository<ProjectFolderRestriction, UUID> {
    void deleteByUserId(UUID userId);
}
