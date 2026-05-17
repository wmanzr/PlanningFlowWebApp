-- Шаг 1/9: мероприятия (корень операционных данных).
-- Требуется: в users есть хотя бы один COORDINATOR, ORGANIZER или ADMIN.
--
-- Демо-день для последующих tasks/assignments — поменяйте одну дату здесь:
\set demo_date '2026-05-17'

BEGIN;

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
    v.status,
    v.start_date,
    v.end_date,
    v.latitude,
    v.longitude,
    (
        SELECT u.id
        FROM users u
        JOIN users_roles ur ON ur.user_entity_id = u.id
        JOIN roles r ON r.id = ur.roles_id
        WHERE r.name IN ('COORDINATOR', 'ORGANIZER', 'ADMIN')
        ORDER BY CASE r.name
            WHEN 'COORDINATOR' THEN 1
            WHEN 'ORGANIZER' THEN 2
            ELSE 3
        END,
        u.id
        LIMIT 1
    )
FROM (VALUES
    (
        'Фестиваль «Весна на ВДНХ»',
        'Сезонное городское мероприятие: ярмарочные павильоны, сцена у фонтана, детская зона. Координация смен волонтёров и технической бригады.',
        'ACTIVE',
        (:'demo_date'::date + TIME '09:00')::timestamp,
        (:'demo_date'::date + TIME '22:00')::timestamp,
        55.829300,
        37.632100
    ),
    (
        'Корпоративный форум Digital RUT',
        'Однодневная конференция: пленарный зал, три секции, регистрация гостей, кейтеринг в перерывах.',
        'ACTIVE',
        (:'demo_date'::date + TIME '08:30')::timestamp,
        (:'demo_date'::date + TIME '18:30')::timestamp,
        55.751200,
        37.618400
    ),
    (
        'Городской марафон «Бегом по набережной»',
        'Дистанции 5 и 10 км, стартовый городок, пункты питания, медицинские посты, разводка потоков на мосту.',
        'ACTIVE',
        (:'demo_date'::date + TIME '07:00')::timestamp,
        (:'demo_date'::date + TIME '14:00')::timestamp,
        55.734500,
        37.601200
    ),
    (
        'Летний концерт в парке Горького',
        'Открытая сцена, звуковое и световое оборудование, зона зрителей, ограничение входа после 19:00.',
        'ACTIVE',
        (:'demo_date'::date + TIME '17:00')::timestamp,
        (:'demo_date'::date + TIME '23:30')::timestamp,
        55.731000,
        37.601800
    ),
    (
        'Ярмарка ремёсел «Мастерская двора»',
        '50 локаций, электропитание для павильонов, охрана складской зоны, вечерний разбор площадки.',
        'ACTIVE',
        (:'demo_date'::date + TIME '10:00')::timestamp,
        (:'demo_date'::date + TIME '20:00')::timestamp,
        55.757800,
        37.619500
    ),
    (
        'Турнир по пляжному волейболу Luzhniki Beach',
        'Групповой этап и плей-офф, судейские столы, медицинское сопровождение, аренда дополнительных сеток.',
        'ACTIVE',
        (:'demo_date'::date + TIME '09:30')::timestamp,
        (:'demo_date'::date + TIME '19:00')::timestamp,
        55.715400,
        37.553600
    ),
    (
        'Благотворительный забег «Километр добра»',
        'Семейный формат, сбор регистраций на месте, фотозона, раздача воды на финише.',
        'ACTIVE',
        (:'demo_date'::date + TIME '08:00')::timestamp,
        (:'demo_date'::date + TIME '13:00')::timestamp,
        55.788700,
        37.634200
    ),
    (
        'Открытие летней сцены у метро Воробьёвы горы',
        'Первый показ сезона: монтаж декораций, репетиция, вечерний прогон света, дежурная бригада до 01:00.',
        'ACTIVE',
        (:'demo_date'::date + TIME '12:00')::timestamp,
        (:'demo_date'::date + TIME '23:59')::timestamp,
        55.710900,
        37.559400
    )
) AS v(title, description, status, start_date, end_date, latitude, longitude);

COMMIT;

SELECT id, title, status, start_date::date AS event_day, creator_id
FROM events
ORDER BY id;
