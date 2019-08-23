ALTER TABLE project_folders ADD COLUMN active boolean NOT NULL DEFAULT true;
ALTER TABLE plan_folders ADD COLUMN active boolean NOT NULL DEFAULT true;
