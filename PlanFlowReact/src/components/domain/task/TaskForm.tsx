import { useEffect, useMemo, useState } from 'react';
import { Controller, useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import Autocomplete, { createFilterOptions } from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import { useAppDispatch, useAppSelector } from '@/store';
import { fetchSkillsThunk } from '@/store/slices/skills/skillsSlice';
import { selectAllSkills, selectSkillsListMeta } from '@/store/slices/skills/selectors';
import {
    Button,
    Input,
    MapView,
    MAP_ZOOM_OVERVIEW,
    Select,
    geoPointFromLatLng,
    resolveMapViewportCenter,
    coerceApiDateTimeToIso,
    fromDateAndTimeInputs,
    toDateInput,
    toTimeInputRoundedToStep,
    type MapMarker,
} from '@/components/ui';
import { asIsoDateTime, asSkillId, LATITUDE_MAX, LATITUDE_MIN, LONGITUDE_MAX, LONGITUDE_MIN, type EventId, type EventResponseDto, type SkillResponseDto, type TaskCreateRequest, type TaskResponseDto, type TaskUpdateRequest, } from '@/types';

const filterSkills = createFilterOptions<SkillResponseDto>({
    matchFrom: 'any',
    stringify: (option) => `${option.name} ${option.category ?? ''}`.trim(),
});
const TITLE_MAX_LENGTH = 200;
const MAX_TASK_DURATION_MS = 8 * 60 * 60 * 1000;
const TIME_STEP_MINUTES = 30;
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
function scheduleDefaultsFromTask(task: TaskResponseDto | undefined): {
    startDate: string;
    startTime: string;
    endDate: string;
    endTime: string;
} {
    if (!task) {
        return { startDate: '', startTime: '', endDate: '', endTime: '' };
    }
    const startIso = coerceApiDateTimeToIso(task.startTime as unknown) ?? String(task.startTime);
    const endIso = coerceApiDateTimeToIso(task.endTime as unknown) ?? String(task.endTime);
    return {
        startDate: toDateInput(startIso),
        startTime: toTimeInputRoundedToStep(startIso, TIME_STEP_MINUTES),
        endDate: toDateInput(endIso),
        endTime: toTimeInputRoundedToStep(endIso, TIME_STEP_MINUTES),
    };
}
function locationDefaultsFromTask(task: TaskResponseDto | undefined): {
    latitude: string;
    longitude: string;
} {
    if (task != null &&
        typeof task.latitude === 'number' &&
        Number.isFinite(task.latitude) &&
        typeof task.longitude === 'number' &&
        Number.isFinite(task.longitude)) {
        return { latitude: String(task.latitude), longitude: String(task.longitude) };
    }
    return { latitude: '', longitude: '' };
}
const schema = z
    .object({
    title: z.string().trim().min(1, 'Заголовок обязателен').max(TITLE_MAX_LENGTH),
    startDate: z.string().min(1, 'Укажите дату начала'),
    startTime: z.string().min(1, 'Укажите время начала'),
    endDate: z.string().min(1, 'Укажите дату завершения'),
    endTime: z.string().min(1, 'Укажите время завершения'),
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
    requiredSkillIds: z.array(z.number().int().positive()).default([]),
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
    else if (endMs - startMs > MAX_TASK_DURATION_MS) {
        ctx.addIssue({
            code: z.ZodIssueCode.custom,
            path: ['endTime'],
            message: 'Задача не может длиться больше 8 часов',
        });
    }
    const hasLat = typeof data.latitude === 'number';
    const hasLng = typeof data.longitude === 'number';
    if (hasLat !== hasLng) {
        ctx.addIssue({
            code: z.ZodIssueCode.custom,
            path: ['latitude'],
            message: 'Координаты выбираются точкой на карте',
        });
    }
});
type TaskFormInput = z.input<typeof schema>;
type TaskFormOutput = z.output<typeof schema>;
export interface TaskFormProps {
    initial?: TaskResponseDto;
    eventId: EventId;
    eventForScheduleValidation?: Pick<
        EventResponseDto,
        'startDate' | 'endDate' | 'latitude' | 'longitude' | 'title'
    >;
    submitting?: boolean;
    onSubmit: (payload: TaskCreateRequest | {
        id: TaskResponseDto['id'];
        body: TaskUpdateRequest;
    }) => void;
    onCancel?: () => void;
}
export const TaskForm = ({ initial, eventId, eventForScheduleValidation, submitting, onSubmit, onCancel, }: TaskFormProps) => {
    const isEdit = initial !== undefined;
    const dispatch = useAppDispatch();
    const skills = useAppSelector(selectAllSkills);
    const skillsList = useAppSelector(selectSkillsListMeta);
    const [mapViewResetKey, setMapViewResetKey] = useState(0);
    const timeOptions = useMemo(() => buildTimeOptions(), []);
    const scheduleDefaults = useMemo(() => scheduleDefaultsFromTask(initial), [initial]);
    const locationDefaults = useMemo(() => locationDefaultsFromTask(initial), [initial]);
    const skillDefaults = useMemo(
        () => (initial?.requiredSkillIds ?? []).map((id) => Number(id)),
        [initial?.requiredSkillIds],
    );
    const { register, handleSubmit, reset, setValue, setError, trigger, control, formState: { errors }, } = useForm<TaskFormInput, unknown, TaskFormOutput>({
        resolver: zodResolver(schema),
        defaultValues: {
            title: initial?.title ?? '',
            ...scheduleDefaults,
            ...locationDefaults,
            requiredSkillIds: skillDefaults,
        },
    });
    const watchedLatitude = useWatch({ control, name: 'latitude' });
    const watchedLongitude = useWatch({ control, name: 'longitude' });
    const selectedSkillIds = useWatch({ control, name: 'requiredSkillIds', defaultValue: skillDefaults });
    const selectedSkills = useMemo(() => {
        const ids = new Set((selectedSkillIds ?? []).map((id) => Number(id)));
        return skills.filter((s) => ids.has(Number(s.id)));
    }, [skills, selectedSkillIds]);
    const mapMarkers = useMemo<MapMarker[]>(() => {
        const markers: MapMarker[] = [];
        const eventLat = eventForScheduleValidation?.latitude;
        const eventLng = eventForScheduleValidation?.longitude;
        if (
            eventLat !== undefined &&
            eventLng !== undefined &&
            Number.isFinite(eventLat) &&
            Number.isFinite(eventLng)
        ) {
            markers.push({
                id: 'event-ref',
                lat: eventLat,
                lng: eventLng,
                kind: 'event',
                label: eventForScheduleValidation?.title ?? 'Мероприятие',
                emphasis: 'default',
            });
        }
        const lat = Number(watchedLatitude);
        const lng = Number(watchedLongitude);
        if (Number.isFinite(lat) && Number.isFinite(lng)) {
            markers.push({
                id: 'task-pos',
                lat,
                lng,
                kind: 'task',
                label: 'Задача',
                emphasis: 'primary',
            });
        }
        return markers;
    }, [
        watchedLatitude,
        watchedLongitude,
        eventForScheduleValidation?.latitude,
        eventForScheduleValidation?.longitude,
        eventForScheduleValidation?.title,
    ]);
    const mapViewportCenter = useMemo(
        () =>
            resolveMapViewportCenter(
                geoPointFromLatLng(initial?.latitude, initial?.longitude),
                geoPointFromLatLng(eventForScheduleValidation?.latitude, eventForScheduleValidation?.longitude),
            ),
        [
            initial?.latitude,
            initial?.longitude,
            eventForScheduleValidation?.latitude,
            eventForScheduleValidation?.longitude,
        ],
    );

    const clearMapLocation = () => {
        setValue('latitude', '', { shouldDirty: true });
        setValue('longitude', '', { shouldDirty: true });
        setMapViewResetKey((key) => key + 1);
        void trigger(['latitude', 'longitude']);
    };
    useEffect(() => {
        if (isEdit) {
            void dispatch(fetchSkillsThunk({ page: 1, size: 500 }));
        }
    }, [dispatch, isEdit]);

    useEffect(() => {
        const sd = scheduleDefaultsFromTask(initial);
        const loc = locationDefaultsFromTask(initial);
        reset({
            title: initial?.title ?? '',
            ...sd,
            ...loc,
            requiredSkillIds: (initial?.requiredSkillIds ?? []).map((id) => Number(id)),
        });
    }, [initial, reset]);
    const submit = handleSubmit((data) => {
        const startIsoRaw = fromDateAndTimeInputs(data.startDate, data.startTime);
        const endIsoRaw = fromDateAndTimeInputs(data.endDate, data.endTime);
        const startMs = new Date(startIsoRaw).getTime();
        const endMs = new Date(endIsoRaw).getTime();
        if (eventForScheduleValidation) {
            const evStart = new Date(eventForScheduleValidation.startDate).getTime();
            const evEnd = new Date(eventForScheduleValidation.endDate).getTime();
            if (startMs < evStart || endMs > evEnd) {
                setError('startDate', {
                    message: 'Время задачи должно быть в пределах дат мероприятия',
                });
                return;
            }
        }
        const startIso = asIsoDateTime(startIsoRaw);
        const endIso = asIsoDateTime(endIsoRaw);
        if (isEdit) {
            const update: TaskUpdateRequest = {
                newTitle: data.title,
                newStartTime: startIso,
                newEndTime: endIso,
            };
            const hadLocation =
                typeof initial.latitude === 'number' && typeof initial.longitude === 'number';
            if (data.latitude === undefined && data.longitude === undefined) {
                if (hadLocation) {
                    update.clearLocation = true;
                }
            } else if (data.latitude !== undefined && data.longitude !== undefined) {
                update.latitude = data.latitude;
                update.longitude = data.longitude;
            }
            update.requiredSkillIds = (data.requiredSkillIds ?? []).map((id) => asSkillId(Number(id)));
            onSubmit({ id: initial.id, body: update });
            return;
        }
        const create: TaskCreateRequest = {
            eventId,
            title: data.title,
            startTime: startIso,
            endTime: endIso,
            ...(data.latitude !== undefined ? { latitude: data.latitude } : {}),
            ...(data.longitude !== undefined ? { longitude: data.longitude } : {}),
        };
        onSubmit(create);
    });
    return (<form className="flex flex-col gap-4" onSubmit={submit} noValidate>
      
      <input type="hidden" {...register('latitude')}/>
      <input type="hidden" {...register('longitude')}/>

      <Input label="Название" error={errors.title?.message} {...register('title')}/>

      <div className="rounded-lg border border-secondary/50 bg-surface-muted p-4">
        <div className="grid gap-4 md:grid-cols-2 md:items-start">
          <div className="grid min-w-0 gap-2">
            <Typography variant="subtitle2">Начало</Typography>
            <div className="grid min-w-0 gap-3 sm:grid-cols-2">
              <Input type="date" aria-label="Начало — дата" error={errors.startDate?.message} {...register('startDate')}/>
              <Controller name="startTime" control={control} render={({ field }) => (<Select aria-label="Начало — время" options={timeOptions} placeholder="Время" error={errors.startTime?.message} name={field.name} value={field.value} onChange={field.onChange} onBlur={field.onBlur} ref={field.ref}/>)}/>
            </div>
          </div>
          <div className="grid min-w-0 gap-2">
            <Typography variant="subtitle2">Завершение</Typography>
            <div className="grid min-w-0 gap-3 sm:grid-cols-2">
              <Input type="date" aria-label="Завершение — дата" error={errors.endDate?.message} {...register('endDate')}/>
              <Controller name="endTime" control={control} render={({ field }) => (<Select aria-label="Завершение — время" options={timeOptions} placeholder="Время" error={errors.endTime?.message} name={field.name} value={field.value} onChange={field.onChange} onBlur={field.onBlur} ref={field.ref}/>)}/>
            </div>
          </div>
        </div>
      </div>

      {isEdit ? (
        <div className="rounded-lg border border-secondary/50 bg-surface-muted px-3 py-3">
          <Typography variant="subtitle2" className="mb-2">
            Требуемые навыки
          </Typography>
          <Autocomplete
            multiple
            disableCloseOnSelect
            disablePortal
            size="small"
            options={skills}
            loading={skillsList.status === 'pending'}
            disabled={skillsList.status === 'pending' && skills.length === 0}
            value={selectedSkills}
            onChange={(_, newValue) => {
              setValue(
                'requiredSkillIds',
                newValue.map((s) => Number(s.id)),
                { shouldDirty: true },
              );
            }}
            filterOptions={filterSkills}
            getOptionLabel={(option) => {
              const cat = option.category?.trim();
              return cat ? `${option.name} (${cat})` : option.name;
            }}
            isOptionEqualToValue={(a, b) => a.id === b.id}
            slotProps={{
              popper: { placement: 'bottom-start' },
              listbox: { sx: { maxHeight: 220 } },
            }}
            renderInput={(params) => (
              <TextField
                {...params}
                placeholder="Поиск навыка…"
                helperText={
                  skillsList.status === 'pending' && skills.length === 0
                    ? 'Загрузка списка…'
                    : 'Для подбора исполнителей'
                }
              />
            )}
          />
        </div>
      ) : null}


      <div className="rounded-lg border border-secondary/50 bg-surface-muted p-4">
        <div className="mb-2 flex items-center justify-between gap-3">
          <div>
            <Typography variant="subtitle2">Локация</Typography>
            <Typography variant="caption" color="text.secondary">
              Нажмите на карту, чтобы выбрать точку.
            </Typography>
            {errors.latitude?.message ? (<Typography variant="caption" color="error" component="p" sx={{ mt: 0.5 }}>
                {errors.latitude.message}
              </Typography>) : null}
          </div>
          {mapMarkers.length > 0 ? (
            <Button type="button" size="sm" variant="ghost" onClick={clearMapLocation}>
              Очистить
            </Button>
          ) : null}
        </div>
        <MapView
            height="240px"
            center={mapViewportCenter}
            zoom={MAP_ZOOM_OVERVIEW}
            viewResetKey={mapViewResetKey}
            markers={mapMarkers}
            onMapClick={(point) => {
                setValue('latitude', String(point.latitude), { shouldDirty: true, shouldTouch: true });
                setValue('longitude', String(point.longitude), { shouldDirty: true, shouldTouch: true });
                void trigger(['latitude', 'longitude']);
            }}
        />
      </div>
      <div className="flex justify-end gap-2">
        {onCancel ? (<Button type="button" variant="ghost" onClick={onCancel}>
            Отмена
          </Button>) : null}
        <Button type="submit" loading={submitting}>
          {isEdit ? 'Сохранить' : 'Создать'}
        </Button>
      </div>
    </form>);
};
