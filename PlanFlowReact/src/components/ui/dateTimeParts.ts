const pad2 = (n: number) => String(Math.trunc(n)).padStart(2, '0');
const formatNaiveLocalDateTimeParts = (year: number, month1Based: number, day: number, hour: number, minute: number, second: number): string => `${year}-${pad2(month1Based)}-${pad2(day)}T${pad2(hour)}:${pad2(minute)}:${pad2(second)}`;
export const coerceApiDateTimeToIso = (value: unknown): string | undefined => {
    if (value == null)
        return undefined;
    if (typeof value === 'string' && value.trim() !== '')
        return value.trim();
    if (Array.isArray(value) && value.length >= 3) {
        const y = Number(value[0]);
        const mo = Number(value[1]);
        const d = Number(value[2]);
        const hh = value.length > 3 ? Number(value[3]) : 0;
        const mm = value.length > 4 ? Number(value[4]) : 0;
        const ss = value.length > 5 ? Number(value[5]) : 0;
        if (![y, mo, d, hh, mm, ss].every((n) => Number.isFinite(n)))
            return undefined;
        return formatNaiveLocalDateTimeParts(y, mo, d, hh, mm, ss);
    }
    return undefined;
};
export const toDateInput = (iso: string | undefined): string => {
    if (!iso)
        return '';
    const date = new Date(iso);
    if (Number.isNaN(date.getTime()))
        return '';
    const offset = date.getTimezoneOffset();
    const localDate = new Date(date.getTime() - offset * 60000);
    return localDate.toISOString().slice(0, 10);
};
export const toTimeInput = (iso: string | undefined): string => {
    if (!iso)
        return '';
    const date = new Date(iso);
    if (Number.isNaN(date.getTime()))
        return '';
    const offset = date.getTimezoneOffset();
    const localDate = new Date(date.getTime() - offset * 60000);
    return localDate.toISOString().slice(11, 16);
};
export const toTimeInputRoundedToStep = (iso: string | undefined, stepMinutes: number): string => {
    if (!iso)
        return '';
    const date = new Date(iso);
    if (Number.isNaN(date.getTime()))
        return '';
    const rounded = roundTimeToStep(date, stepMinutes);
    const offset = rounded.getTimezoneOffset();
    const localDate = new Date(rounded.getTime() - offset * 60000);
    return localDate.toISOString().slice(11, 16);
};
export const fromDateAndTimeInputs = (date: string, time: string): string => {
    if (!date || !time)
        return '';
    const parts = time.trim().split(':');
    const hh = Number(parts[0]);
    const mm = Number(parts[1] ?? 0);
    const ss = Number(parts[2] ?? 0);
    if (![hh, mm, ss].every((n) => Number.isFinite(n)))
        return '';
    const dateParts = date.trim().split('-').map(Number);
    if (dateParts.length < 3 || !dateParts.every((n) => Number.isFinite(n)))
        return '';
    const [ys, ms, ds] = dateParts as [
        number,
        number,
        number
    ];
    return formatNaiveLocalDateTimeParts(ys, ms, ds, hh, mm, ss);
};
export const toNaiveLocalIsoFromTimestamp = (ms: number): string => {
    const d = new Date(ms);
    return formatNaiveLocalDateTimeParts(d.getFullYear(), d.getMonth() + 1, d.getDate(), d.getHours(), d.getMinutes(), d.getSeconds());
};
export const roundTimeToStep = (date: Date, stepMinutes: number): Date => {
    const ms = stepMinutes * 60000;
    return new Date(Math.ceil(date.getTime() / ms) * ms);
};
export const addMinutes = (date: Date, minutes: number): Date => new Date(date.getTime() + minutes * 60000);
