const WEEKDAY_FORMATTER = new Intl.DateTimeFormat('ru-RU', { weekday: 'long' });
export function formatMatchingWorkloadWeekday(isoDateTime: string | undefined): string {
    if (!isoDateTime) {
        return 'За день задачи';
    }
    const date = new Date(isoDateTime);
    if (Number.isNaN(date.getTime())) {
        return 'За день задачи';
    }
    const weekday = WEEKDAY_FORMATTER.format(date);
    return weekday.charAt(0).toUpperCase() + weekday.slice(1);
}
