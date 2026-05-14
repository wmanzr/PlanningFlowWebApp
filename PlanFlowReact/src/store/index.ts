import { configureStore } from '@reduxjs/toolkit';
import { type TypedUseSelectorHook, useDispatch, useSelector } from 'react-redux';
import { rootReducer } from './rootReducer';
import { listenerMiddleware } from './middleware';
import { registerAppListeners } from './listeners';
import { setSessionExpiredHandler } from '@/api';
import { authActions } from './slices/auth/authSlice';
import type { AppDispatch, AppThunk, RootState } from './types';
export const store = configureStore({
    reducer: rootReducer,
    middleware: (getDefault) => getDefault({
        serializableCheck: {
            ignoredActions: [],
        },
    }).prepend(listenerMiddleware.middleware),
    devTools: import.meta.env.DEV,
});
registerAppListeners();
setSessionExpiredHandler(() => {
    store.dispatch(authActions.sessionExpired());
});
export const useAppDispatch: () => AppDispatch = useDispatch;
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;
export type { AppDispatch, AppThunk, RootState };
