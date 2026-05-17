import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { useAppDispatch, useAppSelector } from '@/store';
import { tasksApi } from '@/api';
import { acceptIncidentThunk, createIncidentThunk, fetchIncidentsForEventThunk, incidentsActions, resolveIncidentThunk, } from '@/store/slices/incidents/incidentsSlice';
import { makeSelectIncidentsForEvent, selectIncidentsActionMeta, selectIncidentsListMeta, } from '@/store/slices/incidents/selectors';
import { selectCurrentUser } from '@/store/slices/auth/selectors';
import { fetchEventByIdThunk } from '@/store/slices/events/eventsSlice';
import { selectEventById, selectEventDetailMeta } from '@/store/slices/events/selectors';
import { Button, Card, CardHeader, EmptyState, ErrorMessage, LoadingArea, Modal, PageLayout, Pagination, Select, Textarea, } from '@/components/ui';
import { INCIDENT_STATUS_LABEL, IncidentForm, IncidentRow } from '@/components/domain/incident';
import { buildStatusFilterOptions } from '@/utils/statusFilterOptions';
import { EventStatus, IncidentStatus, asEventId, type IncidentCreateRequest, type IncidentResponseDto, type TaskResponseDto, } from '@/types';
import { PATHS } from '../paths';
const RESOLUTION_MIN_LENGTH = 5;
const RESOLUTION_MAX_LENGTH = 1000;
const resolveSchema = z.object({
    resolutionNotes: z
        .string()
        .trim()
        .min(RESOLUTION_MIN_LENGTH, `Минимум ${RESOLUTION_MIN_LENGTH} символов`)
        .max(RESOLUTION_MAX_LENGTH),
});
type ResolveValues = z.infer<typeof resolveSchema>;
const INCIDENT_PAGE_SIZE = 20;
export const IncidentsPage = () => {
    const params = useParams<{
        eventId: string;
    }>();
    const eventId = useMemo(() => {
        const num = Number.parseInt(params.eventId ?? '', 10);
        return Number.isFinite(num) ? asEventId(num) : undefined;
    }, [params.eventId]);
    const dispatch = useAppDispatch();
    const location = useLocation();
    const navigate = useNavigate();
    const list = useAppSelector(selectIncidentsListMeta);
    const action = useAppSelector(selectIncidentsActionMeta);
    const selectIncidents = useMemo(() => makeSelectIncidentsForEvent(eventId), [eventId]);
    const incidents = useAppSelector(selectIncidents);
    const event = useAppSelector(selectEventById(eventId));
    const detail = useAppSelector(selectEventDetailMeta);
    const user = useAppSelector(selectCurrentUser);
    const eventClosed = !!event &&
        (event.status === EventStatus.COMPLETED || event.status === EventStatus.CANCELLED);
    const hasCoordinator = (event?.coordinatorIds.length ?? 0) > 0;
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [resolveTarget, setResolveTarget] = useState<IncidentResponseDto | null>(null);
    const [page, setPage] = useState(1);
    const [statusFilter, setStatusFilter] = useState<IncidentStatus | ''>('');
    const [formTasks, setFormTasks] = useState<TaskResponseDto[]>([]);
    const statusFilterOptions = useMemo(() => buildStatusFilterOptions(INCIDENT_STATUS_LABEL), []);
    const filteredIncidents = useMemo(() => {
        if (!statusFilter) {
            return incidents;
        }
        return incidents.filter((incident) => incident.status === statusFilter);
    }, [incidents, statusFilter]);
    const openCreateFromStateDone = useRef(false);
    const resolveForm = useForm<ResolveValues>({ defaultValues: { resolutionNotes: '' } });
    useEffect(() => {
        if (eventId !== undefined) {
            void dispatch(fetchEventByIdThunk(eventId));
        }
    }, [dispatch, eventId]);
    useEffect(() => {
        setPage(1);
    }, [eventId, statusFilter]);
    useEffect(() => {
        if (eventId === undefined || !event || !hasCoordinator)
            return;
        void dispatch(fetchIncidentsForEventThunk({ eventId, query: { page, size: INCIDENT_PAGE_SIZE } }));
    }, [dispatch, eventId, event, hasCoordinator, page]);
    useEffect(() => {
        if (eventId === undefined || !event || !hasCoordinator)
            return;
        let cancelled = false;
        void tasksApi
            .forEvent(eventId, { page: 1, size: 200 })
            .then((r) => {
            if (!cancelled)
                setFormTasks(r.items);
        })
            .catch(() => {
            if (!cancelled)
                setFormTasks([]);
        });
        return () => {
            cancelled = true;
        };
    }, [eventId, event, hasCoordinator]);
    useEffect(() => {
        openCreateFromStateDone.current = false;
    }, [eventId]);
    useEffect(() => {
        const st = location.state as {
            openCreate?: boolean;
        } | null;
        if (!st?.openCreate ||
            openCreateFromStateDone.current ||
            eventClosed ||
            !hasCoordinator)
            return;
        openCreateFromStateDone.current = true;
        setIsCreateOpen(true);
        navigate(location.pathname, { replace: true, state: null });
    }, [location.pathname, location.state, navigate, eventClosed, hasCoordinator]);
    useEffect(() => {
        if (!eventClosed)
            return;
        setIsCreateOpen(false);
    }, [eventClosed]);
    useEffect(() => {
        if (hasCoordinator)
            return;
        setIsCreateOpen(false);
    }, [hasCoordinator]);
    if (eventId === undefined) {
        return (<PageLayout title="Инциденты">
        <ErrorMessage message="Некорректный идентификатор мероприятия"/>
      </PageLayout>);
    }
    if (!event) {
        return (<PageLayout title="Инциденты мероприятия">
        {detail.status === 'pending' ? (<LoadingArea />) : (<ErrorMessage message={detail.error?.message ?? 'Мероприятие не найдено'}/>)}
      </PageLayout>);
    }
    if (!hasCoordinator) {
        return (<PageLayout title="Инциденты мероприятия" description={event.title} actions={<Link to={PATHS.eventDetail(event.id)}>
            <Button variant="ghost">К мероприятию</Button>
          </Link>}>
        <Card padded={false}>
          <div className="p-6">
            <EmptyState title="Планирование недоступно" description="Назначьте хотя бы одного координатора мероприятия — затем можно создавать и просматривать инциденты."/>
          </div>
        </Card>
      </PageLayout>);
    }
    const handleCreate = (body: IncidentCreateRequest) => {
        void dispatch(createIncidentThunk(body)).then((result) => {
            if (createIncidentThunk.fulfilled.match(result)) {
                setIsCreateOpen(false);
                void dispatch(fetchIncidentsForEventThunk({ eventId, query: { page: 1, size: INCIDENT_PAGE_SIZE } }));
                setPage(1);
                navigate(PATHS.incidentDetail(result.payload));
            }
        });
    };
    const handleResolve = resolveForm.handleSubmit((values) => {
        if (!resolveTarget)
            return;
        const parsed = resolveSchema.safeParse(values);
        if (!parsed.success)
            return;
        void dispatch(resolveIncidentThunk({
            id: resolveTarget.id,
            body: { resolutionNotes: parsed.data.resolutionNotes },
        })).then((result) => {
            if (resolveIncidentThunk.fulfilled.match(result)) {
                setResolveTarget(null);
                resolveForm.reset({ resolutionNotes: '' });
                void dispatch(fetchIncidentsForEventThunk({ eventId, query: { page, size: INCIDENT_PAGE_SIZE } }));
            }
        });
    });
    return (<PageLayout title="Инциденты мероприятия" actions={<div className="flex gap-2">
          <Link to={PATHS.eventDetail(eventId)}>
            <Button variant="ghost">К мероприятию</Button>
          </Link>
          {!eventClosed ? (<Button onClick={() => setIsCreateOpen(true)}>Создать инцидент</Button>) : null}
        </div>}>
      {action.error ? (<ErrorMessage message={action.error.message} onShown={() => dispatch(incidentsActions.clearActionError())}/>) : null}
      {list.error ? <ErrorMessage message={list.error.message}/> : null}
      {list.status === 'pending' && incidents.length === 0 ? <LoadingArea /> : null}
      {filteredIncidents.length === 0 && list.status !== 'pending' ? (<EmptyState title={incidents.length === 0 ? 'Инцидентов нет' : 'Ничего не найдено'} description={incidents.length === 0 ? undefined : 'Измените фильтр по статусу.'}/>) : null}
      <Card padded={false}>
        <div className="flex flex-col gap-3 p-5">
          <Select label="Статус" options={statusFilterOptions} value={statusFilter} onChange={(e) => {
            setStatusFilter(e.target.value === '' ? '' : (e.target.value as IncidentStatus));
            setPage(1);
        }}/>
          {filteredIncidents.map((incident) => (<IncidentRow key={incident.id} incident={incident} actions={<>
                  {incident.status === IncidentStatus.OPEN ? (<Button size="sm" onClick={() => {
                    void dispatch(acceptIncidentThunk(incident.id)).then((r) => {
                        if (acceptIncidentThunk.fulfilled.match(r)) {
                            void dispatch(fetchIncidentsForEventThunk({ eventId, query: { page, size: INCIDENT_PAGE_SIZE } }));
                        }
                    });
                }} loading={action.status === 'pending'}>
                      Принять
                    </Button>) : null}
                  {incident.status !== IncidentStatus.RESOLVED ? (<Button size="sm" variant="ghost" onClick={() => setResolveTarget(incident)}>
                      Закрыть
                    </Button>) : null}
                </>}/>))}
        </div>
      </Card>

      <div className="mt-4">
        <Pagination page={page} totalPages={list.totalPages} onChange={setPage} disabled={list.status === 'pending'}/>
      </div>

      <Modal open={isCreateOpen && !eventClosed && hasCoordinator} onClose={() => setIsCreateOpen(false)} title="Новый инцидент" size="md">
        {isCreateOpen && !eventClosed && hasCoordinator && user ? (<>
            <CardHeader title="Описание происшествия"/>
            <IncidentForm reporterId={user.id} eventId={eventId} tasks={formTasks} submitting={action.status === 'pending'} onCancel={() => setIsCreateOpen(false)} onSubmit={handleCreate}/>
          </>) : null}
      </Modal>

      <Modal open={resolveTarget !== null} onClose={() => {
            setResolveTarget(null);
            resolveForm.reset({ resolutionNotes: '' });
        }} title="Закрытие инцидента" footer={<>
            <Button variant="ghost" onClick={() => {
                setResolveTarget(null);
                resolveForm.reset({ resolutionNotes: '' });
            }}>
              Отмена
            </Button>
            <Button onClick={handleResolve} loading={action.status === 'pending'}>
              Закрыть инцидент
            </Button>
          </>}>
        <Textarea label="Резолюция" rows={4} error={resolveForm.formState.errors.resolutionNotes?.message} {...resolveForm.register('resolutionNotes')}/>
      </Modal>
    </PageLayout>);
};
