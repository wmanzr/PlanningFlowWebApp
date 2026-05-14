import { createAsyncThunk, createEntityAdapter, createSlice, type PayloadAction, } from '@reduxjs/toolkit';
import { eventsApi, parseApiError } from '@/api';
import { asEventId, type AppApiError, type AsyncStatus, type EventCancelRequest, type EventCreateRequest, type EventDashboardResponseDto, type EventId, type EventPostMortemAiReportResponseDto, type EventResponseDto, type EventUpdateRequest, type ListEventsQuery, type PageResult, } from '@/types';
const eventsAdapter = createEntityAdapter<EventResponseDto>({
    sortComparer: (a, b) => a.startDate.localeCompare(b.startDate),
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
    loadedId: EventId | null;
}
interface DashboardSlice {
    status: AsyncStatus;
    error: AppApiError | null;
    data: EventDashboardResponseDto | null;
}
interface PostMortemSlice {
    status: AsyncStatus;
    error: AppApiError | null;
    data: EventPostMortemAiReportResponseDto | null;
    loadedEventId: EventId | null;
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
    size: 20,
};
const initialDetail: DetailMeta = { status: 'idle', error: null, loadedId: null };
const initialDashboard: DashboardSlice = { status: 'idle', error: null, data: null };
const initialPostMortem: PostMortemSlice = {
    status: 'idle',
    error: null,
    data: null,
    loadedEventId: null,
};
const initialAction: ActionMeta = { status: 'idle', error: null };
export const fetchEventsThunk = createAsyncThunk<PageResult<EventResponseDto>, ListEventsQuery, {
    rejectValue: AppApiError;
}>('events/fetchList', async (query, { rejectWithValue }) => {
    try {
        return await eventsApi.list(query);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const fetchEventByIdThunk = createAsyncThunk<EventResponseDto, EventId, {
    rejectValue: AppApiError;
}>('events/fetchById', async (id, { rejectWithValue }) => {
    try {
        return await eventsApi.byId(id);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const fetchEventDashboardThunk = createAsyncThunk<EventDashboardResponseDto, EventId, {
    rejectValue: AppApiError;
}>('events/fetchDashboard', async (id, { rejectWithValue }) => {
    try {
        return await eventsApi.dashboard(id);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const createEventThunk = createAsyncThunk<EventId, EventCreateRequest, {
    rejectValue: AppApiError;
}>('events/create', async (body, { rejectWithValue }) => {
    try {
        const id = await eventsApi.create(body);
        return asEventId(id);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const updateEventThunk = createAsyncThunk<EventId, EventUpdateRequest, {
    rejectValue: AppApiError;
}>('events/update', async (body, { rejectWithValue }) => {
    try {
        const id = await eventsApi.update(body);
        return asEventId(id);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const startPlanningThunk = createAsyncThunk<EventResponseDto, EventId, {
    rejectValue: AppApiError;
}>('events/startPlanning', async (id, { rejectWithValue }) => {
    try {
        const updatedId = await eventsApi.startPlanning(id);
        return await eventsApi.byId(asEventId(updatedId));
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const activateEventThunk = createAsyncThunk<EventResponseDto, EventId, {
    rejectValue: AppApiError;
}>('events/activate', async (id, { rejectWithValue }) => {
    try {
        const updatedId = await eventsApi.activate(id);
        return await eventsApi.byId(asEventId(updatedId));
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const completeEventThunk = createAsyncThunk<EventResponseDto, EventId, {
    rejectValue: AppApiError;
}>('events/complete', async (id, { rejectWithValue }) => {
    try {
        const updatedId = await eventsApi.complete(id);
        return await eventsApi.byId(asEventId(updatedId));
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const fetchEventPostMortemAiReportThunk = createAsyncThunk<EventPostMortemAiReportResponseDto, EventId, {
    rejectValue: AppApiError;
}>('events/fetchPostMortemAi', async (id, { rejectWithValue }) => {
    try {
        return await eventsApi.getPostMortemAiReport(id);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const cancelEventThunk = createAsyncThunk<EventResponseDto, {
    id: EventId;
    body: EventCancelRequest;
}, {
    rejectValue: AppApiError;
}>('events/cancel', async ({ id, body }, { rejectWithValue }) => {
    try {
        const updatedId = await eventsApi.cancel(id, body);
        return await eventsApi.byId(asEventId(updatedId));
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const eventsSlice = createSlice({
    name: 'events',
    initialState: eventsAdapter.getInitialState({
        list: initialList,
        detail: initialDetail,
        dashboard: initialDashboard,
        postMortem: initialPostMortem,
        action: initialAction,
    }),
    reducers: {
        setListPage(state, action: PayloadAction<{
            page: number;
            size: number;
        }>) {
            state.list.page = action.payload.page;
            state.list.size = action.payload.size;
        },
        clearActionError(state) {
            state.action.error = null;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchEventsThunk.pending, (state) => {
            state.list.status = 'pending';
            state.list.error = null;
        })
            .addCase(fetchEventsThunk.fulfilled, (state, action) => {
            eventsAdapter.upsertMany(state, action.payload.items);
            state.list.status = 'succeeded';
            state.list.totalElements = action.payload.totalElements;
            state.list.totalPages = action.payload.totalPages;
            if (action.meta.arg.page !== undefined) {
                state.list.page = action.meta.arg.page;
            }
            if (action.meta.arg.size !== undefined) {
                state.list.size = action.meta.arg.size;
            }
        })
            .addCase(fetchEventsThunk.rejected, (state, action) => {
            state.list.status = 'failed';
            state.list.error = action.payload ?? null;
        })
            .addCase(fetchEventByIdThunk.pending, (state) => {
            state.detail.status = 'pending';
            state.detail.error = null;
        })
            .addCase(fetchEventByIdThunk.fulfilled, (state, action) => {
            eventsAdapter.upsertOne(state, action.payload);
            state.detail.status = 'succeeded';
            state.detail.loadedId = action.payload.id;
        })
            .addCase(fetchEventByIdThunk.rejected, (state, action) => {
            state.detail.status = 'failed';
            state.detail.error = action.payload ?? null;
        })
            .addCase(fetchEventDashboardThunk.pending, (state, action) => {
            state.dashboard.status = 'pending';
            state.dashboard.error = null;
            const requestedId = action.meta.arg;
            const current = state.dashboard.data;
            if (current !== null && Number(current.eventId) !== Number(requestedId)) {
                state.dashboard.data = null;
            }
        })
            .addCase(fetchEventDashboardThunk.fulfilled, (state, action) => {
            state.dashboard.status = 'succeeded';
            state.dashboard.data = action.payload;
        })
            .addCase(fetchEventDashboardThunk.rejected, (state, action) => {
            state.dashboard.status = 'failed';
            state.dashboard.error = action.payload ?? null;
        })
            .addCase(fetchEventPostMortemAiReportThunk.pending, (state, action) => {
            state.postMortem.status = 'pending';
            state.postMortem.error = null;
            const requestedId = action.meta.arg;
            if (state.postMortem.loadedEventId !== null &&
                Number(state.postMortem.loadedEventId) !== Number(requestedId)) {
                state.postMortem.data = null;
            }
        })
            .addCase(fetchEventPostMortemAiReportThunk.fulfilled, (state, action) => {
            state.postMortem.status = 'succeeded';
            state.postMortem.data = action.payload;
            state.postMortem.loadedEventId = action.meta.arg;
        })
            .addCase(fetchEventPostMortemAiReportThunk.rejected, (state, action) => {
            state.postMortem.status = 'failed';
            state.postMortem.error = action.payload ?? null;
        })
            .addCase(createEventThunk.fulfilled, (state) => {
            state.action.status = 'idle';
            state.action.error = null;
        })
            .addCase(updateEventThunk.fulfilled, (state) => {
            state.action.status = 'idle';
            state.action.error = null;
        })
            .addMatcher((action) => [
            startPlanningThunk.fulfilled.type,
            activateEventThunk.fulfilled.type,
            completeEventThunk.fulfilled.type,
            cancelEventThunk.fulfilled.type,
        ].includes(action.type), (state, action: PayloadAction<EventResponseDto>) => {
            eventsAdapter.upsertOne(state, action.payload);
            state.action.status = 'succeeded';
            state.action.error = null;
        })
            .addMatcher((action) => [
            startPlanningThunk.pending.type,
            activateEventThunk.pending.type,
            completeEventThunk.pending.type,
            cancelEventThunk.pending.type,
            createEventThunk.pending.type,
            updateEventThunk.pending.type,
        ].includes(action.type), (state) => {
            state.action.status = 'pending';
            state.action.error = null;
        })
            .addMatcher((action) => [
            startPlanningThunk.rejected.type,
            activateEventThunk.rejected.type,
            completeEventThunk.rejected.type,
            cancelEventThunk.rejected.type,
            createEventThunk.rejected.type,
            updateEventThunk.rejected.type,
        ].includes(action.type), (state, action: PayloadAction<AppApiError | undefined, string>) => {
            state.action.status = 'failed';
            state.action.error = action.payload ?? null;
        });
    },
});
export const eventsActions = eventsSlice.actions;
export const eventsReducer = eventsSlice.reducer;
export const eventsAdapterSelectors = eventsAdapter.getSelectors();
