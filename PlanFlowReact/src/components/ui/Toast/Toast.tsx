import { useEffect } from 'react';
import MuiAlert from '@mui/material/Alert';
import IconButton from '@mui/material/IconButton';
import CloseIcon from '@mui/icons-material/Close';
import Fade from '@mui/material/Fade';
import { useTheme } from '@mui/material/styles';
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
export const Toast = ({ toast, onBeginClose, onClose }: ToastProps) => {
    const theme = useTheme();
    const isDark = theme.palette.mode === 'dark';
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
      <MuiAlert severity={toast.level} variant="filled" sx={{
            width: 320,
            maxWidth: '100%',
            pointerEvents: 'auto',
            boxShadow: 3,
            cursor: toast.href ? 'pointer' : 'default',
            opacity: 1,
            ...(toast.level === 'info'
                ? {
                    bgcolor: isDark ? theme.palette.secondary.dark : theme.palette.success.light,
                    color: theme.palette.getContrastText(isDark ? theme.palette.secondary.dark : theme.palette.success.light),
                    '& .MuiAlert-icon': {
                        color: theme.palette.getContrastText(isDark ? theme.palette.secondary.dark : theme.palette.success.light),
                        opacity: 1,
                    },
                }
                : {
                    '&.MuiAlert-filledSuccess': {
                        bgcolor: theme.palette.success.dark,
                        color: theme.palette.getContrastText(theme.palette.success.dark),
                    },
                    '&.MuiAlert-filledWarning': {
                        bgcolor: theme.palette.warning.dark,
                        color: theme.palette.getContrastText(theme.palette.warning.dark),
                    },
                    '&.MuiAlert-filledError': {
                        bgcolor: theme.palette.error.dark,
                        color: theme.palette.getContrastText(theme.palette.error.dark),
                    },
                }),
        }} onClick={toast.href
            ? () => {
                window.location.assign(toast.href as string);
            }
            : undefined} action={<IconButton size="small" color="inherit" onClick={() => onBeginClose(toast.id)} aria-label="Закрыть">
            <CloseIcon fontSize="small"/>
          </IconButton>}>
        {toast.message}
      </MuiAlert>
    </Fade>);
};
