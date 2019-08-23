ALTER TABLE projects
    ALTER COLUMN name TYPE citext;

ALTER TABLE projects
    ADD CONSTRAINT unique_name
    UNIQUE(name, organisation_id);
