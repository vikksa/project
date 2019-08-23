package com.vikson.projects.repositories;

import com.vikson.projects.model.ProjectLogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectLogoRepository extends JpaRepository<ProjectLogo, UUID> {
}
