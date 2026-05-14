import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { Button, Input, Select } from '@/components/ui';
import { MatchingMode, type TaskMatchRequest } from '@/types';
const MIN_REQUIRED = 1;
const MAX_REQUIRED = 1000;
const MIN_RADIUS = 0;
const MIN_DAILY_LOAD = 0;
const MIN_GAP = 0;
const schema = z.object({
    requiredCount: z.coerce
        .number()
        .int()
        .min(MIN_REQUIRED, `Не меньше ${MIN_REQUIRED}`)
        .max(MAX_REQUIRED),
    matchingMode: z.enum([MatchingMode.STANDARD, MatchingMode.CRITICAL]),
    geoReferenceRadiusMeters: z.coerce
        .number()
        .min(MIN_RADIUS)
        .optional()
        .or(z.literal('').transform(() => undefined)),
    maxDailyLoadMinutes: z.coerce
        .number()
        .int()
        .min(MIN_DAILY_LOAD)
        .optional()
        .or(z.literal('').transform(() => undefined)),
    minTechnicalGapMinutes: z.coerce
        .number()
        .int()
        .min(MIN_GAP)
        .optional()
        .or(z.literal('').transform(() => undefined)),
});
type Values = z.input<typeof schema>;
const MODE_OPTIONS = [
    { value: MatchingMode.STANDARD, label: 'Стандартный' },
    { value: MatchingMode.CRITICAL, label: 'Критический' },
];
export interface TaskMatchFormProps {
    initialRequired?: number;
    submitting?: boolean;
    onSubmit: (body: TaskMatchRequest) => void;
}
const DEFAULT_REQUIRED = 1;
export const TaskMatchForm = ({ initialRequired = DEFAULT_REQUIRED, submitting, onSubmit, }: TaskMatchFormProps) => {
    const { register, handleSubmit, formState: { errors }, } = useForm<Values>({
        defaultValues: {
            requiredCount: initialRequired,
            matchingMode: MatchingMode.STANDARD,
            geoReferenceRadiusMeters: undefined,
            maxDailyLoadMinutes: undefined,
            minTechnicalGapMinutes: undefined,
        },
    });
    const submit = handleSubmit((rawValues) => {
        const parsed = schema.safeParse(rawValues);
        if (!parsed.success)
            return;
        const data = parsed.data;
        onSubmit({
            requiredCount: data.requiredCount,
            matchingMode: data.matchingMode,
            ...(data.geoReferenceRadiusMeters !== undefined
                ? { geoReferenceRadiusMeters: data.geoReferenceRadiusMeters }
                : {}),
            ...(data.maxDailyLoadMinutes !== undefined
                ? { maxDailyLoadMinutes: data.maxDailyLoadMinutes }
                : {}),
            ...(data.minTechnicalGapMinutes !== undefined
                ? { minTechnicalGapMinutes: data.minTechnicalGapMinutes }
                : {}),
        });
    });
    return (<form className="flex flex-col gap-4" onSubmit={submit} noValidate>
      <div className="grid gap-4 md:grid-cols-2">
        <Input label="Нужно сотрудников" type="number" min={MIN_REQUIRED} error={errors.requiredCount?.message} {...register('requiredCount')}/>
        <Select label="Режим подбора" options={MODE_OPTIONS} {...register('matchingMode')}/>
      </div>
      <div className="grid gap-4 md:grid-cols-3">
        <Input label="Радиус, м" type="number" min={MIN_RADIUS} placeholder="опционально" error={errors.geoReferenceRadiusMeters?.message} {...register('geoReferenceRadiusMeters')}/>
        <Input label="Дневная нагрузка, мин" type="number" min={MIN_DAILY_LOAD} placeholder="опционально" error={errors.maxDailyLoadMinutes?.message} {...register('maxDailyLoadMinutes')}/>
        <Input label="Тех. зазор, мин" type="number" min={MIN_GAP} placeholder="опционально" error={errors.minTechnicalGapMinutes?.message} {...register('minTechnicalGapMinutes')}/>
      </div>
      <div className="flex justify-end">
        <Button type="submit" loading={submitting}>
          Запустить подбор
        </Button>
      </div>
    </form>);
};
