-- Шаг 8: бронирования только INTERNAL (operational), интервал = время задачи.
\set demo_date '2026-05-17'

BEGIN;

DELETE FROM resource_bookings rb
USING tasks t, events e
WHERE rb.task_id = t.id
  AND t.event_id = e.id
  AND e.start_date::date = :'demo_date'::date;

INSERT INTO resource_bookings (task_id, resource_id, status, reserved_from, reserved_to)
SELECT
    t.id,
    r.id,
    v.status,
    GREATEST(t.start_time, e.start_date),
    LEAST(t.end_time, e.end_date)
FROM (VALUES
    ('Монтаж ограждений у главного входа', 'INV-2026-043', 'CONFIRMED'),
    ('Монтаж ограждений у главного входа', 'INV-2026-044', 'CONFIRMED'),
    ('Контроль электропитания павильонов', 'INV-2026-011', 'CONFIRMED'),
    ('Вечерний демонтаж декораций', 'INV-2026-045', 'CONFIRMED'),

    ('Регистрация участников в фойе', 'INV-2026-058', 'CONFIRMED'),
    ('Техподдержка проектора в зале A', 'INV-2026-041', 'CONFIRMED'),
    ('Техподдержка проектора в зале A', 'INV-2026-042', 'CONFIRMED'),
    ('Кофе-брейк — зал B', 'INV-2026-030', 'CONFIRMED'),
    ('Кофе-брейк — зал B', 'INV-2026-031', 'CONFIRMED'),
    ('Разбор стендов и баннеров', 'INV-2026-032', 'CONFIRMED'),

    ('Разметка стартового коридора', 'INV-2026-046', 'CONFIRMED'),
    ('Разметка стартового коридора', 'INV-2026-054', 'CONFIRMED'),
    ('Выдача стартовых номеров', 'INV-2026-057', 'CONFIRMED'),
    ('Пункт питания км 5', 'INV-2026-025', 'CONFIRMED'),
    ('Пункт питания км 5', 'INV-2026-026', 'CONFIRMED'),
    ('Медицинский пост у моста', 'INV-2026-052', 'CONFIRMED'),
    ('Сбор мобильных ограждений', 'INV-2026-034', 'CONFIRMED'),

    ('Монтаж сценического света', 'INV-2026-038', 'CONFIRMED'),
    ('Монтаж сценического света', 'INV-2026-039', 'CONFIRMED'),
    ('Проверка звука перед шоу', 'INV-2026-020', 'CONFIRMED'),
    ('Проверка звука перед шоу', 'INV-2026-040', 'CONFIRMED'),
    ('Демонтаж оборудования после концерта', 'INV-2026-021', 'CONFIRMED'),

    ('Подключение павильона №12', 'INV-2026-012', 'CONFIRMED'),
    ('Контроль складской зоны', 'INV-2026-018', 'CONFIRMED'),
    ('Вечерняя уборка двора', 'INV-2026-051', 'CONFIRMED'),

    ('Установка дополнительных сеток', 'INV-2026-047', 'CONFIRMED'),
    ('Медицинское дежурство', 'INV-2026-052', 'CONFIRMED'),
    ('Сбор инвентаря', 'INV-2026-050', 'CONFIRMED'),

    ('Оформление стартового городка', 'INV-2026-048', 'CONFIRMED'),
    ('Фотозона у финиша', 'INV-2026-049', 'CONFIRMED'),
    ('Раздача воды на финише', 'INV-2026-022', 'CONFIRMED'),
    ('Раздача воды на финише', 'INV-2026-023', 'CONFIRMED'),

    ('Монтаж декораций сцены', 'INV-2026-035', 'CONFIRMED'),
    ('Монтаж декораций сцены', 'INV-2026-032', 'CONFIRMED'),
    ('Вечерний прогон света', 'INV-2026-039', 'CONFIRMED'),
    ('Ночная смена дежурной бригады', 'INV-2026-038', 'CONFIRMED')
) AS v(task_title, inventory_number, status)
JOIN tasks t ON t.title = v.task_title
JOIN events e ON e.id = t.event_id AND e.start_date::date = :'demo_date'::date
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
);

COMMIT;

SELECT COUNT(*) AS bookings
FROM resource_bookings rb
JOIN tasks t ON t.id = rb.task_id
JOIN events e ON e.id = t.event_id
WHERE e.start_date::date = :'demo_date'::date;

SELECT r.source_type, rb.status, COUNT(*) AS cnt
FROM resource_bookings rb
JOIN resources r ON r.id = rb.resource_id
JOIN tasks t ON t.id = rb.task_id
JOIN events e ON e.id = t.event_id
WHERE e.start_date::date = :'demo_date'::date
GROUP BY r.source_type, rb.status
ORDER BY 1, 2;
