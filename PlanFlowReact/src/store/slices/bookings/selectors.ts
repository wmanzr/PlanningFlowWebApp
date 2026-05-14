import { createSelector } from '@reduxjs/toolkit';
import type { RootState } from '../../types';
import { bookingsAdapterSelectors } from './bookingsSlice';
import type { TaskId } from '@/types';
export const selectBookingsListMeta = (state: RootState) => state.bookings.list;
export const selectBookingsActionMeta = (state: RootState) => state.bookings.action;
export const selectAllBookings = (state: RootState) => bookingsAdapterSelectors.selectAll(state.bookings);
export const makeSelectBookingsForTask = (taskId: TaskId | undefined) => createSelector(selectAllBookings, (bookings) => taskId === undefined ? [] : bookings.filter((b) => b.taskId === taskId));
