-- Полная очистка операционных данных (мероприятия, задачи, назначения, инциденты…).
-- НЕ удаляет: users, roles, skills, user_skills, resources (склад).
-- После wipe: заново 01–09 seed + при необходимости 00_seed_internal_resources.
--
-- docker exec -i postgres psql -U postgres -d plan_flow -v ON_ERROR_STOP=1 \
--   < PlanningFlow/src/main/resources/db/cleanup/wipe_operational_data.sql

BEGIN;

DELETE FROM notifications;
DELETE FROM refresh_tokens;
DELETE FROM resource_bookings;
DELETE FROM assignments;
DELETE FROM incidents;
DELETE FROM event_ai_postmortem_reports;
DELETE FROM task_dependencies;
DELETE FROM task_required_skills;
DELETE FROM tasks;
DELETE FROM event_coordinators;
DELETE FROM events;

COMMIT;

SELECT 'notifications' AS t, COUNT(*) FROM notifications
UNION ALL SELECT 'refresh_tokens', COUNT(*) FROM refresh_tokens
UNION ALL SELECT 'resource_bookings', COUNT(*) FROM resource_bookings
UNION ALL SELECT 'assignments', COUNT(*) FROM assignments
UNION ALL SELECT 'incidents', COUNT(*) FROM incidents
UNION ALL SELECT 'event_ai_postmortem_reports', COUNT(*) FROM event_ai_postmortem_reports
UNION ALL SELECT 'task_dependencies', COUNT(*) FROM task_dependencies
UNION ALL SELECT 'task_required_skills', COUNT(*) FROM task_required_skills
UNION ALL SELECT 'tasks', COUNT(*) FROM tasks
UNION ALL SELECT 'event_coordinators', COUNT(*) FROM event_coordinators
UNION ALL SELECT 'events', COUNT(*) FROM events;
