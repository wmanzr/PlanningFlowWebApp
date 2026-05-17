-- Демо-мероприятие для теста «Завершить» + post-mortem ИИ:
-- 3 утренние задачи (все end_time < 12:00), статус DONE, назначения, ресурсы, 1 решённый инцидент.
--
-- Запуск:
--   docker exec -i postgres psql -U postgres -d plan_flow -v ON_ERROR_STOP=1 \
--     < PlanningFlow/src/main/resources/db/seed/09_seed_morning_report_demo_event.sql

\set demo_date '2026-05-17'
\set event_title 'Утренний штаб «Сезон на Манежной»'

BEGIN;

DELETE FROM incidents i
USING events e
WHERE i.event_id = e.id AND e.title = :'event_title';

DELETE FROM resource_bookings rb
USING tasks t, events e
WHERE rb.task_id = t.id AND t.event_id = e.id AND e.title = :'event_title';

DELETE FROM assignments a
USING tasks t, events e
WHERE a.task_id = t.id AND t.event_id = e.id AND e.title = :'event_title';

DELETE FROM task_dependencies td
USING tasks t, events e
WHERE (td.task_id = t.id OR td.depends_on_task_id = t.id)
  AND t.event_id = e.id AND e.title = :'event_title';

DELETE FROM task_required_skills trs
USING tasks t, events e
WHERE trs.task_id = t.id AND t.event_id = e.id AND e.title = :'event_title';

DELETE FROM tasks t
USING events e
WHERE t.event_id = e.id AND e.title = :'event_title';

DELETE FROM event_coordinators ec
USING events e
WHERE ec.event_id = e.id AND e.title = :'event_title';

DELETE FROM events WHERE title = :'event_title';

INSERT INTO events (
    title,
    description,
    status,
    start_date,
    end_date,
    latitude,
    longitude,
    creator_id
)
SELECT
    :'event_title',
    'Короткий утренний цикл перед открытием сезона: монтаж пресс-зоны у Манежа, аккредитация СМИ и гостей, организационный кофе-брейк для штаба.',
    'ACTIVE',
    (:'demo_date'::date + TIME '08:00')::timestamp,
    (:'demo_date'::date + TIME '12:00')::timestamp,
    55.752200,
    37.615600,
    (
        SELECT u.id
        FROM users u
        JOIN users_roles ur ON ur.user_entity_id = u.id
        JOIN roles r ON r.id = ur.roles_id
        WHERE r.name IN ('COORDINATOR', 'ORGANIZER', 'ADMIN')
        ORDER BY CASE r.name WHEN 'COORDINATOR' THEN 1 WHEN 'ORGANIZER' THEN 2 ELSE 3 END, u.id
        LIMIT 1
    );

INSERT INTO event_coordinators (event_id, user_id)
SELECT e.id, c.id
FROM events e
CROSS JOIN LATERAL (
    SELECT u.id
    FROM users u
    JOIN users_roles ur ON ur.user_entity_id = u.id
    JOIN roles r ON r.id = ur.roles_id
    WHERE r.name = 'COORDINATOR'
    ORDER BY u.id
    LIMIT 1
) c
WHERE e.title = :'event_title'
  AND e.start_date::date = :'demo_date'::date;

INSERT INTO tasks (
    title,
    status,
    start_time,
    end_time,
    latitude,
    longitude,
    created_by_user_id,
    event_id
)
SELECT
    v.task_title,
    'DONE',
    v.start_time::timestamp,
    v.end_time::timestamp,
    e.latitude,
    e.longitude,
    (
        SELECT u.id
        FROM users u
        JOIN users_roles ur ON ur.user_entity_id = u.id
        JOIN roles r ON r.id = ur.roles_id
        WHERE r.name = 'COORDINATOR'
        ORDER BY u.id
        LIMIT 1
    ),
    e.id
FROM events e
JOIN (VALUES
    ('Монтаж пресс-трибуны и баннера', '2026-05-17 08:00', '2026-05-17 09:30'),
    ('Аккредитация прессы и гостей',   '2026-05-17 09:00', '2026-05-17 11:00'),
    ('Кофе-брейк для оргштаба',        '2026-05-17 10:30', '2026-05-17 11:45')
) AS v(task_title, start_time, end_time) ON TRUE
WHERE e.title = :'event_title'
  AND e.start_date::date = :'demo_date'::date;

INSERT INTO task_required_skills (task_id, skill_id)
SELECT t.id, s.id
FROM tasks t
JOIN events e ON e.id = t.event_id
JOIN (VALUES
    ('Монтаж пресс-трибуны и баннера', 'Монтажник'),
    ('Монтаж пресс-трибуны и баннера', 'Разнорабочий'),
    ('Аккредитация прессы и гостей', 'Регистратор'),
    ('Аккредитация прессы и гостей', 'Хостес'),
    ('Кофе-брейк для оргштаба', 'Официант'),
    ('Кофе-брейк для оргштаба', 'Бариста')
) AS v(task_title, skill_name) ON t.title = v.task_title
JOIN skills s ON s.name = v.skill_name
WHERE e.title = :'event_title';

INSERT INTO task_dependencies (task_id, depends_on_task_id)
SELECT child.id, parent.id
FROM events e
JOIN tasks parent ON parent.event_id = e.id AND parent.title = 'Монтаж пресс-трибуны и баннера'
JOIN tasks child  ON child.event_id  = e.id AND child.title  = 'Аккредитация прессы и гостей'
WHERE e.title = :'event_title';

INSERT INTO assignments (task_id, user_id, status, assigned_at, responded_at)
SELECT
    t.id,
    pick.user_id,
    'ACCEPTED',
    t.start_time - INTERVAL '1 day',
    t.start_time - INTERVAL '1 day' + INTERVAL '2 hours'
FROM tasks t
JOIN events e ON e.id = t.event_id
CROSS JOIN LATERAL (
    SELECT u.id AS user_id
    FROM users u
    WHERE u.username ~ '^volonter[0-9]{3}$'
      AND EXISTS (
          SELECT 1
          FROM user_skills us
          JOIN task_required_skills trs ON trs.skill_id = us.skill_id AND trs.task_id = t.id
          WHERE us.user_id = u.id
      )
      AND NOT EXISTS (
          SELECT 1
          FROM assignments a2
          JOIN tasks t2 ON t2.id = a2.task_id
          WHERE a2.user_id = u.id
            AND a2.status IN ('PENDING', 'ACCEPTED')
            AND t2.id <> t.id
            AND t2.start_time < t.end_time + INTERVAL '15 minutes'
            AND t2.end_time > t.start_time - INTERVAL '15 minutes'
      )
    ORDER BY u.id
    LIMIT 1
) pick
WHERE e.title = :'event_title';

INSERT INTO resource_bookings (task_id, resource_id, status, reserved_from, reserved_to)
SELECT
    t.id,
    r.id,
    'CONFIRMED',
    t.start_time,
    t.end_time
FROM (VALUES
    ('Монтаж пресс-трибуны и баннера', 'INV-2026-035'),
    ('Монтаж пресс-трибуны и баннера', 'INV-2026-032'),
    ('Аккредитация прессы и гостей',   'INV-2026-040'),
    ('Кофе-брейк для оргштаба',        'INV-2026-020'),
    ('Кофе-брейк для оргштаба',        'INV-2026-021')
) AS v(task_title, inventory_number)
JOIN tasks t ON t.title = v.task_title
JOIN events e ON e.id = t.event_id AND e.title = :'event_title'
JOIN resources r
    ON r.inventory_number = v.inventory_number
   AND r.source_type = 'INTERNAL'
   AND r.operational = TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM resource_bookings rb2
    WHERE rb2.resource_id = r.id
      AND rb2.status IN ('REQUESTED', 'CONFIRMED')
      AND rb2.reserved_from < t.end_time
      AND rb2.reserved_to > t.start_time
      AND rb2.task_id <> t.id
);

INSERT INTO incidents (
    description,
    severity,
    status,
    created_at,
    resolved_at,
    resolution_notes,
    event_id,
    task_id,
    resource_id,
    reporter_id
)
SELECT
    v.description,
    v.severity,
    v.status,
    v.created_at::timestamp,
    v.resolved_at::timestamp,
    v.resolution_notes,
    e.id,
    t.id,
    r.id,
    u.id
FROM (VALUES
    (
        'Аккредитация прессы и гостей',
        'INV-2026-040',
        'volonter019',
        'На столе аккредитации не срабатывает сканер QR-кодов — гости стоят в очереди более 5 минут.',
        'MEDIUM',
        'RESOLVED',
        '2026-05-17 09:40:00',
        '2026-05-17 10:05:00',
        'Перезагрузили терминал, обновили драйвер считывателя, дублировали список гостей на бумажный бэкап. Очередь разобрана за 12 минут.'
    )
) AS v(
    task_title,
    inventory_number,
    reporter_username,
    description,
    severity,
    status,
    created_at,
    resolved_at,
    resolution_notes
)
JOIN events e ON e.title = :'event_title' AND e.start_date::date = :'demo_date'::date
JOIN tasks t ON t.event_id = e.id AND t.title = v.task_title
JOIN resources r
    ON r.inventory_number = v.inventory_number
   AND r.source_type = 'INTERNAL'
   AND r.operational = TRUE
JOIN users u ON u.username = v.reporter_username;

COMMIT;

SELECT e.id, e.title, e.status, e.start_date, e.end_date
FROM events e
WHERE e.title = :'event_title';

SELECT t.id, t.title, t.status, t.start_time, t.end_time
FROM tasks t
JOIN events e ON e.id = t.event_id
WHERE e.title = :'event_title'
ORDER BY t.start_time;
