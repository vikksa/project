SELECT COUNT(s.id)
FROM (
     SELECT id, 'Project' as "type"
     FROM projects WHERE parent_id = :parentId AND active IN(:stateFilterActive,:stateFilterInactive)

     UNION

     SELECT id, 'Folder' as "type"
     FROM project_folders WHERE parent_id = :parentId AND active IN (true,:stateFilterInactive)
) s
WHERE s.type = ANY (:typeFilter);