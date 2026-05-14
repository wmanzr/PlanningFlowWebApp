import Typography from '@mui/material/Typography';
import LinearProgress from '@mui/material/LinearProgress';
import { type ScoreBreakdownResponseDto } from '@/types';
const PERCENT_MAX = 100;
const toPercent = (value: number, max = PERCENT_MAX): number => {
    if (!Number.isFinite(value))
        return 0;
    if (max <= 0)
        return 0;
    return Math.max(0, Math.min(PERCENT_MAX, Math.round((value / max) * PERCENT_MAX)));
};
export interface ScoreBarsProps {
    score: ScoreBreakdownResponseDto;
}
export const ScoreBars = ({ score }: ScoreBarsProps) => {
    const max = Math.max(1, score.totalScore, score.skillScore, score.geoScore, score.loadScore);
    return (<div className="grid gap-1.5">
      <Bar label="Общий" value={score.totalScore} percent={toPercent(score.totalScore, max)}/>
      <Bar label="Навыки" value={score.skillScore} percent={toPercent(score.skillScore, max)}/>
      <Bar label="Гео" value={score.geoScore} percent={toPercent(score.geoScore, max)}/>
      <Bar label="Загрузка" value={score.loadScore} percent={toPercent(score.loadScore, max)}/>
    </div>);
};
const Bar = ({ label, value, percent, }: {
    label: string;
    value: number;
    percent: number;
}) => (<div>
    <div className="flex items-center justify-between">
      <Typography variant="caption" color="text.secondary">{label}</Typography>
      <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>{value.toFixed(2)}</Typography>
    </div>
    <LinearProgress variant="determinate" value={percent} sx={{ height: 6, borderRadius: 3, mt: 0.5 }}/>
  </div>);
