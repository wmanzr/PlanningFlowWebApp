import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Controller, useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import Autocomplete, { createFilterOptions } from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import { Badge, Button, Input, MapView, Select, coerceApiDateTimeToIso, fromDateAndTimeInputs, toDateInput, toNaiveLocalIsoFromTimestamp, toTimeInputRoundedToStep, type MapMarker, } from '@/components/ui';
import { formatDateTime } from '@/components/ui/formatters';
import { surnameWithInitials } from '@/components/domain/event/EventCard';
import { ExecutorMatchingCard, MATCHING_REJECTION_LABELS, } from '@/components/domain/matching';
import { useAppDispatch, useAppSelector } from '@/store';
import { toastsActions } from '@/store/slices/toasts/toastsSlice';
import { matchingActions, runMatchingThunk, } from '@/store/slices/matching/matchingSlice';
import { selectMatchingError, selectMatchingResult, selectMatchingStatus, } from '@/store/slices/matching/selectors';
import { fetchSkillsThunk } from '@/store/slices/skills/skillsSlice';
import { selectAllSkills, selectSkillsListMeta } from '@/store/slices/skills/selectors';
import { fetchEventByIdThunk, fetchEventDashboardThunk, } from '@/store/slices/events/eventsSlice';
import { selectEventById } from '@/store/slices/events/selectors';
import { assignTaskThunk, createTaskThunk, fetchTaskByIdThunk, fetchTasksForEventThunk, updateTaskThunk, } from '@/store/slices/tasks/tasksSlice';
import { makeSelectTasksByEvent, selectTaskById } from '@/store/slices/tasks/selectors';
import { fetchUsersThunk } from '@/store/slices/users/usersSlice';
import { asIsoDateTime, asSkillId, LATITUDE_MAX, LATITUDE_MIN, LONGITUDE_MAX, LONGITUDE_MIN, MatchingMode, UserRole, type AppApiError, type EventId, type EventResponseDto, type RejectedCandidateResponseDto, type SkillId, type SkillResponseDto, type TaskId, type TaskResponseDto, type UserId, type UserResponseDto, } from '@/types';
import { validationErrorsToToastMessage } from '@/utils/validationErrorsToToastMessage';
import { PATHS } from '@/pages/paths';
function formatRejectionTooltip(r: RejectedCandidateResponseDto): string {
    const label = MATCHING_REJECTION_LABELS[r.reason];
    const d = r.details?.trim();
    return d ? `${label}: ${d}` : label;
}
const TITLE_MAX = 200;
const MIN_PEOPLE = 1;
const MAX_PEOPLE = 1000;
const MAX_TASK_DURATION_MS = 8 * 60 * 60 * 1000;
const MATCH_MAX_DAILY_LOAD_MINUTES = 480;
const MATCH_MIN_TECHNICAL_GAP_MINUTES = 15;
const TIME_STEP_MINUTES = 30;
const TASK_MATCHING_RADIUS_OPTIONS: {
    value: number;
    label: string;
}[] = [
    { value: 500, label: 'Локально' },
    { value: 35000, label: 'Город' },
    { value: 65000, label: 'Регион' },
];
const buildTimeOptions = (): {
    value: string;
    label: string;
}[] => {
    const options: {
        value: string;
        label: string;
    }[] = [];
    for (let h = 0; h < 24; h += 1) {
        for (let m = 0; m < 60; m += TIME_STEP_MINUTES) {
            const hh = String(h).padStart(2, '0');
            const mm = String(m).padStart(2, '0');
            const value = `${hh}:${mm}`;
            options.push({ value, label: value });
        }
    }
    return options;
};
const filterSkills = createFilterOptions<SkillResponseDto>({
    matchFrom: 'any',
    stringify: (option) => `${option.name} ${option.category ?? ''}`.trim(),
});
const filterTaskOptions = createFilterOptions<TaskResponseDto>({
    matchFrom: 'any',
    stringify: (t) => `${t.title} ${t.startTime} ${t.endTime}`,
});
function formatTaskPickerLabel(t: TaskResponseDto): string {
    return `${t.title} · ${formatDateTime(t.startTime)} — ${formatDateTime(t.endTime)}`;
}
function buildWizardSchema() {
    return z
        .object({
        title: z.string().trim().min(1, 'Название обязательно').max(TITLE_MAX),
        startDate: z.string().min(1, 'Укажите дату начала'),
        startTime: z.string().min(1, 'Укажите время начала'),
        endDate: z.string().min(1, 'Укажите дату завершения'),
        endTime: z.string().min(1, 'Укажите время завершения'),
        requiredCount: z.coerce
            .number()
            .int()
            .min(MIN_PEOPLE, `Не меньше ${MIN_PEOPLE}`)
            .max(MAX_PEOPLE),
        requiredSkillIds: z.array(z.number().int().positive()).default([]),
        geoReferenceRadiusMeters: z.union([
            z.literal(500),
            z.literal(35000),
            z.literal(65000),
        ]),
        latitude: z
            .union([z.string(), z.number()])
            .optional()
            .transform((value) => value === '' || value === undefined ? undefined : Number(value))
            .refine((value) => value === undefined ||
            (Number.isFinite(value) && value >= LATITUDE_MIN && value <= LATITUDE_MAX), `Широта в диапазоне ${LATITUDE_MIN}…${LATITUDE_MAX}`),
        longitude: z
            .union([z.string(), z.number()])
            .optional()
            .transform((value) => value === '' || value === undefined ? undefined : Number(value))
            .refine((value) => value === undefined ||
            (Number.isFinite(value) && value >= LONGITUDE_MIN && value <= LONGITUDE_MAX), `Долгота в диапазоне ${LONGITUDE_MIN}…${LONGITUDE_MAX}`),
    })
        .superRefine((data, ctx) => {
        const startIso = fromDateAndTimeInputs(data.startDate, data.startTime);
        const endIso = fromDateAndTimeInputs(data.endDate, data.endTime);
        const startMs = new Date(startIso).getTime();
        const endMs = new Date(endIso).getTime();
        if (!startIso || !endIso || Number.isNaN(startMs) || Number.isNaN(endMs)) {
            ctx.addIssue({
                code: z.ZodIssueCode.custom,
                path: ['startDate'],
                message: 'Некорректная дата или время',
            });
            return;
        }
        if (endMs <= startMs) {
            ctx.addIssue({
                code: z.ZodIssueCode.custom,
                path: ['endTime'],
                message: 'Завершение должно быть позже начала',
            });
        }
        const hasLat = typeof data.latitude === 'number';
        const hasLng = typeof data.longitude === 'number';
        if (hasLat !== hasLng) {
            ctx.addIssue({
                code: z.ZodIssueCode.custom,
                path: ['latitude'],
                message: 'Укажите точку на карте',
            });
        }
    });
}
type WizardSchema = ReturnType<typeof buildWizardSchema>;
type WizardInput = z.input<WizardSchema>;
type WizardOutput = z.output<WizardSchema>;
const wizardSchema = buildWizardSchema();
function validateWizardSchedule(data: Pick<WizardOutput, 'startDate' | 'startTime' | 'endDate' | 'endTime'>, eventEntity: EventResponseDto | undefined): {
    field: 'startDate' | 'endDate' | 'startTime' | 'endTime';
    message: string;
} | null {
    const startIso = fromDateAndTimeInputs(data.startDate, data.startTime);
    const endIso = fromDateAndTimeInputs(data.endDate, data.endTime);
    const startMs = new Date(startIso).getTime();
    const endMs = new Date(endIso).getTime();
    if (endMs - startMs > MAX_TASK_DURATION_MS) {
        return { field: 'endTime', message: 'Задача не может длиться больше 8 часов' };
    }
    if (eventEntity) {
        const evStart = new Date(eventEntity.startDate).getTime();
        const evEnd = new Date(eventEntity.endDate).getTime();
        if (startMs < evStart || endMs > evEnd) {
            return {
                field: 'startDate',
                message: 'Время задачи должно быть в пределах дат мероприятия',
            };
        }
    }
    return null;
}
export interface TaskCreateWizardProps {
    open: boolean;
    eventId: EventId;
    onClose: () => void;
}
export const TaskCreateWizard = ({ open, eventId, onClose }: TaskCreateWizardProps) => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const skills = useAppSelector(selectAllSkills);
    const skillsList = useAppSelector(selectSkillsListMeta);
    const timeOptions = useMemo(() => buildTimeOptions(), []);
    const matchResult = useAppSelector(selectMatchingResult);
    const matchStatus = useAppSelector(selectMatchingStatus);
    const matchError = useAppSelector(selectMatchingError);
    const selectTasksForEvent = useMemo(() => makeSelectTasksByEvent(eventId), [eventId]);
    const eventTasks = useAppSelector(selectTasksForEvent);
    const eventEntity = useAppSelector(selectEventById(eventId));
    const [step, setStep] = useState<0 | 1>(0);
    const [createdTaskId, setCreatedTaskId] = useState<TaskId | null>(null);
    const [submitting, setSubmitting] = useState(false);
    const [executorSearch, setExecutorSearch] = useState('');
    const [executors, setExecutors] = useState<UserResponseDto[]>([]);
    const [scheduleRangeMessage, setScheduleRangeMessage] = useState<string | null>(null);
    const schedulePrimedRef = useRef(false);
    const [selectedExecutorIds, setSelectedExecutorIds] = useState(() => new Set<number>());
    const [bulkAssigning, setBulkAssigning] = useState(false);
    const createdTaskEntity = useAppSelector(selectTaskById(createdTaskId ?? undefined));
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
    const taskPickerOptions = useMemo(() => eventTasks.filter((t) => createdTaskId === null || t.id !== createdTaskId), [eventTasks, createdTaskId]);
    const parentTaskValue = useMemo(() => {
        const dep = createdTaskEntity?.dependencyIds?.[0];
        if (dep === undefined)
            return null;
        return taskPickerOptions.find((t) => t.id === dep) ?? null;
    }, [createdTaskEntity?.dependencyIds, taskPickerOptions]);
    const { register, handleSubmit, reset, setValue, control, formState: { errors }, } = useForm<WizardInput, unknown, WizardOutput>({
        mode: 'onSubmit',
        reValidateMode: 'onChange',
        resolver: zodResolver(wizardSchema),
        defaultValues: {
            title: '',
            startDate: '',
            startTime: '',
            endDate: '',
            endTime: '',
            requiredCount: 1,
            requiredSkillIds: [],
            geoReferenceRadiusMeters: 35000,
            latitude: '',
            longitude: '',
        },
    });
    useEffect(() => {
        if (!open || !eventEntity || schedulePrimedRef.current)
            return;
        const evStartIso = coerceApiDateTimeToIso(eventEntity.startDate as unknown) ?? String(eventEntity.startDate);
        const evEndIso = coerceApiDateTimeToIso(eventEntity.endDate as unknown) ?? String(eventEntity.endDate);
        const startMs = new Date(evStartIso).getTime();
        const endMs = new Date(evEndIso).getTime();
        if (Number.isNaN(startMs) || Number.isNaN(endMs) || endMs <= startMs)
            return;
        const endTs = Math.min(startMs + MAX_TASK_DURATION_MS, endMs);
        const endPrimedNaive = toNaiveLocalIsoFromTimestamp(endTs);
        setValue('startDate', toDateInput(evStartIso), { shouldValidate: false });
        setValue('startTime', toTimeInputRoundedToStep(evStartIso, TIME_STEP_MINUTES), {
            shouldValidate: false,
        });
        setValue('endDate', toDateInput(endPrimedNaive), { shouldValidate: false });
        setValue('endTime', toTimeInputRoundedToStep(endPrimedNaive, TIME_STEP_MINUTES), {
            shouldValidate: false,
        });
        schedulePrimedRef.current = true;
    }, [open, eventEntity, setValue]);
    useEffect(() => {
        return () => {
            dispatch(matchingActions.clear());
        };
    }, [dispatch]);
    const watchedLatitude = useWatch({ control, name: 'latitude' });
    const watchedLongitude = useWatch({ control, name: 'longitude' });
    const watchedStartDate = useWatch({ control, name: 'startDate' });
    const watchedStartTime = useWatch({ control, name: 'startTime' });
    const watchedEndDate = useWatch({ control, name: 'endDate' });
    const watchedEndTime = useWatch({ control, name: 'endTime' });
    const selectedSkillIds = useWatch({ control, name: 'requiredSkillIds', defaultValue: [] });
    const watchedRequiredCount = useWatch({ control, name: 'requiredCount', defaultValue: 1 });
    useEffect(() => {
        setScheduleRangeMessage(null);
    }, [watchedStartDate, watchedStartTime, watchedEndDate, watchedEndTime]);
    const selectedSkills = useMemo(() => {
        const ids = new Set((selectedSkillIds ?? []).map(Number));
        return skills.filter((s) => ids.has(Number(s.id)));
    }, [skills, selectedSkillIds]);
    const mapMarkers = useMemo<MapMarker[]>(() => {
        const lat = Number(watchedLatitude);
        const lng = Number(watchedLongitude);
        if (!Number.isFinite(lat) || !Number.isFinite(lng))
            return [];
        return [{ id: 'task-pos', lat, lng, kind: 'task', label: 'Задача' }];
    }, [watchedLatitude, watchedLongitude]);
    const mapCenter = mapMarkers.length > 0 && mapMarkers[0]
        ? { latitude: mapMarkers[0].lat, longitude: mapMarkers[0].lng }
        : undefined;
    useEffect(() => {
        if (!open)
            return;
        void dispatch(fetchSkillsThunk({ page: 1, size: 500 }));
    }, [dispatch, open]);
    useEffect(() => {
        if (!open)
            return;
        void dispatch(fetchEventByIdThunk(eventId));
    }, [dispatch, eventId, open]);
    const resetWizard = useCallback(() => {
        setStep(0);
        setCreatedTaskId(null);
        setSubmitting(false);
        setExecutorSearch('');
        setExecutors([]);
        setSelectedExecutorIds(new Set());
        schedulePrimedRef.current = false;
        setScheduleRangeMessage(null);
        dispatch(matchingActions.clear());
        reset({
            title: '',
            startDate: '',
            startTime: '',
            endDate: '',
            endTime: '',
            requiredCount: 1,
            requiredSkillIds: [],
            geoReferenceRadiusMeters: 35000,
            latitude: '',
            longitude: '',
        });
    }, [dispatch, reset]);
    useEffect(() => {
        if (!open) {
            resetWizard();
        }
    }, [open, resetWizard]);
    useEffect(() => {
        if (!open || step !== 1 || !createdTaskId)
            return;
        void (async () => {
            try {
                const page = await dispatch(fetchUsersThunk({ page: 1, size: 500, role: UserRole.PARTICIPANT })).unwrap();
                setExecutors(page.items);
            }
            catch {
                setExecutors([]);
            }
            void dispatch(fetchTaskByIdThunk(createdTaskId));
            void dispatch(fetchTasksForEventThunk({ eventId, query: { page: 1, size: 200 } }));
        })();
    }, [open, step, createdTaskId, dispatch, eventId]);
    const goNext = handleSubmit(async (data) => {
        setScheduleRangeMessage(null);
        const scheduleIssue = validateWizardSchedule(data, eventEntity);
        if (scheduleIssue) {
            setScheduleRangeMessage(scheduleIssue.message);
            return;
        }
        setSubmitting(true);
        try {
            const lat = data.latitude;
            const lng = data.longitude;
            const skillIds = (data.requiredSkillIds ?? []).map((id) => asSkillId(Number(id)));
            const startIso = asIsoDateTime(fromDateAndTimeInputs(data.startDate, data.startTime));
            const endIso = asIsoDateTime(fromDateAndTimeInputs(data.endDate, data.endTime));
            let taskId = createdTaskId;
            if (!taskId) {
                const id = await dispatch(createTaskThunk({
                    eventId,
                    title: data.title,
                    startTime: startIso,
                    endTime: endIso,
                    ...(typeof lat === 'number' && typeof lng === 'number'
                        ? { latitude: lat, longitude: lng }
                        : {}),
                    ...(skillIds.length > 0 ? { requiredSkillIds: skillIds } : {}),
                })).unwrap();
                taskId = id;
                setCreatedTaskId(id);
            }
            else {
                await dispatch(updateTaskThunk({
                    id: taskId,
                    body: {
                        newTitle: data.title,
                        newStartTime: startIso,
                        newEndTime: endIso,
                        ...(typeof lat === 'number' && typeof lng === 'number'
                            ? { latitude: lat, longitude: lng }
                            : {}),
                        requiredSkillIds: skillIds,
                    },
                })).unwrap();
            }
            await dispatch(runMatchingThunk({
                id: taskId,
                body: {
                    requiredCount: data.requiredCount,
                    matchingMode: MatchingMode.STANDARD,
                    geoReferenceRadiusMeters: data.geoReferenceRadiusMeters,
                    maxDailyLoadMinutes: MATCH_MAX_DAILY_LOAD_MINUTES,
                    minTechnicalGapMinutes: MATCH_MIN_TECHNICAL_GAP_MINUTES,
                },
            })).unwrap();
            setSelectedExecutorIds(new Set());
            void dispatch(fetchTasksForEventThunk({ eventId, query: { page: 1, size: 200 } }));
            void dispatch(fetchEventDashboardThunk(eventId));
            setStep(1);
        }
        catch (raw: unknown) {
            dispatch(toastsActions.push({
                level: 'error',
                message: validationErrorsToToastMessage(raw as AppApiError),
                ttlMs: 6000,
            }));
        }
        finally {
            setSubmitting(false);
        }
    });
    const handleParentTaskChange = useCallback(async (task: TaskResponseDto | null) => {
        if (!createdTaskId)
            return;
        try {
            await dispatch(updateTaskThunk({
                id: createdTaskId,
                body: { dependencyIds: task ? [task.id] : [] },
            })).unwrap();
            await dispatch(fetchTaskByIdThunk(createdTaskId)).unwrap();
        }
        catch (raw: unknown) {
            dispatch(toastsActions.push({
                level: 'error',
                message: validationErrorsToToastMessage(raw as AppApiError),
                ttlMs: 6000,
            }));
        }
    }, [createdTaskId, dispatch]);
    const requiredSlots = useMemo(() => {
        const n = Number(watchedRequiredCount);
        if (!Number.isFinite(n) || n < MIN_PEOPLE)
            return MIN_PEOPLE;
        return Math.min(MAX_PEOPLE, Math.floor(n));
    }, [watchedRequiredCount]);
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
    const handleFinishStep2 = useCallback(async () => {
        if (!createdTaskId) {
            onClose();
            return;
        }
        if (!matchResult) {
            dispatch(toastsActions.push({
                level: 'warning',
                message: 'Результат подбора недоступен.',
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
                await dispatch(assignTaskThunk({ id: createdTaskId, userId: uid as UserId })).unwrap();
            }
            void dispatch(fetchTasksForEventThunk({ eventId, query: { page: 1, size: 200 } }));
            void dispatch(fetchEventDashboardThunk(eventId));
            dispatch(toastsActions.push({
                level: 'success',
                message: 'Исполнители назначены на задачу',
                ttlMs: 4000,
            }));
            navigate(PATHS.taskDetail(eventId, createdTaskId));
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
        createdTaskId,
        dispatch,
        eventId,
        matchResult,
        navigate,
        onClose,
        requiredSlots,
        selectedExecutorIds,
    ]);
    const scheduleBannerMessages = useMemo(() => {
        const lines: string[] = [];
        if (scheduleRangeMessage?.trim()) {
            lines.push(scheduleRangeMessage.trim());
        }
        for (const key of ['startDate', 'startTime', 'endDate', 'endTime'] as const) {
            const raw = errors[key]?.message;
            const m = typeof raw === 'string' ? raw.trim() : '';
            if (m)
                lines.push(m);
        }
        return [...new Set(lines)];
    }, [
        scheduleRangeMessage,
        errors.startDate,
        errors.startTime,
        errors.endDate,
        errors.endTime,
    ]);
    if (!open)
        return null;
    return (<div className="flex w-full min-w-[min(100%,42rem)] flex-col gap-4">
      {step === 0 ? (<form className="flex flex-col gap-4" onSubmit={goNext} noValidate>
          <div className="grid min-w-0 grid-cols-[minmax(0,1fr)_minmax(5.25rem,7rem)] items-end gap-3">
            <Input className="min-w-0" label="Название задачи" error={errors.title?.message} {...register('title')}/>
            <Input className="min-w-0 shrink-0" label="Человек" type="number" min={MIN_PEOPLE} max={MAX_PEOPLE} error={errors.requiredCount?.message} {...register('requiredCount')}/>
          </div>
          <div className="rounded-lg border border-secondary/50 bg-surface-muted p-4">
            {scheduleBannerMessages.length > 0 ? (<div role="alert" className="mb-4 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm leading-snug text-red-900 dark:border-red-900/80 dark:bg-red-950/50 dark:text-red-100">
                <div className="space-y-1">
                  {scheduleBannerMessages.map((msg) => (<p key={msg} className="m-0">
                      {msg}
                    </p>))}
                </div>
              </div>) : null}
            <div className="grid gap-4 md:grid-cols-2 md:items-start">
              <div className="grid min-w-0 gap-2">
                <Typography variant="subtitle2">Начало</Typography>
                <div className="grid min-w-0 gap-3 sm:grid-cols-2">
                  <Input type="date" aria-label="Начало — дата" {...register('startDate')}/>
                  <Controller name="startTime" control={control} render={({ field }) => (<Select aria-label="Начало — время" options={timeOptions} placeholder="Время" name={field.name} value={field.value} onChange={field.onChange} onBlur={field.onBlur} ref={field.ref}/>)}/>
                </div>
              </div>
              <div className="grid min-w-0 gap-2">
                <Typography variant="subtitle2">Завершение</Typography>
                <div className="grid min-w-0 gap-3 sm:grid-cols-2">
                  <Input type="date" aria-label="Завершение — дата" {...register('endDate')}/>
                  <Controller name="endTime" control={control} render={({ field }) => (<Select aria-label="Завершение — время" options={timeOptions} placeholder="Время" name={field.name} value={field.value} onChange={field.onChange} onBlur={field.onBlur} ref={field.ref}/>)}/>
                </div>
              </div>
            </div>
          </div>
          <Autocomplete multiple disableCloseOnSelect disablePortal options={skills} loading={skillsList.status === 'pending'} disabled={skillsList.status === 'pending' && skills.length === 0} value={selectedSkills} onChange={(_, newValue) => {
                setValue('requiredSkillIds', newValue.map((s) => Number(s.id)), { shouldValidate: true });
            }} filterOptions={filterSkills} getOptionLabel={(option) => {
                const cat = option.category?.trim();
                return cat ? `${option.name} (${cat})` : option.name;
            }} isOptionEqualToValue={(a, b) => a.id === b.id} slotProps={{
                popper: {
                    placement: 'top-start',
                    modifiers: [{ name: 'offset', options: { offset: [0, 8] } }],
                },
                listbox: {
                    sx: { maxHeight: 280 },
                },
            }} renderInput={(params) => (<TextField {...params} label="Навыки" placeholder="Введите название для поиска…" helperText={skillsList.status === 'pending' && skills.length === 0
                    ? 'Список навыков загружается…'
                    : 'Введите текст для поиска; можно выбрать несколько навыков'}/>)}/>
          <div className="grid gap-3 sm:grid-cols-2 sm:items-stretch sm:gap-4">
            <div className="rounded-xl border border-secondary/40 bg-surface-muted p-3 shadow-sm">
              <Typography variant="subtitle2" className="mb-2">
                Охват подбора
              </Typography>
              <Controller name="geoReferenceRadiusMeters" control={control} render={({ field }) => (<fieldset className="m-0 min-w-0 border-0 p-0">
                    <legend className="sr-only">Охват гео-подбора</legend>
                    <div className="flex flex-col gap-2">
                      {TASK_MATCHING_RADIUS_OPTIONS.map((opt) => {
                    const selected = field.value === opt.value;
                    return (<label key={opt.value} className="flex cursor-pointer items-center gap-2.5 rounded-md py-0.5">
                            <input type="radio" name={field.name} value={opt.value} checked={selected} onChange={() => field.onChange(opt.value)} onBlur={field.onBlur} className="h-4 w-4 shrink-0 accent-zinc-700 dark:accent-zinc-300"/>
                            <span className={`text-sm ${selected ? 'font-medium text-foreground' : 'text-paragraph'}`}>
                              {opt.label}
                            </span>
                          </label>);
                })}
                    </div>
                  </fieldset>)}/>
              {errors.geoReferenceRadiusMeters?.message ? (<p className="mt-2 text-sm text-danger">{errors.geoReferenceRadiusMeters.message}</p>) : null}
            </div>
            <div className="flex gap-2.5 rounded-xl border border-neutral-200/90 bg-white p-3 shadow-sm dark:border-neutral-700 dark:bg-zinc-900">
              <InfoOutlinedIcon className="mt-0.5 shrink-0 text-neutral-400 dark:text-neutral-500" fontSize="small" aria-hidden/>
              <div className="min-w-0 flex-1 space-y-2">
                <Typography variant="caption" color="text.secondary" className="block font-medium uppercase tracking-wide">
                  Нормативы
                </Typography>
                <Typography variant="body2" color="text.secondary" className="text-sm leading-snug">
                  Суточный лимит труда по РФ — <strong className="font-medium text-foreground">8 ч</strong>.
                </Typography>
                <Typography variant="body2" color="text.secondary" className="text-sm leading-snug">
                  Обязательный перерыв между задачами составляет — <strong className="font-medium text-foreground">15 мин</strong>.
                </Typography>
              </div>
            </div>
          </div>
          <div className="rounded-lg border border-secondary/50 bg-surface-muted p-4">
            <div className="mb-2 flex items-center justify-between gap-3">
              <div>
                <Typography variant="subtitle2">Координаты на карте</Typography>
                <Typography variant="caption" color="text.secondary">
                  Нажмите на карту, чтобы указать место задачи.
                </Typography>
              </div>
              {mapMarkers.length > 0 ? (<Button type="button" size="sm" variant="ghost" onClick={() => {
                    setValue('latitude', '', { shouldValidate: true });
                    setValue('longitude', '', { shouldValidate: true });
                }}>
                  Очистить
                </Button>) : null}
            </div>
            <MapView height="220px" {...(mapCenter ? { center: mapCenter } : {})} markers={mapMarkers} onMapClick={(point) => {
                setValue('latitude', String(point.latitude), { shouldValidate: true });
                setValue('longitude', String(point.longitude), { shouldValidate: true });
            }}/>
            {errors.latitude?.message ? (<p className="mt-2 text-sm text-danger">{errors.latitude.message}</p>) : null}
          </div>
          <div className="flex justify-between gap-2">
            <Button type="button" variant="ghost" onClick={onClose}>
              Отмена
            </Button>
            <Button type="submit" loading={submitting}>
              Далее
            </Button>
          </div>
        </form>) : (<div className="flex flex-col gap-4">
          {matchStatus === 'pending' ? (<Typography variant="body2" color="text.secondary">
              Выполняется подбор кандидатов…
            </Typography>) : null}
          {matchError ? (<Typography variant="body2" color="error">
              {matchError.message}
            </Typography>) : null}

          {taskPickerOptions.length > 0 ? (<div className="flex flex-col gap-2 rounded-lg border border-secondary/40 bg-surface-muted p-3">
              <Autocomplete disablePortal options={taskPickerOptions} value={parentTaskValue} onChange={(_, task) => void handleParentTaskChange(task)} getOptionLabel={(t) => formatTaskPickerLabel(t)} filterOptions={filterTaskOptions} isOptionEqualToValue={(a, b) => a.id === b.id} renderOption={(props, option) => (<li {...props} key={option.id}>
                    <div className="flex flex-col py-0.5">
                      <span>{option.title}</span>
                      <Typography variant="caption" color="text.secondary">
                        {formatDateTime(option.startTime)} — {formatDateTime(option.endTime)}
                      </Typography>
                    </div>
                  </li>)} renderInput={(params) => (<TextField {...params} label="Первоочередная задача" placeholder="Поиск по названию или датам…" size="small"/>)}/>
              <Typography variant="caption" color="text.secondary">
                Необязательно: задача мероприятия, которая должна завершиться раньше этой.
              </Typography>
            </div>) : null}

          {matchResult ? (<>
              <div className="rounded-lg border border-secondary/45 bg-surface-muted/70 px-3 py-2.5">
                <Typography variant="body2" className="font-semibold text-foreground">
                  Выделено: {selectedExecutorIds.size} из {requiredSlots}
                </Typography>
                <Typography variant="caption" color="text.secondary" className="mt-0.5 block">
                  Нажмите на карточку, чтобы выбрать исполнителя. Отклоненные алгоритмом подсвечены серым и недоступны — при наведении показывается причина.
                </Typography>
              </div>
              <div className="grid w-full min-w-0 grid-cols-1 gap-4 lg:grid-cols-2 lg:items-start">
                <div className="flex min-h-0 min-w-0 flex-col gap-3">
                  <Typography variant="subtitle2">Все исполнители</Typography>
                  <TextField size="small" label="Поиск по ФИО" value={executorSearch} onChange={(e) => setExecutorSearch(e.target.value)} placeholder="Имя или фамилия"/>
                  <div className="flex max-h-[min(420px,55vh)] flex-col gap-2 overflow-y-auto pr-1">
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
                <div className="flex min-w-0 flex-col gap-3">
                  <Typography variant="subtitle2">Рекомендованные кандидаты</Typography>
                  <div className="flex max-h-[min(420px,55vh)] flex-col gap-2 overflow-y-auto pr-1">
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
          <div className="flex flex-wrap justify-between gap-2">
            <Button type="button" variant="ghost" onClick={() => {
                setStep(0);
            }}>
              Назад
            </Button>
            <Button type="button" loading={bulkAssigning} onClick={() => void handleFinishStep2()}>
              Готово
            </Button>
          </div>
        </div>)}
    </div>);
};
