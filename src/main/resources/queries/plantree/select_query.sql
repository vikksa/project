SELECT c.id, c.parent_id, c.name, c.version, c.type, c.active, c.plan_type, c.current_revision_id, c.items,
    case when c.last_modified is null then c.last_activity
         when c.last_activity is null then c.last_modified
         when c.last_activity < c.last_modified then c.last_modified else c.last_activity END as actual_last_activity,
    c.bimplus AS "bimplus"
FROM (
        SELECT p.id, p.parent_id, p.name, r.version, 'Plan' as "type", p.active, p.last_activity, p.last_modified, r.type as "plan_type", r.id as "current_revision_id", 0 as "items", p.bimplus_plan_state AS "bimplus"
        FROM plans p INNER JOIN plan_revisions r ON p.current_revision_id = r.id
        WHERE p.parent_id = :parentId AND p.active IN (:stateFilterActive,:stateFilterInactive) AND (p.last_activity > :sinceFilter OR p.last_modified > :sinceFilter)

        UNION

        SELECT f.id, f.parent_id, f.name, NULL as "version", 'Folder' as "type", f.active, NULL as "last_activity", f.last_modified, NULL as "plan_type", NULL as "current_revision_id",
               (SELECT COUNT(ss.id) FROM (SELECT f1.id, 'Folder' as "type" FROM plan_folders f1 WHERE f1.parent_id = f.id AND f1.active IN (true,:stateFilterInactive)
                                          UNION
                                          SELECT p1.id,'Plan' as "type" FROM plans p1 WHERE p1.parent_id = f.id AND p1.active IN (:stateFilterActive,:stateFilterInactive)) ss where ss.type = ANY (:typeFilter)) as "items",
                                          NULL as "bimplus"
        FROM plan_folders f
        WHERE f.parent_id = :parentId AND f.active IN (:stateFilterActive, :stateFilterInactive)
) c
WHERE c.type = ANY (:typeFilter)
ORDER BY c.type, %s %s OFFSET :offsetValue LIMIT :limitValue