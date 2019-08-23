alter table projects
add column bimplus_id uuid,
add column bimplus_project_name text,
add column bimplus_project_state text not null default 'NOT_LINKED';

alter table plans
add column bimplus_attachment_id uuid,
add column bimplus_attachment_name text,
add column bimplus_revision int DEFAULT 0,
add column bimplus_plan_state text not null default 'NOT_LINKED',
add column bimplus_error_message text;