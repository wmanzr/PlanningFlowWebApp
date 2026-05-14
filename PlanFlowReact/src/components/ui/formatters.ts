const DATE_FORMATTER = new Intl.DateTimeFormat('ru-RU', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
});
const DATETIME_FORMATTER = new Intl.DateTimeFormat('ru-RU', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
});
const TIME_FORMATTER = new Intl.DateTimeFormat('ru-RU', {
    hour: '2-digit',
    minute: '2-digit',
});
const safeDate = (input: string | undefined): Date | null => {
    if (!input)
        return null;
    const date = new Date(input);
    return Number.isNaN(date.getTime()) ? null : date;
};
export const formatDate = (input: string | undefined): string => {
    const date = safeDate(input);
    return date ? DATE_FORMATTER.format(date) : '—';
};
export const formatDateTime = (input: string | undefined): string => {
    const date = safeDate(input);
    return date ? DATETIME_FORMATTER.format(date) : '—';
};
export const formatTime = (input: string | undefined): string => {
    const date = safeDate(input);
    return date ? TIME_FORMATTER.format(date) : '—';
};
export const toIsoDateTimeInput = (input: string | undefined): string => {
    const date = safeDate(input);
    if (!date)
        return '';
    const offset = date.getTimezoneOffset();
    const localDate = new Date(date.getTime() - offset * 60000);
    return localDate.toISOString().slice(0, 16);
};
export const fromIsoDateTimeInput = (value: string): string => {
    if (!value)
        return '';
    const trimmed = value.trim();
    const m = trimmed.match(/^(\d{4}-\d{2}-\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?/);
    if (!m)
        return trimmed;
    const sec = m[4] ?? '00';
    return `${m[1]}T${m[2]}:${m[3]}:${sec}`;
};
