CREATE TABLE task_count (
    username text NOT NULL,
    plan_id uuid NOT NULL,
    open_count int NOT NULL,
    done_count int NOT NULL,
    last_modified timestamp,
    CONSTRAINT task_count_pk PRIMARY KEY(username,plan_id)
);