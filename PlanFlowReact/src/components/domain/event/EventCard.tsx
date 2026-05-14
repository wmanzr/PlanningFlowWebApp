import { type ReactNode } from 'react';
import Typography from '@mui/material/Typography';
import { Card, formatDateTime } from '@/components/ui';
import { SelfOrProfileLink } from '@/components/domain/user/SelfOrProfileLink';
import type { EventResponseDto, UserId } from '@/types';
import { EventStatusBadge } from './EventStatusBadge';
export interface EventCardProps {
    event: EventResponseDto;
    onClick?: (id: EventResponseDto['id']) => void;
    rightSlot?: ReactNode;
    userNameById?: ReadonlyMap<UserId, string>;
    viewerUserId?: UserId;
    hideCoordinators?: boolean;
}
export function surnameWithInitials(fullName: string | undefined): string {
    if (!fullName)
        return '—';
    const parts = fullName.trim().split(/\s+/).filter(Boolean);
    if (parts.length === 0)
        return '—';
    const last = parts[0] ?? '';
    const first = parts[1]?.[0] ? `${parts[1][0]}.` : '';
    const middle = parts[2]?.[0] ? `${parts[2][0]}.` : '';
    const initials = `${first}${middle}`;
    return initials ? `${last} ${initials}` : last;
}
export const EventCard = ({ event, onClick, rightSlot, userNameById, viewerUserId, hideCoordinators = false, }: EventCardProps) => {
    const interactive = onClick !== undefined;
    const tasksCount = event.tasksCount ?? 0;
    return (<Card className={[interactive ? 'cursor-pointer transition hover:border-button/60' : '', 'w-full']
            .filter(Boolean)
            .join(' ')} role={interactive ? 'button' : undefined} tabIndex={interactive ? 0 : undefined} onClick={interactive ? () => onClick(event.id) : undefined} onKeyDown={interactive
            ? (event_) => {
                if (event_.key === 'Enter' || event_.key === ' ') {
                    event_.preventDefault();
                    onClick(event.id);
                }
            }
            : undefined}>
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <Typography variant="subtitle1" component="h3" className="break-words font-semibold leading-snug">
              {event.title}
            </Typography>
            <EventStatusBadge status={event.status}/>
          </div>
          <Typography variant="body2" color="text.secondary" className="mt-0.5 line-clamp-2 leading-snug">
            {event.description?.trim() ? event.description : '—'}
          </Typography>

          <div className="mt-2 space-y-1.5 text-left">
            <div className="grid grid-cols-2 gap-x-3 gap-y-0.5">
              <div className="min-w-0">
                <Typography variant="caption" sx={{ fontWeight: 500 }} color="text.primary" component="div">
                  Начало
                </Typography>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }} className="leading-tight">
                  {formatDateTime(event.startDate)}
                </Typography>
              </div>
              <div className="min-w-0 text-right">
                <Typography variant="caption" sx={{ fontWeight: 500 }} color="text.primary" component="div">
                  Завершение
                </Typography>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }} className="leading-tight">
                  {formatDateTime(event.endDate)}
                </Typography>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-x-3 gap-y-0.5">
              {hideCoordinators ? (<div className="min-w-0 col-span-2 text-right sm:col-span-1 sm:col-start-2">
                  <Typography variant="caption" sx={{ fontWeight: 500 }} color="text.primary" component="div">
                    Задачи
                  </Typography>
                  <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }} className="leading-tight">
                    {tasksCount}
                  </Typography>
                </div>) : (<>
                  <div className="min-w-0">
                    <Typography variant="caption" sx={{ fontWeight: 500 }} color="text.primary" component="div">
                      Координаторы
                    </Typography>
                    <Typography variant="caption" color="text.secondary" component="div" sx={{ display: 'block' }} className="leading-tight break-words">
                      <span onClick={(e) => e.stopPropagation()} className="relative z-[1] inline pointer-events-auto">
                        {event.coordinatorIds.length === 0 ? ('не назначены') : (<>
                            {event.coordinatorIds.slice(0, 2).map((id, i) => (<span key={String(id)}>
                                {i > 0 ? ', ' : null}
                                <SelfOrProfileLink subjectUserId={id} viewerUserId={viewerUserId} nameLabel={surnameWithInitials(userNameById?.get(id))} className="text-primary underline-offset-2 hover:underline"/>
                              </span>))}
                            {event.coordinatorIds.length > 2
                    ? ` +${event.coordinatorIds.length - 2}`
                    : null}
                          </>)}
                      </span>
                    </Typography>
                  </div>
                  <div className="min-w-0 text-right">
                    <Typography variant="caption" sx={{ fontWeight: 500 }} color="text.primary" component="div">
                      Задачи
                    </Typography>
                    <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }} className="leading-tight">
                      {tasksCount}
                    </Typography>
                  </div>
                </>)}
            </div>
          </div>
        </div>
        {rightSlot ? <div className="shrink-0">{rightSlot}</div> : null}
      </div>
    </Card>);
};
