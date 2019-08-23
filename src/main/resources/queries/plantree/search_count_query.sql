WITH RECURSIVE children AS (
    SELECT root.id, root.parent_id, root.name, root.active, root.last_activity, root.last_modified, root.type
    FROM (
            SELECT f.id, f.parent_id, f.name, f.active, NULL "last_activity", f.last_modified, 'Folder' as "type"
            FROM plan_folders f
            WHERE f.parent_id = :parentId

            UNION

            SELECT p.id, p.parent_id, p.name, p.active, p.last_activity, p.last_modified, 'Plan' as "type"
            FROM plans p INNER JOIN plan_revisions r ON p.current_revision_id = r.id
            WHERE p.parent_id = :parentId
    ) root

    UNION

    SELECT child.id, child.parent_id, child.name, child.active, child.last_activity, child.last_modified, child.type
    FROM (
            SELECT f.id, f.parent_id, f.name, f.active, NULL "last_activity", f.last_modified, 'Folder' as "type"
            FROM plan_folders f

            UNION

            SELECT p.id, p.parent_id, p.name, p.active, p.last_activity, p.last_modified, 'Plan' as "Type"
            FROM plans p INNER JOIN plan_revisions r ON p.current_revision_id = r.id
    ) child
    JOIN children c ON child.parent_id = c.id
)

SELECT COUNT(*)
FROM children c
WHERE c.name ILIKE :search AND c.active IN (:stateFilterActive,:stateFilterInactive) AND ((c.last_activity IS NULL AND c.last_modified IS NULL) OR (c.last_activity > :sinceFilter OR c.last_modified > :sinceFilter)) AND c.type = ANY (:typeFilter)
