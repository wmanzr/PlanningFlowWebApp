import Typography from '@mui/material/Typography';
import { Card, formatDateTime } from '@/components/ui';
import type { IncidentResponseDto } from '@/types';
import { INCIDENT_SEVERITY_LABEL, IncidentSeverityBadge, IncidentStatusBadge, } from './IncidentBadge';
export interface IncidentCardProps {
    incident: IncidentResponseDto;
    className?: string;
    variant?: 'preview' | 'default';
}
export const IncidentCard = ({ incident, className, variant = 'default' }: IncidentCardProps) => {
    const cardClass = [
        'min-w-0 overflow-hidden',
        variant === 'preview' ? 'py-2' : '',
        className ?? '',
    ]
        .filter(Boolean)
        .join(' ');
    return (<Card className={cardClass}>
      <div className="flex min-w-0 items-start justify-between gap-2">
        <div className="min-w-0 flex-1 overflow-hidden">
          <div className="flex min-w-0 max-w-full items-center gap-2">
            <Typography variant="subtitle1" component="h3" noWrap title={incident.description} sx={{
            fontWeight: 600,
            minWidth: 0,
            flex: '0 1 auto',
            maxWidth: '100%',
        }}>
              {incident.description}
            </Typography>
            <span className="flex shrink-0 items-center gap-2">
              <IncidentSeverityBadge severity={incident.severity}/>
              <IncidentStatusBadge status={incident.status}/>
            </span>
          </div>
        </div>
      </div>
      {variant === 'preview' ? (<div className="mt-2 flex flex-wrap gap-x-4 gap-y-0.5 text-left text-xs text-paragraph">
          <span>
            <span className="font-medium text-headline">Создан: </span>
            {formatDateTime(incident.createdAt)}
          </span>
          <span>
            <span className="font-medium text-headline">Критичность: </span>
            {INCIDENT_SEVERITY_LABEL[incident.severity]}
          </span>
        </div>) : (<Typography variant="caption" color="text.secondary" className="mt-2 block">
          {formatDateTime(incident.createdAt)}
        </Typography>)}
    </Card>);
};
