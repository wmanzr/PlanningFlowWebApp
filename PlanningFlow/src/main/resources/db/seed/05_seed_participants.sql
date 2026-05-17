-- Шаг 5: 100 исполнителей (PARTICIPANT) + навыки под демо-задачи 2026-05-17.
-- Пароль у всех как у пользователя part (тот же bcrypt-хеш).
-- ФИО: нечётный volonter — мужчина, чётный — женщина (фамилия в нужном роде).
\set demo_date '2026-05-17'

BEGIN;

WITH part_password AS (
    SELECT password
    FROM users
    WHERE username = 'part'
    LIMIT 1
),
participant_role AS (
    SELECT id
    FROM roles
    WHERE name = 'PARTICIPANT'
    LIMIT 1
),
name_catalog AS (
    SELECT v.n, v.full_name
    FROM (VALUES
        (3, 'Смирнов Михаил'),
        (4, 'Кузнецова Полина'),
        (5, 'Попов Артём'),
        (6, 'Васильева Алина'),
        (7, 'Соколова Галина'),
        (8, 'Михайлов Кирилл'),
        (9, 'Новикова Наталья'),
        (10, 'Фёдорова Светлана'),
        (11, 'Морозов Данил'),
        (12, 'Волков Артём'),
        (13, 'Алексеев Дмитрий'),
        (14, 'Лебедев Лев'),
        (15, 'Семёнов Олег'),
        (16, 'Егорова София'),
        (17, 'Павлова Лариса'),
        (18, 'Козлов Вадим'),
        (19, 'Степанова Екатерина'),
        (20, 'Николаев Максим'),
        (21, 'Орлов Павел'),
        (22, 'Андреева Юлия'),
        (23, 'Макаров Алексей'),
        (24, 'Никитин Виктор'),
        (25, 'Захарова Ирина'),
        (26, 'Зайцева Надежда'),
        (27, 'Соловьёв Роман'),
        (28, 'Борисов Георгий'),
        (29, 'Яковлева Анна'),
        (30, 'Григорьева Дарья'),
        (31, 'Романова Ксения'),
        (32, 'Воробьёв Никита'),
        (33, 'Сергеев Сергей'),
        (34, 'Куликова Елена'),
        (35, 'Медведев Борис'),
        (36, 'Тарасов Евгений'),
        (37, 'Жукова Марина'),
        (38, 'Белова Ольга'),
        (39, 'Комарова Татьяна'),
        (40, 'Иванов Александр'),
        (41, 'Петрова Валерия'),
        (42, 'Сидоров Иван'),
        (43, 'Фомин Михаил'),
        (44, 'Кузнецова Анна'),
        (45, 'Попова Яна'),
        (46, 'Васильева Алина'),
        (47, 'Соколова Виктория'),
        (48, 'Михайлов Кирилл'),
        (49, 'Новикова Наталья'),
        (50, 'Фёдорова Светлана'),
        (51, 'Морозов Данил'),
        (52, 'Волков Артём'),
        (53, 'Алексеев Дмитрий'),
        (54, 'Лебедев Лев'),
        (55, 'Семёнов Олег'),
        (56, 'Егорова София'),
        (57, 'Павлова Лариса'),
        (58, 'Козлов Вадим'),
        (59, 'Степанова Екатерина'),
        (60, 'Николаев Максим'),
        (61, 'Орлов Павел'),
        (62, 'Андреева Юлия'),
        (63, 'Макаров Алексей'),
        (64, 'Никитин Виктор'),
        (65, 'Захарова Ирина'),
        (66, 'Зайцева Надежда'),
        (67, 'Соловьёв Роман'),
        (68, 'Борисов Георгий'),
        (69, 'Яковлева Анна'),
        (70, 'Григорьева Дарья'),
        (71, 'Романова Ксения'),
        (72, 'Воробьёв Никита'),
        (73, 'Сергеев Сергей'),
        (74, 'Куликова Елена'),
        (75, 'Медведев Борис'),
        (76, 'Тарасов Евгений'),
        (77, 'Жукова Марина'),
        (78, 'Белова Ольга'),
        (79, 'Комарова Татьяна'),
        (80, 'Иванов Александр'),
        (81, 'Петрова Валерия'),
        (82, 'Сидоров Иван'),
        (83, 'Орлов Михаил'),
        (84, 'Кузнецова Полина'),
        (85, 'Попова Яна'),
        (86, 'Васильева Алина'),
        (87, 'Соколова Галина'),
        (88, 'Михайлов Кирилл'),
        (89, 'Новикова Наталья'),
        (90, 'Фёдорова Светлана'),
        (91, 'Морозов Данил'),
        (92, 'Волков Артём'),
        (93, 'Алексеев Дмитрий'),
        (94, 'Лебедев Лев'),
        (95, 'Семёнов Олег'),
        (96, 'Егорова София'),
        (97, 'Павлова Лариса'),
        (98, 'Козлов Вадим'),
        (99, 'Степанова Екатерина'),
        (100, 'Николаев Максим'),
        (101, 'Орлов Павел'),
        (102, 'Андреева Юлия')
    ) AS v(n, full_name)
),
new_users AS (
    SELECT
        gs.n,
        ('volonter' || lpad(gs.n::text, 3, '0')) AS username,
        ('volonter' || lpad(gs.n::text, 3, '0') || '@planflow.demo') AS email,
        nc.full_name,
        (DATE '1992-01-01' + ((gs.n * 97) % 4500)) AS birth_date
    FROM generate_series(3, 102) AS gs(n)
    JOIN name_catalog nc ON nc.n = gs.n
),
inserted_users AS (
    INSERT INTO users (username, password, email, full_name, birth_date)
    SELECT
        nu.username,
        pp.password,
        nu.email,
        nu.full_name,
        nu.birth_date
    FROM new_users nu
    CROSS JOIN part_password pp
    WHERE NOT EXISTS (
        SELECT 1 FROM users u WHERE u.username = nu.username
    )
    RETURNING id, username
),
role_links AS (
    INSERT INTO users_roles (user_entity_id, roles_id)
    SELECT iu.id, pr.id
    FROM inserted_users iu
    CROSS JOIN participant_role pr
    WHERE NOT EXISTS (
        SELECT 1
        FROM users_roles ur
        WHERE ur.user_entity_id = iu.id AND ur.roles_id = pr.id
    )
    RETURNING user_entity_id
),
demo_skills AS (
    SELECT DISTINCT
        s.id AS skill_id,
        ROW_NUMBER() OVER (ORDER BY s.id) - 1 AS skill_idx,
        COUNT(*) OVER () AS skill_cnt
    FROM task_required_skills trs
    JOIN tasks t ON t.id = trs.task_id
    JOIN events e ON e.id = t.event_id
    JOIN skills s ON s.id = trs.skill_id
    WHERE e.start_date::date = :'demo_date'::date
)
INSERT INTO user_skills (user_id, skill_id, skill_tier, verified_at)
SELECT
    iu.id,
    ds.skill_id,
    CASE ((iu.id + ds.skill_idx + slot.n) % 10)
        WHEN 0 THEN 'EXPERT'
        WHEN 1 THEN 'NOVICE'
        ELSE 'PRACTITIONER'
    END,
    CASE
        WHEN ((iu.id + slot.n) % 4) = 0 THEN NULL
        ELSE (TIMESTAMP '2026-05-10 12:00:00' + ((iu.id + slot.n) % 120) * INTERVAL '1 hour')
    END
FROM inserted_users iu
CROSS JOIN generate_series(0, 2) AS slot(n)
JOIN demo_skills ds
    ON ds.skill_idx = ((iu.id * 5 + slot.n * 7) % ds.skill_cnt);

COMMIT;

SELECT COUNT(*) AS participants
FROM users u
JOIN users_roles ur ON ur.user_entity_id = u.id
JOIN roles r ON r.id = ur.roles_id
WHERE r.name = 'PARTICIPANT';

SELECT COUNT(*) AS user_skill_rows
FROM user_skills us
JOIN users u ON u.id = us.user_id
WHERE u.username LIKE 'volonter%';
