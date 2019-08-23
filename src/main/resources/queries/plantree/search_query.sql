WITH RECURSIVE children AS (
    SELECT root.id, root.parent_id, root.name, root.version, root.type, root.active, root.last_activity, root.last_modified, root.plan_type, root.current_revision_id, root.bimplus AS "bimplus"
    FROM (
            SELECT f.id, f.parent_id, f.name, NULL as "version", 'Folder' as "type", f.active, NULL as "last_activity", f.last_modified as "last_modified", NULL as "plan_type", NULL as "current_revision_id", NULL AS "bimplus"
            FROM plan_folders f
            WHERE f.parent_id = :parentId

            UNION

            SELECT p.id, p.parent_id, p.name, r.version, 'Plan' as "type", p.active, p.last_activity, p.last_modified, r.type AS "plan_type", p.current_revision_id, p.bimplus_plan_state AS "bimplus"
            FROM plans p INNER JOIN plan_revisions r ON p.current_revision_id = r.id
            WHERE p.parent_id = :parentId
    ) root

    UNION

    SELECT child.id, child.parent_id, child.name, child.version, child.type, child.active, child.last_activity, child.last_modified, child.plan_type, child.current_revision_id, child.bimplus AS "bimplus"
    FROM (
            SELECT f.id, f.parent_id, f.name, NULL as "version", 'Folder' as "type", f.active, NULL as "last_activity", f.last_modified as "last_modified", NULL as "plan_type", NULL as "current_revision_id", NULL AS "bimplus"
            FROM plan_folders f

            UNION

            SELECT p.id, p.parent_id, p.name, r.version, 'Plan' as "type", p.active, p.last_activity, p.last_modified, r.type AS "plan_type", p.current_revision_id, p.bimplus_plan_state AS "bimplus"
            FROM plans p INNER JOIN plan_revisions r ON p.current_revision_id = r.id
    ) child
    JOIN children c ON child.parent_id = c.id
)

SELECT c.id, c.parent_id, c.name, c.version, c.type, c.active, c.plan_type, c.current_revision_id, c.bimplus AS "bimplus",
    (SELECT COUNT(su.id) FROM
        (   SELECT id, 'Plan' as "type" FROM plans p WHERE p.parent_id = c.id AND p.active IN (:stateFilterActive,:stateFilterInactive)
            UNION
            SELECT id, 'Folder' as "type" FROM plan_folders f WHERE f.parent_id = c.id AND f.active IN (true,:stateFilterInactive)
        ) su where su.type = ANY (:typeFilter)) as "items",
    CASE WHEN c.last_modified IS NULL THEN c.last_activity
         WHEN c.last_activity IS NULL THEN c.last_modified
         WHEN c.last_activity < c.last_modified THEN c.last_modified ELSE c.last_activity END as actual_last_activity
FROM children c
WHERE c.name ILIKE :search AND c.active IN (:stateFilterActive,:stateFilterInactive) AND ((c.last_activity IS NULL AND c.last_modified IS NULL) OR (c.last_activity > :sinceFilter OR c.last_modified > :sinceFilter)) AND c.type = ANY (:typeFilter)
ORDER BY c.type, %s %s OFFSET :offsetValue LIMIT :limitValue