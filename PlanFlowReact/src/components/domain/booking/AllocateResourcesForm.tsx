import { useEffect, useMemo } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import Typography from '@mui/material/Typography';
import { Button, Input, Select, coerceApiDateTimeToIso, fromDateAndTimeInputs, toDateInput, toTimeInputRoundedToStep, } from '@/components/ui';
import { asIsoDateTime, ResourceType, type EventResponseDto, type IsoDateTime, type TaskAllocateResourcesRequest, } from '@/types';
const NAME_MAX_LENGTH = 200;
const REQUIRED_MIN = 1;
const REQUIRED_MAX = 1000;
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
function scheduleDefaultsFromIso(from?: IsoDateTime, to?: IsoDateTime) {
    const startIso = from
        ? (coerceApiDateTimeToIso(from as unknown) ?? String(from))
        : '';
    const endIso = to ? (coerceApiDateTimeToIso(to as unknown) ?? String(to)) : '';
    return {
        reservedStartDate: startIso ? toDateInput(startIso) : '',
        reservedStartTime: startIso ? toTimeInputRoundedToStep(startIso, TIME_STEP_MINUTES) : '',
        reservedEndDate: endIso ? toDateInput(endIso) : '',
        reservedEndTime: endIso ? toTimeInputRoundedToStep(endIso, TIME_STEP_MINUTES) : '',
    };
}
const RESOURCE_TYPE_VALUES = [
    ResourceType.EQUIPMENT,
    ResourceType.TRANSPORT,
    ResourceType.MATERIAL,
] as const;
const schema = z
    .object({
    resourceType: z.union([z.literal(''), z.enum(RESOURCE_TYPE_VALUES)]),
    resourceName: z.string().trim().min(1, 'Укажите наименование').max(NAME_MAX_LENGTH),
    requiredCount: z.coerce.number().int().min(REQUIRED_MIN).max(REQUIRED_MAX),
    reservedStartDate: z.string().min(1, 'Укажите дату начала'),
    reservedStartTime: z.string().min(1, 'Укажите время начала'),
    reservedEndDate: z.string().min(1, 'Укажите дату завершения'),
    reservedEndTime: z.string().min(1, 'Укажите время завершения'),
})
    .superRefine((data, ctx) => {
    if (data.resourceType === '') {
        ctx.addIssue({
            code: z.ZodIssueCode.custom,
            path: ['resourceType'],
            message: 'Выберите ресурс',
        });
    }
    const fromIso = fromDateAndTimeInputs(data.reservedStartDate, data.reservedStartTime);
    const toIso = fromDateAndTimeInputs(data.reservedEndDate, data.reservedEndTime);
    if (!fromIso || !toIso) {
        ctx.addIssue({
            code: z.ZodIssueCode.custom,
            path: ['reservedStartDate'],
            message: 'Некорректная дата или время',
        });
        return;
    }
    if (new Date(toIso).getTime() <= new Date(fromIso).getTime()) {
        ctx.addIssue({
            code: z.ZodIssueCode.custom,
            path: ['reservedEndTime'],
            message: 'Завершение должно быть позже начала',
        });
    }
});
type FormValues = z.input<typeof schema>;
type FormOutput = z.output<typeof schema>;
const TYPE_OPTIONS = [
    { value: ResourceType.EQUIPMENT, label: 'Оборудование' },
    { value: ResourceType.TRANSPORT, label: 'Транспорт' },
    { value: ResourceType.MATERIAL, label: 'Материал' },
];
export interface AllocateResourcesFormProps {
    defaultFrom?: IsoDateTime;
    defaultTo?: IsoDateTime;
    eventForBookingWindow?: Pick<EventResponseDto, 'startDate' | 'endDate'>;
    submitting?: boolean;
    onSubmit: (body: TaskAllocateResourcesRequest) => void;
    onCancel?: () => void;
}
export const AllocateResourcesForm = ({ defaultFrom, defaultTo, eventForBookingWindow, submitting, onSubmit, onCancel, }: AllocateResourcesFormProps) => {
    const timeOptions = useMemo(() => buildTimeOptions(), []);
    const scheduleDefaults = useMemo(() => scheduleDefaultsFromIso(defaultFrom, defaultTo), [defaultFrom, defaultTo]);
    const { register, handleSubmit, reset, setError, control, formState: { errors }, } = useForm<FormValues, unknown, FormOutput>({
        resolver: zodResolver(schema),
        defaultValues: {
            resourceType: '',
            resourceName: '',
            requiredCount: REQUIRED_MIN,
            ...scheduleDefaults,
        },
    });
    useEffect(() => {
        reset({
            resourceType: '',
            resourceName: '',
            requiredCount: REQUIRED_MIN,
            ...scheduleDefaults,
        });
    }, [reset, scheduleDefaults]);
    const submit = handleSubmit((data) => {
        const fromIsoRaw = fromDateAndTimeInputs(data.reservedStartDate, data.reservedStartTime);
        const toIsoRaw = fromDateAndTimeInputs(data.reservedEndDate, data.reservedEndTime);
        const startMs = new Date(fromIsoRaw).getTime();
        const endMs = new Date(toIsoRaw).getTime();
        if (eventForBookingWindow) {
            const evStart = new Date(eventForBookingWindow.startDate).getTime();
            const evEnd = new Date(eventForBookingWindow.endDate).getTime();
            if (startMs < evStart || endMs > evEnd) {
                setError('reservedStartDate', {
                    message: 'Окно брони должно быть в пределах дат мероприятия',
                });
                return;
            }
        }
        if (data.resourceType === '')
            return;
        onSubmit({
            resourceType: data.resourceType,
            resourceName: data.resourceName,
            requiredCount: data.requiredCount,
            reservedFrom: asIsoDateTime(fromIsoRaw),
            reservedTo: asIsoDateTime(toIsoRaw),
        });
    });
    return (<form className="flex flex-col gap-4" onSubmit={submit} noValidate>
      <div className="grid gap-4 md:grid-cols-2 md:items-start">
        <Controller name="resourceType" control={control} render={({ field, fieldState }) => (<Select<ResourceType | ''> label="Тип ресурса" options={TYPE_OPTIONS} placeholder="Выберите ресурс" name={field.name} value={field.value} onBlur={field.onBlur} error={fieldState.error?.message} onChange={(e) => {
                const v = e.target.value;
                field.onChange(v === '' ? '' : (v as ResourceType));
            }}/>)}/>
        <Input label="Наименование" error={errors.resourceName?.message} {...register('resourceName')}/>
      </div>
      <Input label="Количество" type="number" min={REQUIRED_MIN} error={errors.requiredCount?.message} {...register('requiredCount')}/>

      <div className="rounded-lg border border-secondary/50 bg-surface-muted p-4">
        <div className="grid gap-4 md:grid-cols-2 md:items-start">
          <div className="grid min-w-0 gap-2">
            <Typography variant="subtitle2">Начало брони</Typography>
            <div className="grid min-w-0 gap-3 sm:grid-cols-2">
              <Input type="date" aria-label="Начало брони — дата" error={errors.reservedStartDate?.message} {...register('reservedStartDate')}/>
              <Controller name="reservedStartTime" control={control} render={({ field }) => (<Select aria-label="Начало брони — время" options={timeOptions} placeholder="Время" error={errors.reservedStartTime?.message} name={field.name} value={field.value} onChange={field.onChange} onBlur={field.onBlur} ref={field.ref}/>)}/>
            </div>
          </div>
          <div className="grid min-w-0 gap-2">
            <Typography variant="subtitle2">Завершение брони</Typography>
            <div className="grid min-w-0 gap-3 sm:grid-cols-2">
              <Input type="date" aria-label="Завершение брони — дата" error={errors.reservedEndDate?.message} {...register('reservedEndDate')}/>
              <Controller name="reservedEndTime" control={control} render={({ field }) => (<Select aria-label="Завершение брони — время" options={timeOptions} placeholder="Время" error={errors.reservedEndTime?.message} name={field.name} value={field.value} onChange={field.onChange} onBlur={field.onBlur} ref={field.ref}/>)}/>
            </div>
          </div>
        </div>
      </div>

      <div className="flex justify-end gap-2">
        {onCancel ? (<Button type="button" variant="ghost" onClick={onCancel}>
            Отмена
          </Button>) : null}
        <Button type="submit" loading={submitting}>
          Зарезервировать
        </Button>
      </div>
    </form>);
};
