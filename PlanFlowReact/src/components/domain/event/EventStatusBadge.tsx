import { Badge, type BadgeProps } from '@/components/ui';
import { EventStatus } from '@/types';
const TONE_MAP: Record<EventStatus, NonNullable<BadgeProps['tone']>> = {
    [EventStatus.DRAFT]: 'neutral',
    [EventStatus.PLANNING]: 'info',
    [EventStatus.ACTIVE]: 'info',
    [EventStatus.COMPLETED]: 'success',
    [EventStatus.CANCELLED]: 'danger',
};
export const EVENT_STATUS_LABEL: Record<EventStatus, string> = {
    [EventStatus.DRAFT]: 'Черновик',
    [EventStatus.PLANNING]: 'Планирование',
    [EventStatus.ACTIVE]: 'Активно',
    [EventStatus.COMPLETED]: 'Завершено',
    [EventStatus.CANCELLED]: 'Отменено',
};
export interface EventStatusBadgeProps {
    status: EventStatus;
}
export const EventStatusBadge = ({ status }: EventStatusBadgeProps) => (<Badge tone={TONE_MAP[status]}>{EVENT_STATUS_LABEL[status]}</Badge>);
