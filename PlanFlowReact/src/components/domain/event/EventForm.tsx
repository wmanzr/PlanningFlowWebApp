import { useEffect, useMemo, useState } from 'react';
import { Controller, useForm, useWatch } from 'react-hook-form';
import Typography from '@mui/material/Typography';
import {
    Button,
    Input,
    MapView,
    MAP_ZOOM_OVERVIEW,
    Select,
    Textarea,
    geoPointFromLatLng,
    resolveMapViewportCenter,
    fromDateAndTimeInputs,
    roundTimeToStep,
    addMinutes,
    coerceApiDateTimeToIso,
    toDateInput,
    toNaiveLocalIsoFromTimestamp,
    toTimeInputRoundedToStep,
    type MapMarker,
} from '@/components/ui';
import { asIsoDateTime, LATITUDE_MAX, LATITUDE_MIN, LONGITUDE_MAX, LONGITUDE_MIN, type EventCreateRequest, type EventResponseDto, type EventUpdateRequest, } from '@/types';
const TITLE_MAX_LENGTH = 200;
const DESCRIPTION_MAX_LENGTH = 2000;
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
interface EventFormValues {
    title: string;
    description: string;
    startDate: string;
    startTime: string;
    endDate: string;
    endTime: string;
    latitude: string;
    longitude: string;
}
export interface EventFormProps {
    initial?: EventResponseDto;
    onSubmit: (payload: EventCreateRequest | EventUpdateRequest) => void;
    onCancel?: () => void;
    submitting?: boolean;
}
export const EventForm = ({ initial, onSubmit, onCancel, submitting }: EventFormProps) => {
    const isEdit = initial !== undefined;
    const [mapViewResetKey, setMapViewResetKey] = useState(0);
    const timeOptions = useMemo(() => buildTimeOptions(), []);
    const defaults = useMemo(() => {
        if (initial) {
            const startIso = coerceApiDateTimeToIso(initial.startDate as unknown) ?? (initial.startDate as string);
            const endIso = coerceApiDateTimeToIso(initial.endDate as unknown) ?? (initial.endDate as string);
            return {
                startDate: toDateInput(startIso),
                startTime: toTimeInputRoundedToStep(startIso, TIME_STEP_MINUTES),
                endDate: toDateInput(endIso),
                endTime: toTimeInputRoundedToStep(endIso, TIME_STEP_MINUTES),
            };
        }
        const now = roundTimeToStep(new Date(), TIME_STEP_MINUTES);
        const end = addMinutes(now, 24 * 60);
        const nowNaive = toNaiveLocalIsoFromTimestamp(now.getTime());
        const endNaive = toNaiveLocalIsoFromTimestamp(end.getTime());
        return {
            startDate: toDateInput(nowNaive),
            startTime: toTimeInputRoundedToStep(nowNaive, TIME_STEP_MINUTES),
            endDate: toDateInput(endNaive),
            endTime: toTimeInputRoundedToStep(endNaive, TIME_STEP_MINUTES),
        };
    }, [initial]);
    const { register, handleSubmit, reset, setValue, trigger, control } = useForm<EventFormValues>({
        mode: 'onSubmit',
        defaultValues: {
            title: initial?.title ?? '',
            description: initial?.description ?? '',
            startDate: defaults.startDate,
            startTime: defaults.startTime,
            endDate: defaults.endDate,
            endTime: defaults.endTime,
            latitude: initial?.latitude !== undefined ? String(initial.latitude) : '',
            longitude: initial?.longitude !== undefined ? String(initial.longitude) : '',
        },
    });
    const watchedLatitude = useWatch({ control, name: 'latitude' });
    const watchedLongitude = useWatch({ control, name: 'longitude' });
    const mapMarkers = useMemo<MapMarker[]>(() => {
        const lat = Number(watchedLatitude);
        const lng = Number(watchedLongitude);
        if (!Number.isFinite(lat) || !Number.isFinite(lng))
            return [];
        return [
            {
                id: 'event-pos',
                lat,
                lng,
                kind: 'event',
                label: 'Мероприятие',
                emphasis: 'primary',
            },
        ];
    }, [watchedLatitude, watchedLongitude]);
    const mapViewportCenter = useMemo(
        () => resolveMapViewportCenter(geoPointFromLatLng(initial?.latitude, initial?.longitude)),
        [initial?.latitude, initial?.longitude],
    );

    const clearMapLocation = () => {
        setValue('latitude', '', { shouldValidate: true });
        setValue('longitude', '', { shouldValidate: true });
        setMapViewResetKey((key) => key + 1);
    };
    useEffect(() => {
        reset({
            title: initial?.title ?? '',
            description: initial?.description ?? '',
            startDate: defaults.startDate,
            startTime: defaults.startTime,
            endDate: defaults.endDate,
            endTime: defaults.endTime,
            latitude: initial?.latitude !== undefined ? String(initial.latitude) : '',
            longitude: initial?.longitude !== undefined ? String(initial.longitude) : '',
        });
    }, [defaults.endDate, defaults.endTime, defaults.startDate, defaults.startTime, initial, reset]);
    const submit = handleSubmit((data) => {
        const startIso = fromDateAndTimeInputs(data.startDate, data.startTime);
        const endIso = fromDateAndTimeInputs(data.endDate, data.endTime);
        const latStr = data.latitude?.toString().trim() ?? '';
        const lngStr = data.longitude?.toString().trim() ?? '';
        const lat = latStr === '' ? undefined : Number(latStr);
        const lng = lngStr === '' ? undefined : Number(lngStr);
        if (isEdit) {
            const update: EventUpdateRequest = {
                eventId: initial.id,
                title: data.title.trim().slice(0, TITLE_MAX_LENGTH),
                description: (data.description ?? '').slice(0, DESCRIPTION_MAX_LENGTH),
                startDate: asIsoDateTime(startIso),
                endDate: asIsoDateTime(endIso),
            };
            const hadLocation = initial.latitude !== undefined || initial.longitude !== undefined;
            if (latStr === '' && lngStr === '') {
                if (hadLocation) {
                    update.clearLocation = true;
                }
            } else if (lat !== undefined && Number.isFinite(lat) && lng !== undefined && Number.isFinite(lng)) {
                if (lat >= LATITUDE_MIN && lat <= LATITUDE_MAX && lng >= LONGITUDE_MIN && lng <= LONGITUDE_MAX) {
                    update.latitude = lat;
                    update.longitude = lng;
                }
            }
            onSubmit(update);
            return;
        }
        const create: EventCreateRequest = {
            title: data.title.trim().slice(0, TITLE_MAX_LENGTH),
            description: (data.description ?? '').slice(0, DESCRIPTION_MAX_LENGTH),
            startDate: asIsoDateTime(startIso),
            endDate: asIsoDateTime(endIso),
        };
        if (lat !== undefined && Number.isFinite(lat) && lng !== undefined && Number.isFinite(lng)) {
            if (lat >= LATITUDE_MIN && lat <= LATITUDE_MAX && lng >= LONGITUDE_MIN && lng <= LONGITUDE_MAX) {
                create.latitude = lat;
                create.longitude = lng;
            }
        }
        onSubmit(create);
    });
    return (<form className="flex min-h-0 flex-col gap-4" onSubmit={submit} noValidate>
      <Input label="Название" {...register('title')}/>
      <Textarea label="Описание" rows={5} {...register('description')}/>
      <div className="rounded-lg border border-secondary/50 bg-surface-muted p-4">
        <div className="grid gap-4 md:grid-cols-2">
          <div className="grid gap-2">
            <Typography variant="subtitle2">Начало</Typography>
            <div className="grid gap-3 sm:grid-cols-2">
              <Input type="date" aria-label="Начало — дата" {...register('startDate')}/>
              <Controller name="startTime" control={control} render={({ field }) => (<Select aria-label="Начало — время" options={timeOptions} placeholder="Время" name={field.name} value={field.value} onChange={field.onChange} onBlur={field.onBlur} ref={field.ref}/>)}/>
            </div>
          </div>
          <div className="grid gap-2">
            <Typography variant="subtitle2">Завершение</Typography>
            <div className="grid gap-3 sm:grid-cols-2">
              <Input type="date" aria-label="Завершение — дата" {...register('endDate')}/>
              <Controller name="endTime" control={control} render={({ field }) => (<Select aria-label="Завершение — время" options={timeOptions} placeholder="Время" name={field.name} value={field.value} onChange={field.onChange} onBlur={field.onBlur} ref={field.ref}/>)}/>
            </div>
          </div>
        </div>
      </div>

      <div className="rounded-lg border border-secondary/50 bg-surface-muted p-4">
        <div className="mb-2 flex items-center justify-between gap-3">
          <div>
            <Typography variant="subtitle2">Локация</Typography>
            <Typography variant="caption" color="text.secondary">
              Нажмите на карту, чтобы выбрать точку.
            </Typography>
          </div>
          {mapMarkers.length > 0 ? (
            <Button type="button" size="sm" variant="ghost" onClick={clearMapLocation}>
              Очистить
            </Button>
          ) : null}
        </div>
        <MapView
            height={isEdit ? '200px' : '240px'}
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
