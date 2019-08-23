package com.vikson.projects.repositories;

import com.vikson.projects.model.Plan;
import com.vikson.projects.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public interface PlanRepository extends JpaRepository<Plan, UUID> {
    @Query(nativeQuery = true,
        value = "SELECT * FROM plans p WHERE p.project_id IN :projectIds AND p.active=true AND p.name ILIKE %:search% ")
    Stream<Plan> findByProjectIdsAndNameLike(@Param("projectIds") Set<UUID> projectIds,
                                             @Param("search") String search);

    @Query(nativeQuery = true,
        value = "SELECT COUNT(pr.id) FROM projects po INNER JOIN plans pl ON po.id = pl.project_id " +
            "INNER JOIN plan_revisions pr ON pl.id = pr.plan_id " +
            "WHERE po.organisation_id = :org AND po.active IS TRUE AND pl.active IS TRUE")
    long countRevisionsOfActivePlansInOrganisation(@Param("org") UUID org);

    Optional<Plan> findFirstByProjectOrderByLastModifiedDesc(Project project);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE plans SET last_modified = :lastModified WHERE id = :id")
    void setLastModified(@Param("id") UUID id, @Param("lastModified")ZonedDateTime lastModified);

    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM plans INNER JOIN projects ON plans.project_id = projects.id " +
            "WHERE projects.organisation_id = :organisationId")
    int getTotalAmountOfPlans(@Param("organisationId") UUID organisationId);

    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM plans INNER JOIN projects ON plans.project_id = projects.id " +
            "WHERE projects.organisation_id = :organisationId AND plans.active")
    int getAmountOfActivePlans(@Param("organisationId") UUID organisationId);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE plans SET lock = :lock WHERE id = :id")
    void setLock(@Param("id") UUID id, @Param("lock")boolean lock);

    @Query(nativeQuery = true, value = "select count(*) from plans inner join projects p on plans.project_id = p.id where p.id = :projectId")
    int countPlansByProject(@Param("projectId") UUID projectId);

    Stream<Plan> findByProjectId(UUID projectId);

    Page<Plan> findByProjectIdAndActiveIsTrue(UUID projectId, Pageable page);

    Page<Plan> findByIdInAndActiveIsTrue(List<UUID> planIds, Pageable page);

    Page<Plan> findByParentIdAndActiveIsTrue(UUID folderId, Pageable page);
}
