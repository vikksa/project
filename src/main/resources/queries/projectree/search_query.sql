WITH RECURSIVE children AS (
     SELECT i.id, i.parent_id, i.name, i.type, i.active, i.last_activity, i.last_modified, i.bimplus_id, i.bimplus_project_name, i.bimplus_project_state, i.team_slug
           FROM
           (    SELECT pf.id, pf.parent_id, pf.name, 'Folder' as "type", pf.active, NULL as "last_activity", pf.last_modified, NULL as "bimplus_id", NULL as "bimplus_project_name", NULL as "bimplus_project_state", NULL as "team_slug"
                    FROM project_folders pf
                    WHERE parent_id = :parentId

                UNION

                SELECT  p1.id, p1.parent_id, p1.name, 'Project' as "type", p1.active, p1.last_activity, p1.last_modified, p1.bimplus_id, p1.bimplus_project_name, p1.bimplus_project_state, p1.team_slug
                    FROM projects p1
                    WHERE p1.parent_id = :parentId
           ) i
     UNION

    SELECT s.id as "id", s.parent_id as "parent_id", s.name as "name", s.type as "type", s.active, s.last_activity, s.last_modified, s.bimplus_id, s.bimplus_project_name, s.bimplus_project_state, s.team_slug
           FROM
           (    SELECT f.id, f.parent_id, f.name, 'Folder' as "type", f.active, NULL as "last_activity", f.last_modified, NULL as "bimplus_id", NULL as "bimplus_project_name", NULL as "bimplus_project_state", NULL as "team_slug"
                FROM project_folders f

                UNION
                SELECT p.id, p.parent_id, p.name, 'Project' as "type", p.active, p.last_activity, p.last_modified, p.bimplus_id, p.bimplus_project_name, p.bimplus_project_state, p.team_slug
                FROM projects p
           ) s
           JOIN children c ON s.parent_id = c.id
),

ProjectFolder as (
    SELECT id, parent_id, type, bimplus_id, bimplus_project_name, bimplus_project_state, team_slug FROM
    (
        SELECT id, parent_id, 'Project' as "type", bimplus_id, bimplus_project_name, bimplus_project_state, team_slug FROM projects p WHERE p.active IN (:stateFilterActive,:stateFilterInactive)
        UNION
        SELECT id, parent_id, 'Folder' as "type", null as bimplus_id, null as bimplus_project_name, null as bimplus_project_state, null as team_slug  FROM project_folders f WHERE f.active IN (true,:stateFilterInactive)
    ) pf
    WHERE pf.type = ANY(:typeFilter)
)

SELECT cc.id, cc.parent_id, cc.name, cc.type, cc.active, (SELECT COUNT(cpf.id) FROM ProjectFolder cpf WHERE cc.id=cpf.parent_id) as "items", pf.bimplus_id, pf.bimplus_project_name, pf.bimplus_project_state, pf.team_slug,
    CASE WHEN cc.last_activity IS NULL THEN cc.last_modified
         WHEN cc.last_modified IS NULL THEN cc.last_activity
         WHEN cc.last_activity < cc.last_modified THEN cc.last_modified ELSE cc.last_activity END as actual_last_activity
 FROM children cc left join ProjectFolder pf on cc.id = pf.id
WHERE cc.active IN (:stateFilterActive,:stateFilterInactive) AND cc.name ILIKE :search AND cc.type = ANY (:typeFilter)
ORDER BY cc.type, %s %s OFFSET :offsetValue LIMIT :limitValue;