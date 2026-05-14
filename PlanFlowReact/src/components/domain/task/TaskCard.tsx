import type { ReactNode } from 'react';
import Typography from '@mui/material/Typography';
import { Card, formatDateTime } from '@/components/ui';
import type { TaskResponseDto } from '@/types';
import { TaskStatusBadge } from './TaskStatusBadge';
export interface TaskCardProps {
    task: TaskResponseDto;
    onClick?: (id: TaskResponseDto['id']) => void;
    className?: string;
    variant?: 'preview' | 'default';
    titleTrailing?: ReactNode;
    hideDependencies?: boolean;
}
export const TaskCard = ({ task, onClick, className, variant = 'default', titleTrailing, hideDependencies = false, }: TaskCardProps) => {
    const interactive = onClick !== undefined;
    const cardClass = [
        'min-w-0 overflow-hidden',
        interactive ? 'cursor-pointer transition hover:border-button/60' : '',
        variant === 'preview' ? 'py-2' : '',
        className ?? '',
    ]
        .filter(Boolean)
        .join(' ');
    return (<Card className={cardClass} role={interactive ? 'button' : undefined} tabIndex={interactive ? 0 : undefined} onClick={interactive ? () => onClick(task.id) : undefined} onKeyDown={interactive
            ? (e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    onClick(task.id);
                }
            }
            : undefined}>
      <div className="flex min-w-0 items-center justify-between gap-3">
        <div className="min-w-0 flex-1 overflow-hidden">
          <div className="flex min-w-0 max-w-full items-center gap-2">
            <Typography variant="subtitle1" component="h3" noWrap title={task.title} sx={{
            fontWeight: 600,
            minWidth: 0,
            flex: '0 1 auto',
            maxWidth: '100%',
        }}>
              {task.title}
            </Typography>
            <span className="shrink-0">
              <TaskStatusBadge status={task.status}/>
            </span>
          </div>
        </div>
        {titleTrailing ? (<div className="shrink-0" onClick={(e) => e.stopPropagation()} onKeyDown={(e) => e.stopPropagation()} role="presentation">
            {titleTrailing}
          </div>) : null}
      </div>
      {variant === 'preview' ? (<div className="mt-2 flex flex-wrap gap-x-4 gap-y-0.5 text-left text-xs text-paragraph">
          <span>
            <span className="font-medium text-headline">Начало: </span>
            {formatDateTime(task.startTime)}
          </span>
          <span>
            <span className="font-medium text-headline">Завершение: </span>
            {formatDateTime(task.endTime)}
          </span>
        </div>) : (<div className="mt-3 grid grid-cols-2 gap-x-6 gap-y-1">
          <div>
            <Typography variant="caption" sx={{ fontWeight: 500 }} color="text.primary">
              Начало
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
              {formatDateTime(task.startTime)}
            </Typography>
          </div>
          <div>
            <Typography variant="caption" sx={{ fontWeight: 500 }} color="text.primary">
              Завершение
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
              {formatDateTime(task.endTime)}
            </Typography>
          </div>
          <div>
            <Typography variant="caption" sx={{ fontWeight: 500 }} color="text.primary">
              Навыков
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
              {task.requiredSkillIds.length}
            </Typography>
          </div>
          {!hideDependencies ? (<div>
            <Typography variant="caption" sx={{ fontWeight: 500 }} color="text.primary">
              Зависимостей
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
              {task.dependencyIds.length}
            </Typography>
          </div>) : null}
        </div>)}
    </Card>);
};
