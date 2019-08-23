package com.vikson.projects.repositories;

import com.vikson.projects.model.PlanLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlanLevelRepository extends JpaRepository<PlanLevel, UUID> {
}
