import type { AppApiError } from '@/types';
const FIELD_LABELS: Record<string, string> = {
    title: 'Название',
    description: 'Описание',
    startDate: 'Дата начала',
    endDate: 'Дата окончания',
    latitude: 'Широта',
    longitude: 'Долгота',
    eventId: 'Мероприятие',
    birthDate: 'Дата рождения',
};
const KNOWN_FIELD = new Set(Object.keys(FIELD_LABELS));
function lineForFieldError(key: string, message: string): string {
    if (key === '_global')
        return message;
    if (KNOWN_FIELD.has(key)) {
        return `${FIELD_LABELS[key]}: ${message}`;
    }
    return message;
}
export function validationErrorsToToastMessage(err: AppApiError): string {
    if (!err.fieldErrors || Object.keys(err.fieldErrors).length === 0) {
        return err.message;
    }
    const lines: string[] = [];
    for (const [key, message] of Object.entries(err.fieldErrors)) {
        if (key === '_global')
            continue;
        lines.push(lineForFieldError(key, message));
    }
    const globalMsg = err.fieldErrors._global;
    if (typeof globalMsg === 'string' && globalMsg.trim()) {
        lines.push(globalMsg.trim());
    }
    const text = lines.filter(Boolean).join(' · ');
    return text || err.message;
}
