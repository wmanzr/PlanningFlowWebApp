import { useEffect, useMemo } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { fetchEventByIdThunk, fetchEventDashboardThunk } from '@/store/slices/events/eventsSlice';
import { selectEventById, selectEventDashboard, selectEventDetailMeta, } from '@/store/slices/events/selectors';
import { Button, Card, EmptyState, ErrorMessage, LoadingArea, PageLayout, } from '@/components/ui';
import { EventDashboardWidget } from '@/components/domain/event';
import { asEventId } from '@/types';
import { PATHS } from '../paths';
export const EventDashboardPage = () => {
    const params = useParams<{
        eventId: string;
    }>();
    const eventId = useMemo(() => {
        const num = Number.parseInt(params.eventId ?? '', 10);
        return Number.isFinite(num) ? asEventId(num) : undefined;
    }, [params.eventId]);
    const dispatch = useAppDispatch();
    const dashboard = useAppSelector(selectEventDashboard);
    const detail = useAppSelector(selectEventDetailMeta);
    const event = useAppSelector(selectEventById(eventId));
    const hasCoordinator = (event?.coordinatorIds.length ?? 0) > 0;
    useEffect(() => {
        if (eventId !== undefined) {
            void dispatch(fetchEventByIdThunk(eventId));
        }
    }, [dispatch, eventId]);
    useEffect(() => {
        if (eventId === undefined || !hasCoordinator)
            return;
        void dispatch(fetchEventDashboardThunk(eventId));
    }, [dispatch, eventId, hasCoordinator]);
    if (eventId === undefined) {
        return (<PageLayout title="Дашборд">
        <ErrorMessage message="Некорректный идентификатор мероприятия"/>
      </PageLayout>);
    }
    if (!event) {
        return (<PageLayout title="Дашборд мероприятия">
        {detail.status === 'pending' ? (<LoadingArea />) : (<ErrorMessage message={detail.error?.message ?? 'Мероприятие не найдено'}/>)}
      </PageLayout>);
    }
    if (!hasCoordinator) {
        return (<PageLayout title="Дашборд мероприятия" actions={<Link to={PATHS.eventDetail(eventId)}>
            <Button variant="ghost">К мероприятию</Button>
          </Link>}>
        <Card>
          <EmptyState title="Сводка недоступна" description="Назначьте хотя бы одного координатора мероприятия — после этого здесь появится статистика по задачам и инцидентам."/>
        </Card>
      </PageLayout>);
    }
    return (<PageLayout title="Дашборд мероприятия" actions={<Link to={PATHS.eventDetail(eventId)}>
          <Button variant="ghost">К мероприятию</Button>
        </Link>}>
      {dashboard.status === 'pending' && !dashboard.data ? <LoadingArea /> : null}
      {dashboard.error ? <ErrorMessage message={dashboard.error.message}/> : null}
      {dashboard.data ? <EventDashboardWidget data={dashboard.data}/> : null}
    </PageLayout>);
};
