import type { ReactNode } from 'react';
import { Fragment, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
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
import BlockOutlined from '@mui/icons-material/BlockOutlined';
import CheckCircle from '@mui/icons-material/CheckCircle';
import EditOutlined from '@mui/icons-material/EditOutlined';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import { Button, Card, CardHeader, EmptyState, ErrorMessage, Input, LoadingArea, Modal, PageLayout, Spinner, SUMMARY_PREVIEW_PANEL_BODY, SUMMARY_PREVIEW_PANEL_HEADER, Textarea, formatDateTime, geoPointFromLatLng, slicePreviewList, type MapMarker, } from '@/components/ui';
import { SelfOrProfileLink } from '@/components/domain/user/SelfOrProfileLink';
import { EventDashboardWidget, EventForm, EventStatusBadge, EventAiRecommendationsPanel, EventMapPanel } from '@/components/domain/event';
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
const POST_MORTEM_POLL_INTERVAL_MS = 5000;
const POST_MORTEM_POLL_MAX_MS = 2 * 60 * 1000;
const cancelSchema = z.object({
    reason: z
        .string()
        .trim()
        .min(REASON_MIN_LENGTH, `Минимум ${REASON_MIN_LENGTH} символов`)
        .max(REASON_MAX_LENGTH),
});
type CancelValues = z.infer<typeof cancelSchema>;
const PREVIEW_ROW = 'flex min-w-0 shrink-0 flex-col';
const COORD_PICKER_LIST_AREA = 'h-[min(360px,44vh)] min-h-[220px] w-full shrink-0 overflow-hidden rounded-md border border-secondary/35 bg-surface-muted/40';
function formatInitials(fullName: string | undefined): string {
    if (!fullName)
        return '–';
    const parts = fullName.trim().split(/\s+/).filter(Boolean);
    if (parts.length === 0)
        return '–';
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
    const [postMortemPollTimedOut, setPostMortemPollTimedOut] = useState(false);
    const postMortemPollIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
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
    const [createTaskBodyScroll, setCreateTaskBodyScroll] = useState(true);
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
                void dispatch(fetchUsersThunk({ page: 1, size: 500 }));
            }
        }
    }, [dispatch, eventId, canLoadUserDirectory]);
    useEffect(() => {
        if (!isAssignCoordOpen || !canMutateLiveEvent) {
            return;
        }
        void dispatch(fetchUsersThunk({ page: 1, size: 500, role: UserRole.COORDINATOR }));
        if (isAdminUser) {
            void dispatch(fetchUsersThunk({ page: 1, size: 500, role: UserRole.ORGANIZER }));
            void dispatch(fetchUsersThunk({ page: 1, size: 500, role: UserRole.ADMIN }));
        }
    }, [isAssignCoordOpen, canMutateLiveEvent, isAdminUser, dispatch]);
    useEffect(() => {
        if (eventId === undefined || isPureParticipant)
            return;
        if (!event || event.coordinatorIds.length === 0)
            return;
        void dispatch(fetchTasksForEventThunk({ eventId, query: { page: 1, size: 100 } }));
        void dispatch(fetchEventDashboardThunk(eventId));
        void dispatch(fetchIncidentsForEventThunk({ eventId, query: { page: 1, size: 100 } }));
    }, [dispatch, eventId, isPureParticipant, event]);
    const postMortemReportForEvent = event !== undefined &&
        postMortem.loadedEventId !== undefined &&
        Number(postMortem.loadedEventId) === Number(event.id)
        ? postMortem.data
        : null;
    useEffect(() => {
        setPostMortemPollTimedOut(false);
    }, [eventId]);
    useEffect(() => {
        if (postMortemReportForEvent?.status === 'COMPLETED' || postMortemReportForEvent?.status === 'FAILED') {
            if (postMortemPollIntervalRef.current !== null) {
                clearInterval(postMortemPollIntervalRef.current);
                postMortemPollIntervalRef.current = null;
            }
        }
    }, [postMortemReportForEvent?.status]);
    useEffect(() => {
        if (eventId === undefined || !hasCoordinator || !canManageEvent) {
            return undefined;
        }
        if (!event || event.status !== EventStatus.COMPLETED) {
            return undefined;
        }
        let cancelled = false;
        const startedAt = Date.now();
        const stopPolling = () => {
            if (postMortemPollIntervalRef.current !== null) {
                clearInterval(postMortemPollIntervalRef.current);
                postMortemPollIntervalRef.current = null;
            }
        };
        const poll = () => {
            if (cancelled) {
                return;
            }
            void dispatch(fetchEventPostMortemAiReportThunk(eventId));
        };
        poll();
        postMortemPollIntervalRef.current = setInterval(() => {
            if (cancelled) {
                return;
            }
            if (Date.now() - startedAt >= POST_MORTEM_POLL_MAX_MS) {
                setPostMortemPollTimedOut(true);
                stopPolling();
                return;
            }
            poll();
        }, POST_MORTEM_POLL_INTERVAL_MS);
        const maxDurationTimerId = window.setTimeout(() => {
            if (!cancelled) {
                setPostMortemPollTimedOut(true);
                stopPolling();
            }
        }, POST_MORTEM_POLL_MAX_MS);
        return () => {
            cancelled = true;
            stopPolling();
            window.clearTimeout(maxDurationTimerId);
        };
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
    const mapCenter = useMemo(() => geoPointFromLatLng(event?.latitude, event?.longitude), [event?.latitude, event?.longitude]);
    const tasksPreview = useMemo(() => {
        const sorted = [...tasks].sort((a, b) => a.startTime.localeCompare(b.startTime));
        return slicePreviewList(sorted);
    }, [tasks]);
    const incidentsPreview = useMemo(() => slicePreviewList(incidents), [incidents]);
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
            {event.description?.trim() ? event.description : '–'}
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
          <EventMapPanel markers={mapMarkers} {...(mapCenter !== undefined ? { center: mapCenter } : {})}/>
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
    const tasksMoreCount = tasksPreview.moreCount;
    const incidentsMoreCount = incidentsPreview.moreCount;
    return (<PageLayout containerMaxWidth={false}>
      {action.error && !isEditOpen ? (<ErrorMessage message={action.error.message} onShown={() => dispatch(eventsActions.clearActionError())}/>) : null}
      {incidentsAction.error ? (<ErrorMessage message={incidentsAction.error.message} onShown={() => dispatch(incidentsActions.clearActionError())}/>) : null}

      <section className="w-full min-w-0 border-b border-secondary/50 pb-8">
        <div className="flex flex-row items-start justify-between gap-4">
          <div className="min-w-0 flex-1">
            <div className="flex flex-wrap items-center gap-3">
              <Typography variant="h4" component="h1" sx={{ fontWeight: 700 }}>
                {event.title}
              </Typography>
              <EventStatusBadge status={event.status}/>
            </div>
            <Typography variant="body2" color="text.secondary" className="mt-2">
              {formatDateTime(event.startDate)} – {formatDateTime(event.endDate)}
            </Typography>
            {event.description?.trim() ? (<Typography variant="body1" className="mt-4 w-full min-w-0 text-headline">
                {event.description.trim()}
              </Typography>) : null}
          </div>

          {canEditLiveEvent || canMutateLiveEvent ? (<div className="flex shrink-0 flex-col gap-2 md:flex-row md:items-start md:gap-2 [&_button]:!h-10 [&_button]:!w-10 [&_button]:!min-h-[40px] [&_button]:!min-w-[40px] [&_button]:!max-h-[40px] [&_button]:!max-w-[40px]">
              {canEditLiveEvent ? (<Tooltip title="Редактировать мероприятие" placement="top" enterDelay={400}>
                  <span className="inline-flex">
                    <Button size="icon" variant="ghost" className="shrink-0 rounded-[10px] border border-secondary/55 bg-surface/90 text-headline shadow-sm transition-colors hover:border-secondary hover:bg-surface-muted" aria-label="Редактировать мероприятие" onClick={() => {
                        dispatch(eventsActions.clearActionError());
                        setIsEditOpen(true);
                    }}>
                      <EditOutlined sx={{ fontSize: 22, color: 'currentColor' }}/>
                    </Button>
                  </span>
                </Tooltip>) : null}
              {canMutateLiveEvent && event.status === EventStatus.ACTIVE ? (<Tooltip title="Завершить мероприятие" placement="top" enterDelay={400}>
                  <span className="inline-flex">
                    <Button size="icon" variant="ghost" className="shrink-0 rounded-[10px] border border-highlight/35 bg-surface/90 text-highlight shadow-sm transition-colors hover:border-highlight/55 hover:bg-surface-muted" aria-label="Завершить мероприятие" onClick={() => handleStatusAction(completeEventThunk)} loading={action.status === 'pending'}>
                      <CheckCircle sx={{ fontSize: 22, color: 'currentColor' }}/>
                    </Button>
                  </span>
                </Tooltip>) : null}
              {canEditLiveEvent && isCancellable ? (<Tooltip title="Отменить мероприятие" placement="top" enterDelay={400}>
                  <span className="inline-flex">
                    <Button size="icon" variant="ghost" className="shrink-0 rounded-[10px] border border-secondary/55 bg-surface/90 text-tertiary shadow-sm transition-colors hover:border-tertiary/45 hover:bg-surface-muted disabled:opacity-40" aria-label="Отменить мероприятие" onClick={() => setIsCancelOpen(true)} disabled={action.status === 'pending'}>
                      <BlockOutlined sx={{ fontSize: 22, color: 'currentColor' }}/>
                    </Button>
                  </span>
                </Tooltip>) : null}
            </div>) : null}
        </div>

        <dl className={`mt-8 grid w-full min-w-0 grid-cols-1 gap-6 text-sm ${hideEventCoordinatorRoster ? 'sm:grid-cols-2' : 'sm:grid-cols-2 lg:grid-cols-3'}`}>
          <Field label="Создатель" value={event.creatorId === undefined ? ('–') : (<SelfOrProfileLink subjectUserId={asUserId(event.creatorId)} viewerUserId={user?.id} nameLabel={formatInitials(userById.get(event.creatorId)?.fullName)}/>)}/>
          {hideEventCoordinatorRoster ? null : (<Field label="Координаторы" value={<span className="flex flex-wrap items-center gap-x-2 gap-y-1">
                  {event.coordinatorIds.length === 0 ? (<span className="text-paragraph">–</span>) : (<>
                      {event.coordinatorIds.slice(0, 2).map((id, index) => (<Fragment key={id}>
                          {index > 0 ? <span className="text-paragraph">, </span> : null}
                          <SelfOrProfileLink subjectUserId={asUserId(id)} viewerUserId={user?.id} nameLabel={formatInitials(userById.get(id)?.fullName)}/>
                        </Fragment>))}
                      {event.coordinatorIds.length > 2 ? (<span className="text-paragraph">+{event.coordinatorIds.length - 2}</span>) : null}
                    </>)}
                  {canMutateLiveEvent ? (<Button size="icon" variant="ghost" className="h-9 w-9 rounded-full border border-secondary/50" onClick={() => {
                        setCoordSearch('');
                        setIsAssignCoordOpen(true);
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
          {dashboard.status === 'pending' && !dashboard.data ? (<div className="flex justify-center py-6">
              <Spinner size="lg" label="Загрузка статистики"/>
            </div>) : null}
          {dashboard.error ? <ErrorMessage message={dashboard.error.message}/> : null}
          {dashboard.data ? (<EventDashboardWidget data={dashboard.data} variant="embedded"/>) : null}
        </section>) : null}

            <EventMapPanel markers={mapMarkers} {...(mapCenter !== undefined ? { center: mapCenter } : {})}/>

      {hasCoordinator ? (<div className="flex w-full min-w-0 flex-col gap-6">
          <div className="flex w-full min-w-0 flex-col gap-6 lg:flex-row lg:items-stretch">
          <div className="flex min-h-0 w-full min-w-0 flex-1 flex-col">
          <Card padded={false} className="flex min-h-0 w-full flex-1 flex-col cursor-pointer outline-none transition hover:border-primary/40 focus-visible:ring-2 focus-visible:ring-primary/30" role="link" tabIndex={0} onClick={() => navigate(PATHS.eventTasks(event.id))} onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    navigate(PATHS.eventTasks(event.id));
                }
            }}>
            <div className={`${SUMMARY_PREVIEW_PANEL_HEADER} shrink-0`}>
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
            <div className={`${SUMMARY_PREVIEW_PANEL_BODY} flex min-h-0 flex-1 flex-col`}>
              {!canViewTasksPanel ? (<div className="flex min-h-0 flex-1 flex-col">
                  <EmptyState fillContainer title="Нет доступа к задачам"/>
                </div>) : tasks.length === 0 && tasksList.status !== 'pending' ? (<div className="flex min-h-0 flex-1 flex-col">
                  <EmptyState fillContainer title="Задач нет"/>
                </div>) : null}
              {canViewTasksPanel && tasks.length > 0 ? (<div className="flex flex-col gap-2">
                  {tasksPreview.preview.map((task) => (<div key={task.id} className={PREVIEW_ROW} onClick={(e) => {
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
          </div>

          <div className="flex min-h-0 w-full min-w-0 flex-1 flex-col">
          <Card padded={false} className="flex min-h-0 w-full flex-1 flex-col cursor-pointer outline-none transition hover:border-primary/40 focus-visible:ring-2 focus-visible:ring-primary/30" role="link" tabIndex={0} onClick={() => navigate(PATHS.eventIncidents(event.id))} onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    navigate(PATHS.eventIncidents(event.id));
                }
            }}>
            <div className={`${SUMMARY_PREVIEW_PANEL_HEADER} shrink-0`}>
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
            <div className={`${SUMMARY_PREVIEW_PANEL_BODY} flex min-h-0 flex-1 flex-col`}>
              {incidents.length === 0 && incidentsList.status !== 'pending' ? (<div className="flex min-h-0 flex-1 flex-col">
                  <EmptyState fillContainer title="Инцидентов нет"/>
                </div>) : null}
              {incidents.length > 0 ? (<div className="flex flex-col gap-2">
                  {incidentsPreview.preview.map((incident) => (<div key={incident.id} className={PREVIEW_ROW} onClick={(e) => {
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
          </div>
          {canManageEvent && event.status === EventStatus.COMPLETED ? (<EventAiRecommendationsPanel fetchStatus={postMortem.status} fetchError={postMortem.error} report={postMortemReportForEvent} pollTimedOut={postMortemPollTimedOut}/>) : null}
        </div>) : (<Card>
          <EmptyState title="Прежде чем начать планирование, назначьте координаторов"/>
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

      <Modal open={isCreateTaskOpen && canMutateLiveEvent && hasCoordinator} bodyScroll={createTaskBodyScroll} onClose={() => {
            dispatch(tasksActions.clearActionError());
            setCreateTaskBodyScroll(true);
            setIsCreateTaskOpen(false);
        }} title="Новая задача" size="lg">
        {user && canMutateLiveEvent && hasCoordinator && isCreateTaskOpen ? (<TaskCreateWizard open eventId={event.id} onStepChange={(s) => setCreateTaskBodyScroll(s === 0 || s === 2)} onClose={() => {
                dispatch(tasksActions.clearActionError());
                setCreateTaskBodyScroll(true);
                setIsCreateTaskOpen(false);
            }}/>) : null}
      </Modal>

      {canMutateLiveEvent ? (<Modal open={isAssignCoordOpen} onClose={() => setIsAssignCoordOpen(false)} title="Назначение координатора" size="md">
          <div className="flex w-full min-w-0 flex-col gap-3">
            <div className="shrink-0 flex flex-col gap-3">
              <Input label="Поиск" value={coordSearch} onChange={(e) => setCoordSearch(e.target.value)} placeholder="По имени или логину"/>
              {showAssignSelfAsCoordinator ? (<div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between sm:gap-4">
                  <p className="min-w-0 flex-1 text-xs leading-snug text-paragraph">
                    Можно назначить себя или выбрать пользователя в списке ниже.
                  </p>
                  <Button size="sm" variant="primary" className="w-full shrink-0 sm:w-auto" disabled={action.status === 'pending'} onClick={() => user && appendCoordinator(user.id)}>
                    Назначить себя координатором
                  </Button>
                </div>) : null}
              {!showAssignSelfAsCoordinator ? (<div className="text-xs text-paragraph">
                  Выберите пользователя в списке ниже.
                </div>) : null}
            </div>
            <div className={COORD_PICKER_LIST_AREA}>
              {usersList.status === 'pending' ? (<div className="flex h-full items-center justify-center px-2 py-6">
                  <Spinner size="lg" label="Загрузка списка пользователей"/>
                </div>) : (<div className="h-full overflow-y-auto overscroll-contain px-2 py-2">
                  {coordinatorPickerRows.length === 0 ? (<p className="text-sm text-paragraph">
                      {coordSearch.trim() ? 'Таких координаторов не найдено.' : 'Координаторов для выбора нет.'}
                    </p>) : (<div className="grid gap-2 pr-1">
                      {coordinatorPickerRows.map((u) => {
                const already = event.coordinatorIds.includes(u.id);
                return (<div key={String(u.id)} className="flex items-center justify-between gap-3 rounded-lg border border-secondary/50 bg-bg px-3 py-2">
                    <div className="min-w-0">
                      <div className="truncate text-sm font-medium text-headline">
                        {u.fullName}
                      </div>
                      <div className="min-w-0 max-w-full truncate text-xs text-paragraph">
                        <SelfOrProfileLink subjectUserId={asUserId(u.id)} viewerUserId={user ? asUserId(user.id) : undefined} nameLabel={`@${u.username}`} className="inline-block max-w-full truncate text-primary underline-offset-2 hover:underline"/>
                      </div>
                    </div>
                    <Button size="sm" disabled={already || action.status === 'pending'} onClick={() => appendCoordinator(u.id)}>
                      {already ? 'Назначен' : 'Назначить'}
                    </Button>
                  </div>);
            })}
                    </div>)}
                </div>)}
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
        }} title="Отмена мероприятия" description="Укажите причину – она будет сохранена в журнале." footer={<>
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
