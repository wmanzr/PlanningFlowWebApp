import { useEffect } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button, Input, Select } from '@/components/ui';
import { ResourceType, type InternalResourceCreateRequest, type InternalResourceResponseDto, type InternalResourceUpdateRequest, } from '@/types';
const NAME_MAX_LENGTH = 200;
const INV_NUM_MAX_LENGTH = 100;
type InternalResourceFormValues = {
    name: string;
    type: ResourceType | '';
    inventoryNumber: string;
};
const schema = z
    .object({
    name: z.string().trim().min(1, 'Название обязательно').max(NAME_MAX_LENGTH),
    type: z.union([
        z.literal(''),
        z.enum([ResourceType.EQUIPMENT, ResourceType.TRANSPORT, ResourceType.MATERIAL]),
    ]),
    inventoryNumber: z
        .string()
        .trim()
        .min(1, 'Инвентарный номер обязателен')
        .max(INV_NUM_MAX_LENGTH),
})
    .superRefine((data, ctx) => {
    if (data.type === '') {
        ctx.addIssue({
            code: z.ZodIssueCode.custom,
            message: 'Выберите тип ресурса',
            path: ['type'],
        });
    }
});
const TYPE_OPTIONS = [
    { value: ResourceType.EQUIPMENT, label: 'Оборудование' },
    { value: ResourceType.TRANSPORT, label: 'Транспорт' },
    { value: ResourceType.MATERIAL, label: 'Материал' },
];
export interface InternalResourceFormProps {
    initial?: InternalResourceResponseDto;
    submitting?: boolean;
    onSubmit: (body: InternalResourceCreateRequest | InternalResourceUpdateRequest) => void;
    onCancel?: () => void;
}
export const InternalResourceForm = ({ initial, submitting, onSubmit, onCancel, }: InternalResourceFormProps) => {
    const isEdit = initial !== undefined;
    const { register, control, handleSubmit, reset, formState: { errors }, } = useForm<InternalResourceFormValues>({
        resolver: zodResolver(schema),
        defaultValues: {
            name: initial?.name ?? '',
            type: initial?.type ?? '',
            inventoryNumber: initial?.inventoryNumber ?? '',
        },
    });
    useEffect(() => {
        reset({
            name: initial?.name ?? '',
            type: initial?.type ?? '',
            inventoryNumber: initial?.inventoryNumber ?? '',
        });
    }, [initial, reset]);
    const submit = handleSubmit((values) => {
        if (values.type === '')
            return;
        const body: InternalResourceCreateRequest | InternalResourceUpdateRequest = {
            name: values.name,
            type: values.type,
            inventoryNumber: values.inventoryNumber,
        };
        onSubmit(body);
    });
    return (<form className="flex flex-col gap-4" onSubmit={submit} noValidate>
      <Input label="Название" error={errors.name?.message} {...register('name')}/>
      <Controller name="type" control={control} render={({ field, fieldState }) => (<Select<ResourceType | ''> label="Тип" options={TYPE_OPTIONS} placeholder="Выберите тип" name={field.name} value={field.value} onBlur={field.onBlur} error={fieldState.error?.message} onChange={(e) => {
                const v = e.target.value;
                field.onChange(v === '' ? '' : (v as ResourceType));
            }}/>)}/>
      <Input label="Инвентарный номер" error={errors.inventoryNumber?.message} {...register('inventoryNumber')}/>
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
