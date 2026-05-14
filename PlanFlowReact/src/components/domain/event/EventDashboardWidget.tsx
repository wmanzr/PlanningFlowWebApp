import { useEffect, useMemo, useRef, useState } from 'react';
import Typography from '@mui/material/Typography';
import LinearProgress from '@mui/material/LinearProgress';
import { Card, formatDateTime } from '@/components/ui';
import { EventStatus, type EventDashboardResponseDto, type EventId } from '@/types';
const PROGRESS_MAX = 100;
const INTRO_MS = 420;
const clampProgress = (value: number): number => Math.max(0, Math.min(PROGRESS_MAX, Math.round(value)));
const easeOutCubic = (t: number): number => 1 - (1 - t) ** 3;
function useIntroStat(target: number, eventId: EventId): number {
    const [v, setV] = useState(0);
    const rafRef = useRef<number>(0);
    const displayRef = useRef(0);
    const lastEventRef = useRef<EventId | undefined>(undefined);
    useEffect(() => {
        cancelAnimationFrame(rafRef.current);
        const end = Math.max(0, Math.round(Number.isFinite(target) ? target : 0));
        const prevEvent = lastEventRef.current;
        const switched = prevEvent === undefined || Number(prevEvent) !== Number(eventId);
        lastEventRef.current = eventId;
        const start = switched ? 0 : displayRef.current;
        let startTime: number | null = null;
        const loop = (now: number) => {
            if (startTime === null)
                startTime = now;
            const t = Math.min(1, (now - startTime) / INTRO_MS);
            const next = Math.round(start + (end - start) * easeOutCubic(t));
            displayRef.current = next;
            setV(next);
            if (t < 1) {
                rafRef.current = requestAnimationFrame(loop);
            }
        };
        rafRef.current = requestAnimationFrame(loop);
        return () => cancelAnimationFrame(rafRef.current);
    }, [target, eventId]);
    return v;
}
function useIntroProgressBar(target: number, eventId: EventId): number {
    const safe = clampProgress(target);
    const [value, setValue] = useState(0);
    const lastEventRef = useRef<EventId | undefined>(undefined);
    useEffect(() => {
        const prev = lastEventRef.current;
        const switched = prev === undefined || Number(prev) !== Number(eventId);
        lastEventRef.current = eventId;
        if (switched) {
            setValue(0);
            let inner = 0;
            const outer = requestAnimationFrame(() => {
                inner = requestAnimationFrame(() => setValue(safe));
            });
            return () => {
                cancelAnimationFrame(outer);
                cancelAnimationFrame(inner);
            };
        }
        setValue(safe);
        return undefined;
    }, [eventId, safe]);
    return value;
}
function formatCountdown(ms: number): string {
    if (ms <= 0)
        return '0с';
    const totalSec = Math.floor(ms / 1000);
    const s = totalSec % 60;
    const m = Math.floor(totalSec / 60) % 60;
    const h = Math.floor(totalSec / 3600) % 24;
    const d = Math.floor(totalSec / 86400);
    const parts: string[] = [];
    if (d > 0)
        parts.push(`${d}д`);
    if (h > 0 || d > 0)
        parts.push(`${h}ч`);
    if (m > 0 || h > 0 || d > 0)
        parts.push(`${m}м`);
    parts.push(`${s}с`);
    return parts.join(' ');
}
function useNowTick(enabled: boolean, intervalMs = 1000): number {
    const [now, setNow] = useState(() => Date.now());
    useEffect(() => {
        if (!enabled)
            return;
        const id = window.setInterval(() => setNow(Date.now()), intervalMs);
        return () => window.clearInterval(id);
    }, [enabled, intervalMs]);
    return now;
}
const statTileClass = 'rounded-lg border border-secondary/60 bg-surface-muted px-3 py-3 sm:px-4 sm:py-4';
function formatStatValue(value: number | string): string | number {
    if (typeof value === 'number') {
        return Number.isFinite(value) ? value : '—';
    }
    return value === '' ? '—' : value;
}
function BigStat({ label, value }: {
    label: string;
    value: number | string;
}) {
    return (<div className={statTileClass}>
      <Typography variant="body2" color="text.secondary" sx={{ display: 'block' }}>
        {label}
      </Typography>
      <Typography variant="h5" component="p" color="text.primary" sx={{
            fontWeight: 700,
            lineHeight: 1.25,
            mt: 0.5,
            fontVariantNumeric: typeof value === 'number' ? 'tabular-nums' : undefined,
        }}>
        {formatStatValue(value)}
      </Typography>
    </div>);
}
const EventHorizonCountdown = ({ startDate, endDate, eventStatus, }: {
    startDate: string;
    endDate: string;
    eventStatus: EventStatus;
}) => {
    const tickEnabled = eventStatus !== EventStatus.COMPLETED && eventStatus !== EventStatus.CANCELLED;
    const tick = useNowTick(tickEnabled);
    const line = useMemo(() => {
        const startMs = new Date(startDate).getTime();
        const endMs = new Date(endDate).getTime();
        if (!Number.isFinite(startMs) || !Number.isFinite(endMs)) {
            return { title: 'До старта', value: '—' };
        }
        if (eventStatus === EventStatus.COMPLETED) {
            return { title: 'Мероприятие', value: 'Завершено' };
        }
        if (eventStatus === EventStatus.CANCELLED) {
            return { title: 'Мероприятие', value: 'Отменено' };
        }
        if (tick < startMs) {
            return { title: 'До старта', value: formatCountdown(startMs - tick) };
        }
        if (tick < endMs) {
            return { title: 'До окончания', value: formatCountdown(endMs - tick) };
        }
        return { title: 'До окончания', value: 'Время вышло' };
    }, [startDate, endDate, eventStatus, tick]);
    return (<div className={statTileClass}>
      <Typography variant="body2" color="text.secondary" sx={{ display: 'block' }}>
        {line.title}
      </Typography>
      <Typography variant="h5" component="p" color="text.primary" sx={{
            fontWeight: 700,
            lineHeight: 1.25,
            mt: 0.5,
            whiteSpace: 'nowrap',
            fontVariantNumeric: 'tabular-nums',
            fontSize: { xs: '1.1rem', sm: '1.25rem' },
        }}>
        {line.value}
      </Typography>
    </div>);
};
export interface EventDashboardWidgetProps {
    data: EventDashboardResponseDto;
    variant?: 'standalone' | 'embedded';
}
export const EventDashboardWidget = ({ data, variant = 'standalone', }: EventDashboardWidgetProps) => {
    const eventId = data.eventId;
    const totalAnim = useIntroStat(data.totalTasks ?? 0, eventId);
    const activeAnim = useIntroStat(data.activeTasks ?? 0, eventId);
    const completedAnim = useIntroStat(data.completedTasks ?? 0, eventId);
    const executorsAnim = useIntroStat(data.uniqueExecutorsCount ?? 0, eventId);
    const cancelledAnim = useIntroStat(data.cancelledTasksCount ?? 0, eventId);
    const incidentsAnim = useIntroStat(data.totalIncidentsCount ?? 0, eventId);
    const progressDigits = useIntroStat(clampProgress(data.progressPercent), eventId);
    const barValue = useIntroProgressBar(data.progressPercent, eventId);
    const embedded = variant === 'embedded';
    return (<Card>
      <div className="flex flex-col gap-5">
        {embedded ? null : (<div>
            <Typography variant="h5" component="h2">
              {data.title}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {formatDateTime(data.startDate)} — {formatDateTime(data.endDate)}
            </Typography>
          </div>)}
        <div className="grid gap-3 sm:grid-cols-3">
          <BigStat label="Всего задач" value={totalAnim}/>
          <BigStat label="Активных" value={activeAnim}/>
          <BigStat label="Завершено" value={completedAnim}/>
        </div>
        <div>
          <div className="mb-1 flex justify-between">
            <Typography variant="body2" color="text.secondary">
              Прогресс
            </Typography>
            <Typography variant="body2" sx={{
            fontWeight: 600,
            fontVariantNumeric: 'tabular-nums',
        }}>
              {progressDigits}%
            </Typography>
          </div>
          <LinearProgress variant="determinate" value={barValue} sx={{
            height: 8,
            borderRadius: 4,
            '& .MuiLinearProgress-bar': {
                transitionDuration: `${INTRO_MS}ms`,
            },
        }}/>
        </div>
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <BigStat label="Всего исполнителей задействовано" value={executorsAnim}/>
          <BigStat label="Задач отменено" value={cancelledAnim}/>
          <BigStat label="Всего инцидентов" value={incidentsAnim}/>
          <EventHorizonCountdown startDate={data.startDate} endDate={data.endDate} eventStatus={data.eventStatus}/>
        </div>
      </div>
    </Card>);
};
