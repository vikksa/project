SELECT COUNT(c.id) AS "count"
FROM (
        SELECT p.id, 'Plan' as "type"
        FROM plans p
        WHERE p.parent_id = :parentId AND p.active IN (:stateFilterActive,:stateFilterInactive) AND (p.last_activity > :sinceFilter OR p.last_modified > :sinceFilter)

        UNION

        SELECT f.id, 'Folder' as "type"
        FROM plan_folders f
        WHERE f.parent_id = :parentId AND f.active IN (:stateFilterActive,:stateFilterInactive)
) c
WHERE c.type = ANY (:typeFilter)
