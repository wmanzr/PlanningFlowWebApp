-- Шаг 2/9: координаторы мероприятий.
\set demo_date '2026-05-17'

BEGIN;

INSERT INTO event_coordinators (event_id, user_id)
SELECT e.id, coord.id
FROM events e
CROSS JOIN LATERAL (
    SELECT u.id
    FROM users u
    JOIN users_roles ur ON ur.user_entity_id = u.id
    JOIN roles r ON r.id = ur.roles_id
    WHERE r.name = 'COORDINATOR'
    ORDER BY u.id
    LIMIT 1
) coord
WHERE e.start_date::date = :'demo_date'::date
  AND NOT EXISTS (
      SELECT 1
      FROM event_coordinators ec
      WHERE ec.event_id = e.id AND ec.user_id = coord.id
  );

-- Организатор wmanzr — второй координатор на 4 крупных площадках
INSERT INTO event_coordinators (event_id, user_id)
SELECT e.id, org.id
FROM events e
CROSS JOIN LATERAL (
    SELECT u.id
    FROM users u
    JOIN users_roles ur ON ur.user_entity_id = u.id
    JOIN roles r ON r.id = ur.roles_id
    WHERE r.name = 'ORGANIZER'
    ORDER BY u.id
    LIMIT 1
) org
WHERE e.start_date::date = :'demo_date'::date
  AND e.title IN (
      'Фестиваль «Весна на ВДНХ»',
      'Корпоративный форум Digital RUT',
      'Городской марафон «Бегом по набережной»',
      'Летний концерт в парке Горького'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM event_coordinators ec
      WHERE ec.event_id = e.id AND ec.user_id = org.id
  );

COMMIT;

SELECT e.id, e.title, ec.user_id, u.username
FROM events e
JOIN event_coordinators ec ON ec.event_id = e.id
JOIN users u ON u.id = ec.user_id
WHERE e.start_date::date = :'demo_date'::date
ORDER BY e.id, ec.user_id;
