CREATE TABLE project_folder_restrictions(
    user_id uuid PRIMARY KEY,
    folder_id uuid NOT NULL REFERENCES project_folders(id)
);