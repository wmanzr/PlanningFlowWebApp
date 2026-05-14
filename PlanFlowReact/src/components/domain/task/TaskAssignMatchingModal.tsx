import { useCallback, useEffect, useMemo, useState } from 'react';
import Checkbox from '@mui/material/Checkbox';
import FormControlLabel from '@mui/material/FormControlLabel';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import { Badge, Button, Input, Modal, } from '@/components/ui';
import { surnameWithInitials } from '@/components/domain/event/EventCard';
import { ExecutorMatchingCard, MATCHING_REJECTION_LABELS, } from '@/components/domain/matching';
import { useAppDispatch, useAppSelector } from '@/store';
import { toastsActions } from '@/store/slices/toasts/toastsSlice';
import { matchingActions, runMatchingThunk } from '@/store/slices/matching/matchingSlice';
import { selectMatchingError, selectMatchingResult, selectMatchingStatus, } from '@/store/slices/matching/selectors';
import { fetchEventDashboardThunk } from '@/store/slices/events/eventsSlice';
import { assignTaskThunk, fetchTaskByIdThunk, fetchTasksForEventThunk, } from '@/store/slices/tasks/tasksSlice';
import { fetchUsersThunk } from '@/store/slices/users/usersSlice';
import { MatchingMode, UserRole, type AppApiError, type EventId, type RejectedCandidateResponseDto, type SkillId, type TaskId, type UserId, type UserResponseDto, } from '@/types';
import { validationErrorsToToastMessage } from '@/utils/validationErrorsToToastMessage';
function formatRejectionTooltip(r: RejectedCandidateResponseDto): string {
    const label = MATCHING_REJECTION_LABELS[r.reason];
    const d = r.details?.trim();
    return d ? `${label}: ${d}` : label;
}
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
export function TaskAssignMatchingModal({ open, onClose, taskId, eventId, initialPickCount, onAssigned, }: TaskAssignMatchingModalProps) {
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
        if (!Number.isFinite(n) || n < MIN_PEOPLE)
            return MIN_PEOPLE;
        return Math.min(MAX_PEOPLE, Math.floor(n));
    }, [pickCount]);
    useEffect(() => {
        if (!open)
            return;
        setPhase('count');
        setPickCount(initialPickCount);
        setPreferCloserCritical(false);
        setExecutorSearch('');
        setSelectedExecutorIds(new Set());
        dispatch(matchingActions.clear());
    }, [open, initialPickCount, dispatch]);
    useEffect(() => {
        if (!matchResult)
            return;
        const rejectedIds = new Set(matchResult.rejected.map((r) => Number(r.candidateId)));
        if (rejectedIds.size === 0)
            return;
        setSelectedExecutorIds((prev) => {
            let changed = false;
            const next = new Set(prev);
            for (const id of rejectedIds) {
                if (next.delete(id))
                    changed = true;
            }
            return changed ? next : prev;
        });
    }, [matchResult]);
    const metricsByUserId = useMemo(() => {
        const m = new Map<number, {
            distanceMeters?: number;
            workedTodayMinutes: number;
            maxDailyLoadMinutes: number;
            matchedRequiredSkillIds: SkillId[];
        }>();
        if (!matchResult)
            return m;
        for (const r of matchResult.ranked) {
            m.set(Number(r.candidateId), {
                workedTodayMinutes: r.workedTodayMinutes,
                maxDailyLoadMinutes: r.maxDailyLoadMinutes,
                matchedRequiredSkillIds: r.matchedRequiredSkillIds,
                ...(r.distanceMeters !== undefined ? { distanceMeters: r.distanceMeters } : {}),
            });
        }
        for (const r of matchResult.rejected) {
            const id = Number(r.candidateId);
            if (m.has(id))
                continue;
            m.set(id, {
                workedTodayMinutes: r.workedTodayMinutes,
                maxDailyLoadMinutes: r.maxDailyLoadMinutes,
                matchedRequiredSkillIds: r.matchedRequiredSkillIds,
                ...(r.distanceMeters !== undefined ? { distanceMeters: r.distanceMeters } : {}),
            });
        }
        return m;
    }, [matchResult]);
    const rankedCandidateIds = useMemo(() => new Set(matchResult?.ranked.map((r) => Number(r.candidateId)) ?? []), [matchResult?.ranked]);
    const rejectedByCandidateId = useMemo(() => {
        const m = new Map<number, RejectedCandidateResponseDto>();
        for (const r of matchResult?.rejected ?? []) {
            m.set(Number(r.candidateId), r);
        }
        return m;
    }, [matchResult]);
    const leftExecutors = useMemo(() => {
        const q = executorSearch.trim().toLowerCase();
        const rejectedIds = new Set((matchResult?.rejected ?? []).map((r) => Number(r.candidateId)));
        const filtered = executors
            .filter((u) => !rankedCandidateIds.has(Number(u.id)))
            .filter((u) => {
            if (!q)
                return true;
            return `${u.fullName} ${u.username}`.toLowerCase().includes(q);
        });
        return [...filtered].sort((a, b) => {
            const ar = rejectedIds.has(Number(a.id)) ? 0 : 1;
            const br = rejectedIds.has(Number(b.id)) ? 0 : 1;
            if (ar !== br)
                return ar - br;
            return (a.fullName ?? '').localeCompare(b.fullName ?? '', 'ru');
        });
    }, [executors, executorSearch, rankedCandidateIds, matchResult?.rejected]);
    const rejectedNotInUserDirectory = useMemo(() => {
        const ids = new Set(executors.map((u) => Number(u.id)));
        return (matchResult?.rejected ?? []).filter((r) => !ids.has(Number(r.candidateId)));
    }, [matchResult?.rejected, executors]);
    useEffect(() => {
        if (!open || phase !== 'matching')
            return;
        void (async () => {
            try {
                const page = await dispatch(fetchUsersThunk({ page: 1, size: 500, role: UserRole.PARTICIPANT })).unwrap();
                setExecutors(page.items);
            }
            catch {
                setExecutors([]);
            }
        })();
    }, [dispatch, open, phase]);
    useEffect(() => {
        if (!open || phase !== 'matching')
            return;
        void (async () => {
            try {
                await dispatch(runMatchingThunk({
                    id: taskId,
                    body: {
                        requiredCount: requiredSlots,
                        matchingMode: preferCloserCritical ? MatchingMode.CRITICAL : MatchingMode.STANDARD,
                        geoReferenceRadiusMeters: DEFAULT_GEO_RADIUS_METERS,
                        maxDailyLoadMinutes: MATCH_MAX_DAILY_LOAD_MINUTES,
                        minTechnicalGapMinutes: MATCH_MIN_TECHNICAL_GAP_MINUTES,
                    },
                })).unwrap();
            }
            catch (raw: unknown) {
                dispatch(toastsActions.push({
                    level: 'error',
                    message: validationErrorsToToastMessage(raw as AppApiError),
                    ttlMs: 6000,
                }));
            }
        })();
    }, [dispatch, open, phase, taskId, requiredSlots, preferCloserCritical]);
    useEffect(() => {
        return () => {
            dispatch(matchingActions.clear());
        };
    }, [dispatch]);
    const toggleExecutorSelection = useCallback((userId: UserId) => {
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
    }, [requiredSlots]);
    const handleConfirmCountAndContinue = useCallback(() => {
        const n = Number(pickCount);
        if (!Number.isFinite(n) || n < MIN_PEOPLE || n > MAX_PEOPLE) {
            dispatch(toastsActions.push({
                level: 'warning',
                message: `Укажите целое число от ${MIN_PEOPLE} до ${MAX_PEOPLE}.`,
                ttlMs: 5000,
            }));
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
            dispatch(toastsActions.push({
                level: 'warning',
                message: 'Дождитесь результата подбора или исправьте параметры.',
                ttlMs: 4000,
            }));
            return;
        }
        if (selectedExecutorIds.size !== requiredSlots) {
            dispatch(toastsActions.push({
                level: 'warning',
                message: `Выберите ${requiredSlots} ${requiredSlots === 1 ? 'исполнителя' : 'исполнителей'} (сейчас ${selectedExecutorIds.size} из ${requiredSlots}).`,
                ttlMs: 5000,
            }));
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
            dispatch(toastsActions.push({
                level: 'success',
                message: 'Исполнители назначены',
                ttlMs: 4000,
            }));
            onAssigned();
            onClose();
        }
        catch (raw: unknown) {
            dispatch(toastsActions.push({
                level: 'error',
                message: validationErrorsToToastMessage(raw as AppApiError),
                ttlMs: 6000,
            }));
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
    return (<Modal open={open} onClose={onClose} title={phase === 'count' ? 'Сколько человек подобрать?' : 'Назначить на задачу'} size="lg">
      <div className="flex max-h-[min(85vh,720px)] flex-col gap-4 overflow-hidden">
        {phase === 'count' ? (<>
            <Typography variant="body2" color="text.secondary">
              Укажите, сколько исполнителей нужно найти за этот запуск подбора. На следующем шаге вы сможете
              выбрать людей из списков (как при создании задачи).
            </Typography>
            <div className="flex flex-wrap items-end gap-x-6 gap-y-3">
              <div className="min-w-[10rem] max-w-xs flex-1">
                <Input label="Количество человек" type="number" min={MIN_PEOPLE} max={MAX_PEOPLE} value={String(pickCount)} onChange={(e) => setPickCount(Number(e.target.value))}/>
              </div>
              <FormControlLabel className="m-0 shrink-0" control={<Checkbox checked={preferCloserCritical} onChange={(_, checked) => setPreferCloserCritical(checked)} size="small"/>} label={<span className="text-sm text-foreground">
                    Искать кто ближе
                    <Typography component="span" variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                      Критический режим подбора
                    </Typography>
                  </span>}/>
            </div>
            <div className="flex flex-wrap justify-end gap-2 border-t border-secondary/40 pt-3">
              <Button type="button" variant="ghost" onClick={onClose}>
                Отмена
              </Button>
              <Button type="button" onClick={handleConfirmCountAndContinue}>
                Далее
              </Button>
            </div>
          </>) : null}

        {phase === 'matching' ? (<>
            <div className="flex flex-wrap items-center justify-between gap-2 rounded-lg border border-secondary/40 bg-surface-muted/60 px-3 py-2">
              <Typography variant="body2" color="text.secondary">
                Запрос подбора: <strong className="text-foreground">{requiredSlots}</strong> исполнителей
                {preferCloserCritical ? (<>
                    {' '}
                    · <strong className="text-foreground">критический</strong> (ближе)
                  </>) : (<>
                    {' '}
                    · <span className="text-foreground">обычный</span> режим
                  </>)}
              </Typography>
              <Button type="button" size="sm" variant="ghost" onClick={handleBackToCountStep}>
                Изменить число
              </Button>
            </div>

        {matchStatus === 'pending' ? (<Typography variant="body2" color="text.secondary">
            Выполняется подбор кандидатов…
          </Typography>) : null}
        {matchError ? (<Typography variant="body2" color="error">
            {matchError.message}
          </Typography>) : null}

        {matchResult ? (<>
            <div className="rounded-lg border border-secondary/45 bg-surface-muted/70 px-3 py-2.5">
              <Typography variant="body2" className="font-semibold text-foreground">
                Выделено: {selectedExecutorIds.size} из {requiredSlots}
              </Typography>
              <Typography variant="caption" color="text.secondary" className="mt-0.5 block">
                Нажмите на карточку, чтобы выбрать исполнителя. Отклоненные алгоритмом подсвечены серым и недоступны
                — при наведении показывается причина.
              </Typography>
            </div>
            <div className="grid min-h-0 w-full min-w-0 flex-1 grid-cols-1 gap-4 overflow-hidden lg:grid-cols-2 lg:items-start">
              <div className="flex min-h-0 min-w-0 flex-col gap-3">
                <Typography variant="subtitle2">Все исполнители</Typography>
                <TextField size="small" label="Поиск по ФИО" value={executorSearch} onChange={(e) => setExecutorSearch(e.target.value)} placeholder="Имя или фамилия"/>
                <div className="flex max-h-[min(320px,40vh)] flex-col gap-2 overflow-y-auto pr-1">
                  {leftExecutors.length === 0 && rejectedNotInUserDirectory.length === 0 ? (<Typography variant="body2" color="text.secondary">
                      Нет исполнителей для отображения (или ничего не найдено по запросу).
                    </Typography>) : (<>
                      {leftExecutors.map((u) => {
                        const metrics = metricsByUserId.get(Number(u.id));
                        const algorithmMiss = metrics === undefined;
                        const uid = Number(u.id);
                        const rej = rejectedByCandidateId.get(uid);
                        const blockedTip = rej !== undefined ? formatRejectionTooltip(rej) : undefined;
                        const sel = !blockedTip && selectedExecutorIds.has(uid);
                        const addLocked = !blockedTip && !sel && selectedExecutorIds.size >= requiredSlots;
                        return (<ExecutorMatchingCard key={u.id} fullName={surnameWithInitials(u.fullName)} username={u.username} matchedSkillIds={metrics?.matchedRequiredSkillIds ?? []} {...(metrics && !algorithmMiss
                            ? {
                                workedTodayMinutes: metrics.workedTodayMinutes,
                                maxDailyLoadMinutes: metrics.maxDailyLoadMinutes,
                                ...(metrics.distanceMeters !== undefined
                                    ? { distanceMeters: metrics.distanceMeters }
                                    : {}),
                            }
                            : {})} algorithmMiss={algorithmMiss && !blockedTip} {...(blockedTip !== undefined
                            ? { blockedSelectionTooltip: blockedTip, selectable: false }
                            : {
                                selectable: true,
                                selected: sel,
                                selectionAddDisabled: addLocked,
                                onToggleSelect: () => {
                                    if (addLocked)
                                        return;
                                    toggleExecutorSelection(u.id);
                                },
                            })}/>);
                    })}
                      {rejectedNotInUserDirectory.map((r) => (<ExecutorMatchingCard key={`rej-dir-${r.candidateId}`} fullName={surnameWithInitials(r.candidateFullName)} username={r.candidateUsername} matchedSkillIds={r.matchedRequiredSkillIds} workedTodayMinutes={r.workedTodayMinutes} maxDailyLoadMinutes={r.maxDailyLoadMinutes} {...(r.distanceMeters !== undefined
                        ? { distanceMeters: r.distanceMeters }
                        : {})} blockedSelectionTooltip={formatRejectionTooltip(r)} selectable={false}/>))}
                    </>)}
                </div>
              </div>
              <div className="flex min-h-0 min-w-0 flex-col gap-3">
                <Typography variant="subtitle2">Рекомендованные кандидаты</Typography>
                <div className="flex max-h-[min(320px,40vh)] flex-col gap-2 overflow-y-auto pr-1">
                  {matchResult.ranked.length === 0 ? (<Typography variant="body2" color="text.secondary">
                      Алгоритм не предложил кандидатов в этом режиме подбора.
                    </Typography>) : (matchResult.ranked.map((c) => {
                    const uid = Number(c.candidateId);
                    const sel = selectedExecutorIds.has(uid);
                    const addLocked = !sel && selectedExecutorIds.size >= requiredSlots;
                    return (<ExecutorMatchingCard key={c.candidateId} fullName={surnameWithInitials(c.candidateFullName)} username={c.candidateUsername} matchedSkillIds={c.matchedRequiredSkillIds} workedTodayMinutes={c.workedTodayMinutes} maxDailyLoadMinutes={c.maxDailyLoadMinutes} {...(c.distanceMeters !== undefined
                        ? { distanceMeters: c.distanceMeters }
                        : {})} rankBadge={<Badge tone="success">#{c.rank}</Badge>} selectable selected={sel} selectionAddDisabled={addLocked} onToggleSelect={() => {
                            if (addLocked)
                                return;
                            toggleExecutorSelection(c.candidateId);
                        }}/>);
                }))}
                </div>
              </div>
            </div>
          </>) : null}

            <div className="flex flex-wrap justify-end gap-2 border-t border-secondary/40 pt-3">
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
          </>) : null}
      </div>
    </Modal>);
}
