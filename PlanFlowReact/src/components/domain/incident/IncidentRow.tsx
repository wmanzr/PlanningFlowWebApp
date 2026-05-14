import { type ReactNode } from 'react';
import Typography from '@mui/material/Typography';
import { Card, formatDateTime } from '@/components/ui';
import type { IncidentResponseDto } from '@/types';
import { IncidentSeverityBadge, IncidentStatusBadge } from './IncidentBadge';
export interface IncidentRowProps {
    incident: IncidentResponseDto;
    actions?: ReactNode;
}
export const IncidentRow = ({ incident, actions }: IncidentRowProps) => (<Card>
    <div className="flex items-start justify-between gap-3">
      <div className="flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <IncidentSeverityBadge severity={incident.severity}/>
          <IncidentStatusBadge status={incident.status}/>
          <Typography variant="caption" color="text.secondary">
            {formatDateTime(incident.createdAt)}
          </Typography>
        </div>
        <Typography variant="body2" className="mt-2">
          {incident.description}
        </Typography>
        {incident.resolutionNotes ? (<Typography variant="caption" color="text.secondary" className="mt-1" sx={{ display: 'block' }}>
            <strong>Резолюция:</strong> {incident.resolutionNotes}
            {incident.resolvedAt ? ` • ${formatDateTime(incident.resolvedAt)}` : ''}
          </Typography>) : null}
      </div>
      {actions ? <div className="flex flex-wrap gap-2">{actions}</div> : null}
    </div>
  </Card>);
