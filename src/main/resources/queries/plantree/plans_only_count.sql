SELECT COUNT(*)
FROM plans p INNER JOIN plan_revisions r ON p.current_revision_id = r.id
WHERE p.project_id = :project AND p.active = ANY (:active) AND (p.last_activity > :since OR p.last_modified > :since) AND p.name ILIKE :search
