package com.vikson.projects.repositories;

import com.vikson.projects.model.ProjectFolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectFolderRepository extends JpaRepository<ProjectFolder, UUID> {
}
