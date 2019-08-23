CREATE TABLE project_folders (
    id uuid PRIMARY KEY,
    name text NOT NULL,
    organisation_id uuid NOT NULL,
    parent_id uuid
);

CREATE TABLE organisation_root_folder (
    id uuid PRIMARY KEY,
    folder_id uuid NOT NULL REFERENCES project_folders(id)
);

CREATE TABLE projects (
    id uuid PRIMARY KEY,
    organisation_id uuid NOT NULL,
    active boolean NOT NULL DEFAULT True,
    name text NOT NULL,
    initials text NOT NULL,
    description text NOT NULL,
    duration_start timestamp,
    duration_end timestamp,
    street_address text NOT NULL,
    city text NOT NULL,
    zip_code text NOT NULL,
    cc varchar(2) NOT NULL,
    logo_storage_id uuid,
    logo_media_type text,
    logo_size bigint,
    construction_type text NOT NULL,
    client_name text NOT NULL,
    client_assistant_name text NOT NULL,
    parent_id uuid NOT NULL REFERENCES project_folders(id),
    root_folder_id uuid,
    created timestamp NOT NULL,
    created_by uuid NOT NULL,
    last_modified timestamp,
    last_modified_by uuid
);

CREATE TABLE scale_definitions (
    id uuid PRIMARY KEY,
    name text NOT NULL,
    one_star_label text NOT NULL,
    two_star_label text NOT NULL,
    three_star_label text NOT NULL,
    four_star_label text NOT NULL,
    five_star_label text NOT NULL,
    active boolean NOT NULL DEFAULT True,
    costs boolean NOT NULL DEFAULT False,
    priority boolean NOT NULL DEFAULT False,
    project_id uuid NOT NULL REFERENCES projects(id)
);

CREATE TABLE plan_folders (
    id uuid PRIMARY KEY,
    name text NOT NULL,
    project_id uuid NOT NULL REFERENCES projects(id),
    parent_id uuid REFERENCES plan_folders(id)
);

ALTER TABLE projects ADD CONSTRAINT projects_plan_root_folder_fk FOREIGN KEY (root_folder_id) REFERENCES plan_folders(id);

CREATE TABLE plans(
    id uuid PRIMARY KEY,
    name text NOT NULL,
    project_id uuid NOT NULL REFERENCES projects(id),
    parent_id uuid NOT NULL REFERENCES plan_folders(id),
    current_revision_id uuid NOT NULL,
    active boolean NOT NULL,
    created timestamp NOT NULL,
    created_by uuid NOT NULL,
    last_modified timestamp,
    last_modified_by uuid
);

CREATE TABLE plan_revisions(
    id uuid PRIMARY KEY,
    plan_id uuid NOT NULL REFERENCES plans(id),
    version text NOT NULL,
    type text NOT NULL,
    file_name text,
    content_type text,
    image_width int NOT NULL,
    image_height int NOT NULL,
    nw_lat text NOT NULL,
    nw_long text NOT NULL,
    se_lat text NOT NULL,
    se_long text NOT NULL,
    created timestamp NOT NULL,
    created_by uuid NOT NULL,
    last_modified timestamp,
    last_modified_by uuid
);

ALTER TABLE plans ADD CONSTRAINT plans_current_revision_fk FOREIGN KEY (current_revision_id) REFERENCES plan_revisions(id);

CREATE TABLE plan_levels (
    id uuid PRIMARY KEY,
    tiles_x int NOT NULL,
    tiles_y int NOT NULL,
    revision_id uuid NOT NULL REFERENCES plan_revisions(id)
);