import { EventStatus } from '@/types';
export function isNowWithinEventWindow(nowMs: number, startIso: string, endIso: string): boolean {
    const start = Date.parse(startIso);
    const end = Date.parse(endIso);
    if (!Number.isFinite(start) || !Number.isFinite(end))
        return false;
    return nowMs >= start && nowMs <= end;
}
export function eventMayActivateWhenExecutorStartsTask(params: {
    eventStatus: EventStatus;
    eventStartIso: string;
    eventEndIso: string;
    nowMs?: number;
}): boolean {
    if (params.eventStatus !== EventStatus.PLANNING)
        return false;
    const now = params.nowMs ?? Date.now();
    return isNowWithinEventWindow(now, params.eventStartIso, params.eventEndIso);
}
