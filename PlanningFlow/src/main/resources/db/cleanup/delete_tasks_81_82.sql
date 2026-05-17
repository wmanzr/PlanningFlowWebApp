-- Удаление задач id 81, 82 («проверка 1», «проверка 2»).
BEGIN;

DELETE FROM assignments WHERE task_id IN (81, 82);
DELETE FROM resource_bookings WHERE task_id IN (81, 82);
DELETE FROM task_dependencies
WHERE task_id IN (81, 82) OR depends_on_task_id IN (81, 82);
DELETE FROM task_required_skills WHERE task_id IN (81, 82);
DELETE FROM incidents WHERE task_id IN (81, 82);
DELETE FROM tasks WHERE id IN (81, 82);

COMMIT;

SELECT id, title FROM tasks WHERE id IN (81, 82);
