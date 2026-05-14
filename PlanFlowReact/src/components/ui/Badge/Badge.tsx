import { type HTMLAttributes } from 'react';
import MuiChip from '@mui/material/Chip';
import { alpha, useTheme } from '@mui/material/styles';
type BadgeTone = 'neutral' | 'info' | 'accent' | 'warning' | 'danger' | 'success';
export type BadgeProps = HTMLAttributes<HTMLSpanElement> & {
    tone?: BadgeTone | null;
    outline?: boolean | null;
};
export const Badge = ({ className, tone = 'neutral', outline, children, ...rest }: BadgeProps) => {
    const theme = useTheme();
    const p = theme.app;
    const err = theme.palette.error.main;
    const errCt = theme.palette.error.contrastText;
    const suc = theme.palette.success.main;
    const sucCt = theme.palette.success.contrastText;
    const warn = theme.palette.warning.main;
    const warnCt = theme.palette.warning.contrastText;
    const toneColors: Record<BadgeTone, {
        bg: string;
        color: string;
        border: string;
    }> = {
        neutral: { bg: `${p.secondary}99`, color: p.headline, border: p.secondary },
        info: { bg: p.secondary, color: p.headline, border: p.secondary },
        accent: { bg: p.button, color: p.buttonText, border: p.button },
        warning: {
            bg: alpha(warn, theme.palette.mode === 'dark' ? 0.32 : 0.88),
            color: warnCt,
            border: warn,
        },
        danger: { bg: err, color: errCt, border: err },
        success: { bg: suc, color: sucCt, border: suc },
    };
    const t = toneColors[tone ?? 'neutral'];
    return (<MuiChip label={children} size="small" variant={outline ? 'outlined' : 'filled'} className={className} sx={{
            ...(outline
                ? { borderColor: t.border, color: t.border, backgroundColor: 'transparent' }
                : { backgroundColor: t.bg, color: t.color }),
            fontWeight: 500,
            fontSize: '0.75rem',
            height: 24,
        }} {...(rest as Record<string, unknown>)}/>);
};
