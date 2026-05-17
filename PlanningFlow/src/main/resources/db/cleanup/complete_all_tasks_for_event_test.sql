-- Тест «Завершить мероприятие» + отчёт ИИ: все задачи → DONE, мероприятие → ACTIVE.
-- По умолчанию: «Благотворительный забег «Километр добра»».

\set event_title 'Благотворительный забег «Километр добра»'

BEGIN;

INSERT INTO assignments (task_id, user_id, status, assigned_at, responded_at)
SELECT
    t.id,
    u.id,
    'ACCEPTED',
    t.start_time,
    t.start_time + INTERVAL '5 minutes'
FROM tasks t
JOIN events e ON e.id = t.event_id
CROSS JOIN LATERAL (
    SELECT u2.id
    FROM users u2
    JOIN users_roles ur ON ur.user_entity_id = u2.id
    JOIN roles r ON r.id = ur.roles_id
    WHERE r.name = 'VOLUNTEER'
    ORDER BY u2.id
    LIMIT 1
) u
WHERE e.title = :'event_title'
  AND t.status = 'OPEN'
  AND NOT EXISTS (SELECT 1 FROM assignments a WHERE a.task_id = t.id);

UPDATE tasks t
SET status = 'ASSIGNED'
FROM events e
WHERE t.event_id = e.id
  AND e.title = :'event_title'
  AND t.status = 'OPEN'
  AND EXISTS (SELECT 1 FROM assignments a WHERE a.task_id = t.id);

UPDATE tasks t
SET status = 'DONE'
FROM events e
WHERE t.event_id = e.id
  AND e.title = :'event_title'
  AND t.status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS');

UPDATE events
SET status = 'ACTIVE'
WHERE title = :'event_title'
  AND status IN ('DRAFT', 'PLANNING');

COMMIT;

SELECT
    e.id,
    e.title,
    e.status AS event_status,
    COUNT(t.id) AS tasks_total,
    COUNT(*) FILTER (WHERE t.status = 'DONE') AS tasks_done,
    COUNT(*) FILTER (WHERE t.status NOT IN ('DONE', 'CANCELLED')) AS blocking_tasks
FROM events e
JOIN tasks t ON t.event_id = e.id
WHERE e.title = :'event_title'
GROUP BY e.id, e.title, e.status;
