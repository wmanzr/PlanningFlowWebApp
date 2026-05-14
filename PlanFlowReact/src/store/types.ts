import type { Action, ThunkAction } from '@reduxjs/toolkit';
import type { rootReducer } from './rootReducer';
export type RootState = ReturnType<typeof rootReducer>;
export type AppDispatch = AppDispatchType;
interface AppDispatchType {
    <ReturnType>(action: ThunkAction<ReturnType, RootState, unknown, Action>): ReturnType;
    <T extends Action>(action: T): T;
}
export type AppThunk<R = void> = ThunkAction<R, RootState, unknown, Action>;
