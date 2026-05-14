import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { Button, Modal, Textarea } from '@/components/ui';
import type { AssignmentRejectRequest } from '@/types';
const REASON_MIN_LENGTH = 5;
const REASON_MAX_LENGTH = 500;
const schema = z.object({
    reason: z
        .string()
        .trim()
        .min(REASON_MIN_LENGTH, `Минимум ${REASON_MIN_LENGTH} символов`)
        .max(REASON_MAX_LENGTH),
});
type Values = z.infer<typeof schema>;
export interface AssignmentRejectDialogProps {
    open: boolean;
    submitting?: boolean;
    onClose: () => void;
    onSubmit: (body: AssignmentRejectRequest) => void;
}
export const AssignmentRejectDialog = ({ open, submitting, onClose, onSubmit, }: AssignmentRejectDialogProps) => {
    const { register, handleSubmit, formState: { errors }, reset, } = useForm<Values>({ defaultValues: { reason: '' } });
    const submit = handleSubmit((values) => {
        const parsed = schema.safeParse(values);
        if (!parsed.success)
            return;
        onSubmit({ reason: parsed.data.reason });
        reset();
    });
    return (<Modal open={open} onClose={() => {
            reset();
            onClose();
        }} title="Отказ от назначения" description="Укажите причину отказа." footer={<>
          <Button variant="ghost" onClick={() => {
                reset();
                onClose();
            }}>
            Отмена
          </Button>
          <Button variant="danger" onClick={submit} loading={submitting}>
            Отказаться
          </Button>
        </>}>
      <Textarea label="Причина" rows={4} error={errors.reason?.message} {...register('reason')}/>
    </Modal>);
};
