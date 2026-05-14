import Typography from '@mui/material/Typography';
import { Card, CardHeader } from '@/components/ui';
import { UserRole, type UserResponseDto } from '@/types';
function formatWorkedHours(hours: number): string {
    if (!Number.isFinite(hours) || hours <= 0) {
        return '0';
    }
    return hours.toLocaleString('ru-RU', { maximumFractionDigits: 1, minimumFractionDigits: 0 });
}
function formatActivityCount(value: unknown): string {
    const n = Number(value);
    const safe = Number.isFinite(n) ? Math.trunc(n) : 0;
    const clamped = safe < 0 ? 0 : safe;
    return clamped.toLocaleString('ru-RU', { maximumFractionDigits: 0 });
}
const activityMetricSx = {
    fontWeight: 700,
    mt: 0.5,
    fontVariantNumeric: 'tabular-nums' as const,
    color: 'text.primary',
};
export interface UserActivityReadOnlyProps {
    user: UserResponseDto;
    className?: string;
}
export function UserActivityReadOnly({ user, className }: UserActivityReadOnlyProps) {
    if (user.roles.includes(UserRole.ADMIN)) {
        return null;
    }
    return (<Card className={className}>
      <CardHeader title="Активность"/>
      {user.roles.includes(UserRole.COORDINATOR) ? (<div className="grid gap-4 sm:grid-cols-3">
          <div>
            <Typography variant="body2" color="text.secondary">
              Мероприятий завершено
            </Typography>
            <Typography variant="h5" sx={activityMetricSx}>
              {formatActivityCount(user.coordinatorCompletedEventsCount)}
            </Typography>
          </div>
          <div>
            <Typography variant="body2" color="text.secondary">
              Задач создано
            </Typography>
            <Typography variant="h5" sx={activityMetricSx}>
              {formatActivityCount(user.coordinatorTasksCreatedCount)}
            </Typography>
          </div>
          <div>
            <Typography variant="body2" color="text.secondary">
              Бронирований создано
            </Typography>
            <Typography variant="h5" sx={activityMetricSx}>
              {formatActivityCount(user.coordinatorBookingsCreatedCount)}
            </Typography>
          </div>
        </div>) : user.roles.includes(UserRole.ORGANIZER) ? (<div className="grid gap-4 sm:grid-cols-3">
          <div>
            <Typography variant="body2" color="text.secondary">
              Мероприятий создано
            </Typography>
            <Typography variant="h5" sx={activityMetricSx}>
              {formatActivityCount(user.organizerEventsCreatedCount)}
            </Typography>
          </div>
          <div>
            <Typography variant="body2" color="text.secondary">
              Задач создано
            </Typography>
            <Typography variant="h5" sx={activityMetricSx}>
              {formatActivityCount(user.organizerTasksCreatedCount)}
            </Typography>
          </div>
          <div>
            <Typography variant="body2" color="text.secondary">
              Бронирований создано
            </Typography>
            <Typography variant="h5" sx={activityMetricSx}>
              {formatActivityCount(user.organizerBookingsCreatedCount)}
            </Typography>
          </div>
        </div>) : user.roles.includes(UserRole.PARTICIPANT) ? (<div className="grid gap-4 sm:grid-cols-3">
          <div>
            <Typography variant="body2" color="text.secondary">
              Задач выполнено
            </Typography>
            <Typography variant="h5" sx={activityMetricSx}>
              {formatActivityCount(user.completedTasksCount)}
            </Typography>
          </div>
          <div>
            <Typography variant="body2" color="text.secondary">
              Мероприятий с участием
            </Typography>
            <Typography variant="h5" sx={activityMetricSx}>
              {formatActivityCount(user.eventsParticipatedCount)}
            </Typography>
          </div>
          <div>
            <Typography variant="body2" color="text.secondary">
              Часов отработано
            </Typography>
            <Typography variant="h5" sx={activityMetricSx}>
              {formatWorkedHours(user.totalWorkedHours)} ч
            </Typography>
          </div>
        </div>) : (<Typography variant="body2" color="text.secondary">
          Сводка активности показывается для организаторов, координаторов и исполнителей.
        </Typography>)}
    </Card>);
}
