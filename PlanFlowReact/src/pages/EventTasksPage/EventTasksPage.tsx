import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { fetchEventByIdThunk } from '@/store/slices/events/eventsSlice';
import { selectEventById } from '@/store/slices/events/selectors';
import { fetchTasksForEventThunk, tasksActions } from '@/store/slices/tasks/tasksSlice';
import { selectTasksListMeta } from '@/store/slices/tasks/selectors';
import { selectCurrentUser, selectHasRole } from '@/store/slices/auth/selectors';
import { Button, Card, EmptyState, ErrorMessage, Input, LoadingArea, Modal, PageLayout, Pagination, } from '@/components/ui';
import { TaskCard, TaskCreateWizard } from '@/components/domain/task';
import { EventStatus, UserRole, asEventId, type TaskResponseDto, } from '@/types';
import { PATHS } from '../paths';
const PAGE_SIZE = 20;
export const EventTasksPage = () => {
    const params = useParams<{
        eventId: string;
    }>();
    const eventId = useMemo(() => {
        const num = Number.parseInt(params.eventId ?? '', 10);
        return Number.isFinite(num) ? asEventId(num) : undefined;
    }, [params.eventId]);
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const event = useAppSelector(selectEventById(eventId));
    const tasksList = useAppSelector(selectTasksListMeta);
    const user = useAppSelector(selectCurrentUser);
    const isAdminRole = useAppSelector(selectHasRole(UserRole.ADMIN));
    const canManageEvent = useMemo(() => {
        if (!user || !event)
            return false;
        return (isAdminRole ||
            event.creatorId === user.id ||
            event.coordinatorIds.includes(user.id));
    }, [user, event, isAdminRole]);
    const eventClosed = !!event &&
        (event.status === EventStatus.COMPLETED || event.status === EventStatus.CANCELLED);
    const hasCoordinator = (event?.coordinatorIds.length ?? 0) > 0;
    const canPlanTasks = canManageEvent && !eventClosed && hasCoordinator;
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [taskSearch, setTaskSearch] = useState('');
    const [page, setPage] = useState(1);
    const [items, setItems] = useState<TaskResponseDto[]>([]);
    useEffect(() => {
        if (eventId !== undefined) {
            void dispatch(fetchEventByIdThunk(eventId));
        }
    }, [dispatch, eventId]);
    useEffect(() => {
        setPage(1);
    }, [eventId, hasCoordinator]);
    useEffect(() => {
        setPage(1);
    }, [taskSearch]);
    useEffect(() => {
        if (eventId === undefined || !event || !hasCoordinator)
            return;
        let cancelled = false;
        void dispatch(fetchTasksForEventThunk({ eventId, query: { page, size: PAGE_SIZE } }))
            .unwrap()
            .then((result) => {
            if (!cancelled)
                setItems(result.items);
        })
            .catch(() => {
            if (!cancelled)
                setItems([]);
        });
        return () => {
            cancelled = true;
        };
    }, [dispatch, eventId, event, hasCoordinator, page]);
    useEffect(() => {
        if (!eventClosed)
            return;
        setIsCreateOpen(false);
    }, [eventClosed]);
    const refetchList = () => {
        if (eventId === undefined || !event || !hasCoordinator)
            return;
        void dispatch(fetchTasksForEventThunk({ eventId, query: { page, size: PAGE_SIZE } }))
            .unwrap()
            .then((result) => setItems(result.items))
            .catch(() => setItems([]));
    };
    const sortedFilteredTasks = useMemo(() => {
        const q = taskSearch.trim().toLowerCase();
        const list = [...items].sort((a, b) => a.startTime.localeCompare(b.startTime));
        if (!q)
            return list;
        return list.filter((t) => t.title.toLowerCase().includes(q));
    }, [items, taskSearch]);
    if (eventId === undefined) {
        return (<PageLayout title="Задачи">
        <ErrorMessage message="Некорректный идентификатор мероприятия"/>
      </PageLayout>);
    }
    if (!event) {
        return (<PageLayout title="Задачи">
        {tasksList.status === 'pending' ? <LoadingArea /> : <ErrorMessage message="Мероприятие не найдено"/>}
      </PageLayout>);
    }
    if (!hasCoordinator) {
        return (<PageLayout title="Задачи мероприятия" description={event.title} actions={<Link to={PATHS.eventDetail(event.id)}>
            <Button variant="ghost">К мероприятию</Button>
          </Link>}>
        <Card padded={false}>
          <div className="p-6">
            <EmptyState title="Планирование недоступно" description="Назначьте хотя бы одного координатора мероприятия — затем можно создавать и просматривать задачи."/>
          </div>
        </Card>
      </PageLayout>);
    }
    return (<PageLayout title="Задачи мероприятия" description={event.title} actions={<div className="flex flex-wrap gap-2">
          <Link to={PATHS.eventDetail(event.id)}>
            <Button variant="ghost">К мероприятию</Button>
          </Link>
          {canPlanTasks ? (<Button variant="secondary" onClick={() => setIsCreateOpen(true)}>
              Добавить задачу
            </Button>) : null}
        </div>}>
      {tasksList.error ? <ErrorMessage message={tasksList.error.message}/> : null}
      <Card padded={false}>
        <div className="flex flex-col gap-3 p-4">
          <Input label="Поиск по названию" placeholder="Введите название задачи" value={taskSearch} onChange={(e) => setTaskSearch(e.target.value)}/>
          {sortedFilteredTasks.length === 0 && tasksList.status !== 'pending' ? (<EmptyState title={items.length === 0 ? 'Задач нет' : 'Ничего не найдено'} description={items.length === 0
                ? canPlanTasks
                    ? 'Создайте первую задачу для этого мероприятия.'
                    : 'Задачи по завершенному или отмененному мероприятию доступны только для просмотра.'
                : 'Измените запрос поиска.'}/>) : null}
          {sortedFilteredTasks.map((task) => (<TaskCard key={task.id} task={task} onClick={(taskId) => navigate(PATHS.taskDetail(event.id, taskId))}/>))}
        </div>
      </Card>

      <div className="mt-4">
        <Pagination page={page} totalPages={tasksList.totalPages} onChange={setPage} disabled={tasksList.status === 'pending'}/>
      </div>

      <Modal open={isCreateOpen && canPlanTasks} onClose={() => {
            dispatch(tasksActions.clearActionError());
            setIsCreateOpen(false);
            refetchList();
        }} title="Новая задача" size="lg">
        {user && canPlanTasks && isCreateOpen ? (<TaskCreateWizard open eventId={event.id} onClose={() => {
                dispatch(tasksActions.clearActionError());
                setIsCreateOpen(false);
                refetchList();
            }}/>) : null}
      </Modal>
    </PageLayout>);
};
