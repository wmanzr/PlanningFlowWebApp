import CircularProgress from '@mui/material/CircularProgress';
import { useTheme } from '@mui/material/styles';
type SpinnerSize = 'sm' | 'md' | 'lg';
type SpinnerTone = 'primary' | 'muted';
export type SpinnerProps = {
    size?: SpinnerSize | null;
    tone?: SpinnerTone | null;
    className?: string;
    label?: string;
};
const SIZE_MAP: Record<SpinnerSize, number> = { sm: 16, md: 24, lg: 40 };
export const Spinner = ({ size = 'md', tone = 'primary', className, label }: SpinnerProps) => {
    const theme = useTheme();
    const pxSize = SIZE_MAP[size ?? 'md'];
    return (<span role="status" aria-label={label ?? 'Загрузка'} className={className}>
      <CircularProgress size={pxSize} thickness={size === 'lg' ? 3 : 4} sx={{
            color: tone === 'muted' ? theme.app.paragraph : theme.app.button,
        }}/>
      {label ? <span className="sr-only">{label}</span> : null}
    </span>);
};
