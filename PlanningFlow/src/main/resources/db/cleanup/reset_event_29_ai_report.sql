-- Сброс теста post-mortem ИИ: мероприятие 29 → ACTIVE, удалить отчёт только для него.
-- Задачи 86–88 не трогаем (остаются DONE).

BEGIN;

DELETE FROM event_ai_postmortem_reports
WHERE event_id = 29;

UPDATE events
SET status = 'ACTIVE'
WHERE id = 29;

COMMIT;

SELECT id, title, status FROM events WHERE id = 29;

SELECT event_id, status, LEFT(COALESCE(report_text, ''), 80) AS report_preview
FROM event_ai_postmortem_reports
WHERE event_id = 29;
