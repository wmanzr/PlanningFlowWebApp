import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { fetchEventByIdThunk } from '@/store/slices/events/eventsSlice';
import { selectEventById } from '@/store/slices/events/selectors';
import { fetchTasksForEventThunk } from '@/store/slices/tasks/tasksSlice';
import { makeSelectTasksByEvent, selectTasksListMeta, } from '@/store/slices/tasks/selectors';
import { Button, Card, ErrorMessage, LoadingArea, MapView, PageLayout, Select, type MapMarker, } from '@/components/ui';
import { asEventId, type GeoPoint, type TaskStatus, } from '@/types';
import { TaskStatus as TaskStatusEnum } from '@/types';
import { PATHS } from '../paths';
const STATUS_FILTER_OPTIONS: {
    value: TaskStatus | 'ALL';
    label: string;
}[] = [
    { value: 'ALL', label: 'Все статусы' },
    { value: TaskStatusEnum.OPEN, label: 'Открыта' },
    { value: TaskStatusEnum.ASSIGNED, label: 'Назначена' },
    { value: TaskStatusEnum.IN_PROGRESS, label: 'В работе' },
    { value: TaskStatusEnum.DONE, label: 'Завершена' },
    { value: TaskStatusEnum.CANCELLED, label: 'Отменена' },
];
export const EventMapPage = () => {
    const params = useParams<{
        eventId: string;
    }>();
    const eventId = useMemo(() => {
        const num = Number.parseInt(params.eventId ?? '', 10);
        return Number.isFinite(num) ? asEventId(num) : undefined;
    }, [params.eventId]);
    const dispatch = useAppDispatch();
    const event = useAppSelector(selectEventById(eventId));
    const selectTasks = useMemo(() => makeSelectTasksByEvent(eventId), [eventId]);
    const tasks = useAppSelector(selectTasks);
    const list = useAppSelector(selectTasksListMeta);
    const [statusFilter, setStatusFilter] = useState<TaskStatus | 'ALL'>('ALL');
    useEffect(() => {
        if (eventId !== undefined) {
            void dispatch(fetchEventByIdThunk(eventId));
        }
    }, [dispatch, eventId]);
    const hasCoordinator = (event?.coordinatorIds.length ?? 0) > 0;
    useEffect(() => {
        if (eventId === undefined || !event || !hasCoordinator)
            return;
        void dispatch(fetchTasksForEventThunk({ eventId, query: { page: 1, size: 200 } }));
    }, [dispatch, eventId, event, hasCoordinator]);
    const filteredTasks = useMemo(() => tasks.filter((t) => statusFilter === 'ALL' || t.status === statusFilter), [tasks, statusFilter]);
    const markers = useMemo<MapMarker[]>(() => {
        const result: MapMarker[] = [];
        if (event?.latitude !== undefined && event?.longitude !== undefined) {
            result.push({
                id: `event-${event.id}`,
                lat: event.latitude,
                lng: event.longitude,
                kind: 'event',
                label: event.title,
            });
        }
        filteredTasks.forEach((task) => {
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
    }, [event, filteredTasks]);
    const center: GeoPoint | undefined = event?.latitude !== undefined && event?.longitude !== undefined
        ? { latitude: event.latitude, longitude: event.longitude }
        : undefined;
    if (eventId === undefined) {
        return (<PageLayout title="Карта мероприятия">
        <ErrorMessage message="Некорректный идентификатор мероприятия"/>
      </PageLayout>);
    }
    return (<PageLayout title="Карта мероприятия" description={event?.title ?? 'Загрузка…'} actions={<Link to={PATHS.eventDetail(eventId)}>
          <Button variant="ghost">К мероприятию</Button>
        </Link>}>
      {list.error ? <ErrorMessage message={list.error.message}/> : null}
      <Card>
        <div className="mb-4 flex flex-wrap items-end justify-between gap-3">
          <div>
            <h2 className="text-lg font-semibold text-headline">Точки мероприятия</h2>
            <p className="text-sm text-paragraph">
              Метки: 1 мероприятие, {filteredTasks.filter((t) => t.latitude !== undefined).length}{' '}
              задач с координатами
            </p>
          </div>
          <div className="w-56">
            <Select label="Фильтр по статусу" options={STATUS_FILTER_OPTIONS} value={statusFilter} onChange={(e) => setStatusFilter(e.target.value as TaskStatus | 'ALL')}/>
          </div>
        </div>
        {!event && list.status === 'pending' ? (<LoadingArea />) : (<MapView {...(center ? { center } : {})} markers={markers} height="520px" zoom={12}/>)}
      </Card>
    </PageLayout>);
};
