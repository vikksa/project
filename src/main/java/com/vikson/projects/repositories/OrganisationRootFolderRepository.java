package com.vikson.projects.repositories;

import com.vikson.projects.model.OrganisationRootFolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrganisationRootFolderRepository extends JpaRepository<OrganisationRootFolder, UUID> {
}
