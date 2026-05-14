import { Badge, type BadgeProps } from '@/components/ui';
import { BookingStatus } from '@/types';
const TONE_MAP: Record<BookingStatus, NonNullable<BadgeProps['tone']>> = {
    [BookingStatus.REQUESTED]: 'info',
    [BookingStatus.CONFIRMED]: 'success',
    [BookingStatus.FAILED]: 'danger',
    [BookingStatus.CANCELLED]: 'warning',
};
const LABEL_MAP: Record<BookingStatus, string> = {
    [BookingStatus.REQUESTED]: 'Запрошено',
    [BookingStatus.CONFIRMED]: 'Подтверждено',
    [BookingStatus.FAILED]: 'Сбой',
    [BookingStatus.CANCELLED]: 'Отменено',
};
export interface BookingStatusBadgeProps {
    status: BookingStatus;
}
export const BookingStatusBadge = ({ status }: BookingStatusBadgeProps) => (<Badge tone={TONE_MAP[status]}>{LABEL_MAP[status]}</Badge>);
