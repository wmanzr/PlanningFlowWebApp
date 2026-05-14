import { createAsyncThunk, createEntityAdapter, createSlice, type PayloadAction, } from '@reduxjs/toolkit';
import { tasksApi, parseApiError, type ListTasksForUserQuery } from '@/api';
import { asTaskId, type AppApiError, type AsyncStatus, type EventId, type ListTasksForEventQuery, type PageResult, type ReserveResourcesResponseDto, type TaskAllocateResourcesRequest, type TaskCreateRequest, type TaskId, type TaskResponseDto, type TaskUpdateRequest, type UserId, } from '@/types';
import { fetchEventByIdThunk } from '../events/eventsSlice';
const tasksAdapter = createEntityAdapter<TaskResponseDto>({
    sortComparer: (a, b) => a.startTime.localeCompare(b.startTime),
});
interface ListMeta {
    status: AsyncStatus;
    error: AppApiError | null;
    totalElements: number;
    totalPages: number;
    page: number;
    size: number;
}
interface DetailMeta {
    status: AsyncStatus;
    error: AppApiError | null;
}
interface ActionMeta {
    status: AsyncStatus;
    error: AppApiError | null;
}
const initialList: ListMeta = {
    status: 'idle',
    error: null,
    totalElements: 0,
    totalPages: 0,
    page: 1,
    size: 50,
};
const initialDetail: DetailMeta = { status: 'idle', error: null };
const initialAction: ActionMeta = { status: 'idle', error: null };
export const fetchTasksForEventThunk = createAsyncThunk<PageResult<TaskResponseDto>, {
    eventId: EventId;
    query: ListTasksForEventQuery;
}, {
    rejectValue: AppApiError;
}>('tasks/fetchForEvent', async ({ eventId, query }, { rejectWithValue }) => {
    try {
        return await tasksApi.forEvent(eventId, query);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const fetchTasksForUserThunk = createAsyncThunk<PageResult<TaskResponseDto>, {
    userId: UserId;
    query: ListTasksForUserQuery;
}, {
    rejectValue: AppApiError;
}>('tasks/fetchForUser', async ({ userId, query }, { rejectWithValue }) => {
    try {
        return await tasksApi.forUser(userId, query);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const fetchTaskByIdThunk = createAsyncThunk<TaskResponseDto, TaskId, {
    rejectValue: AppApiError;
}>('tasks/fetchById', async (id, { rejectWithValue }) => {
    try {
        return await tasksApi.byId(id);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const createTaskThunk = createAsyncThunk<TaskId, TaskCreateRequest, {
    rejectValue: AppApiError;
}>('tasks/create', async (body, { rejectWithValue }) => {
    try {
        const id = await tasksApi.create(body);
        return asTaskId(id);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const updateTaskThunk = createAsyncThunk<TaskResponseDto, {
    id: TaskId;
    body: TaskUpdateRequest;
}, {
    rejectValue: AppApiError;
}>('tasks/update', async ({ id, body }, { rejectWithValue }) => {
    try {
        const updatedId = await tasksApi.update(id, body);
        return await tasksApi.byId(asTaskId(updatedId));
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
const buildStatusThunk = (name: string, fn: (id: TaskId) => Promise<number>) => createAsyncThunk<TaskResponseDto, TaskId, {
    rejectValue: AppApiError;
}>(`tasks/${name}`, async (id, { rejectWithValue }) => {
    try {
        const updatedId = await fn(id);
        return await tasksApi.byId(asTaskId(updatedId));
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const startTaskExecutionThunk = createAsyncThunk<TaskResponseDto, TaskId, {
    rejectValue: AppApiError;
}>('tasks/startExecution', async (id, { dispatch, rejectWithValue }) => {
    try {
        const updatedId = await tasksApi.startExecution(id);
        const task = await tasksApi.byId(asTaskId(updatedId));
        if (task.eventId != null) {
            await dispatch(fetchEventByIdThunk(task.eventId)).unwrap();
        }
        return task;
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const markTaskDoneThunk = buildStatusThunk('done', tasksApi.done);
export const cancelTaskThunk = buildStatusThunk('cancel', tasksApi.cancel);
export const assignTaskThunk = createAsyncThunk<number, {
    id: TaskId;
    userId: UserId;
}, {
    rejectValue: AppApiError;
}>('tasks/assign', async ({ id, userId }, { rejectWithValue }) => {
    try {
        return await tasksApi.assign(id, { userId });
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const unassignTaskThunk = createAsyncThunk<void, {
    id: TaskId;
    userId: UserId;
}, {
    rejectValue: AppApiError;
}>('tasks/unassign', async ({ id, userId }, { rejectWithValue }) => {
    try {
        await tasksApi.unassign(id, userId);
        return undefined;
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const allocateTaskResourcesThunk = createAsyncThunk<ReserveResourcesResponseDto, {
    id: TaskId;
    body: TaskAllocateResourcesRequest;
}, {
    rejectValue: AppApiError;
}>('tasks/allocateResources', async ({ id, body }, { rejectWithValue }) => {
    try {
        return await tasksApi.allocateResources(id, body);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const tasksSlice = createSlice({
    name: 'tasks',
    initialState: tasksAdapter.getInitialState({
        list: initialList,
        detail: initialDetail,
        action: initialAction,
    }),
    reducers: {
        clearActionError(state) {
            state.action.error = null;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchTasksForEventThunk.pending, (state) => {
            state.list.status = 'pending';
            state.list.error = null;
        })
            .addCase(fetchTasksForEventThunk.fulfilled, (state, action) => {
            tasksAdapter.upsertMany(state, action.payload.items);
            state.list.status = 'succeeded';
            const q = action.meta.arg.query;
            const size = q.size ?? 20;
            if (size <= 50) {
                state.list.totalElements = action.payload.totalElements;
                state.list.totalPages = action.payload.totalPages;
                if (q.page !== undefined) {
                    state.list.page = q.page;
                }
                if (q.size !== undefined) {
                    state.list.size = q.size;
                }
            }
        })
            .addCase(fetchTasksForEventThunk.rejected, (state, action) => {
            state.list.status = 'failed';
            state.list.error = action.payload ?? null;
        })
            .addCase(fetchTasksForUserThunk.pending, (state) => {
            state.list.status = 'pending';
            state.list.error = null;
        })
            .addCase(fetchTasksForUserThunk.fulfilled, (state, action) => {
            tasksAdapter.upsertMany(state, action.payload.items);
            state.list.status = 'succeeded';
            state.list.totalElements = action.payload.totalElements;
            state.list.totalPages = action.payload.totalPages;
            const q = action.meta.arg.query;
            if (q.page !== undefined) {
                state.list.page = q.page;
            }
            if (q.size !== undefined) {
                state.list.size = q.size;
            }
        })
            .addCase(fetchTasksForUserThunk.rejected, (state, action) => {
            state.list.status = 'failed';
            state.list.error = action.payload ?? null;
        })
            .addCase(fetchTaskByIdThunk.pending, (state) => {
            state.detail.status = 'pending';
            state.detail.error = null;
        })
            .addCase(fetchTaskByIdThunk.fulfilled, (state, action) => {
            tasksAdapter.upsertOne(state, action.payload);
            state.detail.status = 'succeeded';
        })
            .addCase(fetchTaskByIdThunk.rejected, (state, action) => {
            state.detail.status = 'failed';
            state.detail.error = action.payload ?? null;
        })
            .addCase(createTaskThunk.fulfilled, (state) => {
            state.action.status = 'idle';
            state.action.error = null;
        })
            .addCase(assignTaskThunk.fulfilled, (state) => {
            state.action.status = 'idle';
            state.action.error = null;
        })
            .addCase(unassignTaskThunk.fulfilled, (state) => {
            state.action.status = 'idle';
            state.action.error = null;
        })
            .addCase(allocateTaskResourcesThunk.fulfilled, (state) => {
            state.action.status = 'idle';
            state.action.error = null;
        })
            .addMatcher((action) => [
            startTaskExecutionThunk.fulfilled.type,
            markTaskDoneThunk.fulfilled.type,
            cancelTaskThunk.fulfilled.type,
            updateTaskThunk.fulfilled.type,
        ].includes(action.type), (state, action: PayloadAction<TaskResponseDto>) => {
            tasksAdapter.upsertOne(state, action.payload);
            state.action.status = 'succeeded';
            state.action.error = null;
        })
            .addMatcher((action) => [
            startTaskExecutionThunk.pending.type,
            markTaskDoneThunk.pending.type,
            cancelTaskThunk.pending.type,
            updateTaskThunk.pending.type,
            createTaskThunk.pending.type,
            assignTaskThunk.pending.type,
            unassignTaskThunk.pending.type,
            allocateTaskResourcesThunk.pending.type,
        ].includes(action.type), (state) => {
            state.action.status = 'pending';
            state.action.error = null;
        })
            .addMatcher((action) => [
            startTaskExecutionThunk.rejected.type,
            markTaskDoneThunk.rejected.type,
            cancelTaskThunk.rejected.type,
            updateTaskThunk.rejected.type,
            createTaskThunk.rejected.type,
            assignTaskThunk.rejected.type,
            unassignTaskThunk.rejected.type,
            allocateTaskResourcesThunk.rejected.type,
        ].includes(action.type), (state, action: PayloadAction<AppApiError | undefined>) => {
            state.action.status = 'failed';
            state.action.error = action.payload ?? null;
        });
    },
});
export const tasksActions = tasksSlice.actions;
export const tasksReducer = tasksSlice.reducer;
export const tasksAdapterSelectors = tasksAdapter.getSelectors();
