-- Шаг 6: назначения (категории навыков + приоритет точного skill_id; 2 задачи OPEN для матчинга).
\set demo_date '2026-05-17'

BEGIN;

DELETE FROM assignments a
USING tasks t, events e
WHERE a.task_id = t.id
  AND t.event_id = e.id
  AND e.start_date::date = :'demo_date'::date;

UPDATE tasks t
SET status = 'OPEN'
FROM events e
WHERE t.event_id = e.id
  AND e.start_date::date = :'demo_date'::date
  AND t.status IN ('ASSIGNED', 'IN_PROGRESS', 'DONE');

DO $$
DECLARE
    r_task RECORD;
    v_user_id INT;
    v_assigned_at TIMESTAMP;
    v_gap INTERVAL := INTERVAL '15 minutes';
    v_max_daily_seconds DOUBLE PRECISION := 8 * 3600;
    v_skip_titles TEXT[] := ARRAY[
        'Модерация секции «DevOps»',
        'Проверка звука перед шоу'
    ];
    v_strict_daily BOOLEAN;
BEGIN
    FOR v_strict_daily IN SELECT unnest(ARRAY[TRUE, FALSE]) LOOP
        FOR r_task IN
            SELECT
                t.id,
                t.title,
                t.start_time,
                t.end_time,
                EXTRACT(EPOCH FROM (t.end_time - t.start_time)) AS duration_sec
            FROM tasks t
            JOIN events e ON e.id = t.event_id
            WHERE e.start_date::date = DATE '2026-05-17'
              AND NOT (t.title = ANY (v_skip_titles))
              AND NOT EXISTS (
                  SELECT 1
                  FROM assignments a
                  WHERE a.task_id = t.id
                    AND a.status IN ('PENDING', 'ACCEPTED')
              )
            ORDER BY t.start_time, t.id
        LOOP
            v_assigned_at := r_task.start_time - INTERVAL '2 days';

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
              AND (
                  NOT v_strict_daily
                  OR (
                      SELECT COALESCE(SUM(EXTRACT(EPOCH FROM (t2.end_time - t2.start_time))), 0)
                      FROM assignments a
                      JOIN tasks t2 ON t2.id = a.task_id
                      JOIN events e2 ON e2.id = t2.event_id
                      WHERE a.user_id = u.id
                        AND a.status IN ('PENDING', 'ACCEPTED')
                        AND e2.start_date::date = DATE '2026-05-17'
                  ) + r_task.duration_sec <= v_max_daily_seconds
              )
            ORDER BY
                (
                    SELECT COUNT(*)
                    FROM assignments a
                    JOIN tasks t2 ON t2.id = a.task_id
                    JOIN events e2 ON e2.id = t2.event_id
                    WHERE a.user_id = u.id
                      AND a.status IN ('PENDING', 'ACCEPTED')
                      AND e2.start_date::date = DATE '2026-05-17'
                ),
                (
                    EXISTS (
                        SELECT 1
                        FROM user_skills us
                        JOIN task_required_skills trs
                            ON trs.skill_id = us.skill_id AND trs.task_id = r_task.id
                        WHERE us.user_id = u.id
                    )
                ) DESC,
                u.id
            LIMIT 1;

            IF v_user_id IS NULL THEN
                CONTINUE;
            END IF;

            INSERT INTO assignments (task_id, user_id, status, assigned_at, responded_at)
            VALUES (
                r_task.id,
                v_user_id,
                'ACCEPTED',
                v_assigned_at,
                v_assigned_at + INTERVAL '3 hours'
            );

            UPDATE tasks
            SET status = 'ASSIGNED'
            WHERE id = r_task.id AND status = 'OPEN';
        END LOOP;
    END LOOP;
END $$;

UPDATE tasks t
SET status = 'DONE'
FROM events e
WHERE t.event_id = e.id
  AND e.start_date::date = :'demo_date'::date
  AND t.status = 'ASSIGNED'
  AND t.end_time <= TIMESTAMP '2026-05-17 11:00:00'
  AND t.title <> ALL (ARRAY[
      'Модерация секции «DevOps»',
      'Проверка звука перед шоу'
  ]);

COMMIT;

SELECT
    COUNT(*) FILTER (WHERE a.id IS NOT NULL) AS assigned_tasks,
    COUNT(*) FILTER (WHERE a.id IS NULL) AS open_for_matching
FROM tasks t
JOIN events e ON e.id = t.event_id
LEFT JOIN assignments a
    ON a.task_id = t.id AND a.status IN ('PENDING', 'ACCEPTED')
WHERE e.start_date::date = :'demo_date'::date;

SELECT t.title, t.status
FROM tasks t
JOIN events e ON e.id = t.event_id
LEFT JOIN assignments a
    ON a.task_id = t.id AND a.status IN ('PENDING', 'ACCEPTED')
WHERE e.start_date::date = :'demo_date'::date
  AND a.id IS NULL
ORDER BY t.start_time;
