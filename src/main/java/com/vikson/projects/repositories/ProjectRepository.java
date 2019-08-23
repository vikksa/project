package com.vikson.projects.repositories;

import com.vikson.projects.model.Project;
import com.vikson.projects.model.ProjectLocation;
import com.vikson.projects.model.values.BimplusProjectState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query("SELECT p FROM Project as p WHERE p.name.name LIKE :title AND (p.organisationId = :organisationId OR p.id IN :ids)")
    Page<Project> findByNameNameContainingIgnoreCaseAndActiveIsTrueAndOrganisationIdOrIdIn(
            @Param("title") String title,
            @Param("organisationId") UUID organisationId,
            @Param("ids") Set<UUID> ids,
            Pageable pageable);

    Page<Project> findByNameNameContainingIgnoreCaseAndIdInAndActiveIsTrue(String title, Set<UUID> ids, Pageable pageable);

    Stream<Project> findByOrganisationIdAndActiveIsTrue(UUID organisationId, Pageable page);

    Stream<Project> findByOrganisationIdAndActiveIsTrue(UUID organisationId);

    Stream<Project> findByOrganisationId(UUID organisationId);

    Page<Project> findByNameNameContainingIgnoreCaseAndOrganisationIdEquals(String name, UUID organisationId, Pageable pageable);

    boolean existsByNameNameIgnoreCaseAndOrganisationId(String name, UUID organisationId);

    boolean existsByBimplusConfigIdAndBimplusConfigStateNot(UUID id, BimplusProjectState state);

    long countByOrganisationIdAndActiveIsTrue(UUID organisationId);

    @Modifying(flushAutomatically = true)
    @Query(nativeQuery = true, value = "UPDATE projects SET last_modified = :lastModified WHERE id = :id")
    void setLastModified(@Param("id") UUID id, @Param("lastModified") ZonedDateTime lastModified);

    @Query("SELECT COUNT(id) FROM Project as p WHERE p.organisationId = :organisationId")
    int getTotalAmountOfProjects(@Param("organisationId") UUID organisationId);

    @Query("SELECT COUNT(id) FROM Project as p WHERE p.organisationId = :organisationId AND p.active = TRUE")
    int getAmountOfActiveProjects(@Param("organisationId") UUID organisationId);

    @Query(nativeQuery = true, value = "SELECT SUM(pr.archive_size) FROM plans as p LEFT JOIN plan_revisions as pr " +
            "ON p.current_revision_id = pr.id " +
            "WHERE p.active = true " +
            "AND p.project_id = :projectId")
    Long getProjectSyncSize(@Param("projectId") UUID projectId);

    List<Project> findAllByIdInAndActiveIsTrue(List<UUID> ids);

    @Query("SELECT " +
            "DISTINCT " +
            "NEW com.vikson.projects.model.ProjectLocation(p.address.zipCode, p.address.cc) " +
            "FROM Project as p WHERE p.address IS NOT NULL AND  p.address.zipCode IS NOT NULL ")
    Page<ProjectLocation> getLocationsDistinctByZipCode(Pageable pageable);
}
