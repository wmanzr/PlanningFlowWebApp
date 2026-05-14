import { forwardRef, useId, type ReactNode, type TextareaHTMLAttributes } from 'react';
import TextField from '@mui/material/TextField';
export interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
    label?: ReactNode;
    hint?: ReactNode;
    error?: ReactNode;
}
export const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(({ id, label, hint, error, className, rows = 4, value, onChange, name, placeholder, disabled, readOnly, ...rest }, ref) => {
    const reactId = useId();
    const textareaId = id ?? reactId;
    const slots: Record<string, unknown> = {
        inputLabel: { shrink: Boolean(label) },
        input: {
            readOnly,
            sx: {
                '& textarea': { resize: 'none', overflowY: 'auto' },
            },
        },
        htmlInput: rest,
    };
    if (error) {
        slots.formHelperText = { sx: { color: 'error.main' } };
    }
    return (<TextField inputRef={ref} id={textareaId} name={name} label={label} multiline rows={rows} value={value} onChange={onChange as unknown as React.ChangeEventHandler<HTMLInputElement>} placeholder={placeholder} disabled={disabled} error={!!error} helperText={error || hint || undefined} fullWidth className={className} slotProps={slots}/>);
});
Textarea.displayName = 'Textarea';
