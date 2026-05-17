-- Шаг 7: инциденты — 5 из 8 мероприятий (по 1–3), 3 без инцидентов.
-- Без инцидентов: концерт, волейбол, открытие сцены Воробьёвы горы.
\set demo_date '2026-05-17'

BEGIN;

DELETE FROM incidents i
USING events e
WHERE i.event_id = e.id
  AND e.start_date::date = :'demo_date'::date;

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
    -- 1. ВДНХ (3)
    (
        'Фестиваль «Весна на ВДНХ»',
        'Контроль электропитания павильонов',
        'INV-2026-011',
        'coor',
        'Павильон №4: кратковременное отключение линии из-за перегрузки удлинителя.',
        'MEDIUM',
        'RESOLVED',
        '2026-05-17 12:25:00',
        '2026-05-17 13:00:00',
        'Заменили удлинитель, питание восстановлено.'
    ),
    (
        'Фестиваль «Весна на ВДНХ»',
        'Дежурство у сцены у фонтана',
        NULL,
        'volonter021',
        'Скопление гостей у барьера перед вечерним блоком программы.',
        'LOW',
        'RESOLVED',
        '2026-05-17 17:15:00',
        '2026-05-17 17:45:00',
        'Усилили разметку прохода и добавили указатели.'
    ),
    (
        'Фестиваль «Весна на ВДНХ»',
        'Вечерний демонтаж декораций',
        NULL,
        'coor',
        'Задержка вывоза декораций с площадки на 20 минут.',
        'LOW',
        'OPEN',
        '2026-05-17 21:10:00',
        NULL,
        NULL
    ),

    -- 2. Форум (2)
    (
        'Корпоративный форум Digital RUT',
        'Техподдержка проектора в зале A',
        'INV-2026-041',
        'coor',
        'В зале A пропало изображение с проектора во время доклада.',
        'HIGH',
        'IN_PROGRESS',
        '2026-05-17 10:20:00',
        NULL,
        NULL
    ),
    (
        'Корпоративный форум Digital RUT',
        'Кофе-брейк — зал B',
        NULL,
        'volonter037',
        'В зале B закончилась вода в кулере раньше графика.',
        'LOW',
        'RESOLVED',
        '2026-05-17 11:45:00',
        '2026-05-17 12:10:00',
        'Подвезли запасные бутыли из склада.'
    ),

    -- 3. Марафон (3)
    (
        'Городской марафон «Бегом по набережной»',
        'Медицинский пост у моста',
        'INV-2026-052',
        'volonter014',
        'Участнику на 10 км оказана помощь при растяжении связки.',
        'MEDIUM',
        'RESOLVED',
        '2026-05-17 10:10:00',
        '2026-05-17 10:40:00',
        'Состояние стабильное, с дистанции снят.'
    ),
    (
        'Городской марафон «Бегом по набережной»',
        'Пункт питания км 5',
        NULL,
        'coor',
        'На пункте питания закончились стаканы, очередь остановилась на 6 минут.',
        'LOW',
        'RESOLVED',
        '2026-05-17 09:15:00',
        '2026-05-17 09:30:00',
        'Доставили стаканы из стартового городка.'
    ),
    (
        'Городской марафон «Бегом по набережной»',
        'Сопровождение лидеров на финише',
        NULL,
        'volonter022',
        'Сужение коридора у финишной арки из-за фотосъёмки.',
        'LOW',
        'OPEN',
        '2026-05-17 11:50:00',
        NULL,
        NULL
    ),

    -- 4. Ярмарка (2)
    (
        'Ярмарка ремёсел «Мастерская двора»',
        'Контроль складской зоны',
        'INV-2026-018',
        'coor',
        'На складе ослабло крепление навеса после порыва ветра.',
        'MEDIUM',
        'RESOLVED',
        '2026-05-17 13:50:00',
        '2026-05-17 15:00:00',
        'Навес перезакрепили, зона частично ограничена.'
    ),
    (
        'Ярмарка ремёсел «Мастерская двора»',
        'Подключение павильона №12',
        'INV-2026-011',
        'volonter036',
        'В павильоне №12 нестабильная розетка, искрение при подключении.',
        'HIGH',
        'IN_PROGRESS',
        '2026-05-17 10:05:00',
        NULL,
        NULL
    ),

    -- 5. Забег «Километр добра» (1)
    (
        'Благотворительный забег «Километр добра»',
        'Раздача воды на финише',
        'INV-2026-022',
        'volonter019',
        'Протечка крана у бака с водой на финише.',
        'LOW',
        'RESOLVED',
        '2026-05-17 11:05:00',
        '2026-05-17 11:25:00',
        'Кран заменён, покрытие протёрто.'
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
JOIN events e
    ON e.title = v.event_title
   AND e.start_date::date = :'demo_date'::date
LEFT JOIN tasks t
    ON t.event_id = e.id
   AND t.title = v.task_title
LEFT JOIN resources r
    ON v.inventory_number IS NOT NULL
   AND r.inventory_number = v.inventory_number
   AND r.source_type = 'INTERNAL'
   AND r.operational = TRUE
JOIN users u
    ON u.username = v.reporter_username;

COMMIT;

SELECT e.title, COUNT(i.id) AS incidents
FROM events e
LEFT JOIN incidents i ON i.event_id = e.id
WHERE e.start_date::date = :'demo_date'::date
GROUP BY e.id, e.title
ORDER BY e.id;
