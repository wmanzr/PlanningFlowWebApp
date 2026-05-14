import { useMemo, type ReactNode } from 'react';
import { Link } from 'react-router-dom';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { Badge, Card } from '@/components/ui';
import { useAppSelector } from '@/store';
import { selectAllSkills } from '@/store/slices/skills/selectors';
import type { RankedCandidateResponseDto } from '@/types';
import { ScoreBars } from './ScoreBars';
const METERS_IN_KM = 1000;
const formatDistance = (meters: number | undefined): string => {
    if (meters === undefined)
        return '—';
    if (meters < METERS_IN_KM)
        return `${Math.round(meters)} м`;
    return `${(meters / METERS_IN_KM).toFixed(1)} км`;
};
const formatHours = (minutes: number): string => {
    const h = minutes / 60;
    const s = h.toFixed(1).replace(/\.0$/, '');
    return `${s} ч`;
};
export interface CandidateRowProps {
    candidate: RankedCandidateResponseDto;
    actions?: ReactNode;
    emphasizeRecommended?: boolean;
    profileTo?: string;
}
export const CandidateRow = ({ candidate, actions, emphasizeRecommended = false, profileTo, }: CandidateRowProps) => {
    const skills = useAppSelector(selectAllSkills);
    const skillNameById = useMemo(() => new Map(skills.map((s) => [s.id, s.name] as const)), [skills]);
    const nameLine = (<div className="flex flex-wrap items-center gap-2">
      <Badge tone="accent">Место {candidate.rank}</Badge>
      <Typography variant="subtitle1" component="h3" sx={{ fontWeight: 600 }}>
        {candidate.candidateFullName}
      </Typography>
      <Typography variant="caption" color="text.secondary">
        @{candidate.candidateUsername}
      </Typography>
    </div>);
    return (<Card className={emphasizeRecommended
            ? 'ring-2 ring-emerald-500/40 ring-offset-2 ring-offset-surface'
            : undefined}>
      <div className="grid gap-4 md:grid-cols-[1fr_auto]">
        <div className="flex flex-col gap-3">
          {profileTo ? (<Link to={profileTo} className="w-fit rounded focus-visible:outline focus-visible:outline-2">
              {nameLine}
            </Link>) : (nameLine)}
          {emphasizeRecommended ? (<Typography variant="caption" color="success.main" sx={{ fontWeight: 500 }}>
              Рекомендовано алгоритмом подбора для этой задачи
            </Typography>) : null}
          <ScoreBars score={candidate.score}/>
          <Box className="grid grid-cols-2 gap-x-4 gap-y-1">
            <div>
              <Typography variant="caption" sx={{ fontWeight: 500 }} color="text.primary">
                Расстояние
              </Typography>
              <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                {formatDistance(candidate.distanceMeters)}
              </Typography>
            </div>
            <div>
              <Typography variant="caption" sx={{ fontWeight: 500 }} color="text.primary">
                Сегодня проработано
              </Typography>
              <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                {formatHours(candidate.workedTodayMinutes)} / лимит {formatHours(candidate.maxDailyLoadMinutes)}
              </Typography>
            </div>
            <div className="col-span-2">
              <Typography variant="caption" sx={{ fontWeight: 500 }} color="text.primary">
                Навыки (пересечение с задачей)
              </Typography>
              <div className="mt-1 flex flex-wrap gap-1">
                {candidate.matchedRequiredSkillIds.length === 0 ? (<Typography variant="caption" color="text.secondary">
                    —
                  </Typography>) : (candidate.matchedRequiredSkillIds.map((id) => (<Badge key={id} tone="info">
                      {skillNameById.get(id) ?? 'Навык'}
                    </Badge>)))}
              </div>
            </div>
          </Box>
        </div>
        {actions ? <div className="flex flex-col items-end gap-2">{actions}</div> : null}
      </div>
    </Card>);
};
