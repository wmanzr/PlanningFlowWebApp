import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { Button, Input, fromIsoDateTimeInput, toIsoDateTimeInput, } from '@/components/ui';
import { asIsoDateTime, type ResourceBookingRescheduleRequest, type ResourceBookingResponseDto, } from '@/types';
const schema = z
    .object({
    reservedFrom: z.string().min(1, 'Укажите начало'),
    reservedTo: z.string().min(1, 'Укажите завершение'),
})
    .superRefine((data, ctx) => {
    if (new Date(data.reservedTo).getTime() <= new Date(data.reservedFrom).getTime()) {
        ctx.addIssue({
            code: z.ZodIssueCode.custom,
            path: ['reservedTo'],
            message: 'Завершение должно быть позже начала',
        });
    }
});
type Values = z.infer<typeof schema>;
export interface RescheduleFormProps {
    booking: ResourceBookingResponseDto;
    submitting?: boolean;
    onSubmit: (body: ResourceBookingRescheduleRequest) => void;
    onCancel?: () => void;
}
export const RescheduleForm = ({ booking, submitting, onSubmit, onCancel, }: RescheduleFormProps) => {
    const { register, handleSubmit, reset, formState: { errors }, } = useForm<Values>({
        defaultValues: {
            reservedFrom: toIsoDateTimeInput(booking.reservedFrom),
            reservedTo: toIsoDateTimeInput(booking.reservedTo),
        },
    });
    useEffect(() => {
        reset({
            reservedFrom: toIsoDateTimeInput(booking.reservedFrom),
            reservedTo: toIsoDateTimeInput(booking.reservedTo),
        });
    }, [booking.id, booking.reservedFrom, booking.reservedTo, reset]);
    const submit = handleSubmit((rawValues) => {
        const parsed = schema.safeParse(rawValues);
        if (!parsed.success)
            return;
        onSubmit({
            reservedFrom: asIsoDateTime(fromIsoDateTimeInput(parsed.data.reservedFrom)),
            reservedTo: asIsoDateTime(fromIsoDateTimeInput(parsed.data.reservedTo)),
        });
    });
    return (<form className="flex flex-col gap-4" onSubmit={submit} noValidate>
      <div className="grid gap-4 md:grid-cols-2">
        <Input label="Начало" type="datetime-local" error={errors.reservedFrom?.message} {...register('reservedFrom')}/>
        <Input label="Завершение" type="datetime-local" error={errors.reservedTo?.message} {...register('reservedTo')}/>
      </div>
      <div className="flex justify-end gap-2">
        {onCancel ? (<Button type="button" variant="ghost" onClick={onCancel}>
            Отмена
          </Button>) : null}
        <Button type="submit" loading={submitting}>
          Перенести
        </Button>
      </div>
    </form>);
};
