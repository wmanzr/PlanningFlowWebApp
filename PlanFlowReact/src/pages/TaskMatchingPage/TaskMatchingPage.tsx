import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { matchingActions, runMatchingThunk, } from '@/store/slices/matching/matchingSlice';
import { selectMatchingError, selectMatchingResult, selectMatchingStatus, } from '@/store/slices/matching/selectors';
import { fetchSkillsThunk } from '@/store/slices/skills/skillsSlice';
import { selectTaskById } from '@/store/slices/tasks/selectors';
import { fetchTaskByIdThunk } from '@/store/slices/tasks/tasksSlice';
import { assignTaskThunk } from '@/store/slices/tasks/tasksSlice';
import { selectCurrentUser } from '@/store/slices/auth/selectors';
import { Badge, Button, Card, CardHeader, EmptyState, ErrorMessage, PageLayout, Tabs, } from '@/components/ui';
import { CandidateRow, RejectedRow } from '@/components/domain/matching';
import { TaskMatchForm } from '@/components/domain/task';
import { asEventId, asTaskId, type TaskMatchRequest } from '@/types';
import { isViewerSubject } from '@/utils/isViewerSubject';
import { PATHS } from '../paths';
type TabValue = 'ranked' | 'rejected';
export const TaskMatchingPage = () => {
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
    const currentUser = useAppSelector(selectCurrentUser);
    const result = useAppSelector(selectMatchingResult);
    const status = useAppSelector(selectMatchingStatus);
    const error = useAppSelector(selectMatchingError);
    const task = useAppSelector(selectTaskById(taskId));
    const [tab, setTab] = useState<TabValue>('ranked');
    useEffect(() => {
        if (taskId !== undefined)
            void dispatch(fetchTaskByIdThunk(taskId));
        void dispatch(fetchSkillsThunk({ page: 1, size: 500 }));
        return () => {
            dispatch(matchingActions.clear());
        };
    }, [dispatch, taskId]);
    if (taskId === undefined || eventId === undefined) {
        return (<PageLayout title="Подбор">
        <ErrorMessage message="Некорректные параметры маршрута"/>
      </PageLayout>);
    }
    const handleMatch = (body: TaskMatchRequest) => {
        void dispatch(runMatchingThunk({ id: taskId, body }));
    };
    const handleAssign = (userId: number) => {
        void dispatch(assignTaskThunk({ id: taskId, userId: userId as never }));
    };
    return (<PageLayout title="Подбор персонала" description={task ? `Задача «${task.title}»` : 'Задача'} actions={<Link to={PATHS.taskDetail(eventId, taskId)}>
          <Button variant="ghost">К задаче</Button>
        </Link>}>
      {error ? <ErrorMessage message={error.message}/> : null}
      <Card>
        <CardHeader title="Параметры подбора" subtitle="Алгоритм выполняется на бэкенде."/>
        <TaskMatchForm submitting={status === 'pending'} onSubmit={handleMatch}/>
      </Card>

      {result ? (<Card padded={false}>
          <div className="flex flex-wrap items-center justify-between gap-3 border-b border-secondary/60 px-5 py-4">
            <div>
              <h2 className="text-lg font-semibold text-headline">Результат</h2>
              <p className="text-sm text-paragraph">
                Требуется {result.requiredCount}, найдено {result.ranked.length}, отклонено{' '}
                {result.rejected.length}, дефицит {result.shortageCount}.
              </p>
            </div>
            <Tabs<TabValue> value={tab} onChange={setTab} items={[
                {
                    value: 'ranked',
                    label: (<span className="flex items-center gap-2">
                      Подходящие <Badge tone="success">{result.ranked.length}</Badge>
                    </span>),
                },
                {
                    value: 'rejected',
                    label: (<span className="flex items-center gap-2">
                      Отклоненные <Badge tone="danger">{result.rejected.length}</Badge>
                    </span>),
                },
            ]}/>
          </div>
          <div className="flex flex-col gap-3 p-5">
            {tab === 'ranked' &&
                (result.ranked.length === 0 ? (<EmptyState title="Подходящих не найдено" description="Попробуйте ослабить ограничения или сменить режим подбора."/>) : (result.ranked.map((candidate) => (<CandidateRow key={candidate.candidateId} candidate={candidate} profileTo={isViewerSubject(currentUser?.id, candidate.candidateId)
                        ? PATHS.profile
                        : PATHS.userDetail(candidate.candidateId)} actions={<Button size="sm" onClick={() => handleAssign(candidate.candidateId)}>
                        Назначить
                      </Button>}/>))))}
            {tab === 'rejected' &&
                (result.rejected.length === 0 ? (<EmptyState title="Никто не отклонен" description="Все кандидаты прошли все проверки."/>) : (result.rejected.map((candidate) => (<RejectedRow key={candidate.candidateId} candidate={candidate} profileTo={isViewerSubject(currentUser?.id, candidate.candidateId)
                        ? PATHS.profile
                        : PATHS.userDetail(candidate.candidateId)}/>))))}
          </div>
        </Card>) : null}
    </PageLayout>);
};
