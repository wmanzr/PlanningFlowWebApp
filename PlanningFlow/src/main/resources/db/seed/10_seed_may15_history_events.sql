-- История на 15 мая: 3 мероприятия (ACTIVE), 2–6 задач в каждом (все DONE),
-- назначения на зарегистрированных волонтёров, навыки, зависимости, бронирования, инциденты.
--
-- Требуется: users/roles, skills, resources (00_seed_skills, 00_seed_internal_resources,
-- 05_seed_participants или аналог с volonter003+).
--
-- Запуск:
--   docker exec -i postgres psql -U postgres -d plan_flow -v ON_ERROR_STOP=1 \
--     < PlanningFlow/src/main/resources/db/seed/10_seed_may15_history_events.sql

\set history_date '2026-05-15'

BEGIN;

-- --- идемпотентная очистка трёх мероприятий ---
DELETE FROM incidents i
USING events e
WHERE i.event_id = e.id
  AND e.title IN (
      'Майский фестиваль в Сокольниках (15.05)',
      'Арт-двор на Кузнецком мосту (15.05)',
      'Семейный пикник «Зелёная аллея» (15.05)'
  );

DELETE FROM resource_bookings rb
USING tasks t, events e
WHERE rb.task_id = t.id
  AND t.event_id = e.id
  AND e.title IN (
      'Майский фестиваль в Сокольниках (15.05)',
      'Арт-двор на Кузнецком мосту (15.05)',
      'Семейный пикник «Зелёная аллея» (15.05)'
  );

DELETE FROM assignments a
USING tasks t, events e
WHERE a.task_id = t.id
  AND t.event_id = e.id
  AND e.title IN (
      'Майский фестиваль в Сокольниках (15.05)',
      'Арт-двор на Кузнецком мосту (15.05)',
      'Семейный пикник «Зелёная аллея» (15.05)'
  );

DELETE FROM task_dependencies td
USING tasks t, events e
WHERE (td.task_id = t.id OR td.depends_on_task_id = t.id)
  AND t.event_id = e.id
  AND e.title IN (
      'Майский фестиваль в Сокольниках (15.05)',
      'Арт-двор на Кузнецком мосту (15.05)',
      'Семейный пикник «Зелёная аллея» (15.05)'
  );

DELETE FROM task_required_skills trs
USING tasks t, events e
WHERE trs.task_id = t.id
  AND t.event_id = e.id
  AND e.title IN (
      'Майский фестиваль в Сокольниках (15.05)',
      'Арт-двор на Кузнецком мосту (15.05)',
      'Семейный пикник «Зелёная аллея» (15.05)'
  );

DELETE FROM tasks t
USING events e
WHERE t.event_id = e.id
  AND e.title IN (
      'Майский фестиваль в Сокольниках (15.05)',
      'Арт-двор на Кузнецком мосту (15.05)',
      'Семейный пикник «Зелёная аллея» (15.05)'
  );

DELETE FROM event_coordinators ec
USING events e
WHERE ec.event_id = e.id
  AND e.title IN (
      'Майский фестиваль в Сокольниках (15.05)',
      'Арт-двор на Кузнецком мосту (15.05)',
      'Семейный пикник «Зелёная аллея» (15.05)'
  );

DELETE FROM events
WHERE title IN (
    'Майский фестиваль в Сокольниках (15.05)',
    'Арт-двор на Кузнецком мосту (15.05)',
    'Семейный пикник «Зелёная аллея» (15.05)'
);

-- --- мероприятия ---
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
    v.title,
    v.description,
    'ACTIVE',
    v.start_date::timestamp,
    v.end_date::timestamp,
    v.latitude,
    v.longitude,
    (
        SELECT u.id
        FROM users u
        JOIN users_roles ur ON ur.user_entity_id = u.id
        JOIN roles r ON r.id = ur.roles_id
        WHERE r.name IN ('COORDINATOR', 'ORGANIZER', 'ADMIN')
        ORDER BY CASE r.name WHEN 'COORDINATOR' THEN 1 WHEN 'ORGANIZER' THEN 2 ELSE 3 END, u.id
        LIMIT 1
    )
FROM (VALUES
    (
        'Майский фестиваль в Сокольниках (15.05)',
        'Архивное мероприятие 15 мая: парковая сцена, регистрация гостей, вечерний демонтаж. Все задачи выполнены.',
        (:'history_date'::date + TIME '08:00')::text,
        (:'history_date'::date + TIME '19:30')::text,
        55.794200,
        37.680400
    ),
    (
        'Арт-двор на Кузнецком мосту (15.05)',
        'Архивное мероприятие 15 мая: уличная экспозиция, мастер-классы, вечерний сбор стендов.',
        (:'history_date'::date + TIME '11:00')::text,
        (:'history_date'::date + TIME '21:00')::text,
        55.761400,
        37.624500
    ),
    (
        'Семейный пикник «Зелёная аллея» (15.05)',
        'Архивное мероприятие 15 мая: семейный формат, детская зона, барбекю, финальная уборка.',
        (:'history_date'::date + TIME '09:00')::text,
        (:'history_date'::date + TIME '18:00')::text,
        55.707800,
        37.586900
    )
) AS v(title, description, start_date, end_date, latitude, longitude);

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
WHERE e.title IN (
    'Майский фестиваль в Сокольниках (15.05)',
    'Арт-двор на Кузнецком мосту (15.05)',
    'Семейный пикник «Зелёная аллея» (15.05)'
);

-- --- задачи (все DONE) ---
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
    e.latitude + v.d_lat,
    e.longitude + v.d_lon,
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
FROM (VALUES
    -- Сокольники: 4 задачи (d_lat, d_lon — смещение от центра мероприятия)
    ('Майский фестиваль в Сокольниках (15.05)', 'Монтаж входной арки',           '2026-05-15 08:00', '2026-05-15 10:00',  0.00120,  0.00040),
    ('Майский фестиваль в Сокольниках (15.05)', 'Регистрация гостей у входа',    '2026-05-15 10:00', '2026-05-15 14:00',  0.00035,  0.00055),
    ('Майский фестиваль в Сокольниках (15.05)', 'Дежурство у главной сцены',     '2026-05-15 14:00', '2026-05-15 18:30', -0.00090,  0.00110),
    ('Майский фестиваль в Сокольниках (15.05)', 'Вечерний демонтаж площадки',    '2026-05-15 18:30', '2026-05-15 19:30', -0.00055, -0.00210),

    -- Кузнецкий: 3 задачи
    ('Арт-двор на Кузнецком мосту (15.05)', 'Оформление стендов во дворе',       '2026-05-15 11:00', '2026-05-15 13:30',  0.00045, -0.00035),
    ('Арт-двор на Кузнецком мосту (15.05)', 'Проведение мастер-классов',         '2026-05-15 13:30', '2026-05-15 17:00', -0.00070,  0.00095),
    ('Арт-двор на Кузнецком мосту (15.05)', 'Сбор экспозиции и упаковка',        '2026-05-15 19:00', '2026-05-15 21:00',  0.00160, -0.00185),

    -- Пикник: 5 задач
    ('Семейный пикник «Зелёная аллея» (15.05)', 'Разметка площадки и навесов',   '2026-05-15 09:00', '2026-05-15 10:30',  0.00050,  0.00030),
    ('Семейный пикник «Зелёная аллея» (15.05)', 'Пункт выдачи пледов и ковриков', '2026-05-15 10:00', '2026-05-15 14:00', -0.00035,  0.00115),
    ('Семейный пикник «Зелёная аллея» (15.05)', 'Детская анимационная зона',     '2026-05-15 11:00', '2026-05-15 16:00',  0.00105, -0.00105),
    ('Семейный пикник «Зелёная аллея» (15.05)', 'Барбекю-зона и раздача напитков', '2026-05-15 12:00', '2026-05-15 17:00', -0.00085, -0.00060),
    ('Семейный пикник «Зелёная аллея» (15.05)', 'Уборка территории после пикника', '2026-05-15 16:30', '2026-05-15 18:00',  0.00185,  0.00205)
) AS v(event_title, task_title, start_time, end_time, d_lat, d_lon)
JOIN events e ON e.title = v.event_title
WHERE e.start_date::date = :'history_date'::date;

-- --- навыки ---
INSERT INTO task_required_skills (task_id, skill_id)
SELECT t.id, s.id
FROM tasks t
JOIN events e ON e.id = t.event_id
JOIN (VALUES
    ('Монтаж входной арки', 'Монтажник'),
    ('Монтаж входной арки', 'Разнорабочий'),
    ('Регистрация гостей у входа', 'Регистратор'),
    ('Регистрация гостей у входа', 'Хостес'),
    ('Дежурство у главной сцены', 'Контроллер'),
    ('Дежурство у главной сцены', 'Дежурный'),
    ('Вечерний демонтаж площадки', 'Монтажник'),
    ('Вечерний демонтаж площадки', 'Грузчик'),

    ('Оформление стендов во дворе', 'Декоратор'),
    ('Оформление стендов во дворе', 'Художник'),
    ('Проведение мастер-классов', 'Ведущий'),
    ('Проведение мастер-классов', 'Аниматор'),
    ('Сбор экспозиции и упаковка', 'Грузчик'),
    ('Сбор экспозиции и упаковка', 'Разнорабочий'),

    ('Разметка площадки и навесов', 'Разнорабочий'),
    ('Разметка площадки и навесов', 'Монтажник'),
    ('Пункт выдачи пледов и ковриков', 'Регистратор'),
    ('Пункт выдачи пледов и ковриков', 'Хостес'),
    ('Детская анимационная зона', 'Аниматор'),
    ('Детская анимационная зона', 'Ведущий'),
    ('Барбекю-зона и раздача напитков', 'Повар'),
    ('Барбекю-зона и раздача напитков', 'Официант'),
    ('Уборка территории после пикника', 'Разнорабочий'),
    ('Уборка территории после пикника', 'Посудомойщик')
) AS v(task_title, skill_name) ON t.title = v.task_title
JOIN skills s ON s.name = v.skill_name
WHERE e.start_date::date = :'history_date'::date
  AND e.title LIKE '%(15.05)';

-- --- зависимости ---
INSERT INTO task_dependencies (task_id, depends_on_task_id)
SELECT child.id, parent.id
FROM events e
JOIN tasks parent ON parent.event_id = e.id
JOIN tasks child  ON child.event_id  = e.id
WHERE e.start_date::date = :'history_date'::date
  AND e.title LIKE '%(15.05)'
  AND (
      (e.title = 'Майский фестиваль в Сокольниках (15.05)'
       AND parent.title = 'Монтаж входной арки'
       AND child.title  = 'Регистрация гостей у входа')
      OR
      (e.title = 'Майский фестиваль в Сокольниках (15.05)'
       AND parent.title = 'Дежурство у главной сцены'
       AND child.title  = 'Вечерний демонтаж площадки')
      OR
      (e.title = 'Арт-двор на Кузнецком мосту (15.05)'
       AND parent.title = 'Оформление стендов во дворе'
       AND child.title  = 'Проведение мастер-классов')
      OR
      (e.title = 'Семейный пикник «Зелёная аллея» (15.05)'
       AND parent.title = 'Барбекю-зона и раздача напитков'
       AND child.title  = 'Уборка территории после пикника')
  );

-- --- назначения (волонтёры с подходящим навыком, без пересечений в этот день) ---
DO $$
DECLARE
    r_task RECORD;
    v_user_id INT;
    v_gap INTERVAL := INTERVAL '15 minutes';
BEGIN
    FOR r_task IN
        SELECT t.id, t.start_time, t.end_time
        FROM tasks t
        JOIN events e ON e.id = t.event_id
        WHERE e.start_date::date = DATE '2026-05-15'
          AND e.title LIKE '%(15.05)'
        ORDER BY t.start_time, t.id
    LOOP
        SELECT u.id
        INTO v_user_id
        FROM users u
        WHERE u.username ~ '^volonter[0-9]{3}$'
          AND EXISTS (
              SELECT 1
              FROM user_skills us
              JOIN task_required_skills trs
                  ON trs.skill_id = us.skill_id AND trs.task_id = r_task.id
              WHERE us.user_id = u.id
          )
          AND NOT EXISTS (
              SELECT 1
              FROM assignments a
              JOIN tasks t2 ON t2.id = a.task_id
              WHERE a.user_id = u.id
                AND a.status IN ('PENDING', 'ACCEPTED')
                AND t2.start_time < r_task.end_time + v_gap
                AND t2.end_time > r_task.start_time - v_gap
          )
        ORDER BY u.id
        LIMIT 1;

        IF v_user_id IS NOT NULL THEN
            INSERT INTO assignments (task_id, user_id, status, assigned_at, responded_at)
            VALUES (
                r_task.id,
                v_user_id,
                'ACCEPTED',
                r_task.start_time - INTERVAL '3 days',
                r_task.start_time - INTERVAL '3 days' + INTERVAL '4 hours'
            );
        END IF;
    END LOOP;
END $$;

-- --- бронирования ресурсов ---
INSERT INTO resource_bookings (task_id, resource_id, status, reserved_from, reserved_to)
SELECT
    t.id,
    r.id,
    'CONFIRMED',
    GREATEST(t.start_time, e.start_date),
    LEAST(t.end_time, e.end_date)
FROM (VALUES
    ('Монтаж входной арки', 'INV-2026-043'),
    ('Монтаж входной арки', 'INV-2026-044'),
    ('Регистрация гостей у входа', 'INV-2026-058'),
    ('Дежурство у главной сцены', 'INV-2026-020'),
    ('Вечерний демонтаж площадки', 'INV-2026-045'),

    ('Оформление стендов во дворе', 'INV-2026-035'),
    ('Оформление стендов во дворе', 'INV-2026-032'),
    ('Проведение мастер-классов', 'INV-2026-049'),
    ('Сбор экспозиции и упаковка', 'INV-2026-050'),

    ('Разметка площадки и навесов', 'INV-2026-046'),
    ('Разметка площадки и навесов', 'INV-2026-018'),
    ('Пункт выдачи пледов и ковриков', 'INV-2026-057'),
    ('Детская анимационная зона', 'INV-2026-048'),
    ('Барбекю-зона и раздача напитков', 'INV-2026-025'),
    ('Барбекю-зона и раздача напитков', 'INV-2026-026'),
    ('Уборка территории после пикника', 'INV-2026-051')
) AS v(task_title, inventory_number)
JOIN tasks t ON t.title = v.task_title
JOIN events e ON e.id = t.event_id AND e.start_date::date = :'history_date'::date
JOIN resources r
    ON r.inventory_number = v.inventory_number
   AND r.source_type = 'INTERNAL'
   AND r.operational = TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM resource_bookings rb2
    WHERE rb2.resource_id = r.id
      AND rb2.status IN ('REQUESTED', 'CONFIRMED')
      AND rb2.reserved_from < LEAST(t.end_time, e.end_date)
      AND rb2.reserved_to > GREATEST(t.start_time, e.start_date)
      AND rb2.task_id <> t.id
);

-- --- инциденты ---
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
        'Майский фестиваль в Сокольниках (15.05)',
        'Регистрация гостей у входа',
        'INV-2026-058',
        'volonter012',
        'Планшет регистрации завис на синхронизации списка гостей.',
        'MEDIUM',
        'RESOLVED',
        '2026-05-15 10:45:00',
        '2026-05-15 11:10:00',
        'Перезапустили приложение, переключились на офлайн-список.'
    ),
    (
        'Майский фестиваль в Сокольниках (15.05)',
        'Дежурство у главной сцены',
        NULL,
        'volonter034',
        'Скопление гостей у барьера перед вечерним блоком.',
        'LOW',
        'RESOLVED',
        '2026-05-15 17:20:00',
        '2026-05-15 17:50:00',
        'Добавили разметку прохода и дежурного у турникета.'
    ),
    (
        'Арт-двор на Кузнецком мосту (15.05)',
        'Проведение мастер-классов',
        NULL,
        'volonter045',
        'Нехватка стульев в зоне мастер-класса.',
        'LOW',
        'RESOLVED',
        '2026-05-15 14:30:00',
        '2026-05-15 15:00:00',
        'Переставили 12 стульев из соседнего двора.'
    ),
    (
        'Семейный пикник «Зелёная аллея» (15.05)',
        'Барбекю-зона и раздача напитков',
        'INV-2026-025',
        'volonter067',
        'Закончился уголь для мангалов раньше графика.',
        'MEDIUM',
        'RESOLVED',
        '2026-05-15 14:10:00',
        '2026-05-15 14:40:00',
        'Доставили запас угля со склада, очередь разобрана.'
    ),
    (
        'Семейный пикник «Зелёная аллея» (15.05)',
        'Детская анимационная зона',
        NULL,
        'volonter028',
        'Аниматор задержался на 25 минут из-за пробок.',
        'LOW',
        'RESOLVED',
        '2026-05-15 11:25:00',
        '2026-05-15 12:00:00',
        'Временно усилили зону вторым волонтёром из пункта выдачи.'
    )
) AS v(
    event_title,
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
JOIN events e ON e.title = v.event_title AND e.start_date::date = :'history_date'::date
JOIN tasks t ON t.event_id = e.id AND t.title = v.task_title
LEFT JOIN resources r
    ON r.inventory_number = v.inventory_number
   AND r.source_type = 'INTERNAL'
   AND r.operational = TRUE
JOIN users u ON u.username = v.reporter_username;

COMMIT;

-- --- сводка ---
SELECT e.id, e.title, e.status, e.start_date::date AS day,
       COUNT(t.id) AS tasks,
       COUNT(*) FILTER (WHERE t.status = 'DONE') AS tasks_done
FROM events e
LEFT JOIN tasks t ON t.event_id = e.id
WHERE e.start_date::date = :'history_date'::date
  AND e.title LIKE '%(15.05)'
GROUP BY e.id, e.title, e.status, e.start_date
ORDER BY e.id;

SELECT t.id, e.title AS event_title, t.title AS task_title, t.status,
       (SELECT COUNT(*) FROM assignments a WHERE a.task_id = t.id) AS assignments,
       (SELECT COUNT(*) FROM resource_bookings rb WHERE rb.task_id = t.id) AS bookings
FROM tasks t
JOIN events e ON e.id = t.event_id
WHERE e.start_date::date = :'history_date'::date
  AND e.title LIKE '%(15.05)'
ORDER BY e.id, t.start_time;
