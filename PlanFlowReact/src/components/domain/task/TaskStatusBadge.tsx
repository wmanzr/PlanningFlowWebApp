import { Badge, type BadgeProps } from '@/components/ui';
import { TaskStatus } from '@/types';
const TONE_MAP: Record<TaskStatus, NonNullable<BadgeProps['tone']>> = {
    [TaskStatus.OPEN]: 'neutral',
    [TaskStatus.ASSIGNED]: 'info',
    [TaskStatus.IN_PROGRESS]: 'accent',
    [TaskStatus.DONE]: 'success',
    [TaskStatus.CANCELLED]: 'danger',
};
const LABEL_MAP: Record<TaskStatus, string> = {
    [TaskStatus.OPEN]: 'Открыта',
    [TaskStatus.ASSIGNED]: 'Назначена',
    [TaskStatus.IN_PROGRESS]: 'В работе',
    [TaskStatus.DONE]: 'Завершена',
    [TaskStatus.CANCELLED]: 'Отменена',
};
export interface TaskStatusBadgeProps {
    status: TaskStatus;
}
export const TaskStatusBadge = ({ status }: TaskStatusBadgeProps) => (<Badge tone={TONE_MAP[status]}>{LABEL_MAP[status]}</Badge>);
