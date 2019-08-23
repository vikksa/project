package com.vikson.projects.repositories;

import com.vikson.projects.model.PlanFolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlanFolderRepository extends JpaRepository<PlanFolder, UUID> {
}
