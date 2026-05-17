-- Сдвиг всех операционных дат в БД на новый тестовый день (время суток сохраняется).
-- Не трогает: users.birth_date, refresh_tokens.expires_at
--
-- Перед запуском поменяйте даты:
\set old_demo_date '2026-05-20'
\set new_demo_date '2026-05-17'
--
-- docker exec -i postgres psql -U postgres -d plan_flow -v ON_ERROR_STOP=1 \
--   < PlanningFlow/src/main/resources/db/cleanup/shift_demo_dates.sql

BEGIN;

UPDATE events
SET start_date = start_date + (:'new_demo_date'::date - :'old_demo_date'::date) * INTERVAL '1 day',
    end_date   = end_date   + (:'new_demo_date'::date - :'old_demo_date'::date) * INTERVAL '1 day';

UPDATE tasks
SET start_time = start_time + (:'new_demo_date'::date - :'old_demo_date'::date) * INTERVAL '1 day',
    end_time   = end_time   + (:'new_demo_date'::date - :'old_demo_date'::date) * INTERVAL '1 day';

UPDATE assignments
SET assigned_at  = assigned_at  + (:'new_demo_date'::date - :'old_demo_date'::date) * INTERVAL '1 day',
    responded_at = responded_at + (:'new_demo_date'::date - :'old_demo_date'::date) * INTERVAL '1 day'
WHERE assigned_at IS NOT NULL;

UPDATE resource_bookings
SET reserved_from = reserved_from + (:'new_demo_date'::date - :'old_demo_date'::date) * INTERVAL '1 day',
    reserved_to   = reserved_to   + (:'new_demo_date'::date - :'old_demo_date'::date) * INTERVAL '1 day';

UPDATE incidents
SET created_at  = created_at  + (:'new_demo_date'::date - :'old_demo_date'::date) * INTERVAL '1 day',
    resolved_at = resolved_at + (:'new_demo_date'::date - :'old_demo_date'::date) * INTERVAL '1 day'
WHERE created_at IS NOT NULL;

UPDATE event_ai_postmortem_reports
SET created_at = created_at + (:'new_demo_date'::date - :'old_demo_date'::date) * INTERVAL '1 day',
    updated_at = updated_at + (:'new_demo_date'::date - :'old_demo_date'::date) * INTERVAL '1 day';

UPDATE notifications
SET created_at = created_at + (:'new_demo_date'::date - :'old_demo_date'::date) * INTERVAL '1 day',
    read_at    = read_at    + (:'new_demo_date'::date - :'old_demo_date'::date) * INTERVAL '1 day'
WHERE created_at IS NOT NULL;

COMMIT;

SELECT (:'new_demo_date'::date - :'old_demo_date'::date) AS shift_days;

SELECT start_date::date AS event_day, COUNT(*) AS events
FROM events
GROUP BY 1
ORDER BY 1;

SELECT e.title, e.start_date::date, e.end_date::date
FROM events e
ORDER BY e.start_date
LIMIT 15;

SELECT MIN(t.start_time)::date AS min_task_day,
       MAX(t.end_time)::date   AS max_task_day,
       COUNT(*)                AS tasks
FROM tasks t;
