SELECT p.id, p.name, p.parent_id, p.current_revision_id, r.version, r.type as "plan_type", 'Plan' as "type", p.active, 0 as "items", p.bimplus_plan_state AS "bimplus",
    CASE WHEN p.last_modified IS NULL THEN p.last_activity
         WHEN p.last_activity IS NULL THEN p.last_modified
         WHEN p.last_activity < p.last_modified THEN p.last_modified ELSE p.last_activity END as actual_last_activity
FROM plans p INNER JOIN plan_revisions r ON p.current_revision_id = r.id
WHERE p.id = ANY (:planIds) AND p.active = ANY (:active) AND (p.last_activity > :since OR p.last_modified > :since) AND p.name ILIKE :search
ORDER BY %s %s OFFSET :offset LIMIT :limit