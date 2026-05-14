import type { ReactNode } from 'react';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Chip from '@mui/material/Chip';
import IconButton from '@mui/material/IconButton';
import RefreshIcon from '@mui/icons-material/Refresh';
import { useAppDispatch, useAppSelector } from '@/store';
import { allocateTaskResourcesThunk, cancelTaskThunk, fetchTaskByIdThunk, markTaskDoneThunk, startTaskExecutionThunk, unassignTaskThunk, updateTaskThunk, tasksActions, } from '@/store/slices/tasks/tasksSlice';
import { fetchBookingsForTaskThunk } from '@/store/slices/bookings/bookingsSlice';
import { makeSelectBookingsForTask, selectBookingsListMeta, } from '@/store/slices/bookings/selectors';
import { fetchEventByIdThunk } from '@/store/slices/events/eventsSlice';
import { selectEventById } from '@/store/slices/events/selectors';
import { fetchSkillsThunk } from '@/store/slices/skills/skillsSlice';
import { selectAllSkills } from '@/store/slices/skills/selectors';
import { selectTaskActionMeta, selectTaskById, selectTaskDetailMeta, } from '@/store/slices/tasks/selectors';
import { selectCurrentUser } from '@/store/slices/auth/selectors';
import { acceptAssignmentThunk, rejectAssignmentThunk, } from '@/store/slices/users/usersSlice';
import { selectUsersActionMeta } from '@/store/slices/users/selectors';
import { toastsActions } from '@/store/slices/toasts/toastsSlice';
import { surnameWithInitials } from '@/components/domain/event/EventCard';
import { SelfOrProfileLink } from '@/components/domain/user/SelfOrProfileLink';
import { Badge, Button, Card, CardHeader, ErrorMessage, LoadingArea, MapView, Modal, PageLayout, Textarea, formatDateTime, type MapMarker, } from '@/components/ui';
import { TaskAssignMatchingModal, TaskForm, TaskStatusBadge } from '@/components/domain/task';
import { AllocateResourcesForm, BookingRow } from '@/components/domain/booking';
import { AssignStatus, TaskStatus, UserRole, type EventId, asAssignmentId, asEventId, asTaskId, asUserId, type TaskCreateRequest, type TaskResponseDto, type TaskUpdateRequest, type UserId, } from '@/types';
import { eventMayActivateWhenExecutorStartsTask } from '@/utils/eventActivationOnTaskStart';
import { userIdsEqual } from '@/utils/userIdsEqual';
import { PATHS } from '../paths';
function isExecutorParticipantView(user: ReturnType<typeof selectCurrentUser>): boolean {
    if (!user?.roles?.length)
        return false;
    const { roles } = user;
    return (roles.includes(UserRole.PARTICIPANT) &&
        !roles.includes(UserRole.ADMIN) &&
        !roles.includes(UserRole.ORGANIZER) &&
        !roles.includes(UserRole.COORDINATOR));
}
function TaskBookingsRefreshIcon({ listPending, onRefresh, }: {
    listPending: boolean;
    onRefresh: () => void;
}) {
    return (<IconButton size="small" aria-label="Обновить статусы резервов" disabled={listPending} onClick={onRefresh} edge="end">
      <RefreshIcon fontSize="small" className={listPending ? 'animate-spin' : undefined}/>
    </IconButton>);
}
function bookingsRemainingWord(n: number): string {
    const m = n % 100;
    if (m >= 11 && m <= 14) {
        return 'бронирований';
    }
    const k = n % 10;
    if (k === 1)
        return 'бронирование';
    if (k >= 2 && k <= 4)
        return 'бронирования';
    return 'бронирований';
}
function formatCountdown(ms: number): string {
    if (!Number.isFinite(ms) || ms <= 0)
        return '0:00';
    const totalSec = Math.floor(ms / 1000);
    const days = Math.floor(totalSec / 86400);
    const h = Math.floor((totalSec % 86400) / 3600);
    const m = Math.floor((totalSec % 3600) / 60);
    const s = totalSec % 60;
    if (days > 0)
        return `${days} д ${h} ч`;
    if (h > 0)
        return `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
    return `${m}:${String(s).padStart(2, '0')}`;
}
function assignStatusLabel(status: AssignStatus): string {
    switch (status) {
        case AssignStatus.PENDING:
            return 'Ожидает подтверждения';
        case AssignStatus.ACCEPTED:
            return 'Подтвердил участие';
        case AssignStatus.REJECTED:
            return 'Отклонил';
        case AssignStatus.CANCELLED:
            return 'Снято';
        default:
            return status;
    }
}
function organizerAssignmentLabel(status: AssignStatus): string {
    switch (status) {
        case AssignStatus.PENDING:
            return 'Ожидание подтверждения';
        case AssignStatus.ACCEPTED:
            return 'Подтвердил';
        default:
            return assignStatusLabel(status);
    }
}
function TaskScheduleCountdown({ startIso, endIso }: {
    startIso: string;
    endIso: string;
}) {
    const [now, setNow] = useState(() => Date.now());
    useEffect(() => {
        const id = window.setInterval(() => setNow(Date.now()), 1000);
        return () => window.clearInterval(id);
    }, []);
    const start = new Date(startIso).getTime();
    const end = new Date(endIso).getTime();
    let caption: string;
    let value: string;
    if (now < start) {
        caption = 'До начала';
        value = formatCountdown(start - now);
    }
    else if (now < end) {
        caption = 'До завершения';
        value = formatCountdown(end - now);
    }
    else {
        caption = 'Срок';
        value = 'завершения прошел';
    }
    return (<div className="min-w-[7.5rem] text-right">
      <Typography variant="caption" sx={{ fontWeight: 500 }} color="text.primary" component="div">
        {caption}
      </Typography>
      <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }} className="leading-tight tabular-nums">
        {value}
      </Typography>
    </div>);
}
export const TaskDetailPage = () => {
    const params = useParams<{
        eventId: string;
        taskId: string;
    }>();
    const taskId = useMemo(() => {
        const num = Number.parseInt(params.taskId ?? '', 10);
        return Number.isFinite(num) ? asTaskId(num) : undefined;
    }, [params.taskId]);
    const eventId = useMemo(() => {
        const num = Number.parseInt(params.eventId ?? '', 10);
        return Number.isFinite(num) ? asEventId(num) : undefined;
    }, [params.eventId]);
    const dispatch = useAppDispatch();
    const task = useAppSelector(selectTaskById(taskId));
    const event = useAppSelector(selectEventById(eventId));
    const allSkills = useAppSelector(selectAllSkills);
    const skillNameById = useMemo(() => new Map(allSkills.map((s) => [s.id, s.name] as const)), [allSkills]);
    const detail = useAppSelector(selectTaskDetailMeta);
    const action = useAppSelector(selectTaskActionMeta);
    const usersAction = useAppSelector(selectUsersActionMeta);
    const user = useAppSelector(selectCurrentUser);
    const isExecutorView = isExecutorParticipantView(user);
    const bookingsList = useAppSelector(selectBookingsListMeta);
    const selectBookingsForTask = useMemo(() => makeSelectBookingsForTask(taskId), [taskId]);
    const bookingsForTask = useAppSelector(selectBookingsForTask);
    const bookingsPreviewList = useMemo(() => [...bookingsForTask]
        .sort((a, b) => a.reservedFrom.localeCompare(b.reservedFrom))
        .slice(0, 3), [bookingsForTask]);
    const bookingsMoreCount = Math.max(0, bookingsForTask.length - bookingsPreviewList.length);
    const [isEditOpen, setIsEditOpen] = useState(false);
    const [isMatchingOpen, setIsMatchingOpen] = useState(false);
    const [isAllocateResourcesOpen, setIsAllocateResourcesOpen] = useState(false);
    const [isRejectOpen, setIsRejectOpen] = useState(false);
    const [rejectReason, setRejectReason] = useState('');
    useEffect(() => {
        if (taskId !== undefined)
            void dispatch(fetchTaskByIdThunk(taskId));
    }, [dispatch, taskId]);
    useEffect(() => {
        if (taskId === undefined)
            return;
        void dispatch(fetchBookingsForTaskThunk({ taskId, query: { page: 1, size: 100 } }));
    }, [dispatch, taskId]);
    useEffect(() => {
        if (eventId !== undefined)
            void dispatch(fetchEventByIdThunk(eventId));
    }, [dispatch, eventId]);
    useEffect(() => {
        void dispatch(fetchSkillsThunk({ page: 1, size: 500 }));
    }, [dispatch]);
    const refreshTaskBookings = useCallback(() => {
        if (taskId === undefined)
            return;
        void dispatch(fetchBookingsForTaskThunk({ taskId, query: { page: 1, size: 100 } }));
    }, [dispatch, taskId]);
    const depSig = task?.dependencyIds.join(',') ?? '';
    useEffect(() => {
        if (isExecutorView)
            return;
        if (!depSig)
            return;
        for (const raw of depSig.split(',')) {
            const id = Number.parseInt(raw, 10);
            if (Number.isFinite(id))
                void dispatch(fetchTaskByIdThunk(asTaskId(id)));
        }
    }, [dispatch, depSig, isExecutorView]);
    const mapMarkers = useMemo((): MapMarker[] => {
        if (!task)
            return [];
        const markers: MapMarker[] = [];
        if (event?.latitude != null && event?.longitude != null) {
            markers.push({
                id: 'event',
                lat: event.latitude,
                lng: event.longitude,
                kind: 'event',
                label: event.title,
            });
        }
        if (typeof task.latitude === 'number' && typeof task.longitude === 'number') {
            markers.push({
                id: 'task',
                lat: task.latitude,
                lng: task.longitude,
                kind: 'task',
                label: task.title,
            });
        }
        return markers;
    }, [event, task]);
    const mapCenter = useMemo(() => {
        if (!task)
            return undefined;
        if (typeof task.latitude === 'number' && typeof task.longitude === 'number') {
            return { latitude: task.latitude, longitude: task.longitude };
        }
        if (event?.latitude != null && event?.longitude != null) {
            return { latitude: event.latitude, longitude: event.longitude };
        }
        return undefined;
    }, [event, task]);
    if (taskId === undefined || eventId === undefined) {
        return (<PageLayout title="Задача">
        <ErrorMessage message="Некорректные параметры маршрута"/>
      </PageLayout>);
    }
    if (!task && detail.status === 'pending')
        return <LoadingArea />;
    if (!task && detail.error) {
        return (<PageLayout title="Задача">
        <ErrorMessage message={detail.error.message}/>
      </PageLayout>);
    }
    if (!task)
        return null;
    const assignments = task.assignments ?? [];
    const visibleOrganizerAssignments = assignments.filter((a) => a.status !== AssignStatus.REJECTED && a.status !== AssignStatus.CANCELLED);
    const activeAssignmentSlots = visibleOrganizerAssignments.length;
    let suggestedMatchingPickCount = 1;
    const quotaHint = task.requiredParticipantCount;
    if (typeof quotaHint === 'number' && quotaHint > activeAssignmentSlots) {
        suggestedMatchingPickCount = Math.min(1000, Math.max(1, quotaHint - activeAssignmentSlots));
    }
    const participantAssignmentsVisible = assignments.filter((a) => a.status === AssignStatus.PENDING || a.status === AssignStatus.ACCEPTED);
    const participantAcceptedCount = participantAssignmentsVisible.filter((a) => a.status === AssignStatus.ACCEPTED).length;
    const myAssignment = user
        ? participantAssignmentsVisible.find((a) => userIdsEqual(user.id, a.userId))
        : undefined;
    const myPending = myAssignment?.status === AssignStatus.PENDING;
    const handleEditSubmit = (payload: TaskCreateRequest | {
        id: TaskResponseDto['id'];
        body: TaskUpdateRequest;
    }) => {
        if (!('body' in payload) || !('id' in payload))
            return;
        void dispatch(updateTaskThunk({ id: payload.id, body: payload.body })).then((result) => {
            if (updateTaskThunk.fulfilled.match(result)) {
                setIsEditOpen(false);
            }
        });
    };
    const handleRemoveFromTask = async (userId: UserId) => {
        try {
            await dispatch(unassignTaskThunk({ id: task.id, userId })).unwrap();
            void dispatch(fetchTaskByIdThunk(task.id));
            dispatch(toastsActions.push({
                level: 'success',
                message: 'Исполнитель снят с задачи',
                ttlMs: 4000,
            }));
        }
        catch {
        }
    };
    const handleAcceptAssignment = () => {
        if (!myAssignment)
            return;
        void dispatch(acceptAssignmentThunk(asAssignmentId(myAssignment.id))).then((r) => {
            if (acceptAssignmentThunk.fulfilled.match(r)) {
                void dispatch(fetchTaskByIdThunk(task.id));
            }
        });
    };
    const handleRejectAssignment = () => {
        const reason = rejectReason.trim();
        if (!myAssignment || reason.length === 0)
            return;
        void dispatch(rejectAssignmentThunk({ id: asAssignmentId(myAssignment.id), body: { reason } })).then((r) => {
            if (rejectAssignmentThunk.fulfilled.match(r)) {
                setIsRejectOpen(false);
                setRejectReason('');
                void dispatch(fetchTaskByIdThunk(task.id));
            }
        });
    };
    const isAssignable = task.status !== TaskStatus.DONE && task.status !== TaskStatus.CANCELLED;
    const canStart = task.status === TaskStatus.ASSIGNED;
    const canFinish = task.status === TaskStatus.IN_PROGRESS;
    const canCancel = task.status !== TaskStatus.DONE && task.status !== TaskStatus.CANCELLED;
    const showEventActivationHint = !isExecutorView &&
        canStart &&
        event &&
        eventMayActivateWhenExecutorStartsTask({
            eventStatus: event.status,
            eventStartIso: event.startDate,
            eventEndIso: event.endDate,
        });
    if (isExecutorView) {
        return (<PageLayout title={task.title} description={event ? `Мероприятие «${event.title}»` : 'Ваша задача'} actions={<div className="flex flex-wrap items-end justify-end gap-3">
            <TaskScheduleCountdown startIso={task.startTime} endIso={task.endTime}/>
          </div>}>
        {action.error ? (<ErrorMessage message={action.error.message} onShown={() => dispatch(tasksActions.clearActionError())}/>) : null}

        <Card className="overflow-hidden border-border/70 bg-gradient-to-br from-surface via-bg to-surface-muted/30 shadow-lg">
          <div className="border-b border-border/50 bg-surface-muted/25 px-5 py-4">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div className="min-w-0 flex-1">
                <Typography variant="overline" color="text.secondary" sx={{ letterSpacing: '0.08em' }}>
                  Задача
                </Typography>
                <div className="mt-1 flex flex-wrap items-center gap-2">
                  <Typography variant="h5" sx={{ fontWeight: 700 }} className="break-words">
                    {task.title}
                  </Typography>
                  <TaskStatusBadge status={task.status}/>
                </div>
              </div>
              <div className="flex shrink-0 flex-wrap items-center justify-end gap-2">
                {canStart ? (<Button size="sm" loading={action.status === 'pending'} onClick={() => void dispatch(startTaskExecutionThunk(task.id))}>
                    Начать задачу
                  </Button>) : null}
                {canFinish ? (<Button size="sm" variant="secondary" loading={action.status === 'pending'} onClick={() => void dispatch(markTaskDoneThunk(task.id))}>
                    Завершить задачу
                  </Button>) : null}
              </div>
            </div>
          </div>

          <div className="space-y-5 px-5 py-6">
            {myPending ? (<Box sx={{
                    p: 2,
                    borderRadius: 2,
                    border: 1,
                    borderColor: 'divider',
                    bgcolor: 'action.hover',
                }}>
                <Typography variant="subtitle2" sx={{ fontWeight: 600 }} gutterBottom>
                  Подтверждение участия
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Подтвердите назначение или откажитесь — координатор увидит статус.
                </Typography>
                <div className="flex flex-wrap gap-2">
                  <Button size="sm" loading={usersAction.status === 'pending'} onClick={handleAcceptAssignment}>
                    Подтвердить участие
                  </Button>
                  <Button size="sm" variant="secondary" onClick={() => setIsRejectOpen(true)}>
                    Отклонить
                  </Button>
                </div>
              </Box>) : null}

            <dl className="grid gap-4 text-sm md:grid-cols-2">
              <Field label="Начало" value={formatDateTime(task.startTime)}/>
              <Field label="Завершение" value={formatDateTime(task.endTime)}/>
              <Field label="Создатель" value={task.createdByUserId === undefined ? ('—') : (<SelfOrProfileLink subjectUserId={asUserId(task.createdByUserId)} viewerUserId={user?.id} nameLabel={surnameWithInitials(task.createdByFullName ?? undefined)}/>)}/>
              <div className="md:col-span-2">
                <dt className="text-xs uppercase tracking-wide text-paragraph">Требуемые навыки</dt>
                <dd className="mt-1 flex flex-wrap gap-2">
                  {task.requiredSkillIds.length === 0 ? (<span className="text-paragraph">—</span>) : (task.requiredSkillIds.map((id) => (<Badge key={id} tone="info">
                        {skillNameById.get(id) ?? 'Навык'}
                      </Badge>)))}
                </dd>
              </div>
            </dl>
          </div>
        </Card>

        <Card className="border-border/60 shadow-sm">
          <CardHeader title="Участники" subtitle={participantAssignmentsVisible.length > 0
                ? `Подтвердили участие: ${participantAcceptedCount} из ${participantAssignmentsVisible.length}`
                : 'Активных назначений нет'}/>
          <div className="space-y-3 px-5 pb-5">
            {participantAssignmentsVisible.length === 0 ? (<Typography variant="body2" color="text.secondary">
                Назначений нет.
              </Typography>) : (<ul className="space-y-2">
                {participantAssignmentsVisible.map((a) => (<li key={a.id} className="flex flex-wrap items-center justify-between gap-2 rounded-xl border border-border/50 bg-surface-muted/20 px-3 py-2">
                    <span className="font-medium text-headline">
                      <SelfOrProfileLink subjectUserId={a.userId} viewerUserId={user?.id} nameLabel={surnameWithInitials(a.participantFullName)} className="font-medium text-primary underline-offset-2 hover:underline"/>
                    </span>
                    <Chip size="small" label={assignStatusLabel(a.status)} variant="outlined"/>
                  </li>))}
              </ul>)}
          </div>
        </Card>

        <Card className="border-border/60 shadow-sm">
          <CardHeader title={<span className="inline-flex items-center gap-0.5">
                Ресурсы
                <TaskBookingsRefreshIcon listPending={bookingsList.status === 'pending'} onRefresh={refreshTaskBookings}/>
              </span>} subtitle="Зарезервировано для этой задачи" actions={<Link to={PATHS.taskBookings(eventId, task.id)}>
                <Button size="sm" variant="ghost">
                  Подробнее
                </Button>
              </Link>}/>
          <div className="space-y-3 px-5 pb-5">
            {bookingsList.error ? <ErrorMessage message={bookingsList.error.message}/> : null}
            {bookingsList.status === 'pending' && bookingsForTask.length === 0 ? <LoadingArea /> : null}
            {bookingsForTask.length === 0 && bookingsList.status !== 'pending' ? (<Typography variant="body2" color="text.secondary">
                Бронирований нет.
              </Typography>) : (<div className="flex flex-col gap-3">
                {bookingsPreviewList.map((b) => (<BookingRow key={b.id} booking={b}/>))}
                {bookingsMoreCount > 0 ? (<Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', pt: 0.5 }}>
                    и еще {bookingsMoreCount} {bookingsRemainingWord(bookingsMoreCount)}
                  </Typography>) : null}
              </div>)}
          </div>
        </Card>

        <Card className="overflow-hidden border-border/60 shadow-sm">
          <CardHeader title="Карта" subtitle="Точки мероприятия и задачи (если заданы координаты)"/>
          <div className="px-3 pb-3">
            <MapView height="280px" zoom={12} {...(mapCenter ? { center: mapCenter } : {})} markers={mapMarkers}/>
          </div>
        </Card>

        <div className="flex flex-wrap gap-2">
          <Link to={PATHS.myTasks}>
            <Button variant="ghost">← Мои задачи</Button>
          </Link>
          {event ? (<Link to={PATHS.eventDetail(event.id)} state={{ returnTaskId: task.id }}>
              <Button variant="ghost">К мероприятию</Button>
            </Link>) : null}
        </div>

        <Modal open={isRejectOpen} onClose={() => {
                setIsRejectOpen(false);
                setRejectReason('');
            }} title="Отклонить назначение" footer={<>
              <Button variant="ghost" onClick={() => {
                    setIsRejectOpen(false);
                    setRejectReason('');
                }}>
                Закрыть
              </Button>
              <Button variant="danger" loading={usersAction.status === 'pending'} disabled={rejectReason.trim().length === 0} onClick={handleRejectAssignment}>
                Отклонить
              </Button>
            </>}>
          <Textarea label="Причина" rows={3} value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} placeholder="Кратко укажите причину"/>
        </Modal>
      </PageLayout>);
    }
    return (<PageLayout title={task.title} description={event
            ? `Мероприятие «${event.title}»`
            : 'Карточка задачи в рамках выбранного мероприятия'} actions={<div className="flex flex-wrap gap-2">
          <Button variant="secondary" onClick={() => setIsEditOpen(true)}>
            Редактировать
          </Button>
        </div>}>
      {action.error ? (<ErrorMessage message={action.error.message} onShown={() => dispatch(tasksActions.clearActionError())}/>) : null}
      {showEventActivationHint ? (<Typography variant="body2" color="text.secondary" className="mb-4 max-w-3xl">
          Мероприятие «{event?.title ?? '—'}» в статусе планирования, срок уже начался. После нажатия «В работу» оно
          станет активным, если это первый переход любой задачи этого мероприятия в статус «в работе» (как на
          сервере).
        </Typography>) : null}
      <Card>
        <CardHeader title={<div className="flex items-center gap-3">
              <span>{task.title}</span>
              <TaskStatusBadge status={task.status}/>
            </div>} actions={<div className="flex flex-wrap gap-2">
              {canStart ? (<Button size="sm" loading={action.status === 'pending'} onClick={() => void dispatch(startTaskExecutionThunk(task.id))}>
                  В работу
                </Button>) : null}
              {canFinish ? (<Button size="sm" loading={action.status === 'pending'} onClick={() => dispatch(markTaskDoneThunk(task.id))}>
                  Завершить
                </Button>) : null}
              {canCancel ? (<Button size="sm" variant="danger" loading={action.status === 'pending'} onClick={() => dispatch(cancelTaskThunk(task.id))}>
                  Отменить
                </Button>) : null}
            </div>}/>
        <dl className="grid gap-4 px-5 pb-5 text-sm md:grid-cols-2">
          <Field label="Начало" value={formatDateTime(task.startTime)}/>
          <Field label="Завершение" value={formatDateTime(task.endTime)}/>
          <Field label="Координаты" value={typeof task.latitude === 'number' && typeof task.longitude === 'number'
            ? `${task.latitude.toFixed(4)}, ${task.longitude.toFixed(4)}`
            : '—'}/>
          <Field label="Создатель" value={task.createdByUserId === undefined ? ('—') : (<SelfOrProfileLink subjectUserId={asUserId(task.createdByUserId)} viewerUserId={user?.id} nameLabel={surnameWithInitials(task.createdByFullName ?? undefined)}/>)}/>
          <div className="md:col-span-2">
            <dt className="text-xs uppercase tracking-wide text-paragraph">Требуемые навыки</dt>
            <dd className="mt-1 flex flex-wrap gap-2">
              {task.requiredSkillIds.length === 0 ? (<span className="text-paragraph">—</span>) : (task.requiredSkillIds.map((id) => (<Badge key={id} tone="info">
                    {skillNameById.get(id) ?? 'Навык'}
                  </Badge>)))}
            </dd>
          </div>
          <div className="md:col-span-2">
            <dt className="text-xs uppercase tracking-wide text-paragraph">Зависимости</dt>
            <dd className="mt-1 flex flex-wrap gap-2">
              {task.dependencyIds.length === 0 ? (<span className="text-paragraph">—</span>) : (task.dependencyIds.map((id) => (<TaskDependencyLink key={id} eventId={eventId} depId={id}/>)))}
            </dd>
          </div>
        </dl>
      </Card>

      <Card>
        <CardHeader title="Назначения" actions={<Button size="sm" disabled={!isAssignable} onClick={() => setIsMatchingOpen(true)}>
              Назначить на задачу
            </Button>}/>
        <div className="space-y-3 px-5 pb-5">
          {visibleOrganizerAssignments.length === 0 ? (<Typography variant="body2" color="text.secondary">
              Пока никого не назначили или все активные назначения сняты.
            </Typography>) : (<ul className="space-y-2">
              {visibleOrganizerAssignments.map((a) => (<li key={a.id} className="group flex flex-wrap items-center justify-between gap-2 rounded-xl border border-border/50 bg-surface-muted/20 px-3 py-2">
                  <div className="flex min-w-0 flex-1 flex-wrap items-center gap-2">
                    <span className="font-medium text-headline">
                      <SelfOrProfileLink subjectUserId={a.userId} viewerUserId={user?.id} nameLabel={surnameWithInitials(a.participantFullName)} className="font-medium text-primary underline-offset-2 hover:underline"/>
                    </span>
                    <Chip size="small" label={organizerAssignmentLabel(a.status)} variant="outlined"/>
                  </div>
                  {isAssignable ? (<Button size="sm" variant="danger" className="shrink-0 opacity-0 transition-opacity group-hover:opacity-100 group-focus-within:opacity-100" loading={action.status === 'pending'} onClick={() => void handleRemoveFromTask(a.userId)}>
                      Снять с задачи
                    </Button>) : null}
                </li>))}
            </ul>)}
        </div>
      </Card>

      <Card>
        <CardHeader title={<span className="inline-flex items-center gap-0.5">
              Бронирование ресурсов
              <TaskBookingsRefreshIcon listPending={bookingsList.status === 'pending'} onRefresh={refreshTaskBookings}/>
            </span>} actions={<>
              <Link to={PATHS.taskBookings(eventId, task.id)}>
                <Button size="sm" variant="ghost">
                  Управление резервами
                </Button>
              </Link>
              <Button size="sm" disabled={!isAssignable} onClick={() => setIsAllocateResourcesOpen(true)}>
                Забронировать ресурс
              </Button>
            </>}/>
        <div className="space-y-3 px-5 pb-5">
          {bookingsList.error ? <ErrorMessage message={bookingsList.error.message}/> : null}
          {bookingsList.status === 'pending' && bookingsForTask.length === 0 ? <LoadingArea /> : null}
          {bookingsForTask.length === 0 && bookingsList.status !== 'pending' ? (<Typography variant="body2" color="text.secondary">
              Бронирований нет — укажите наименование и нажмите «Забронировать ресурс».
            </Typography>) : (<div className="flex flex-col gap-3">
              {bookingsPreviewList.map((b) => (<BookingRow key={b.id} booking={b}/>))}
              {bookingsMoreCount > 0 ? (<Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', pt: 0.5 }}>
                  и еще {bookingsMoreCount} {bookingsRemainingWord(bookingsMoreCount)}
                </Typography>) : null}
            </div>)}
        </div>
      </Card>

      <TaskAssignMatchingModal open={isMatchingOpen} onClose={() => setIsMatchingOpen(false)} taskId={task.id} eventId={eventId} initialPickCount={suggestedMatchingPickCount} onAssigned={() => void dispatch(fetchTaskByIdThunk(task.id))}/>

      <Modal open={isAllocateResourcesOpen} onClose={() => setIsAllocateResourcesOpen(false)} title="Забронировать ресурс" size="md">
        <>
          <CardHeader title={`Задача «${task.title}»`}/>
          <AllocateResourcesForm defaultFrom={task.startTime} defaultTo={task.endTime} {...(event
        ? {
            eventForBookingWindow: {
                startDate: event.startDate,
                endDate: event.endDate,
            },
        }
        : {})} submitting={action.status === 'pending'} onCancel={() => setIsAllocateResourcesOpen(false)} onSubmit={(body) => {
            void dispatch(allocateTaskResourcesThunk({ id: task.id, body })).then((result) => {
                if (allocateTaskResourcesThunk.fulfilled.match(result)) {
                    setIsAllocateResourcesOpen(false);
                    void dispatch(fetchBookingsForTaskThunk({ taskId: task.id, query: { page: 1, size: 100 } }));
                    dispatch(toastsActions.push({
                        level: 'success',
                        message: 'Ресурс забронирован',
                        ttlMs: 4000,
                    }));
                }
            });
        }}/>
        </>
      </Modal>

      <Modal open={isEditOpen} onClose={() => setIsEditOpen(false)} title="Редактирование задачи" size="lg">
        {user && isEditOpen ? (<TaskForm initial={task} eventId={asEventId(task.eventId ?? eventId)} {...(event ? { eventForScheduleValidation: event } : {})} submitting={action.status === 'pending'} onCancel={() => setIsEditOpen(false)} onSubmit={handleEditSubmit}/>) : null}
      </Modal>

    </PageLayout>);
};
const Field = ({ label, value }: {
    label: string;
    value: ReactNode;
}) => (<div>
    <dt className="text-xs uppercase tracking-wide text-paragraph">{label}</dt>
    <dd className="mt-1 text-headline">{value}</dd>
  </div>);
const TaskDependencyLink = ({ eventId, depId, }: {
    eventId: EventId;
    depId: number;
}) => {
    const tid = asTaskId(depId);
    const dep = useAppSelector(selectTaskById(tid));
    return (<Link to={PATHS.taskDetail(eventId, tid)}>
      <Badge tone="neutral">{dep?.title ?? 'Связанная задача'}</Badge>
    </Link>);
};
