import { forwardRef, useId, type InputHTMLAttributes, type ReactNode } from 'react';
import TextField from '@mui/material/TextField';
import InputAdornment from '@mui/material/InputAdornment';
import { cn } from '../cn';
export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
    label?: ReactNode;
    hint?: ReactNode;
    error?: ReactNode;
    leftSlot?: ReactNode;
    rightSlot?: ReactNode;
}
export const Input = forwardRef<HTMLInputElement, InputProps>(({ id, label, hint, error, leftSlot, rightSlot, className, type, value, onChange, name, placeholder, disabled, readOnly, autoFocus, min, max, step, ...rest }, ref) => {
    const reactId = useId();
    const inputId = id ?? reactId;
    const slots: Record<string, unknown> = {
        input: {
            readOnly,
            startAdornment: leftSlot ? (<InputAdornment position="start">{leftSlot}</InputAdornment>) : undefined,
            endAdornment: rightSlot ? (<InputAdornment position="end">{rightSlot}</InputAdornment>) : undefined,
        },
        htmlInput: { min, max, step, ...rest },
        inputLabel: {
            shrink: Boolean(label),
        },
    };
    if (error) {
        slots.formHelperText = { sx: { color: 'error.main' } };
    }
    return (<div className={cn('flex w-full min-w-0 max-w-full flex-col gap-1', className)}>
        <TextField inputRef={ref} id={inputId} name={name} label={label} type={type} value={value} onChange={onChange} placeholder={placeholder} disabled={disabled} autoFocus={autoFocus} error={!!error} helperText={error || hint || undefined} fullWidth className="min-w-0 max-w-full" slotProps={slots} sx={type === 'date' || type === 'datetime-local'
            ? {
                maxWidth: '100%',
                minWidth: 0,
                '& .MuiOutlinedInput-root': {
                    alignItems: 'center',
                    maxWidth: '100%',
                    minWidth: 0,
                },
                ...(type === 'datetime-local'
                    ? {
                        '& .MuiOutlinedInput-input': {
                            minWidth: 0,
                            width: '100%',
                            maxWidth: '100%',
                            boxSizing: 'border-box',
                        },
                    }
                    : {}),
            }
            : undefined}/>
      </div>);
});
Input.displayName = 'Input';
