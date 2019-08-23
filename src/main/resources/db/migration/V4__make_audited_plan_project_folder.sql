ALTER TABLE "plan_folders"
     ADD COLUMN created timestamp ,
     ADD COLUMN created_by uuid,
     ADD COLUMN last_modified timestamp,
     ADD COLUMN last_modified_by uuid;

ALTER TABLE "project_folders"
     ADD COLUMN created timestamp ,
     ADD COLUMN created_by uuid,
     ADD COLUMN last_modified timestamp,
     ADD COLUMN last_modified_by uuid;