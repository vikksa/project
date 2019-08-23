SELECT s.id, s.parent_id, s.name, s.type, s.active,
    (SELECT COUNT(su.id) FROM
                        (   SELECT id,'Project' as "type" FROM projects p WHERE p.parent_id = s.id AND active IN (:stateFilterActive,:stateFilterInactive)
                            UNION
                            SELECT id,'Folder' as "type" FROM project_folders f WHERE f.parent_id = s.id AND active IN (true,:stateFilterInactive)) su where su."type" = ANY(:typeFilter)
                        ) as "items",
    CASE WHEN s.last_activity IS NULL THEN s.last_modified
         WHEN s.last_modified IS NULL THEN s.last_activity
         WHEN s.last_activity < s.last_modified THEN s.last_modified ELSE s.last_activity END as actual_last_activity,
    s.bimplus_id, s.bimplus_project_name, s.bimplus_project_state, s.team_slug
FROM (
     SELECT id as "id", name, parent_id, 'Project' as "type", active, last_activity, last_modified as "last_modified", bimplus_id, bimplus_project_name, bimplus_project_state, team_slug
     FROM projects WHERE parent_id = :parentId AND active IN (:stateFilterActive,:stateFilterInactive)

     UNION

     SELECT id, name, parent_id, 'Folder' as "type", active, NULL as "last_activity", last_modified as "last_modified", NULL as "bimplus_id", NULL as "bimplus_project_name", NULL as "bimplus_project_state", NULL as "team_slug"
     FROM project_folders WHERE parent_id = :parentId AND active IN (true,:stateFilterInactive)
) s
WHERE s.type = ANY (:typeFilter)
ORDER BY s.type, %s %s OFFSET :offsetValue LIMIT :limitValue