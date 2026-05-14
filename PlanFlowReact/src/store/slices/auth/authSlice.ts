import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { authApi, authStorage, parseApiError, } from '@/api';
import { asUserId, type AppApiError, type AsyncStatus, type LoginRequest, type RegisterRequest, type UserId, type UserRole, } from '@/types';
import { decodeAccessClaims, extractRoles } from './jwt';
export interface CurrentUser {
    id: UserId;
    username: string;
    roles: UserRole[];
}
interface AuthState {
    user: CurrentUser | null;
    status: AsyncStatus;
    error: AppApiError | null;
    initialised: boolean;
}
const initialState: AuthState = {
    user: null,
    status: 'idle',
    error: null,
    initialised: false,
};
const buildUserFromAccessToken = (accessToken: string): CurrentUser | null => {
    const claims = decodeAccessClaims(accessToken);
    if (!claims)
        return null;
    const idNumber = Number.parseInt(claims.sub, 10);
    if (!Number.isFinite(idNumber))
        return null;
    return {
        id: asUserId(idNumber),
        username: claims.username,
        roles: extractRoles(claims),
    };
};
let restoreSessionInFlight: Promise<CurrentUser | null> | null = null;
export const loginThunk = createAsyncThunk<CurrentUser, LoginRequest, {
    rejectValue: AppApiError;
}>('auth/login', async (credentials, { rejectWithValue }) => {
    try {
        const response = await authApi.login(credentials);
        authStorage.setAccessToken(response.accessToken);
        authStorage.setRefreshToken(response.refreshToken);
        const user = buildUserFromAccessToken(response.accessToken);
        if (!user) {
            authStorage.clear();
            return rejectWithValue({
                message: 'Не удалось распознать токен авторизации',
                errorCode: 'AUTH_TOKEN_INVALID',
                httpStatus: null,
                timestamp: Date.now(),
            });
        }
        return user;
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const registerThunk = createAsyncThunk<CurrentUser, RegisterRequest, {
    rejectValue: AppApiError;
}>('auth/register', async (body, { rejectWithValue }) => {
    try {
        const response = await authApi.register(body);
        authStorage.setAccessToken(response.accessToken);
        authStorage.setRefreshToken(response.refreshToken);
        const user = buildUserFromAccessToken(response.accessToken);
        if (!user) {
            authStorage.clear();
            return rejectWithValue({
                message: 'Не удалось распознать токен авторизации',
                errorCode: 'AUTH_TOKEN_INVALID',
                httpStatus: null,
                timestamp: Date.now(),
            });
        }
        return user;
    }
    catch (err) {
        const parsed = parseApiError(err);
        return rejectWithValue(parsed);
    }
});
export const restoreSessionThunk = createAsyncThunk<CurrentUser | null, void, {
    rejectValue: AppApiError;
}>('auth/restoreSession', async (_, { rejectWithValue }) => {
    if (!restoreSessionInFlight) {
        restoreSessionInFlight = (async (): Promise<CurrentUser | null> => {
            const refreshToken = authStorage.getRefreshToken();
            if (!refreshToken) {
                return null;
            }
            try {
                const response = await authApi.refresh({ refreshToken });
                authStorage.setAccessToken(response.accessToken);
                authStorage.setRefreshToken(response.refreshToken);
                return buildUserFromAccessToken(response.accessToken);
            }
            catch (err) {
                authStorage.clear();
                throw err;
            }
            finally {
                restoreSessionInFlight = null;
            }
        })();
    }
    try {
        return await restoreSessionInFlight;
    }
    catch (err) {
        return rejectWithValue(parseApiError(err));
    }
});
export const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        sessionExpired(state) {
            state.user = null;
            state.status = 'idle';
            state.error = null;
            authStorage.clear();
        },
        logout(state) {
            state.user = null;
            state.status = 'idle';
            state.error = null;
            authStorage.clear();
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(loginThunk.pending, (state) => {
            state.status = 'pending';
            state.error = null;
        })
            .addCase(loginThunk.fulfilled, (state, action) => {
            state.status = 'succeeded';
            state.user = action.payload;
            state.error = null;
        })
            .addCase(loginThunk.rejected, (state, action) => {
            state.status = 'failed';
            state.error = action.payload ?? null;
            state.user = null;
        })
            .addCase(registerThunk.pending, (state) => {
            state.status = 'pending';
            state.error = null;
        })
            .addCase(registerThunk.fulfilled, (state, action) => {
            state.status = 'succeeded';
            state.user = action.payload;
            state.error = null;
        })
            .addCase(registerThunk.rejected, (state, action) => {
            state.status = 'failed';
            state.error = action.payload ?? null;
            state.user = null;
        })
            .addCase(restoreSessionThunk.pending, (state) => {
            state.status = 'pending';
        })
            .addCase(restoreSessionThunk.fulfilled, (state, action) => {
            state.status = action.payload ? 'succeeded' : 'idle';
            state.user = action.payload;
            state.initialised = true;
        })
            .addCase(restoreSessionThunk.rejected, (state) => {
            state.status = 'idle';
            state.user = null;
            state.initialised = true;
        });
    },
});
export const authActions = authSlice.actions;
export const authReducer = authSlice.reducer;
