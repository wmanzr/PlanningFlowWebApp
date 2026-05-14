import { useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '@/store';
import { selectToasts } from '@/store/slices/toasts/selectors';
import { toastsActions } from '@/store/slices/toasts/toastsSlice';
import { ToastCenter } from '@/components/ui';
export const AppToastsBridge = () => {
    const dispatch = useAppDispatch();
    const toasts = useAppSelector(selectToasts);
    const handleBeginDismiss = useCallback((id: string) => dispatch(toastsActions.beginDismiss(id)), [dispatch]);
    const handleDismiss = useCallback((id: string) => dispatch(toastsActions.dismiss(id)), [dispatch]);
    return (<ToastCenter toasts={toasts} onBeginDismiss={handleBeginDismiss} onDismiss={handleDismiss}/>);
};
