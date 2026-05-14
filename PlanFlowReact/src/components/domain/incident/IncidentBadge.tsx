import { Badge, type BadgeProps } from '@/components/ui';
import { IncidentSeverity, IncidentStatus } from '@/types';
const SEVERITY_TONE: Record<IncidentSeverity, NonNullable<BadgeProps['tone']>> = {
    [IncidentSeverity.LOW]: 'neutral',
    [IncidentSeverity.MEDIUM]: 'info',
    [IncidentSeverity.HIGH]: 'warning',
    [IncidentSeverity.CRITICAL]: 'danger',
};
export const INCIDENT_SEVERITY_LABEL: Record<IncidentSeverity, string> = {
    [IncidentSeverity.LOW]: 'Низкая',
    [IncidentSeverity.MEDIUM]: 'Средняя',
    [IncidentSeverity.HIGH]: 'Высокая',
    [IncidentSeverity.CRITICAL]: 'Критическая',
};
const STATUS_TONE: Record<IncidentStatus, NonNullable<BadgeProps['tone']>> = {
    [IncidentStatus.OPEN]: 'warning',
    [IncidentStatus.IN_PROGRESS]: 'info',
    [IncidentStatus.RESOLVED]: 'success',
};
export const INCIDENT_STATUS_LABEL: Record<IncidentStatus, string> = {
    [IncidentStatus.OPEN]: 'Открыт',
    [IncidentStatus.IN_PROGRESS]: 'В работе',
    [IncidentStatus.RESOLVED]: 'Решен',
};
export const IncidentSeverityBadge = ({ severity }: {
    severity: IncidentSeverity;
}) => (<Badge tone={SEVERITY_TONE[severity]}>{INCIDENT_SEVERITY_LABEL[severity]}</Badge>);
export const IncidentStatusBadge = ({ status }: {
    status: IncidentStatus;
}) => (<Badge tone={STATUS_TONE[status]}>{INCIDENT_STATUS_LABEL[status]}</Badge>);
