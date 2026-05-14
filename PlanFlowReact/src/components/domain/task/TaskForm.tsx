import { useEffect, useMemo } from 'react';
import { Controller, useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import Typography from '@mui/material/Typography';
import { Button, Input, MapView, Select, coerceApiDateTimeToIso, fromDateAndTimeInputs, toDateInput, toTimeInputRoundedToStep, type MapMarker, } from '@/components/ui';
import { asIsoDateTime, LATITUDE_MAX, LATITUDE_MIN, LONGITUDE_MAX, LONGITUDE_MIN, type EventId, type EventResponseDto, type TaskCreateRequest, type TaskResponseDto, type TaskUpdateRequest, } from '@/types';
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
    eventForScheduleValidation?: Pick<EventResponseDto, 'startDate' | 'endDate'>;
    submitting?: boolean;
    onSubmit: (payload: TaskCreateRequest | {
        id: TaskResponseDto['id'];
        body: TaskUpdateRequest;
    }) => void;
    onCancel?: () => void;
}
export const TaskForm = ({ initial, eventId, eventForScheduleValidation, submitting, onSubmit, onCancel, }: TaskFormProps) => {
    const isEdit = initial !== undefined;
    const timeOptions = useMemo(() => buildTimeOptions(), []);
    const scheduleDefaults = useMemo(() => scheduleDefaultsFromTask(initial), [initial]);
    const locationDefaults = useMemo(() => locationDefaultsFromTask(initial), [initial]);
    const { register, handleSubmit, reset, setValue, setError, control, formState: { errors }, } = useForm<TaskFormInput, unknown, TaskFormOutput>({
        resolver: zodResolver(schema),
        defaultValues: {
            title: initial?.title ?? '',
            ...scheduleDefaults,
            ...locationDefaults,
        },
    });
    const watchedLatitude = useWatch({ control, name: 'latitude' });
    const watchedLongitude = useWatch({ control, name: 'longitude' });
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
        const sd = scheduleDefaultsFromTask(initial);
        const loc = locationDefaultsFromTask(initial);
        reset({
            title: initial?.title ?? '',
            ...sd,
            ...loc,
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
                ...(data.latitude !== undefined ? { latitude: data.latitude } : {}),
                ...(data.longitude !== undefined ? { longitude: data.longitude } : {}),
            };
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

      <div className="rounded-lg border border-secondary/50 bg-surface-muted p-4">
        <div className="mb-2 flex items-center justify-between gap-3">
          <div>
            <Typography variant="subtitle2">Локация</Typography>
            <Typography variant="caption" color="text.secondary">
              Нажмите на карту, чтобы выбрать точку. Поля координат скрыты.
            </Typography>
            {errors.latitude?.message ? (<Typography variant="caption" color="error" component="p" sx={{ mt: 0.5 }}>
                {errors.latitude.message}
              </Typography>) : null}
          </div>
          {mapMarkers.length > 0 ? (<Button type="button" size="sm" variant="ghost" onClick={() => {
                setValue('latitude', '', { shouldValidate: true });
                setValue('longitude', '', { shouldValidate: true });
            }}>
              Очистить
            </Button>) : null}
        </div>
        <MapView height="240px" {...(mapCenter ? { center: mapCenter } : {})} markers={mapMarkers} onMapClick={(point) => {
            setValue('latitude', String(point.latitude), { shouldValidate: true });
            setValue('longitude', String(point.longitude), { shouldValidate: true });
        }}/>
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
