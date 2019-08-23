ALTER TABLE plans DROP COLUMN current_revision_id;
ALTER TABLE plans ADD COLUMN current_revision_id uuid;