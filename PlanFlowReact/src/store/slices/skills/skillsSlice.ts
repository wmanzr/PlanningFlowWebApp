import { createAsyncThunk, createEntityAdapter, createSlice, type PayloadAction, } from '@reduxjs/toolkit';
import { skillsApi, parseApiError } from '@/api';
import { asSkillId, type AppApiError, type AsyncStatus, type ListSkillsQuery, type PageResult, type SkillCreateRequest, type SkillId, type SkillResponseDto, } from '@/types';
const skillsAdapter = createEntityAdapter<SkillResponseDto>({
    sortComparer: (a, b) => a.name.localeCompare(b.name),
});
interface ListMeta {
    status: AsyncStatus;
    error: AppApiError | null;
    totalElements: number;
    totalPages: number;
}
interface CategoriesState {
    status: AsyncStatus;
    error: AppApiError | null;
    data: string[];
}
interface ActionMeta {
    status: AsyncStatus;
    error: AppApiError | null;
}
const initialList: ListMeta = { status: 'idle', error: null, totalElements: 0, totalPages: 0 };
const initialCategories: CategoriesState = { status: 'idle', error: null, data: [] };
const initialAction: ActionMeta = { status: 'idle', error: null };
export const fetchSkillsThunk = createAsyncThunk<PageResult<SkillResponseDto>, ListSkillsQuery, {
    rejectValue: AppApiError;
}>('skills/fetchList', async (query, { rejectWithValue }) => {
    try {
        return await skillsApi.list(query);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const fetchSkillCategoriesThunk = createAsyncThunk<string[], void, {
    rejectValue: AppApiError;
}>('skills/fetchCategories', async (_, { rejectWithValue }) => {
    try {
        return await skillsApi.categories();
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const createSkillThunk = createAsyncThunk<SkillId, SkillCreateRequest, {
    rejectValue: AppApiError;
}>('skills/create', async (body, { rejectWithValue }) => {
    try {
        const id = await skillsApi.create(body);
        return asSkillId(id);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const deleteSkillThunk = createAsyncThunk<SkillId, SkillId, {
    rejectValue: AppApiError;
}>('skills/delete', async (id, { rejectWithValue }) => {
    try {
        await skillsApi.delete(id);
        return id;
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const skillsSlice = createSlice({
    name: 'skills',
    initialState: skillsAdapter.getInitialState({
        list: initialList,
        categories: initialCategories,
        action: initialAction,
    }),
    reducers: {
        clearActionError(state) {
            state.action.error = null;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchSkillsThunk.pending, (state) => {
            state.list.status = 'pending';
            state.list.error = null;
        })
            .addCase(fetchSkillsThunk.fulfilled, (state, action) => {
            skillsAdapter.setAll(state, action.payload.items);
            state.list.status = 'succeeded';
            state.list.totalElements = action.payload.totalElements;
            state.list.totalPages = action.payload.totalPages;
        })
            .addCase(fetchSkillsThunk.rejected, (state, action) => {
            state.list.status = 'failed';
            state.list.error = action.payload ?? null;
        })
            .addCase(fetchSkillCategoriesThunk.pending, (state) => {
            state.categories.status = 'pending';
            state.categories.error = null;
        })
            .addCase(fetchSkillCategoriesThunk.fulfilled, (state, action) => {
            state.categories.status = 'succeeded';
            state.categories.data = action.payload;
        })
            .addCase(fetchSkillCategoriesThunk.rejected, (state, action) => {
            state.categories.status = 'failed';
            state.categories.error = action.payload ?? null;
        })
            .addCase(createSkillThunk.pending, (state) => {
            state.action.status = 'pending';
            state.action.error = null;
        })
            .addCase(createSkillThunk.fulfilled, (state) => {
            state.action.status = 'succeeded';
        })
            .addCase(createSkillThunk.rejected, (state, action: PayloadAction<AppApiError | undefined>) => {
            state.action.status = 'failed';
            state.action.error = action.payload ?? null;
        })
            .addCase(deleteSkillThunk.pending, (state) => {
            state.action.status = 'pending';
            state.action.error = null;
        })
            .addCase(deleteSkillThunk.fulfilled, (state, action: PayloadAction<SkillId>) => {
            skillsAdapter.removeOne(state, action.payload);
            state.action.status = 'succeeded';
            state.action.error = null;
        })
            .addCase(deleteSkillThunk.rejected, (state, action: PayloadAction<AppApiError | undefined>) => {
            state.action.status = 'failed';
            state.action.error = action.payload ?? null;
        });
    },
});
export const skillsActions = skillsSlice.actions;
export const skillsReducer = skillsSlice.reducer;
export const skillsAdapterSelectors = skillsAdapter.getSelectors();
