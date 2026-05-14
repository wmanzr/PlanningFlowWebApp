import { useEffect, type ReactNode } from 'react';
import { useAppDispatch } from '@/store';
import { restoreSessionThunk } from '@/store/slices/auth/authSlice';
import { uiActions } from '@/store/slices/ui/uiSlice';
export const AppBootstrap = ({ children }: {
    children: ReactNode;
}) => {
    const dispatch = useAppDispatch();
    useEffect(() => {
        void dispatch(restoreSessionThunk()).finally(() => {
            dispatch(uiActions.bootstrapped());
        });
    }, [dispatch]);
    return <>{children}</>;
};
