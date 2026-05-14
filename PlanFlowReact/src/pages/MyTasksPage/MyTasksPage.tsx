import type { ChangeEvent } from 'react';
import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { fetchTasksForUserThunk } from '@/store/slices/tasks/tasksSlice';
import { selectTasksListMeta } from '@/store/slices/tasks/selectors';
import { selectCurrentUser } from '@/store/slices/auth/selectors';
import { acceptAssignmentThunk, rejectAssignmentThunk, } from '@/store/slices/users/usersSlice';
import { selectUsersActionMeta } from '@/store/slices/users/selectors';
import { EmptyState, ErrorMessage, Input, LoadingArea, Modal, PageLayout, Tabs, Button, Textarea, Pagination, } from '@/components/ui';
import { TaskCard } from '@/components/domain/task';
import { AssignStatus, AssignmentFilter, asAssignmentId, asEventId, type AssignmentFilter as AssignmentFilterType, type TaskResponseDto, } from '@/types';
import { PATHS } from '../paths';
const PAGE_SIZE = 20;
const FILTER_FROM_PARAM = (value: string | null): AssignmentFilterType => {
    if (value === AssignmentFilter.ALL)
        return AssignmentFilter.ALL;
    if (value === AssignmentFilter.NOT_CONFIRMED)
        return AssignmentFilter.NOT_CONFIRMED;
    if (value === AssignmentFilter.CONFIRMED)
        return AssignmentFilter.CONFIRMED;
    return AssignmentFilter.ALL;
};
export const MyTasksPage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const user = useAppSelector(selectCurrentUser);
    const list = useAppSelector(selectTasksListMeta);
    const usersAction = useAppSelector(selectUsersActionMeta);
    const filter = FILTER_FROM_PARAM(searchParams.get('filter'));
    const [titleSearch, setTitleSearch] = useState('');
    const [page, setPage] = useState(1);
    const [items, setItems] = useState<TaskResponseDto[]>([]);
    const [rejectOpen, setRejectOpen] = useState(false);
    const [rejectReason, setRejectReason] = useState('');
    const [rejectAssignmentId, setRejectAssignmentId] = useState<number | null>(null);
    const onFilterChange = (value: AssignmentFilterType) => {
        setPage(1);
        const p = new URLSearchParams(searchParams);
        p.set('filter', value);
        setSearchParams(p, { replace: true });
    };
    useEffect(() => {
        if (!user)
            return;
        let cancelled = false;
        const titleQ = titleSearch.trim();
        void dispatch(fetchTasksForUserThunk({
            userId: user.id,
            query: {
                filter,
                page,
                size: PAGE_SIZE,
                ...(titleQ ? { title: titleQ } : {}),
            },
        }))
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
    }, [dispatch, user, filter, titleSearch, page]);
    const openReject = (assignmentId: number) => {
        setRejectAssignmentId(assignmentId);
        setRejectReason('');
        setRejectOpen(true);
    };
    const submitReject = () => {
        const reason = rejectReason.trim();
        if (rejectAssignmentId === null || reason.length === 0)
            return;
        void dispatch(rejectAssignmentThunk({
            id: asAssignmentId(rejectAssignmentId),
            body: { reason },
        })).then((r) => {
            if (rejectAssignmentThunk.fulfilled.match(r) && user) {
                setRejectOpen(false);
                setRejectAssignmentId(null);
                setRejectReason('');
                void dispatch(fetchTasksForUserThunk({
                    userId: user.id,
                    query: {
                        filter,
                        page,
                        size: PAGE_SIZE,
                        ...(titleSearch.trim() ? { title: titleSearch.trim() } : {}),
                    },
                }))
                    .unwrap()
                    .then((result) => setItems(result.items))
                    .catch(() => setItems([]));
            }
        });
    };
    const acceptOne = (assignmentId: number) => {
        void dispatch(acceptAssignmentThunk(asAssignmentId(assignmentId))).then((r) => {
            if (acceptAssignmentThunk.fulfilled.match(r) && user) {
                void dispatch(fetchTasksForUserThunk({
                    userId: user.id,
                    query: {
                        filter,
                        page,
                        size: PAGE_SIZE,
                        ...(titleSearch.trim() ? { title: titleSearch.trim() } : {}),
                    },
                }))
                    .unwrap()
                    .then((result) => setItems(result.items))
                    .catch(() => setItems([]));
            }
        });
    };
    return (<PageLayout title="Мои задачи" description="Фильтр по статусу назначения, поиск по названию задачи. Для назначений в ожидании подтверждения используйте кнопки справа от названия.">
      <Tabs<AssignmentFilterType> value={filter} onChange={onFilterChange} items={[
            { value: AssignmentFilter.ALL, label: 'Все' },
            { value: AssignmentFilter.CONFIRMED, label: 'Подтвержденные' },
            { value: AssignmentFilter.NOT_CONFIRMED, label: 'Не подтвержденные' },
        ]}/>
      <div className="mt-4 grid gap-3 md:grid-cols-2">
        <Input className="md:col-span-2" label="Поиск по названию" placeholder="например, монтаж сцены" value={titleSearch} onChange={(e: ChangeEvent<HTMLInputElement>) => {
                setTitleSearch(e.target.value);
                setPage(1);
            }}/>
      </div>
      {list.error ? <ErrorMessage message={list.error.message}/> : null}
      {list.status === 'pending' && items.length === 0 ? <LoadingArea /> : null}
      {items.length === 0 && list.status !== 'pending' ? (<EmptyState title="Задач нет" description="Измените фильтр или параметры поиска."/>) : null}
      <div className="mt-4 grid gap-3">
        {items.map((task) => {
            const va = task.viewerAssignment;
            const showActions = !!va && va.status === AssignStatus.PENDING;
            return (<TaskCard key={task.id} task={task} hideDependencies onClick={(taskId) => {
                    if (task.eventId !== undefined) {
                        navigate(PATHS.taskDetail(asEventId(task.eventId), taskId));
                    }
                }} titleTrailing={showActions && va ? (<div className="flex flex-wrap items-center justify-end gap-2">
                    <Button size="sm" loading={usersAction.status === 'pending'} onClick={() => acceptOne(va.id)}>
                      Подтвердить
                    </Button>
                    <Button size="sm" variant="secondary" onClick={() => openReject(va.id)}>
                      Отклонить
                    </Button>
                  </div>) : undefined}/>);
        })}
      </div>

      <div className="mt-4">
        <Pagination page={page} totalPages={list.totalPages} onChange={setPage} disabled={list.status === 'pending'}/>
      </div>

      <Modal open={rejectOpen} onClose={() => {
            setRejectOpen(false);
            setRejectReason('');
            setRejectAssignmentId(null);
        }} title="Отклонить назначение" footer={<>
            <Button variant="ghost" onClick={() => {
                setRejectOpen(false);
                setRejectReason('');
                setRejectAssignmentId(null);
            }}>
              Закрыть
            </Button>
            <Button variant="danger" loading={usersAction.status === 'pending'} disabled={rejectReason.trim().length === 0} onClick={submitReject}>
              Отклонить
            </Button>
          </>}>
        <Textarea label="Причина" rows={3} value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} placeholder="Укажите причину отказа"/>
      </Modal>
    </PageLayout>);
};
