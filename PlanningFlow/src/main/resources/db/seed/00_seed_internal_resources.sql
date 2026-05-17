-- Заполнение внутреннего склада (INTERNAL): 60 позиций.
-- Схема JPA: resources, discriminator source_type = 'INTERNAL'.
-- Повторный запуск: пропускает уже существующие inventory_number.
--
-- docker exec -i postgres psql -U postgres -d plan_flow -v ON_ERROR_STOP=1 \
--   < PlanningFlow/src/main/resources/db/seed/00_seed_internal_resources.sql

BEGIN;

INSERT INTO resources (
    name,
    type,
    operational,
    source_type,
    inventory_number,
    external_api_id
)
SELECT v.name, v.type, v.operational, v.source_type, v.inventory_number, v.external_api_id
FROM (VALUES
    ('LADA Vesta',                          'TRANSPORT',  true, 'INTERNAL', 'INV-2026-001', NULL::varchar),
    ('LADA Vesta',                          'TRANSPORT',  true, 'INTERNAL', 'INV-2026-002', NULL),
    ('Офисный стул',                        'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-003', NULL),
    ('Офисный стул',                        'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-004', NULL),
    ('Офисный стул',                        'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-005', NULL),
    ('Офисный стул',                        'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-006', NULL),
    ('Офисный стул',                        'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-007', NULL),
    ('Монитор 24"',                         'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-008', NULL),
    ('Монитор 24"',                         'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-009', NULL),
    ('Монитор 24"',                         'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-010', NULL),
    ('Удлинитель 50 м',                     'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-011', NULL),
    ('Удлинитель 50 м',                     'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-012', NULL),
    ('Удлинитель 50 м',                     'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-013', NULL),
    ('Удлинитель 50 м',                     'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-014', NULL),
    ('Рабочая куртка',                      'MATERIAL',   true, 'INTERNAL', 'INV-2026-015', NULL),
    ('Рабочая куртка',                      'MATERIAL',   true, 'INTERNAL', 'INV-2026-016', NULL),
    ('Рабочая куртка',                      'MATERIAL',   true, 'INTERNAL', 'INV-2026-017', NULL),
    ('Навес складской',                     'MATERIAL',   true, 'INTERNAL', 'INV-2026-018', NULL),
    ('Навес складской',                     'MATERIAL',   true, 'INTERNAL', 'INV-2026-019', NULL),
    ('Музыкальная колонка',                 'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-020', NULL),
    ('Музыкальная колонка',                 'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-021', NULL),
    ('Бутылка воды 19 л',                   'MATERIAL',   true, 'INTERNAL', 'INV-2026-022', NULL),
    ('Бутылка воды 19 л',                   'MATERIAL',   true, 'INTERNAL', 'INV-2026-023', NULL),
    ('Бутылка воды 19 л',                   'MATERIAL',   true, 'INTERNAL', 'INV-2026-024', NULL),
    ('Казан 12 л',                          'MATERIAL',   true, 'INTERNAL', 'INV-2026-025', NULL),
    ('Казан 12 л',                          'MATERIAL',   true, 'INTERNAL', 'INV-2026-026', NULL),
    ('Пластиковый стул',                    'MATERIAL',   true, 'INTERNAL', 'INV-2026-027', NULL),
    ('Пластиковый стул',                    'MATERIAL',   true, 'INTERNAL', 'INV-2026-028', NULL),
    ('Пластиковый стул',                    'MATERIAL',   true, 'INTERNAL', 'INV-2026-029', NULL),
    ('Стол складной',                       'MATERIAL',   true, 'INTERNAL', 'INV-2026-030', NULL),
    ('Стол складной',                       'MATERIAL',   true, 'INTERNAL', 'INV-2026-031', NULL),
    ('ГАЗель NEXT',                         'TRANSPORT',  true, 'INTERNAL', 'INV-2026-032', NULL),
    ('Прицеп легковой',                     'TRANSPORT',  true, 'INTERNAL', 'INV-2026-033', NULL),
    ('Renault Logan',                       'TRANSPORT',  true, 'INTERNAL', 'INV-2026-034', NULL),
    ('Ford Transit',                        'TRANSPORT',  true, 'INTERNAL', 'INV-2026-035', NULL),
    ('Велосипед складной',                  'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-036', NULL),
    ('Фургон рефрижератор',                 'TRANSPORT',  true, 'INTERNAL', 'INV-2026-037', NULL),
    ('Генератор бензиновый 5 кВт',          'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-038', NULL),
    ('Прожектор переносной',                'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-039', NULL),
    ('Радиомикрофон',                       'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-040', NULL),
    ('Проектор',                            'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-041', NULL),
    ('Wi‑Fi роутер',                        'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-042', NULL),
    ('Перфоратор',                          'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-043', NULL),
    ('Стремянка 3 ступени',                 'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-044', NULL),
    ('Верёвка бельевая 50 м',               'MATERIAL',   true, 'INTERNAL', 'INV-2026-045', NULL),
    ('Тент уличный 4×6 м',                  'MATERIAL',   true, 'INTERNAL', 'INV-2026-046', NULL),
    ('Палатка туристическая',               'MATERIAL',   true, 'INTERNAL', 'INV-2026-047', NULL),
    ('Спальный мешок',                      'MATERIAL',   true, 'INTERNAL', 'INV-2026-048', NULL),
    ('Фонарь налобный',                     'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-049', NULL),
    ('Ведро оцинкованное',                  'MATERIAL',   true, 'INTERNAL', 'INV-2026-050', NULL),
    ('Швабра с отжимом',                    'MATERIAL',   true, 'INTERNAL', 'INV-2026-051', NULL),
    ('Аптечка первой помощи',               'MATERIAL',   true, 'INTERNAL', 'INV-2026-052', NULL),
    ('Огнетушитель ОП-4',                   'MATERIAL',   true, 'INTERNAL', 'INV-2026-053', NULL),
    ('Знак аварийной остановки',            'MATERIAL',   true, 'INTERNAL', 'INV-2026-054', NULL),
    ('Домкрат гидравлический',              'EQUIPMENT',  true, 'INTERNAL', 'INV-2026-055', NULL),
    ('Канистра металлическая 20 л',         'MATERIAL',   true, 'INTERNAL', 'INV-2026-056', NULL),
    ('Доска маркерная',                     'MATERIAL',   true, 'INTERNAL', 'INV-2026-057', NULL),
    ('Флипчарт мобильный',                  'MATERIAL',   true, 'INTERNAL', 'INV-2026-058', NULL),
    ('Умывальник переносной',               'MATERIAL',   true, 'INTERNAL', 'INV-2026-059', NULL),
    ('Лопата штыковая',                     'MATERIAL',   true, 'INTERNAL', 'INV-2026-060', NULL)
) AS v(name, type, operational, source_type, inventory_number, external_api_id)
WHERE NOT EXISTS (
    SELECT 1
    FROM resources r
    WHERE r.inventory_number = v.inventory_number
      AND r.source_type = 'INTERNAL'
);

SELECT setval(
    pg_get_serial_sequence('resources', 'id'),
    COALESCE((SELECT MAX(id) FROM resources), 1)
);

COMMIT;

SELECT source_type, type, COUNT(*) AS cnt
FROM resources
WHERE source_type = 'INTERNAL'
GROUP BY source_type, type
ORDER BY type;

SELECT COUNT(*) AS internal_total
FROM resources
WHERE source_type = 'INTERNAL' AND operational = TRUE;
