import { createPortal } from 'react-dom';
import Stack from '@mui/material/Stack';
import { Toast, type ToastViewModel } from './Toast';
export interface ToastCenterProps {
    toasts: ToastViewModel[];
    onBeginDismiss: (id: string) => void;
    onDismiss: (id: string) => void;
}
export const ToastCenter = ({ toasts, onBeginDismiss, onDismiss }: ToastCenterProps) => {
    if (toasts.length === 0)
        return null;
    return createPortal(<Stack spacing={1} sx={{
            position: 'fixed',
            top: 16,
            right: 16,
            left: 'auto',
            transform: 'none',
            zIndex: 1600,
            pointerEvents: 'none',
            alignItems: 'flex-end',
            maxWidth: 'min(100vw - 32px, 360px)',
            px: 0,
        }}>
      {toasts.map((toast) => (<Toast key={toast.id} toast={toast} onBeginClose={onBeginDismiss} onClose={onDismiss}/>))}
    </Stack>, document.body);
};
