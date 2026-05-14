import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { tasksApi, parseApiError } from '@/api';
import { type AppApiError, type AsyncStatus, type MatchTaskResponseDto, type TaskId, type TaskMatchRequest, } from '@/types';
interface MatchingState {
    status: AsyncStatus;
    error: AppApiError | null;
    data: MatchTaskResponseDto | null;
    taskId: TaskId | null;
}
const initialState: MatchingState = {
    status: 'idle',
    error: null,
    data: null,
    taskId: null,
};
export const runMatchingThunk = createAsyncThunk<MatchTaskResponseDto, {
    id: TaskId;
    body: TaskMatchRequest;
}, {
    rejectValue: AppApiError;
}>('matching/run', async ({ id, body }, { rejectWithValue }) => {
    try {
        return await tasksApi.match(id, body);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const matchingSlice = createSlice({
    name: 'matching',
    initialState,
    reducers: {
        clear(state) {
            state.data = null;
            state.error = null;
            state.status = 'idle';
            state.taskId = null;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(runMatchingThunk.pending, (state, action) => {
            state.status = 'pending';
            state.error = null;
            state.taskId = action.meta.arg.id;
        })
            .addCase(runMatchingThunk.fulfilled, (state, action) => {
            state.status = 'succeeded';
            state.data = action.payload;
        })
            .addCase(runMatchingThunk.rejected, (state, action) => {
            state.status = 'failed';
            state.error = action.payload ?? null;
        });
    },
});
export const matchingActions = matchingSlice.actions;
export const matchingReducer = matchingSlice.reducer;
