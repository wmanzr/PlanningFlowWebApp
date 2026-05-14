import { createAsyncThunk, createEntityAdapter, createSlice, type PayloadAction, } from '@reduxjs/toolkit';
import { usersApi, parseApiError, type ListUsersQuery } from '@/api';
import { type AppApiError, type AssignmentId, type AssignmentRejectRequest, type AsyncStatus, type PageResult, type UserId, type UserProfileUpdateRequest, type UserResponseDto, type UserSkillResponseDto, type UserSkillsUpdateRequest, type UserViewerContextDto, } from '@/types';
const usersAdapter = createEntityAdapter<UserResponseDto>({
    sortComparer: (a, b) => a.username.localeCompare(b.username),
});
interface ListMeta {
    status: AsyncStatus;
    error: AppApiError | null;
    totalElements: number;
    totalPages: number;
}
interface SkillsState {
    status: AsyncStatus;
    error: AppApiError | null;
    data: UserSkillResponseDto[];
    loadedFor: UserId | null;
}
interface ActionMeta {
    status: AsyncStatus;
    error: AppApiError | null;
}
interface ViewerContextState {
    status: AsyncStatus;
    error: AppApiError | null;
    data: UserViewerContextDto | null;
    loadedFor: UserId | null;
}
const initialList: ListMeta = { status: 'idle', error: null, totalElements: 0, totalPages: 0 };
const initialSkills: SkillsState = { status: 'idle', error: null, data: [], loadedFor: null };
const initialAction: ActionMeta = { status: 'idle', error: null };
const initialViewerContext: ViewerContextState = {
    status: 'idle',
    error: null,
    data: null,
    loadedFor: null,
};
export const fetchUsersThunk = createAsyncThunk<PageResult<UserResponseDto>, ListUsersQuery, {
    rejectValue: AppApiError;
}>('users/fetchList', async (query, { rejectWithValue }) => {
    try {
        return await usersApi.list(query);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const fetchUserByIdThunk = createAsyncThunk<UserResponseDto, UserId, {
    rejectValue: AppApiError;
}>('users/fetchById', async (id, { rejectWithValue }) => {
    try {
        return await usersApi.byId(id);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const fetchUserSkillsThunk = createAsyncThunk<{
    id: UserId;
    skills: UserSkillResponseDto[];
}, UserId, {
    rejectValue: AppApiError;
}>('users/fetchSkills', async (id, { rejectWithValue }) => {
    try {
        const skills = await usersApi.skills(id);
        return { id, skills };
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const fetchUserViewerContextThunk = createAsyncThunk<{
    id: UserId;
    context: UserViewerContextDto;
}, UserId, {
    rejectValue: AppApiError;
}>('users/fetchViewerContext', async (id, { rejectWithValue }) => {
    try {
        const context = await usersApi.viewerContext(id);
        return { id, context };
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const updateUserProfileThunk = createAsyncThunk<UserResponseDto, {
    id: UserId;
    body: UserProfileUpdateRequest;
}, {
    rejectValue: AppApiError;
}>('users/updateProfile', async ({ id, body }, { rejectWithValue }) => {
    try {
        return await usersApi.updateProfile(id, body);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const updateUserSkillsThunk = createAsyncThunk<number[], {
    id: UserId;
    body: UserSkillsUpdateRequest;
}, {
    rejectValue: AppApiError;
}>('users/updateSkills', async ({ id, body }, { rejectWithValue }) => {
    try {
        return await usersApi.updateSkills(id, body);
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const acceptAssignmentThunk = createAsyncThunk<AssignmentId, AssignmentId, {
    rejectValue: AppApiError;
}>('users/acceptAssignment', async (id, { rejectWithValue }) => {
    try {
        await usersApi.acceptAssignment(id);
        return id;
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const rejectAssignmentThunk = createAsyncThunk<AssignmentId, {
    id: AssignmentId;
    body: AssignmentRejectRequest;
}, {
    rejectValue: AppApiError;
}>('users/rejectAssignment', async ({ id, body }, { rejectWithValue }) => {
    try {
        await usersApi.rejectAssignment(id, body);
        return id;
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const usersSlice = createSlice({
    name: 'users',
    initialState: usersAdapter.getInitialState({
        list: initialList,
        skills: initialSkills,
        viewerContext: initialViewerContext,
        action: initialAction,
    }),
    reducers: {
        clearActionError(state) {
            state.action.error = null;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchUsersThunk.pending, (state) => {
            state.list.status = 'pending';
            state.list.error = null;
        })
            .addCase(fetchUsersThunk.fulfilled, (state, action) => {
            usersAdapter.upsertMany(state, action.payload.items);
            state.list.status = 'succeeded';
            state.list.totalElements = action.payload.totalElements;
            state.list.totalPages = action.payload.totalPages;
        })
            .addCase(fetchUsersThunk.rejected, (state, action) => {
            state.list.status = 'failed';
            state.list.error = action.payload ?? null;
        })
            .addCase(fetchUserByIdThunk.fulfilled, (state, action) => {
            usersAdapter.upsertOne(state, action.payload);
        })
            .addCase(fetchUserSkillsThunk.pending, (state) => {
            state.skills.status = 'pending';
            state.skills.error = null;
        })
            .addCase(fetchUserSkillsThunk.fulfilled, (state, action) => {
            state.skills.status = 'succeeded';
            state.skills.data = action.payload.skills;
            state.skills.loadedFor = action.payload.id;
        })
            .addCase(fetchUserSkillsThunk.rejected, (state, action) => {
            state.skills.status = 'failed';
            state.skills.error = action.payload ?? null;
        })
            .addCase(fetchUserViewerContextThunk.pending, (state) => {
            state.viewerContext.status = 'pending';
            state.viewerContext.error = null;
        })
            .addCase(fetchUserViewerContextThunk.fulfilled, (state, action) => {
            state.viewerContext.status = 'succeeded';
            state.viewerContext.data = action.payload.context;
            state.viewerContext.loadedFor = action.payload.id;
            state.viewerContext.error = null;
        })
            .addCase(fetchUserViewerContextThunk.rejected, (state, action) => {
            state.viewerContext.status = 'failed';
            state.viewerContext.error = action.payload ?? null;
            state.viewerContext.data = null;
            state.viewerContext.loadedFor = null;
        })
            .addMatcher((action) => action.type === updateUserProfileThunk.fulfilled.type, (state, action: PayloadAction<UserResponseDto>) => {
            usersAdapter.upsertOne(state, action.payload);
            state.action.status = 'succeeded';
            state.action.error = null;
        })
            .addMatcher((action) => [
            updateUserProfileThunk.pending.type,
            updateUserSkillsThunk.pending.type,
            acceptAssignmentThunk.pending.type,
            rejectAssignmentThunk.pending.type,
        ].includes(action.type), (state) => {
            state.action.status = 'pending';
            state.action.error = null;
        })
            .addMatcher((action) => [
            updateUserProfileThunk.rejected.type,
            updateUserSkillsThunk.rejected.type,
            acceptAssignmentThunk.rejected.type,
            rejectAssignmentThunk.rejected.type,
        ].includes(action.type), (state, action: PayloadAction<AppApiError | undefined>) => {
            state.action.status = 'failed';
            state.action.error = action.payload ?? null;
        })
            .addMatcher((action) => [
            updateUserSkillsThunk.fulfilled.type,
            acceptAssignmentThunk.fulfilled.type,
            rejectAssignmentThunk.fulfilled.type,
        ].includes(action.type), (state) => {
            state.action.status = 'succeeded';
            state.action.error = null;
        });
    },
});
export const usersActions = usersSlice.actions;
export const usersReducer = usersSlice.reducer;
export const usersAdapterSelectors = usersAdapter.getSelectors();
