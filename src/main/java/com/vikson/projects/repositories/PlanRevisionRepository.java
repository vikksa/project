package com.vikson.projects.repositories;

import com.vikson.projects.model.PlanRevision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlanRevisionRepository extends JpaRepository<PlanRevision, UUID> {
}
