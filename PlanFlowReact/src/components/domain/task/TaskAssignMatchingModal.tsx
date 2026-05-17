import { useCallback, useEffect, useMemo, useState } from 'react';
import Checkbox from '@mui/material/Checkbox';
import FormControlLabel from '@mui/material/FormControlLabel';
import Typography from '@mui/material/Typography';
import { Button, Input, Modal } from '@/components/ui';
import { ExecutorMatchingPicker, useAutoSelectRankedExecutors } from '@/components/domain/matching';
import { useAppDispatch, useAppSelector } from '@/store';
import { toastsActions } from '@/store/slices/toasts/toastsSlice';
import { matchingActions, runMatchingThunk } from '@/store/slices/matching/matchingSlice';
import { selectMatchingError, selectMatchingResult, selectMatchingStatus } from '@/store/slices/matching/selectors';
import { fetchEventDashboardThunk } from '@/store/slices/events/eventsSlice';
import { assignTaskThunk, fetchTaskByIdThunk, fetchTasksForEventThunk } from '@/store/slices/tasks/tasksSlice';
import { fetchUsersThunk } from '@/store/slices/users/usersSlice';
import { MatchingMode, UserRole, type AppApiError, type EventId, type TaskId, type UserId, type UserResponseDto } from '@/types';
import { validationErrorsToToastMessage } from '@/utils/validationErrorsToToastMessage';

const MATCH_MAX_DAILY_LOAD_MINUTES = 480;
const MATCH_MIN_TECHNICAL_GAP_MINUTES = 15;
const DEFAULT_GEO_RADIUS_METERS = 35000;
const MIN_PEOPLE = 1;
const MAX_PEOPLE = 1000;

export interface TaskAssignMatchingModalProps {
    open: boolean;
    onClose: () => void;
    taskId: TaskId;
    eventId: EventId;
    initialPickCount: number;
    onAssigned: () => void;
}

export function TaskAssignMatchingModal({
    open,
    onClose,
    taskId,
    eventId,
    initialPickCount,
    onAssigned,
}: TaskAssignMatchingModalProps) {
    const dispatch = useAppDispatch();
    const matchResult = useAppSelector(selectMatchingResult);
    const matchStatus = useAppSelector(selectMatchingStatus);
    const matchError = useAppSelector(selectMatchingError);
    const [phase, setPhase] = useState<'count' | 'matching'>('count');
    const [pickCount, setPickCount] = useState(initialPickCount);
    const [preferCloserCritical, setPreferCloserCritical] = useState(false);
    const [executorSearch, setExecutorSearch] = useState('');
    const [executors, setExecutors] = useState<UserResponseDto[]>([]);
    const [selectedExecutorIds, setSelectedExecutorIds] = useState(() => new Set<number>());
    const [bulkAssigning, setBulkAssigning] = useState(false);

    const requiredSlots = useMemo(() => {
        const n = Number(pickCount);
        if (!Number.isFinite(n) || n < MIN_PEOPLE) {
            return MIN_PEOPLE;
        }
        return Math.min(MAX_PEOPLE, Math.floor(n));
    }, [pickCount]);

    useEffect(() => {
        if (!open) {
            return;
        }
        setPhase('count');
        setPickCount(initialPickCount);
        setPreferCloserCritical(false);
        setExecutorSearch('');
        setSelectedExecutorIds(new Set());
        dispatch(matchingActions.clear());
    }, [open, initialPickCount, dispatch]);

    useEffect(() => {
        if (!open) {
            return;
        }
        void dispatch(fetchTaskByIdThunk(taskId));
    }, [dispatch, open, taskId]);

    useAutoSelectRankedExecutors(
        matchResult,
        requiredSlots,
        setSelectedExecutorIds,
        open && phase === 'matching',
    );

    useEffect(() => {
        if (!open || phase !== 'matching') {
            return;
        }
        void (async () => {
            try {
                const page = await dispatch(
                    fetchUsersThunk({ page: 1, size: 500, role: UserRole.PARTICIPANT }),
                ).unwrap();
                setExecutors(page.items);
            }
            catch {
                setExecutors([]);
            }
        })();
    }, [dispatch, open, phase]);

    useEffect(() => {
        if (!open || phase !== 'matching') {
            return;
        }
        void (async () => {
            try {
                await dispatch(
                    runMatchingThunk({
                        id: taskId,
                        body: {
                            requiredCount: requiredSlots,
                            matchingMode: preferCloserCritical ? MatchingMode.CRITICAL : MatchingMode.STANDARD,
                            geoReferenceRadiusMeters: DEFAULT_GEO_RADIUS_METERS,
                            maxDailyLoadMinutes: MATCH_MAX_DAILY_LOAD_MINUTES,
                            minTechnicalGapMinutes: MATCH_MIN_TECHNICAL_GAP_MINUTES,
                        },
                    }),
                ).unwrap();
            }
            catch (raw: unknown) {
                dispatch(
                    toastsActions.push({
                        level: 'error',
                        message: validationErrorsToToastMessage(raw as AppApiError),
                        ttlMs: 6000,
                    }),
                );
            }
        })();
    }, [dispatch, open, phase, taskId, requiredSlots, preferCloserCritical]);

    useEffect(() => {
        return () => {
            dispatch(matchingActions.clear());
        };
    }, [dispatch]);

    const toggleExecutorSelection = useCallback(
        (userId: UserId) => {
            setSelectedExecutorIds((prev) => {
                const next = new Set(prev);
                const id = Number(userId);
                if (next.has(id)) {
                    next.delete(id);
                }
                else if (next.size < requiredSlots) {
                    next.add(id);
                }
                return next;
            });
        },
        [requiredSlots],
    );

    const handleConfirmCountAndContinue = useCallback(() => {
        const n = Number(pickCount);
        if (!Number.isFinite(n) || n < MIN_PEOPLE || n > MAX_PEOPLE) {
            dispatch(
                toastsActions.push({
                    level: 'warning',
                    message: `Укажите целое число от ${MIN_PEOPLE} до ${MAX_PEOPLE}.`,
                    ttlMs: 5000,
                }),
            );
            return;
        }
        setPhase('matching');
    }, [dispatch, pickCount]);

    const handleBackToCountStep = useCallback(() => {
        dispatch(matchingActions.clear());
        setSelectedExecutorIds(new Set());
        setExecutorSearch('');
        setPhase('count');
    }, [dispatch]);

    const handleDone = useCallback(async () => {
        if (!matchResult) {
            dispatch(
                toastsActions.push({
                    level: 'warning',
                    message: 'Дождитесь результата подбора или исправьте параметры.',
                    ttlMs: 4000,
                }),
            );
            return;
        }
        if (selectedExecutorIds.size !== requiredSlots) {
            dispatch(
                toastsActions.push({
                    level: 'warning',
                    message: `Выберите ${requiredSlots} ${requiredSlots === 1 ? 'исполнителя' : 'исполнителей'} (сейчас ${selectedExecutorIds.size} из ${requiredSlots}).`,
                    ttlMs: 5000,
                }),
            );
            return;
        }
        setBulkAssigning(true);
        try {
            const ids = Array.from(selectedExecutorIds);
            for (const uid of ids) {
                await dispatch(assignTaskThunk({ id: taskId, userId: uid as UserId })).unwrap();
            }
            void dispatch(fetchTasksForEventThunk({ eventId, query: { page: 1, size: 200 } }));
            void dispatch(fetchEventDashboardThunk(eventId));
            void dispatch(fetchTaskByIdThunk(taskId));
            dispatch(
                toastsActions.push({
                    level: 'success',
                    message: 'Исполнители назначены',
                    ttlMs: 4000,
                }),
            );
            onAssigned();
            onClose();
        }
        catch (raw: unknown) {
            dispatch(
                toastsActions.push({
                    level: 'error',
                    message: validationErrorsToToastMessage(raw as AppApiError),
                    ttlMs: 6000,
                }),
            );
        }
        finally {
            setBulkAssigning(false);
        }
    }, [
        dispatch,
        eventId,
        matchResult,
        onAssigned,
        onClose,
        requiredSlots,
        selectedExecutorIds,
        taskId,
    ]);

    return (
        <Modal
            open={open}
            onClose={onClose}
            title={phase === 'count' ? 'Сколько человек подобрать?' : 'Назначить на задачу'}
            size={phase === 'count' ? 'sm' : 'lg'}
        >
            <div
                className={
                    phase === 'matching'
                        ? 'flex min-h-[min(640px,78vh)] flex-col gap-4 overflow-hidden'
                        : 'flex flex-col gap-4'
                }
            >
                {phase === 'count' ? (
                    <>
                        <Typography variant="body2" color="text.secondary">
                            Укажите, сколько исполнителей нужно найти за этот запуск подбора. На следующем шаге вы
                            сможете выбрать людей из списков (как при создании задачи).
                        </Typography>
                        <div className="flex flex-wrap items-end gap-x-6 gap-y-3">
                            <div className="min-w-[10rem] max-w-xs flex-1">
                                <Input
                                    label="Количество человек"
                                    type="number"
                                    min={MIN_PEOPLE}
                                    max={MAX_PEOPLE}
                                    value={String(pickCount)}
                                    onChange={(e) => setPickCount(Number(e.target.value))}
                                />
                            </div>
                            <FormControlLabel
                                className="m-0 shrink-0"
                                control={
                                    <Checkbox
                                        checked={preferCloserCritical}
                                        onChange={(_, checked) => setPreferCloserCritical(checked)}
                                        size="small"
                                    />
                                }
                                label={
                                    <span className="text-sm text-headline">
                                        Искать кто ближе
                                        <Typography
                                            component="span"
                                            variant="caption"
                                            color="text.secondary"
                                            sx={{ display: 'block' }}
                                        >
                                            Критический режим подбора
                                        </Typography>
                                    </span>
                                }
                            />
                        </div>
                        <div className="flex flex-wrap justify-end gap-2 border-t border-secondary/40 pt-3">
                            <Button type="button" variant="ghost" onClick={onClose}>
                                Отмена
                            </Button>
                            <Button type="button" onClick={handleConfirmCountAndContinue}>
                                Далее
                            </Button>
                        </div>
                    </>
                ) : null}

                {phase === 'matching' ? (
                    <>
                        <div className="flex flex-wrap items-center justify-between gap-2 rounded-lg border border-secondary/40 bg-surface-muted/60 px-3 py-2">
                            <Typography variant="body2" color="text.secondary">
                                Запрос подбора: <strong className="text-headline">{requiredSlots}</strong> исполнителей
                                {preferCloserCritical ? (
                                    <>
                                        {' '}
                                        · <strong className="text-headline">критический</strong> (ближе)
                                    </>
                                ) : (
                                    <>
                                        {' '}
                                        · <span className="text-headline">обычный</span> режим
                                    </>
                                )}
                            </Typography>
                            <Button type="button" size="sm" variant="ghost" onClick={handleBackToCountStep}>
                                Изменить число
                            </Button>
                        </div>

                        <ExecutorMatchingPicker
                            matchResult={matchResult}
                            matchStatus={matchStatus}
                            matchError={matchError}
                            requiredSlots={requiredSlots}
                            executors={executors}
                            executorSearch={executorSearch}
                            onExecutorSearchChange={setExecutorSearch}
                            selectedExecutorIds={selectedExecutorIds}
                            onToggleExecutor={toggleExecutorSelection}
                        />

                        <div className="mt-auto flex flex-wrap justify-end gap-2 border-t border-secondary/40 pt-3">
                            <Button type="button" variant="ghost" onClick={handleBackToCountStep}>
                                Назад
                            </Button>
                            <Button type="button" variant="ghost" onClick={onClose}>
                                Отмена
                            </Button>
                            <Button type="button" loading={bulkAssigning} onClick={() => void handleDone()}>
                                Готово
                            </Button>
                        </div>
                    </>
                ) : null}
            </div>
        </Modal>
    );
}
