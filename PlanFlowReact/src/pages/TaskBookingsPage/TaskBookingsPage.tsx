import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import IconButton from '@mui/material/IconButton';
import RefreshIcon from '@mui/icons-material/Refresh';
import { useAppDispatch, useAppSelector } from '@/store';
import { bookingsActions, cancelBookingThunk, fetchBookingsForTaskThunk, } from '@/store/slices/bookings/bookingsSlice';
import { makeSelectBookingsForTask, selectBookingsActionMeta, selectBookingsListMeta, } from '@/store/slices/bookings/selectors';
import { allocateTaskResourcesThunk, fetchTaskByIdThunk, tasksActions, } from '@/store/slices/tasks/tasksSlice';
import { fetchEventByIdThunk } from '@/store/slices/events/eventsSlice';
import { selectEventById } from '@/store/slices/events/selectors';
import { selectTaskActionMeta, selectTaskById } from '@/store/slices/tasks/selectors';
import { Button, Card, CardHeader, EmptyState, ErrorMessage, LoadingArea, Modal, PageLayout, } from '@/components/ui';
import { AllocateResourcesForm, BookingRow, } from '@/components/domain/booking';
import { asEventId, asTaskId, BookingStatus, } from '@/types';
import { PATHS } from '../paths';
export const TaskBookingsPage = () => {
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
    const list = useAppSelector(selectBookingsListMeta);
    const action = useAppSelector(selectBookingsActionMeta);
    const selectBookings = useMemo(() => makeSelectBookingsForTask(taskId), [taskId]);
    const bookings = useAppSelector(selectBookings);
    const task = useAppSelector(selectTaskById(taskId));
    const taskAction = useAppSelector(selectTaskActionMeta);
    const event = useAppSelector(selectEventById(eventId));
    const [isAllocateOpen, setIsAllocateOpen] = useState(false);
    const [cancellingBookingId, setCancellingBookingId] = useState<number | null>(null);
    const refreshBookings = useCallback(() => {
        if (taskId === undefined)
            return;
        void dispatch(fetchBookingsForTaskThunk({ taskId, query: { page: 1, size: 100 } }));
    }, [dispatch, taskId]);
    useEffect(() => {
        if (taskId !== undefined) {
            void dispatch(fetchBookingsForTaskThunk({ taskId, query: { page: 1, size: 100 } }));
            void dispatch(fetchTaskByIdThunk(taskId));
        }
    }, [dispatch, taskId]);
    useEffect(() => {
        if (eventId !== undefined)
            void dispatch(fetchEventByIdThunk(eventId));
    }, [dispatch, eventId]);
    if (taskId === undefined || eventId === undefined) {
        return (<PageLayout title="Резервы">
        <ErrorMessage message="Некорректные параметры маршрута"/>
      </PageLayout>);
    }
    return (<PageLayout title={<span className="inline-flex flex-wrap items-center gap-1">
          Резервирование ресурсов
          <IconButton size="small" aria-label="Обновить список резервов" disabled={list.status === 'pending'} onClick={refreshBookings}>
            <RefreshIcon fontSize="small" className={list.status === 'pending' ? 'animate-spin' : undefined}/>
          </IconButton>
        </span>} description={task
            ? `Задача «${task.title}»`
            : 'Список резервов по выбранной задаче'} actions={<div className="flex gap-2">
          <Link to={PATHS.taskDetail(eventId, taskId)}>
            <Button variant="ghost">К задаче</Button>
          </Link>
          <Button onClick={() => setIsAllocateOpen(true)}>Зарезервировать</Button>
        </div>}>
      {action.error ? (<ErrorMessage message={action.error.message} onShown={() => dispatch(bookingsActions.clearActionError())}/>) : null}
      {taskAction.error ? (<ErrorMessage message={taskAction.error.message} onShown={() => dispatch(tasksActions.clearActionError())}/>) : null}
      {list.error ? <ErrorMessage message={list.error.message}/> : null}
      {list.status === 'pending' && bookings.length === 0 ? <LoadingArea /> : null}
      {bookings.length === 0 && list.status !== 'pending' ? (<EmptyState title="Резервов нет" description="Создайте резерв вручную или через автоматический подбор."/>) : null}
      <Card padded={false}>
        <div className="flex flex-col gap-3 p-5">
          {bookings.map((booking) => (<BookingRow key={booking.id} booking={booking} {...(booking.status !== BookingStatus.CANCELLED
            ? {
                rowHoverCancel: {
                    loading: cancellingBookingId === booking.id && action.status === 'pending',
                    onCancel: () => {
                        setCancellingBookingId(booking.id);
                        void dispatch(cancelBookingThunk(booking.id))
                            .then((result) => {
                            if (cancelBookingThunk.fulfilled.match(result)) {
                                void dispatch(fetchBookingsForTaskThunk({
                                    taskId,
                                    query: { page: 1, size: 100 },
                                }));
                            }
                        })
                            .finally(() => {
                            setCancellingBookingId(null);
                        });
                    },
                },
            }
            : {})}/>))}
        </div>
      </Card>

      <Modal open={isAllocateOpen} onClose={() => setIsAllocateOpen(false)} title="Резервирование ресурса" size="ml">
        {task ? (<>
            <CardHeader title={`Задача «${task.title}»`} subtitle="Бэкенд выберет внутренний или внешний ресурс автоматически."/>
            <AllocateResourcesForm defaultFrom={task.startTime} defaultTo={task.endTime} {...(event
            ? {
                eventForBookingWindow: {
                    startDate: event.startDate,
                    endDate: event.endDate,
                },
            }
            : {})} submitting={taskAction.status === 'pending'} onCancel={() => setIsAllocateOpen(false)} onSubmit={(body) => {
                void dispatch(allocateTaskResourcesThunk({ id: task.id, body })).then((result) => {
                    if (allocateTaskResourcesThunk.fulfilled.match(result)) {
                        setIsAllocateOpen(false);
                        void dispatch(fetchBookingsForTaskThunk({
                            taskId: task.id,
                            query: { page: 1, size: 100 },
                        }));
                    }
                });
            }}/>
          </>) : null}
      </Modal>
    </PageLayout>);
};
