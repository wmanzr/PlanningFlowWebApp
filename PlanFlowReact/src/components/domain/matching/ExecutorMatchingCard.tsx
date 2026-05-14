import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { type ReactNode, useMemo } from 'react';
import { Link } from 'react-router-dom';
import LinearProgress from '@mui/material/LinearProgress';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import { Badge, Card } from '@/components/ui';
import { useAppSelector } from '@/store';
import { selectAllSkills } from '@/store/slices/skills/selectors';
import type { SkillId } from '@/types';
const METERS_IN_KM = 1000;
export const formatDistanceMeters = (meters: number): string => {
    if (!(meters > 0))
        return '';
    if (meters < METERS_IN_KM)
        return `${Math.round(meters)} м`;
    const km = meters / METERS_IN_KM;
    return Number.isInteger(km) ? `${km} км` : `${km.toFixed(1)} км`;
};
export const describeMatchingDistance = (meters: number | undefined): string => {
    if (meters === undefined || meters <= 0)
        return '';
    return formatDistanceMeters(meters);
};
export const formatWorkedHours = (minutes: number): string => {
    const h = minutes / 60;
    const s = h.toFixed(1).replace(/\.0$/, '');
    return `${s} ч`;
};
export interface ExecutorMatchingCardProps {
    fullName: string;
    username: string;
    matchedSkillIds: SkillId[];
    distanceMeters?: number;
    workedTodayMinutes?: number;
    maxDailyLoadMinutes?: number;
    algorithmMiss?: boolean;
    profileTo?: string;
    rankBadge?: ReactNode;
    actions?: ReactNode;
    selectable?: boolean;
    selected?: boolean;
    onToggleSelect?: () => void;
    selectionAddDisabled?: boolean;
    blockedSelectionTooltip?: string;
}
export const ExecutorMatchingCard = ({ fullName, username, matchedSkillIds, distanceMeters, workedTodayMinutes, maxDailyLoadMinutes, algorithmMiss = false, profileTo, rankBadge, actions, selectable = false, selected = false, onToggleSelect, selectionAddDisabled = false, blockedSelectionTooltip, }: ExecutorMatchingCardProps) => {
    const skills = useAppSelector(selectAllSkills);
    const skillNameById = useMemo(() => new Map(skills.map((s) => [s.id, s.name] as const)), [skills]);
    const hasMetrics = !algorithmMiss &&
        workedTodayMinutes !== undefined &&
        maxDailyLoadMinutes !== undefined;
    const loadPercent = hasMetrics && maxDailyLoadMinutes > 0
        ? Math.min(100, Math.round((workedTodayMinutes / maxDailyLoadMinutes) * 100))
        : 0;
    const distanceLabel = distanceMeters !== undefined && distanceMeters > 0 ? formatDistanceMeters(distanceMeters) : '';
    const leftBlock = (<div className="min-w-0">
      <div className="flex flex-wrap items-center gap-x-2 gap-y-0.5">
        {rankBadge}
        <Typography variant="subtitle2" component="h3" sx={{ fontWeight: 600 }} className="truncate">
          {fullName}
        </Typography>
      </div>
      <Typography variant="caption" color="text.secondary" sx={{ lineHeight: 1.25, display: 'block' }}>
        @{username}
      </Typography>
      {matchedSkillIds.length > 0 ? (<div className="mt-1 flex flex-wrap gap-1">
          {matchedSkillIds.map((id) => (<Badge key={id} tone="info">
              {skillNameById.get(id) ?? 'Навык'}
            </Badge>))}
        </div>) : null}
    </div>);
    const rightBlock = (<div className="flex max-w-[13rem] shrink-0 flex-col items-end gap-1 text-right">
      {algorithmMiss ? (<Typography variant="caption" color="text.secondary" sx={{ fontSize: '0.7rem' }}>
          Данные подбора недоступны
        </Typography>) : (<>
          {distanceLabel ? (<Typography variant="caption" color="text.secondary" sx={{ fontSize: '0.7rem' }} className="font-medium">
              {distanceLabel}
            </Typography>) : null}
          {hasMetrics ? (<>
              <Typography variant="caption" color="text.secondary" sx={{ fontSize: '0.7rem' }}>
                Сегодня: {formatWorkedHours(workedTodayMinutes)} из лимита{' '}
                {formatWorkedHours(maxDailyLoadMinutes)}
              </Typography>
              <LinearProgress variant="determinate" value={loadPercent} sx={{ width: '100%', maxWidth: 168, height: 4, borderRadius: 1 }}/>
            </>) : null}
        </>)}
      {actions ? <div className="mt-0.5 flex flex-col items-end gap-1">{actions}</div> : null}
    </div>);
    const cardSurfaceClass = blockedSelectionTooltip
        ? 'opacity-60'
        : selectable && selected
            ? 'bg-emerald-500/18 ring-2 ring-emerald-600 ring-offset-2 ring-offset-surface dark:bg-emerald-500/25 dark:ring-emerald-400'
            : selectable && rankBadge
                ? 'ring-1 ring-emerald-500/25 ring-offset-1 ring-offset-surface'
                : !selectable && rankBadge
                    ? 'ring-2 ring-emerald-500/35 ring-offset-2 ring-offset-surface'
                    : '';
    const cardInner = (<Card padded={false} className={`px-3 py-2 ${cardSurfaceClass}`}>
      {blockedSelectionTooltip ? (<div className="flex w-full cursor-not-allowed items-start gap-2.5 text-left">
          <span className="mt-0.5 flex h-6 w-6 shrink-0 items-center justify-center" aria-hidden>
            <span className="inline-flex h-[18px] w-[18px] rounded-full border-2 border-neutral-400/40 bg-neutral-100/90 dark:border-neutral-600 dark:bg-zinc-800/90"/>
          </span>
          <div className="flex min-w-0 flex-1 items-start justify-between gap-3">
            <div className="min-w-0 flex-1">{leftBlock}</div>
            {rightBlock}
          </div>
        </div>) : selectable ? (<button type="button" onClick={onToggleSelect} aria-pressed={selected} className={`flex w-full items-start gap-2.5 rounded-md text-left outline-none transition-[opacity,background-color] focus-visible:ring-2 focus-visible:ring-emerald-500/60 ${selectionAddDisabled && !selected ? 'cursor-not-allowed opacity-45' : ''}`}>
          <span className="mt-0.5 flex h-6 w-6 shrink-0 items-center justify-center" aria-hidden>
            {selected ? (<CheckCircleIcon className="text-emerald-600 dark:text-emerald-400" sx={{ fontSize: 22 }}/>) : (<span className="inline-flex h-[18px] w-[18px] rounded-full border-2 border-neutral-400/90 bg-white/80 dark:border-neutral-500 dark:bg-zinc-900/80"/>)}
          </span>
          <div className="flex min-w-0 flex-1 items-start justify-between gap-3">
            <div className="min-w-0 flex-1">{leftBlock}</div>
            {rightBlock}
          </div>
        </button>) : (<div className="flex items-start justify-between gap-3">
          {profileTo ? (<Link to={profileTo} className="min-w-0 flex-1 rounded focus-visible:outline focus-visible:outline-2">
              {leftBlock}
            </Link>) : (<div className="min-w-0 flex-1">{leftBlock}</div>)}
          {rightBlock}
        </div>)}
    </Card>);
    if (blockedSelectionTooltip) {
        return (<Tooltip title={blockedSelectionTooltip} arrow placement="top">
        <span className="block w-full">{cardInner}</span>
      </Tooltip>);
    }
    return cardInner;
};
