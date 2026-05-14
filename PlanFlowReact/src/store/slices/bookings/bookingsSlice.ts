import { createAsyncThunk, createEntityAdapter, createSlice, type PayloadAction, } from '@reduxjs/toolkit';
import { bookingsApi, parseApiError } from '@/api';
import { type AppApiError, type AsyncStatus, type BookingId, type PageQuery, type PageResult, type ResourceBookingRescheduleRequest, type ResourceBookingResponseDto, type TaskId, } from '@/types';
const bookingsAdapter = createEntityAdapter<ResourceBookingResponseDto>({
    sortComparer: (a, b) => a.reservedFrom.localeCompare(b.reservedFrom),
});
interface ListMeta {
    status: AsyncStatus;
    error: AppApiError | null;
    totalElements: number;
    totalPages: number;
}
interface ActionMeta {
    status: AsyncStatus;
    error: AppApiError | null;
}
const initialList: ListMeta = { status: 'idle', error: null, totalElements: 0, totalPages: 0 };
const initialAction: ActionMeta = { status: 'idle', error: null };
export const fetchBookingsForTaskThunk = createAsyncThunk<PageResult<ResourceBookingResponseDto>, {
    taskId: TaskId;
    query: PageQuery;
}, {
    rejectValue: AppApiError;
}>('bookings/fetchForTask', async ({ taskId, query }, { rejectWithValue }) => {
    try {
        return await bookingsApi.forTask(taskId, query);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const rescheduleBookingThunk = createAsyncThunk<ResourceBookingResponseDto, {
    id: BookingId;
    body: ResourceBookingRescheduleRequest;
}, {
    rejectValue: AppApiError;
}>('bookings/reschedule', async ({ id, body }, { rejectWithValue }) => {
    try {
        const updatedId = await bookingsApi.reschedule(id, body);
        return await bookingsApi.byId(updatedId as BookingId);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
const bookingStatusThunk = (name: string, fn: (id: BookingId) => Promise<number>) => createAsyncThunk<ResourceBookingResponseDto, BookingId, {
    rejectValue: AppApiError;
}>(`bookings/${name}`, async (id, { rejectWithValue }) => {
    try {
        const updatedId = await fn(id);
        return await bookingsApi.byId(updatedId as BookingId);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const confirmBookingThunk = bookingStatusThunk('confirm', bookingsApi.confirm);
export const failBookingThunk = bookingStatusThunk('fail', bookingsApi.fail);
export const cancelBookingThunk = bookingStatusThunk('cancel', bookingsApi.cancel);
export const bookingsSlice = createSlice({
    name: 'bookings',
    initialState: bookingsAdapter.getInitialState({
        list: initialList,
        action: initialAction,
    }),
    reducers: {
        clearActionError(state) {
            state.action.error = null;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchBookingsForTaskThunk.pending, (state) => {
            state.list.status = 'pending';
            state.list.error = null;
        })
            .addCase(fetchBookingsForTaskThunk.fulfilled, (state, action) => {
            bookingsAdapter.upsertMany(state, action.payload.items);
            state.list.status = 'succeeded';
            state.list.totalElements = action.payload.totalElements;
            state.list.totalPages = action.payload.totalPages;
        })
            .addCase(fetchBookingsForTaskThunk.rejected, (state, action) => {
            state.list.status = 'failed';
            state.list.error = action.payload ?? null;
        })
            .addMatcher((action) => [
            confirmBookingThunk.fulfilled.type,
            failBookingThunk.fulfilled.type,
            cancelBookingThunk.fulfilled.type,
            rescheduleBookingThunk.fulfilled.type,
        ].includes(action.type), (state, action: PayloadAction<ResourceBookingResponseDto>) => {
            bookingsAdapter.upsertOne(state, action.payload);
            state.action.status = 'succeeded';
            state.action.error = null;
        })
            .addMatcher((action) => [
            confirmBookingThunk.pending.type,
            failBookingThunk.pending.type,
            cancelBookingThunk.pending.type,
            rescheduleBookingThunk.pending.type,
        ].includes(action.type), (state) => {
            state.action.status = 'pending';
            state.action.error = null;
        })
            .addMatcher((action) => [
            confirmBookingThunk.rejected.type,
            failBookingThunk.rejected.type,
            cancelBookingThunk.rejected.type,
            rescheduleBookingThunk.rejected.type,
        ].includes(action.type), (state, action: PayloadAction<AppApiError | undefined>) => {
            state.action.status = 'failed';
            state.action.error = action.payload ?? null;
        });
    },
});
export const bookingsActions = bookingsSlice.actions;
export const bookingsReducer = bookingsSlice.reducer;
export const bookingsAdapterSelectors = bookingsAdapter.getSelectors();
