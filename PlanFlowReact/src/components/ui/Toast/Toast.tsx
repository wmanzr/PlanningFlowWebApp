import { useEffect, type ReactElement } from 'react';
import type { SvgIconProps } from '@mui/material/SvgIcon';
import Box from '@mui/material/Box';
import Fade from '@mui/material/Fade';
import IconButton from '@mui/material/IconButton';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import type { Theme } from '@mui/material/styles';
import { useTheme } from '@mui/material/styles';
import CloseIcon from '@mui/icons-material/Close';
import DoneAllRounded from '@mui/icons-material/DoneAllRounded';
import ErrorRounded from '@mui/icons-material/ErrorRounded';
import InfoRounded from '@mui/icons-material/InfoRounded';
import WarningAmberRounded from '@mui/icons-material/WarningAmberRounded';
import { toastDisplayMessage } from './toastMessage';
export type ToastLevel = 'info' | 'success' | 'warning' | 'error';
export interface ToastViewModel {
    id: string;
    level: ToastLevel;
    message: string;
    href?: string | null;
    ttlMs: number;
    closing?: boolean;
}
export interface ToastProps {
    toast: ToastViewModel;
    onBeginClose: (id: string) => void;
    onClose: (id: string) => void;
}
const TOAST_WIDTH_PX = 360;
const TOAST_ICON_BY_LEVEL: Record<ToastLevel, (props: SvgIconProps) => ReactElement> = {
    success: (p) => <DoneAllRounded {...p}/>,
    error: (p) => <ErrorRounded {...p}/>,
    warning: (p) => <WarningAmberRounded {...p}/>,
    info: (p) => <InfoRounded {...p}/>,
};
function toastColors(theme: Theme, level: ToastLevel): {
    bg: string;
    fg: string;
    border: string;
} {
    const bg = level === 'success'
        ? theme.palette.success.main
        : level === 'error'
            ? theme.palette.error.main
            : level === 'warning'
                ? theme.palette.warning.main
                : theme.palette.info.main;
    const fg = theme.palette.getContrastText(bg);
    const border = theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.12)' : 'rgba(0,0,0,0.08)';
    return { bg, fg, border };
}
export const Toast = ({ toast, onBeginClose, onClose }: ToastProps) => {
    const theme = useTheme();
    const { bg, fg, border } = toastColors(theme, toast.level);
    const Icon = TOAST_ICON_BY_LEVEL[toast.level];
    const body = toastDisplayMessage(toast.level, toast.message);
    const ariaRole = toast.level === 'error' ? 'alert' : 'status';
    useEffect(() => {
        if (toast.ttlMs <= 0)
            return undefined;
        const timeout = window.setTimeout(() => onBeginClose(toast.id), toast.ttlMs);
        return () => window.clearTimeout(timeout);
    }, [toast.id, toast.ttlMs, onBeginClose]);
    useEffect(() => {
        if (!toast.closing)
            return undefined;
        const timeout = window.setTimeout(() => onClose(toast.id), 220);
        return () => window.clearTimeout(timeout);
    }, [toast.closing, toast.id, onClose]);
    return (<Fade in={!toast.closing} timeout={200}>
      <Paper elevation={8} role={ariaRole} aria-live={toast.level === 'error' ? 'assertive' : 'polite'} onClick={toast.href
            ? () => {
                window.location.assign(toast.href as string);
            }
            : undefined} sx={{
            width: '100%',
            maxWidth: TOAST_WIDTH_PX,
            minHeight: 52,
            px: 2,
            py: 1.25,
            display: 'flex',
            alignItems: 'flex-start',
            gap: 1.25,
            cursor: toast.href ? 'pointer' : 'default',
            bgcolor: bg,
            color: fg,
            opacity: 1,
            backgroundImage: 'none',
            border: `1px solid ${border}`,
            boxShadow: theme.shadows[10],
            pointerEvents: 'auto',
        }}>
        <Box sx={{
            mt: 0.125,
            display: 'flex',
            flexShrink: 0,
            color: fg,
            alignItems: 'center',
            justifyContent: 'center',
        }} aria-hidden>
          <Icon sx={{ fontSize: 22 }}/>
        </Box>
        <Typography component="div" variant="body2" sx={{
            flex: 1,
            minWidth: 0,
            pt: 0.125,
            fontWeight: 500,
            fontSize: '0.875rem',
            lineHeight: 1.4,
            color: fg,
        }}>
          {body}
        </Typography>
        <IconButton size="small" onClick={() => onBeginClose(toast.id)} aria-label="Закрыть" sx={{
            flexShrink: 0,
            mt: -0.25,
            mr: -0.5,
            color: fg,
            opacity: 0.9,
            '&:hover': { opacity: 1, bgcolor: theme.palette.action.hover },
        }}>
          <CloseIcon fontSize="small"/>
        </IconButton>
      </Paper>
    </Fade>);
};
