import { forwardRef, useId, type ReactNode, type SelectHTMLAttributes } from 'react';
import TextField from '@mui/material/TextField';
import MuiMenuItem from '@mui/material/MenuItem';
export interface SelectOption<T extends string | number> {
    value: T;
    label: string;
    disabled?: boolean;
}
export interface SelectProps<T extends string | number = string> extends Omit<SelectHTMLAttributes<HTMLSelectElement>, 'children'> {
    label?: ReactNode;
    hint?: ReactNode;
    error?: ReactNode;
    suppressErrorHelperText?: boolean;
    options: SelectOption<T>[];
    placeholder?: string;
}
const SelectInner = forwardRef<HTMLSelectElement, SelectProps>(({ id, label, hint, error, suppressErrorHelperText, options, placeholder, className, value, onChange, name, disabled, }, ref) => {
    const reactId = useId();
    const selectId = id ?? reactId;
    const selectedValue = value === undefined || value === null || value === ''
        ? ''
        : String(value);
    const selectedLabel = options.find((o) => String(o.value) === selectedValue)?.label ?? '';
    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (onChange) {
            const syntheticEvent = {
                ...e,
                target: { ...e.target, name: name ?? '', value: e.target.value },
            } as unknown as React.ChangeEvent<HTMLSelectElement>;
            onChange(syntheticEvent);
        }
    };
    const helperText = suppressErrorHelperText ? hint || undefined : error || hint || undefined;
    const slots: Record<string, unknown> = {
        inputLabel: { shrink: Boolean(label) },
        select: {
            displayEmpty: true,
            MenuProps: {
                PaperProps: { sx: { maxHeight: 360 } },
                anchorOrigin: { vertical: 'bottom', horizontal: 'left' },
                transformOrigin: { vertical: 'top', horizontal: 'left' },
            },
            renderValue: (v: unknown) => {
                const vv = v === undefined || v === null || v === '' ? '' : String(v);
                if (vv === '') {
                    return selectedLabel || placeholder || '';
                }
                return options.find((o) => String(o.value) === vv)?.label ?? vv;
            },
        },
    };
    if (error && !suppressErrorHelperText) {
        slots.formHelperText = { sx: { color: 'error.main' } };
    }
    return (<TextField inputRef={ref} id={selectId} select name={name} label={label} value={selectedValue} onChange={handleChange} disabled={disabled} error={!!error} helperText={helperText} fullWidth className={className} slotProps={slots}>
        {placeholder ? (<MuiMenuItem value="" disabled>
            {placeholder}
          </MuiMenuItem>) : null}
        {options.map((option) => (<MuiMenuItem key={String(option.value)} value={String(option.value)} disabled={option.disabled}>
            {option.label}
          </MuiMenuItem>))}
      </TextField>);
});
SelectInner.displayName = 'Select';
export const Select = SelectInner as <T extends string | number = string>(props: SelectProps<T> & {
    ref?: React.Ref<HTMLSelectElement>;
}) => ReturnType<typeof SelectInner>;
