import type { ChangeEvent } from 'react';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { toastsActions } from '@/store/slices/toasts/toastsSlice';
import { createEventThunk, eventsActions, fetchEventsThunk, } from '@/store/slices/events/eventsSlice';
import { selectEventActionMeta, selectEventsListMeta, } from '@/store/slices/events/selectors';
import { selectCurrentUser } from '@/store/slices/auth/selectors';
import { fetchUsersThunk } from '@/store/slices/users/usersSlice';
import { selectAllUsers } from '@/store/slices/users/selectors';
import { Button, EmptyState, ErrorMessage, Input, LoadingArea, Modal, PageLayout, Pagination, } from '@/components/ui';
import { EventCard, EventForm } from '@/components/domain/event';
import { UserRole, asEventId, type AppApiError, type EventCreateRequest, type EventResponseDto, type EventUpdateRequest, } from '@/types';
import { validationErrorsToToastMessage } from '@/utils/validationErrorsToToastMessage';
import { PATHS } from '../paths';
const PAGE_SIZE = 20;
export const EventsListPage = () => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const list = useAppSelector(selectEventsListMeta);
    const action = useAppSelector(selectEventActionMeta);
    const currentUser = useAppSelector(selectCurrentUser);
    const allUsers = useAppSelector(selectAllUsers);
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [titleSearch, setTitleSearch] = useState('');
    const [page, setPage] = useState(1);
    const [items, setItems] = useState<EventResponseDto[]>([]);
    const canCreate = !!currentUser &&
        (currentUser.roles.includes(UserRole.ADMIN) || currentUser.roles.includes(UserRole.ORGANIZER));
    const isCoordinatorOnly = !!currentUser &&
        currentUser.roles.includes(UserRole.COORDINATOR) &&
        !currentUser.roles.includes(UserRole.ORGANIZER) &&
        !currentUser.roles.includes(UserRole.ADMIN);
    useEffect(() => {
        let cancelled = false;
        const titleQ = titleSearch.trim();
        const query = {
            page,
            size: PAGE_SIZE,
            ...(titleQ ? { title: titleQ } : {}),
        };
        void dispatch(fetchEventsThunk(query))
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
    }, [dispatch, page, titleSearch]);
    useEffect(() => {
        if (!currentUser)
            return;
        const canLoadUserDirectory = currentUser.roles.includes(UserRole.ADMIN) ||
            currentUser.roles.includes(UserRole.ORGANIZER) ||
            currentUser.roles.includes(UserRole.COORDINATOR);
        if (!canLoadUserDirectory)
            return;
        void dispatch(fetchUsersThunk({ page: 1, size: 400 }));
    }, [dispatch, currentUser]);
    const userNameById = useMemo(() => new Map(allUsers.map((u) => [u.id, u.fullName] as const)), [allUsers]);
    const sortedEvents = useMemo(() => [...items].sort((a, b) => b.startDate.localeCompare(a.startDate)), [items]);
    const handleTitleChange = (value: string) => {
        setTitleSearch(value);
        setPage(1);
    };
    const handleCreate = (payload: EventCreateRequest | EventUpdateRequest) => {
        if ('eventId' in payload)
            return;
        void dispatch(createEventThunk(payload))
            .unwrap()
            .then((id) => {
            setIsCreateOpen(false);
            navigate(PATHS.eventDetail(asEventId(id)));
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
    return (<PageLayout title="Мероприятия" description={isCoordinatorOnly
            ? 'Здесь отображаются мероприятия, на которые вас назначили координатором.'
            : 'Здесь отображаются ваши мероприятия.'} actions={canCreate ? (<Button onClick={() => {
                dispatch(eventsActions.clearActionError());
                setIsCreateOpen(true);
            }}>
            Создать мероприятие
          </Button>) : null}>
      <div className="mb-4 grid gap-3 md:grid-cols-2">
        <Input className="md:col-span-2" label="Поиск по названию" placeholder="например, городской фестиваль" value={titleSearch} onChange={(e: ChangeEvent<HTMLInputElement>) => handleTitleChange(e.target.value)}/>
      </div>
      {list.status === 'pending' && items.length === 0 ? <LoadingArea /> : null}
      {list.error ? <ErrorMessage message={list.error.message}/> : null}
      {list.status !== 'pending' && items.length === 0 && !list.error ? (<EmptyState title={isCoordinatorOnly ? 'Нет назначенных мероприятий' : 'Мероприятий нет'} description={isCoordinatorOnly
                ? 'Когда вас назначат координатором на мероприятие, оно появится здесь.'
                : 'Создайте первое мероприятие или измените параметры поиска.'}/>) : null}
      <div className="grid w-full gap-3">
        {sortedEvents.map((event) => (<EventCard key={event.id} event={event} userNameById={userNameById} {...(currentUser ? { viewerUserId: currentUser.id } : {})} hideCoordinators={isCoordinatorOnly} onClick={(id) => navigate(PATHS.eventDetail(id))}/>))}
      </div>
      <Pagination page={page} totalPages={list.totalPages} onChange={setPage} disabled={list.status === 'pending'}/>
      <Modal open={isCreateOpen} onClose={() => {
            setIsCreateOpen(false);
            dispatch(eventsActions.clearActionError());
        }} title="Новое мероприятие" size="lg">
        {currentUser ? (<EventForm onSubmit={handleCreate} onCancel={() => setIsCreateOpen(false)} submitting={action.status === 'pending'}/>) : null}
      </Modal>
    </PageLayout>);
};
