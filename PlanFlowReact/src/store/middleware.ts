import { createListenerMiddleware, type TypedStartListening } from '@reduxjs/toolkit';
import type { AppDispatch, RootState } from './types';
export const listenerMiddleware = createListenerMiddleware();
export type AppStartListening = TypedStartListening<RootState, AppDispatch>;
export const startAppListening = listenerMiddleware.startListening as AppStartListening;
