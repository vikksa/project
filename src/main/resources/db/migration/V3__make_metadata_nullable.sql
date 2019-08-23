ALTER TABLE plan_revisions ALTER COLUMN image_width DROP NOT NULL;
ALTER TABLE plan_revisions ALTER COLUMN image_height DROP NOT NULL;
ALTER TABLE plan_revisions ALTER COLUMN nw_lat DROP NOT NULL;
ALTER TABLE plan_revisions ALTER COLUMN nw_long DROP NOT NULL;
ALTER TABLE plan_revisions ALTER COLUMN se_lat DROP NOT NULL;
ALTER TABLE plan_revisions ALTER COLUMN se_long DROP NOT NULL;