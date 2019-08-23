WITH RECURSIVE children AS (
 SELECT i.id, i.parent_id, i.name, i.type, i.active, i.last_activity, i.items
   FROM
   (SELECT pf.id, pf.parent_id, pf.name, 'Folder' as "type", pf.active, NULL as "last_activity", NULL as "items"
            FROM project_folders pf
            WHERE parent_id = :parentId

         UNION

         SELECT  p1.id, p1.parent_id, p1.name, 'Project' as "type", p1.active, p1.last_activity, NULL as "items"
            FROM projects p1
            WHERE p1.parent_id = :parentId
   ) i

   UNION

    SELECT s.id as "id", s.parent_id as "parent_id", s.name as "name", s.type as "type", s.active, s.last_activity, s.items
            FROM (SELECT f.id, f.parent_id, f.name, 'Folder' as "type", f.active, NULL as "last_activity", NULL as "items"
                        FROM project_folders f

                    UNION

                  SELECT p.id, p.parent_id, p.name, 'Project' as "type", p.active, p.last_activity, NULL as "items"
                  FROM projects p
            ) s
            JOIN children c ON s.parent_id = c.id
)
SELECT COUNT(*) FROM children
WHERE active IN (:stateFilterActive,:stateFilterInactive) AND name ILIKE :search AND children.type = ANY (:typeFilter)