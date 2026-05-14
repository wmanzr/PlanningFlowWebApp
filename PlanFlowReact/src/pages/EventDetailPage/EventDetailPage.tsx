import type { ReactNode } from 'react';
import { Fragment, useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { useAppDispatch, useAppSelector } from '@/store';
import { toastsActions } from '@/store/slices/toasts/toastsSlice';
import { cancelEventThunk, completeEventThunk, eventsActions, fetchEventByIdThunk, fetchEventDashboardThunk, fetchEventPostMortemAiReportThunk, updateEventThunk, } from '@/store/slices/events/eventsSlice';
import { selectEventActionMeta, selectEventById, selectEventDetailMeta, selectEventDashboard, selectEventPostMortem, } from '@/store/slices/events/selectors';
import { createIncidentThunk, fetchIncidentsForEventThunk, incidentsActions, } from '@/store/slices/incidents/incidentsSlice';
import { makeSelectIncidentsForEvent, selectIncidentsActionMeta, selectIncidentsListMeta, } from '@/store/slices/incidents/selectors';
import { fetchUsersThunk } from '@/store/slices/users/usersSlice';
import { selectAllUsers, selectUsersListMeta } from '@/store/slices/users/selectors';
import { fetchTasksForEventThunk, tasksActions } from '@/store/slices/tasks/tasksSlice';
import { makeSelectTasksByEvent, selectTasksListMeta } from '@/store/slices/tasks/selectors';
import { selectCurrentUser, selectHasRole } from '@/store/slices/auth/selectors';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import Typography from '@mui/material/Typography';
import { Button, Card, CardHeader, EmptyState, ErrorMessage, Input, LoadingArea, MapView, Modal, PageLayout, Textarea, formatDateTime, type MapMarker, } from '@/components/ui';
import { SelfOrProfileLink } from '@/components/domain/user/SelfOrProfileLink';
import { EventDashboardWidget, EventForm, EventStatusBadge, EventAiRecommendationsPanel } from '@/components/domain/event';
import { IncidentCard, IncidentForm } from '@/components/domain/incident';
import { TaskCard, TaskCreateWizard } from '@/components/domain/task';
import { EventStatus, asEventId, asTaskId, asUserId, UserRole, type EventCreateRequest, type EventUpdateRequest, type AppApiError, type IncidentCreateRequest, type UserResponseDto, } from '@/types';
import { userIdsEqual } from '@/utils/userIdsEqual';
import { validationErrorsToToastMessage } from '@/utils/validationErrorsToToastMessage';
import { authStorage } from '@/api';
import { decodeAccessClaims, extractRoles } from '@/store/slices/auth/jwt';
import { PATHS } from '../paths';
const REASON_MIN_LENGTH = 5;
const REASON_MAX_LENGTH = 500;
const cancelSchema = z.object({
    reason: z
        .string()
        .trim()
        .min(REASON_MIN_LENGTH, `Минимум ${REASON_MIN_LENGTH} символов`)
        .max(REASON_MAX_LENGTH),
});
type CancelValues = z.infer<typeof cancelSchema>;
const PANEL_HEADER_ROW = 'flex flex-wrap items-center justify-between gap-3 border-b border-secondary/60 px-5 py-4';
const SUMMARY_PANEL_BODY = 'flex min-h-[280px] flex-1 flex-col gap-3 overflow-y-auto overflow-x-hidden p-5';
const PREVIEW_ROW = 'flex min-w-0 shrink-0 flex-col';
function formatInitials(fullName: string | undefined): string {
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
function normalizeRoleToken(r: unknown): string {
    let s = String(r ?? '').trim();
    if (s.startsWith('ROLE_')) {
        s = s.slice('ROLE_'.length);
    }
    return s.toUpperCase();
}
function userRoleStrings(u: UserResponseDto): string[] {
    return (u.roles ?? []).map((r) => normalizeRoleToken(r));
}
function userHasPlatformAdminRole(u: UserResponseDto): boolean {
    return userRoleStrings(u).includes(UserRole.ADMIN);
}
function userEligibleForCoordinatorPicker(u: UserResponseDto, currentUserId: number | undefined, adminPicker: boolean): boolean {
    if (currentUserId === undefined)
        return false;
    if (userIdsEqual(currentUserId, u.id))
        return false;
    const roles = userRoleStrings(u);
    if (adminPicker) {
        return (roles.includes(UserRole.ORGANIZER) ||
            roles.includes(UserRole.COORDINATOR) ||
            roles.includes(UserRole.ADMIN));
    }
    return roles.includes(UserRole.COORDINATOR);
}
function tasksRemainingWord(n: number): string {
    const m = n % 100;
    if (m >= 11 && m <= 14) {
        return 'задач';
    }
    const k = n % 10;
    if (k === 1)
        return 'задача';
    if (k >= 2 && k <= 4)
        return 'задачи';
    return 'задач';
}
export const EventDetailPage = () => {
    const params = useParams<{
        eventId: string;
    }>();
    const eventId = useMemo(() => {
        const num = Number.parseInt(params.eventId ?? '', 10);
        return Number.isFinite(num) ? asEventId(num) : undefined;
    }, [params.eventId]);
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const location = useLocation();
    const event = useAppSelector(selectEventById(eventId));
    const detail = useAppSelector(selectEventDetailMeta);
    const action = useAppSelector(selectEventActionMeta);
    const tasksList = useAppSelector(selectTasksListMeta);
    const selectTasks = useMemo(() => makeSelectTasksByEvent(eventId), [eventId]);
    const tasks = useAppSelector(selectTasks);
    const selectIncidents = useMemo(() => makeSelectIncidentsForEvent(eventId), [eventId]);
    const incidents = useAppSelector(selectIncidents);
    const incidentsList = useAppSelector(selectIncidentsListMeta);
    const incidentsAction = useAppSelector(selectIncidentsActionMeta);
    const dashboard = useAppSelector(selectEventDashboard);
    const postMortem = useAppSelector(selectEventPostMortem);
    const user = useAppSelector(selectCurrentUser);
    const isPureParticipant = useMemo(() => !!user &&
        user.roles.includes(UserRole.PARTICIPANT) &&
        !user.roles.includes(UserRole.ADMIN) &&
        !user.roles.includes(UserRole.ORGANIZER) &&
        !user.roles.includes(UserRole.COORDINATOR), [user]);
    const isAdminRole = useAppSelector(selectHasRole(UserRole.ADMIN));
    const canManageEvent = useMemo(() => {
        if (!user || !event)
            return false;
        return (isAdminRole ||
            userIdsEqual(event.creatorId, user.id) ||
            event.coordinatorIds.some((id) => userIdsEqual(id, user.id)));
    }, [user, event, isAdminRole]);
    const hasCoordinator = useMemo(() => (event?.coordinatorIds?.length ?? 0) > 0, [event?.coordinatorIds]);
    const canEditEvent = useMemo(() => {
        if (!user || !event)
            return false;
        return isAdminRole || (event.creatorId !== undefined && userIdsEqual(event.creatorId, user.id));
    }, [user, event, isAdminRole]);
    const eventClosed = !!event &&
        (event.status === EventStatus.COMPLETED || event.status === EventStatus.CANCELLED);
    const canMutateLiveEvent = canManageEvent && !eventClosed;
    const canEditLiveEvent = canEditEvent && !eventClosed;
    const usersList = useAppSelector(selectUsersListMeta);
    const allUsers = useAppSelector(selectAllUsers);
    const userById = useMemo(() => new Map(allUsers.map((u) => [u.id, u] as const)), [allUsers]);
    const [isEditOpen, setIsEditOpen] = useState(false);
    const [isCancelOpen, setIsCancelOpen] = useState(false);
    const [isCreateTaskOpen, setIsCreateTaskOpen] = useState(false);
    const [isCreateIncidentOpen, setIsCreateIncidentOpen] = useState(false);
    const [isAssignCoordOpen, setIsAssignCoordOpen] = useState(false);
    const [coordSearch, setCoordSearch] = useState('');
    const isAdminUser = useMemo(() => {
        if (!user)
            return false;
        if (user.roles.includes(UserRole.ADMIN) || isAdminRole)
            return true;
        const token = authStorage.getAccessToken();
        if (token) {
            const claims = decodeAccessClaims(token);
            if (claims && extractRoles(claims).includes(UserRole.ADMIN))
                return true;
        }
        const row = allUsers.find((u) => userIdsEqual(user.id, u.id));
        return row ? userHasPlatformAdminRole(row) : false;
    }, [user, isAdminRole, allUsers]);
    const coordinatorPickerRows = useMemo(() => {
        const q = coordSearch.trim().toLowerCase();
        return allUsers
            .filter((u) => userEligibleForCoordinatorPicker(u, user?.id, isAdminUser))
            .filter((u) => !userIdsEqual(user?.id, u.id))
            .filter((u) => {
            if (!q)
                return true;
            return (u.username.toLowerCase().includes(q) ||
                u.fullName.toLowerCase().includes(q) ||
                u.email.toLowerCase().includes(q));
        });
    }, [allUsers, coordSearch, user, isAdminUser]);
    const showAssignSelfAsCoordinator = useMemo(() => {
        if (!user || !event)
            return false;
        if (!canMutateLiveEvent)
            return false;
        if (event.coordinatorIds.some((id) => userIdsEqual(user.id, id)))
            return false;
        return (isAdminUser ||
            user.roles.includes(UserRole.ORGANIZER) ||
            userIdsEqual(event.creatorId, user.id));
    }, [user, event, isAdminUser, canMutateLiveEvent]);
    const appendCoordinator = useCallback((pickUserId: number) => {
        if (!event || !user)
            return;
        const next = Array.from(new Set([...event.coordinatorIds, pickUserId]));
        void dispatch(updateEventThunk({ eventId: event.id, coordinatorIds: next } as never)).then((r) => {
            if (updateEventThunk.fulfilled.match(r)) {
                setIsAssignCoordOpen(false);
                void dispatch(fetchEventByIdThunk(event.id));
            }
        });
    }, [dispatch, event, user]);
    const cancelForm = useForm<CancelValues>({ defaultValues: { reason: '' } });
    useEffect(() => {
        if (!eventClosed)
            return;
        setIsEditOpen(false);
        setIsCreateTaskOpen(false);
        setIsCreateIncidentOpen(false);
        setIsAssignCoordOpen(false);
    }, [eventClosed]);
    useEffect(() => {
        if (!canEditLiveEvent)
            setIsEditOpen(false);
    }, [canEditLiveEvent]);
    const canLoadUserDirectory = useMemo(() => {
        if (!user?.roles?.length)
            return false;
        return (user.roles.includes(UserRole.ADMIN) ||
            user.roles.includes(UserRole.ORGANIZER) ||
            user.roles.includes(UserRole.COORDINATOR));
    }, [user?.roles]);
    useEffect(() => {
        if (eventId !== undefined) {
            void dispatch(fetchEventByIdThunk(eventId));
            if (canLoadUserDirectory) {
                void dispatch(fetchUsersThunk({ page: 1, size: 400 }));
            }
        }
    }, [dispatch, eventId, canLoadUserDirectory]);
    useEffect(() => {
        if (eventId === undefined || isPureParticipant)
            return;
        if (!event || event.coordinatorIds.length === 0)
            return;
        void dispatch(fetchTasksForEventThunk({ eventId, query: { page: 1, size: 100 } }));
        void dispatch(fetchEventDashboardThunk(eventId));
        void dispatch(fetchIncidentsForEventThunk({ eventId, query: { page: 1, size: 100 } }));
    }, [dispatch, eventId, isPureParticipant, event]);
    useEffect(() => {
        if (eventId === undefined || !hasCoordinator || !canManageEvent)
            return;
        if (!event || event.status !== EventStatus.COMPLETED)
            return;
        void dispatch(fetchEventPostMortemAiReportThunk(eventId));
    }, [dispatch, eventId, event?.id, event?.status, hasCoordinator, canManageEvent]);
    useEffect(() => {
        if (!event || event.coordinatorIds.length > 0)
            return;
        setIsCreateTaskOpen(false);
        setIsCreateIncidentOpen(false);
    }, [event]);
    const mapMarkers = useMemo<MapMarker[]>(() => {
        if (!event)
            return [];
        const result: MapMarker[] = [];
        if (event.latitude !== undefined && event.longitude !== undefined) {
            result.push({
                id: `event-${event.id}`,
                lat: event.latitude,
                lng: event.longitude,
                kind: 'event',
                label: event.title,
            });
        }
        tasks.forEach((task) => {
            if (task.latitude === undefined || task.longitude === undefined)
                return;
            result.push({
                id: `task-${task.id}`,
                lat: task.latitude,
                lng: task.longitude,
                kind: 'task',
                label: task.title,
            });
        });
        return result;
    }, [event, tasks]);
    const mapCenter = event?.latitude !== undefined && event?.longitude !== undefined
        ? { latitude: event.latitude, longitude: event.longitude }
        : undefined;
    const tasksPreviewList = useMemo(() => [...tasks].sort((a, b) => a.startTime.localeCompare(b.startTime)).slice(0, 3), [tasks]);
    const incidentsPreviewList = useMemo(() => incidents.slice(0, 3), [incidents]);
    if (eventId === undefined) {
        return (<PageLayout title="Мероприятие">
        <ErrorMessage message="Некорректный идентификатор мероприятия"/>
      </PageLayout>);
    }
    if (!event && detail.status === 'pending')
        return <LoadingArea />;
    if (!event && detail.error) {
        return (<PageLayout title="Мероприятие">
        <ErrorMessage message={detail.error.message}/>
      </PageLayout>);
    }
    if (!event)
        return null;
    if (isPureParticipant) {
        const returnTaskId = (location.state as {
            returnTaskId?: number;
        } | null)?.returnTaskId;
        return (<PageLayout containerMaxWidth={false}>
        <section className="flex flex-col gap-6">
          <div className="flex flex-wrap items-center gap-3">
            <Typography variant="h4" component="h1" sx={{ fontWeight: 700 }}>
              {event.title}
            </Typography>
            <EventStatusBadge status={event.status}/>
          </div>
          <Typography variant="body2" color="text.secondary" className="max-w-3xl leading-relaxed">
            {event.description?.trim() ? event.description : '—'}
          </Typography>
          <Card>
            <div className="grid gap-4 p-5 text-sm sm:grid-cols-2">
              <div>
                <Typography variant="caption" sx={{ fontWeight: 600 }} color="text.primary" component="div">
                  Начало мероприятия
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                  {formatDateTime(event.startDate)}
                </Typography>
              </div>
              <div>
                <Typography variant="caption" sx={{ fontWeight: 600 }} color="text.primary" component="div">
                  Окончание
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                  {formatDateTime(event.endDate)}
                </Typography>
              </div>
            </div>
          </Card>
          <div>
            <Button variant="secondary" onClick={() => {
                if (returnTaskId !== undefined && Number.isFinite(returnTaskId)) {
                    navigate(PATHS.taskDetail(event.id, asTaskId(returnTaskId)));
                    return;
                }
                navigate(-1);
            }}>
              Назад к задаче
            </Button>
          </div>
        </section>
      </PageLayout>);
    }
    const canViewTasksPanel = canManageEvent && hasCoordinator;
    const canPlanTasks = canMutateLiveEvent && hasCoordinator;
    const handleStatusAction = (actionFn: typeof completeEventThunk) => {
        void dispatch(actionFn(event.id));
    };
    const handleCancelSubmit = cancelForm.handleSubmit(async (values) => {
        const parsed = cancelSchema.safeParse(values);
        if (!parsed.success)
            return;
        const result = await dispatch(cancelEventThunk({ id: event.id, body: { reason: parsed.data.reason } }));
        if (cancelEventThunk.fulfilled.match(result)) {
            setIsCancelOpen(false);
            cancelForm.reset({ reason: '' });
        }
    });
    const handleEditSubmit = (payload: EventCreateRequest | EventUpdateRequest) => {
        void dispatch(updateEventThunk(payload as EventUpdateRequest))
            .unwrap()
            .then(() => {
            setIsEditOpen(false);
            void dispatch(fetchEventByIdThunk(event.id));
        })
            .catch((raw: unknown) => {
            const err = raw as AppApiError;
            dispatch(toastsActions.push({
                level: 'error',
                message: validationErrorsToToastMessage(err),
                ttlMs: 5000,
            }));
        });
    };
    const handleCreateIncident = (body: IncidentCreateRequest) => {
        void dispatch(createIncidentThunk(body)).then((result) => {
            if (createIncidentThunk.fulfilled.match(result)) {
                setIsCreateIncidentOpen(false);
                void dispatch(fetchIncidentsForEventThunk({ eventId: event.id, query: { page: 1, size: 100 } }));
                navigate(PATHS.incidentDetail(result.payload));
            }
        });
    };
    const isCancellable = event.status !== EventStatus.CANCELLED && event.status !== EventStatus.COMPLETED;
    const hideEventCoordinatorRoster = !!user &&
        user.roles.includes(UserRole.COORDINATOR) &&
        !user.roles.includes(UserRole.ORGANIZER) &&
        !user.roles.includes(UserRole.ADMIN);
    const tasksMoreCount = Math.max(0, tasks.length - tasksPreviewList.length);
    const incidentsMoreCount = Math.max(0, incidents.length - incidentsPreviewList.length);
    return (<PageLayout containerMaxWidth={false}>
      {action.error && !isEditOpen ? (<ErrorMessage message={action.error.message} onShown={() => dispatch(eventsActions.clearActionError())}/>) : null}
      {incidentsAction.error ? (<ErrorMessage message={incidentsAction.error.message} onShown={() => dispatch(incidentsActions.clearActionError())}/>) : null}

      <section className="w-full min-w-0 border-b border-secondary/50 pb-8">
        <div className="flex flex-wrap items-center gap-3">
          <Typography variant="h4" component="h1" sx={{ fontWeight: 700 }}>
            {event.title}
          </Typography>
          <EventStatusBadge status={event.status}/>
        </div>
        <Typography variant="body2" color="text.secondary" className="mt-2">
          {formatDateTime(event.startDate)} — {formatDateTime(event.endDate)}
        </Typography>
        {event.description?.trim() ? (<Typography variant="body1" className="mt-4 w-full min-w-0 text-headline">
            {event.description.trim()}
          </Typography>) : null}

        {canEditLiveEvent || canMutateLiveEvent ? (<div className="mt-5 flex flex-wrap gap-2">
            {canEditLiveEvent ? (<Button size="sm" variant="secondary" onClick={() => {
                    dispatch(eventsActions.clearActionError());
                    setIsEditOpen(true);
                }}>
                Редактировать мероприятие
              </Button>) : null}
            {canMutateLiveEvent && event.status === EventStatus.ACTIVE ? (<Button size="sm" onClick={() => handleStatusAction(completeEventThunk)} loading={action.status === 'pending'}>
                Завершить
              </Button>) : null}
            {canEditLiveEvent && isCancellable ? (<Button size="sm" variant="danger" onClick={() => setIsCancelOpen(true)} disabled={action.status === 'pending'}>
                Отменить мероприятие
              </Button>) : null}
          </div>) : null}

        <dl className={`mt-8 grid w-full min-w-0 grid-cols-1 gap-6 text-sm ${hideEventCoordinatorRoster ? 'sm:grid-cols-2' : 'sm:grid-cols-2 lg:grid-cols-3'}`}>
          <Field label="Создатель" value={event.creatorId === undefined ? ('—') : (<SelfOrProfileLink subjectUserId={asUserId(event.creatorId)} viewerUserId={user?.id} nameLabel={formatInitials(userById.get(event.creatorId)?.fullName)}/>)}/>
          {hideEventCoordinatorRoster ? null : (<Field label="Координаторы" value={<span className="flex flex-wrap items-center gap-x-2 gap-y-1">
                  {event.coordinatorIds.length === 0 ? (<span className="text-paragraph">—</span>) : (<>
                      {event.coordinatorIds.slice(0, 2).map((id, index) => (<Fragment key={id}>
                          {index > 0 ? <span className="text-paragraph">, </span> : null}
                          <SelfOrProfileLink subjectUserId={asUserId(id)} viewerUserId={user?.id} nameLabel={formatInitials(userById.get(id)?.fullName)}/>
                        </Fragment>))}
                      {event.coordinatorIds.length > 2 ? (<span className="text-paragraph">+{event.coordinatorIds.length - 2}</span>) : null}
                    </>)}
                  {canMutateLiveEvent ? (<Button size="icon" variant="ghost" className="h-9 w-9 rounded-full border border-secondary/50" onClick={() => {
                        setCoordSearch('');
                        setIsAssignCoordOpen(true);
                        void dispatch(fetchUsersThunk({ page: 1, size: 400 }));
                    }} aria-label="Назначить координатора">
                      <AddCircleIcon sx={{ fontSize: 20 }}/>
                    </Button>) : null}
                </span>}/>)}
          <Field label="Точка на карте" value={typeof event.latitude === 'number' && typeof event.longitude === 'number'
            ? `${event.latitude.toFixed(4)}, ${event.longitude.toFixed(4)}`
            : 'не задана'}/>
        </dl>
      </section>

      {hasCoordinator ? (<section className="space-y-4">
          <Typography variant="h6" component="h2" sx={{ fontWeight: 600 }}>
            Сводка
          </Typography>
          {dashboard.status === 'pending' && !dashboard.data ? (<Typography variant="body2" color="text.secondary">
              Загрузка статистики…
            </Typography>) : null}
          {dashboard.error ? <ErrorMessage message={dashboard.error.message}/> : null}
          {dashboard.data ? (<EventDashboardWidget data={dashboard.data} variant="embedded"/>) : null}
        </section>) : null}

      <section className="w-full max-w-full overflow-hidden rounded-lg border border-secondary/50 bg-surface-muted/80">
        <div className="flex flex-col gap-1 px-1 py-2 sm:px-0">
          <div className="flex flex-wrap items-center justify-between gap-2 px-3 sm:px-1">
            <Typography variant="caption" color="text.secondary">
              Карта мероприятия и задач с координатами
            </Typography>
            <Link to={PATHS.eventMap(event.id)} className="text-xs font-medium text-primary underline">
              Расширенная карта и фильтр
            </Link>
          </div>
          <div className="w-full max-w-full overflow-hidden">
            <MapView height="140px" zoom={11} {...(mapCenter ? { center: mapCenter } : {})} markers={mapMarkers}/>
          </div>
        </div>
      </section>

      {hasCoordinator ? (<div className="flex w-full min-w-0 flex-col gap-6">
          <div className="grid w-full min-w-0 gap-6 lg:grid-cols-2 lg:items-stretch">
          <Card padded={false} className="flex h-full min-h-0 w-full min-w-0 flex-col cursor-pointer outline-none transition hover:border-primary/40 focus-visible:ring-2 focus-visible:ring-primary/30" role="link" tabIndex={0} onClick={() => navigate(PATHS.eventTasks(event.id))} onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    navigate(PATHS.eventTasks(event.id));
                }
            }}>
            <div className={PANEL_HEADER_ROW}>
              <div>
                <h2 className="text-lg font-semibold text-headline">Задачи</h2>
                <p className="text-sm text-paragraph">
                  Всего: {tasks.length}
                  {tasksList.status === 'pending' ? ' • загрузка…' : ''}
                </p>
              </div>
              {canMutateLiveEvent ? (<Button variant="secondary" size="sm" onClick={(e) => {
                    e.stopPropagation();
                    setIsCreateTaskOpen(true);
                }}>
                  Добавить задачу
                </Button>) : null}
            </div>
            <div className={SUMMARY_PANEL_BODY}>
              {!canViewTasksPanel ? (<EmptyState title="Нет доступа к задачам" description="Задачи доступны организатору, администратору, создателю и координаторам мероприятия."/>) : tasks.length === 0 && tasksList.status !== 'pending' ? (<EmptyState title="Задач нет" description={canPlanTasks
                    ? 'Создайте первую задачу мероприятия.'
                    : 'Задачи по этому мероприятию отображаются в режиме просмотра.'}/>) : null}
              {canViewTasksPanel ? (<div className="flex flex-col gap-2">
                  {tasksPreviewList.map((task) => (<div key={task.id} className={PREVIEW_ROW} onClick={(e) => {
                        e.stopPropagation();
                        navigate(PATHS.taskDetail(event.id, task.id));
                    }} onKeyDown={(e) => {
                        if (e.key === 'Enter' || e.key === ' ') {
                            e.preventDefault();
                            e.stopPropagation();
                            navigate(PATHS.taskDetail(event.id, task.id));
                        }
                    }} role="button" tabIndex={0}>
                      <TaskCard task={task} variant="preview" className="w-full shrink-0"/>
                    </div>))}
                </div>) : null}
              {canViewTasksPanel && tasksMoreCount > 0 ? (<div className="flex justify-center pt-3">
                  <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center' }}>
                    и еще {tasksMoreCount} {tasksRemainingWord(tasksMoreCount)}
                  </Typography>
                </div>) : null}
            </div>
          </Card>

          <Card padded={false} className="flex h-full min-h-0 w-full min-w-0 flex-col cursor-pointer outline-none transition hover:border-primary/40 focus-visible:ring-2 focus-visible:ring-primary/30" role="link" tabIndex={0} onClick={() => navigate(PATHS.eventIncidents(event.id))} onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    navigate(PATHS.eventIncidents(event.id));
                }
            }}>
            <div className={PANEL_HEADER_ROW}>
              <div>
                <h2 className="text-lg font-semibold text-headline">Инциденты</h2>
                <p className="text-sm text-paragraph">
                  Всего: {incidents.length}
                  {incidentsList.status === 'pending' ? ' • загрузка…' : ''}
                </p>
              </div>
              {!eventClosed ? (<Button size="sm" variant="secondary" onClick={(e) => {
                    e.stopPropagation();
                    dispatch(incidentsActions.clearActionError());
                    setIsCreateIncidentOpen(true);
                }}>
                  Создать инцидент
                </Button>) : null}
            </div>
            <div className={SUMMARY_PANEL_BODY}>
              {incidents.length === 0 && incidentsList.status !== 'pending' ? (<EmptyState title="Инцидентов нет" description="На этом мероприятии пока не зафиксировано инцидентов."/>) : null}
              {incidents.length > 0 ? (<div className="flex min-h-0 flex-1 flex-col gap-2">
                  {incidentsPreviewList.map((incident) => (<div key={incident.id} className={PREVIEW_ROW} onClick={(e) => {
                        e.stopPropagation();
                        navigate(PATHS.incidentDetail(incident.id));
                    }} onKeyDown={(e) => {
                        if (e.key === 'Enter' || e.key === ' ') {
                            e.preventDefault();
                            e.stopPropagation();
                            navigate(PATHS.incidentDetail(incident.id));
                        }
                    }} role="button" tabIndex={0}>
                      <IncidentCard incident={incident} variant="preview" className="w-full shrink-0"/>
                    </div>))}
                </div>) : null}
              {incidentsMoreCount > 0 ? (<div className="flex justify-center pt-3">
                  <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center' }}>
                    и еще {incidentsMoreCount}
                  </Typography>
                </div>) : null}
            </div>
          </Card>
          </div>
          {canManageEvent && event.status === EventStatus.COMPLETED ? (<EventAiRecommendationsPanel fetchStatus={postMortem.status} fetchError={postMortem.error} report={postMortem.loadedEventId !== undefined && Number(postMortem.loadedEventId) === Number(event.id) ? postMortem.data : null}/>) : null}
        </div>) : (<Card>
          <EmptyState title="Планирование недоступно" description="Назначьте хотя бы одного координатора мероприятия (блок «Координаторы» выше). После этого здесь появятся задачи и инциденты — для всех ролей, включая организаторов и администраторов."/>
        </Card>)}

      <Modal open={isCreateIncidentOpen && !eventClosed && hasCoordinator} onClose={() => {
            dispatch(incidentsActions.clearActionError());
            setIsCreateIncidentOpen(false);
        }} title="Новый инцидент" size="md">
        {isCreateIncidentOpen && !eventClosed && hasCoordinator && user ? (<>
            <CardHeader title="Описание происшествия"/>
            <IncidentForm reporterId={user.id} eventId={event.id} tasks={tasks} submitting={incidentsAction.status === 'pending'} onCancel={() => {
                dispatch(incidentsActions.clearActionError());
                setIsCreateIncidentOpen(false);
            }} onSubmit={handleCreateIncident}/>
          </>) : null}
      </Modal>

      <Modal open={isCreateTaskOpen && canMutateLiveEvent && hasCoordinator} onClose={() => {
            dispatch(tasksActions.clearActionError());
            setIsCreateTaskOpen(false);
        }} title="Новая задача" size="lg">
        {user && canMutateLiveEvent && hasCoordinator && isCreateTaskOpen ? (<TaskCreateWizard open eventId={event.id} onClose={() => {
                dispatch(tasksActions.clearActionError());
                setIsCreateTaskOpen(false);
            }}/>) : null}
      </Modal>

      {canMutateLiveEvent ? (<Modal open={isAssignCoordOpen} onClose={() => setIsAssignCoordOpen(false)} title="Назначение координатора" size="md">
          <div className="flex flex-col gap-3">
            <Input label="Поиск" value={coordSearch} onChange={(e) => setCoordSearch(e.target.value)} placeholder="По имени или логину"/>
            {showAssignSelfAsCoordinator ? (<div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between sm:gap-4">
                <p className="min-w-0 flex-1 text-xs leading-snug text-paragraph">
                  {isAdminUser
                    ? 'Администратор может назначить себя координатором или выбрать пользователя в списке ниже.'
                    : 'Организатор или создатель мероприятия может назначить себя координатором.'}
                </p>
                <Button size="sm" variant="primary" className="w-full shrink-0 sm:w-auto" disabled={action.status === 'pending'} onClick={() => user && appendCoordinator(user.id)}>
                  Назначить себя координатором
                </Button>
              </div>) : null}
            {!showAssignSelfAsCoordinator && isAdminUser ? (<div className="text-xs text-paragraph">
                Выберите организатора или координатора. Назначить себя нельзя.
              </div>) : null}
            {!showAssignSelfAsCoordinator && !isAdminUser ? (<div className="text-xs text-paragraph">
                Ниже — другие координаторы, которых можно добавить к мероприятию.
              </div>) : null}
            {usersList.status === 'pending' ? (<div className="text-sm text-paragraph">Загрузка…</div>) : null}
            <div className="grid gap-2">
              {coordinatorPickerRows.map((u) => {
                const already = event.coordinatorIds.includes(u.id);
                return (<div key={String(u.id)} className="flex items-center justify-between gap-3 rounded-lg border border-secondary/50 bg-bg px-3 py-2">
                    <div className="min-w-0">
                      <div className="truncate text-sm font-medium text-headline">
                        {u.fullName}
                      </div>
                      <div className="truncate text-xs text-paragraph">@{u.username}</div>
                    </div>
                    <Button size="sm" disabled={already || action.status === 'pending'} onClick={() => appendCoordinator(u.id)}>
                      {already ? 'Назначен' : 'Назначить'}
                    </Button>
                  </div>);
            })}
              {usersList.status !== 'pending' && coordinatorPickerRows.length === 0 ? (<div className="text-sm text-paragraph">Никого не найдено.</div>) : null}
            </div>
          </div>
        </Modal>) : null}

      <Modal open={isEditOpen && canEditLiveEvent} onClose={() => {
            setIsEditOpen(false);
            dispatch(eventsActions.clearActionError());
        }} title="Редактирование мероприятия" size="lg">
        {user && canEditLiveEvent && isEditOpen ? (<EventForm initial={event} submitting={action.status === 'pending'} onCancel={() => setIsEditOpen(false)} onSubmit={handleEditSubmit}/>) : null}
      </Modal>
      <Modal open={isCancelOpen} onClose={() => {
            setIsCancelOpen(false);
            cancelForm.reset({ reason: '' });
        }} title="Отмена мероприятия" description="Укажите причину — она будет сохранена в журнале." footer={<>
            <Button variant="ghost" onClick={() => {
                setIsCancelOpen(false);
                cancelForm.reset({ reason: '' });
            }}>
              Закрыть
            </Button>
            <Button variant="danger" onClick={handleCancelSubmit} loading={action.status === 'pending'}>
              Подтвердить отмену
            </Button>
          </>}>
        <Textarea label="Причина" rows={4} error={cancelForm.formState.errors.reason?.message} {...cancelForm.register('reason')}/>
      </Modal>
      <div className="flex justify-end">
        <Button variant="ghost" onClick={() => navigate(PATHS.home)}>
          ← К списку мероприятий
        </Button>
      </div>
    </PageLayout>);
};
const Field = ({ label, value }: {
    label: string;
    value: ReactNode;
}) => (<div className="min-w-0 w-full">
    <dt className="text-xs uppercase tracking-wide text-paragraph">{label}</dt>
    <dd className="mt-1 min-w-0 break-words text-headline">{value}</dd>
  </div>);
