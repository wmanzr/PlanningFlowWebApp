import { createAsyncThunk, createEntityAdapter, createSlice, type PayloadAction, } from '@reduxjs/toolkit';
import { incidentsApi, parseApiError } from '@/api';
import { asIncidentId, type AppApiError, type AsyncStatus, type EventId, type IncidentCreateRequest, type IncidentId, type IncidentResolveRequest, type IncidentResponseDto, type PageQuery, type PageResult, } from '@/types';
const incidentsAdapter = createEntityAdapter<IncidentResponseDto>({
    sortComparer: (a, b) => b.createdAt.localeCompare(a.createdAt),
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
export const fetchIncidentsForEventThunk = createAsyncThunk<PageResult<IncidentResponseDto>, {
    eventId: EventId;
    query: PageQuery;
}, {
    rejectValue: AppApiError;
}>('incidents/fetchForEvent', async ({ eventId, query }, { rejectWithValue }) => {
    try {
        return await incidentsApi.forEvent(eventId, query);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const fetchIncidentByIdThunk = createAsyncThunk<IncidentResponseDto, IncidentId, {
    rejectValue: AppApiError;
}>('incidents/fetchById', async (id, { rejectWithValue }) => {
    try {
        return await incidentsApi.byId(id);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const createIncidentThunk = createAsyncThunk<IncidentId, IncidentCreateRequest, {
    rejectValue: AppApiError;
}>('incidents/create', async (body, { rejectWithValue }) => {
    try {
        const id = await incidentsApi.create(body);
        return asIncidentId(id);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const acceptIncidentThunk = createAsyncThunk<IncidentId, IncidentId, {
    rejectValue: AppApiError;
}>('incidents/accept', async (id, { rejectWithValue }) => {
    try {
        await incidentsApi.accept(id);
        return id;
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const resolveIncidentThunk = createAsyncThunk<IncidentId, {
    id: IncidentId;
    body: IncidentResolveRequest;
}, {
    rejectValue: AppApiError;
}>('incidents/resolve', async ({ id, body }, { rejectWithValue }) => {
    try {
        await incidentsApi.resolve(id, body);
        return id;
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const incidentsSlice = createSlice({
    name: 'incidents',
    initialState: incidentsAdapter.getInitialState({
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
            .addCase(fetchIncidentsForEventThunk.pending, (state) => {
            state.list.status = 'pending';
            state.list.error = null;
        })
            .addCase(fetchIncidentsForEventThunk.fulfilled, (state, action) => {
            incidentsAdapter.upsertMany(state, action.payload.items);
            state.list.status = 'succeeded';
            const size = action.meta.arg.query.size ?? 20;
            if (size <= 50) {
                state.list.totalElements = action.payload.totalElements;
                state.list.totalPages = action.payload.totalPages;
            }
        })
            .addCase(fetchIncidentsForEventThunk.rejected, (state, action) => {
            state.list.status = 'failed';
            state.list.error = action.payload ?? null;
        })
            .addCase(fetchIncidentByIdThunk.fulfilled, (state, action) => {
            incidentsAdapter.upsertOne(state, action.payload);
        })
            .addMatcher((action) => [
            createIncidentThunk.pending.type,
            acceptIncidentThunk.pending.type,
            resolveIncidentThunk.pending.type,
        ].includes(action.type), (state) => {
            state.action.status = 'pending';
            state.action.error = null;
        })
            .addMatcher((action) => [
            createIncidentThunk.fulfilled.type,
            acceptIncidentThunk.fulfilled.type,
            resolveIncidentThunk.fulfilled.type,
        ].includes(action.type), (state) => {
            state.action.status = 'succeeded';
            state.action.error = null;
        })
            .addMatcher((action) => [
            createIncidentThunk.rejected.type,
            acceptIncidentThunk.rejected.type,
            resolveIncidentThunk.rejected.type,
        ].includes(action.type), (state, action: PayloadAction<AppApiError | undefined>) => {
            state.action.status = 'failed';
            state.action.error = action.payload ?? null;
        });
    },
});
export const incidentsActions = incidentsSlice.actions;
export const incidentsReducer = incidentsSlice.reducer;
export const incidentsAdapterSelectors = incidentsAdapter.getSelectors();
