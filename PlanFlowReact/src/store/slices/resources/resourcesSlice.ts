import { createAsyncThunk, createEntityAdapter, createSlice, type PayloadAction, } from '@reduxjs/toolkit';
import { internalResourcesApi, parseApiError, type ListInternalResourcesQuery, } from '@/api';
import { asResourceId, type AppApiError, type AsyncStatus, type InternalResourceCreateRequest, type InternalResourceResponseDto, type InternalResourceUpdateRequest, type PageResult, type ResourceId, } from '@/types';
const resourcesAdapter = createEntityAdapter<InternalResourceResponseDto>({
    sortComparer: (a, b) => a.name.localeCompare(b.name),
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
export const fetchInternalResourcesThunk = createAsyncThunk<PageResult<InternalResourceResponseDto>, ListInternalResourcesQuery, {
    rejectValue: AppApiError;
}>('resources/fetchInternal', async (query, { rejectWithValue }) => {
    try {
        return await internalResourcesApi.list(query);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const createInternalResourceThunk = createAsyncThunk<ResourceId, InternalResourceCreateRequest, {
    rejectValue: AppApiError;
}>('resources/createInternal', async (body, { rejectWithValue }) => {
    try {
        const id = await internalResourcesApi.create(body);
        return asResourceId(id);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const updateInternalResourceThunk = createAsyncThunk<InternalResourceResponseDto, {
    id: ResourceId;
    body: InternalResourceUpdateRequest;
}, {
    rejectValue: AppApiError;
}>('resources/updateInternal', async ({ id, body }, { rejectWithValue }) => {
    try {
        const updatedId = await internalResourcesApi.update(id, body);
        return await internalResourcesApi.byId(asResourceId(updatedId));
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const markResourceOperationalThunk = createAsyncThunk<InternalResourceResponseDto, ResourceId, {
    rejectValue: AppApiError;
}>('resources/markOperational', async (id, { rejectWithValue }) => {
    try {
        const updatedId = await internalResourcesApi.markOperational(id);
        return await internalResourcesApi.byId(asResourceId(updatedId));
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const markResourceBrokenThunk = createAsyncThunk<InternalResourceResponseDto, ResourceId, {
    rejectValue: AppApiError;
}>('resources/markBroken', async (id, { rejectWithValue }) => {
    try {
        const updatedId = await internalResourcesApi.markBroken(id);
        return await internalResourcesApi.byId(asResourceId(updatedId));
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const deleteInternalResourceThunk = createAsyncThunk<ResourceId, ResourceId, {
    rejectValue: AppApiError;
}>('resources/deleteInternal', async (id, { rejectWithValue }) => {
    try {
        await internalResourcesApi.delete(id);
        return id;
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const resourcesSlice = createSlice({
    name: 'resources',
    initialState: resourcesAdapter.getInitialState({
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
            .addCase(fetchInternalResourcesThunk.pending, (state) => {
            state.list.status = 'pending';
            state.list.error = null;
        })
            .addCase(fetchInternalResourcesThunk.fulfilled, (state, action) => {
            resourcesAdapter.setAll(state, action.payload.items);
            state.list.status = 'succeeded';
            state.list.totalElements = action.payload.totalElements;
            state.list.totalPages = action.payload.totalPages;
        })
            .addCase(fetchInternalResourcesThunk.rejected, (state, action) => {
            state.list.status = 'failed';
            state.list.error = action.payload ?? null;
        })
            .addCase(createInternalResourceThunk.fulfilled, (state) => {
            state.action.status = 'idle';
            state.action.error = null;
        })
            .addCase(deleteInternalResourceThunk.fulfilled, (state, action) => {
            resourcesAdapter.removeOne(state, action.payload);
            state.action.status = 'succeeded';
            state.action.error = null;
        })
            .addMatcher((action) => [
            updateInternalResourceThunk.fulfilled.type,
            markResourceOperationalThunk.fulfilled.type,
            markResourceBrokenThunk.fulfilled.type,
        ].includes(action.type), (state, action: PayloadAction<InternalResourceResponseDto>) => {
            resourcesAdapter.upsertOne(state, action.payload);
            state.action.status = 'succeeded';
            state.action.error = null;
        })
            .addMatcher((action) => [
            updateInternalResourceThunk.pending.type,
            markResourceOperationalThunk.pending.type,
            markResourceBrokenThunk.pending.type,
            createInternalResourceThunk.pending.type,
            deleteInternalResourceThunk.pending.type,
        ].includes(action.type), (state) => {
            state.action.status = 'pending';
            state.action.error = null;
        })
            .addMatcher((action) => [
            updateInternalResourceThunk.rejected.type,
            markResourceOperationalThunk.rejected.type,
            markResourceBrokenThunk.rejected.type,
            createInternalResourceThunk.rejected.type,
            deleteInternalResourceThunk.rejected.type,
        ].includes(action.type), (state, action: PayloadAction<AppApiError | undefined>) => {
            state.action.status = 'failed';
            state.action.error = action.payload ?? null;
        });
    },
});
export const resourcesActions = resourcesSlice.actions;
export const resourcesReducer = resourcesSlice.reducer;
export const resourcesAdapterSelectors = resourcesAdapter.getSelectors();
