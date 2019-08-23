CREATE TABLE project_logo (
    logo_storage_id uuid PRIMARY KEY,
    logo_media_type text NOT NULL,
    logo_size bigint NOT NULL,
    logo_checksum text NOT NULL
);

INSERT INTO project_logo
SELECT logo_storage_id, logo_media_type, logo_size, logo_checksum
FROM projects
WHERE logo_storage_id IS NOT NULL;

ALTER TABLE projects
DROP COLUMN logo_media_type,
DROP COLUMN logo_size,
DROP COLUMN logo_checksum;

ALTER TABLE projects ADD CONSTRAINT logo_storage_id FOREIGN KEY (logo_storage_id) REFERENCES project_logo;
ALTER TABLE projects RENAME COLUMN logo_storage_id TO project_logo_id;
ALTER TABLE projects ADD COLUMN report_logo_id uuid REFERENCES project_logo(logo_storage_id);