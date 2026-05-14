import axios, { type AxiosError, type AxiosInstance, type AxiosRequestConfig, type InternalAxiosRequestConfig, } from 'axios';
import { type AuthTokenResponse } from '@/types';
import { authStorage } from './authStorage';
import { API_BASE, ENDPOINTS } from './endpoints';
const HTTP_TIMEOUT_MS = 15000;
const HTTP_UNAUTHORIZED = 401;
const HTTP_FORBIDDEN = 403;
interface RetriableConfig extends InternalAxiosRequestConfig {
    _retried?: boolean;
}
type SessionExpiredHandler = () => void;
let onSessionExpired: SessionExpiredHandler | null = null;
export const setSessionExpiredHandler = (handler: SessionExpiredHandler | null): void => {
    onSessionExpired = handler;
};
type ForbiddenHandler = () => void;
let onForbidden: ForbiddenHandler | null = null;
export const setForbiddenHandler = (handler: ForbiddenHandler | null): void => {
    onForbidden = handler;
};
const baseURL = (import.meta.env.VITE_API_BASE_URL ?? '').toString();
export const http: AxiosInstance = axios.create({
    baseURL,
    timeout: HTTP_TIMEOUT_MS,
    headers: { 'Content-Type': 'application/json' },
});
const isAuthEndpoint = (url: string | undefined): boolean => {
    if (!url)
        return false;
    return (url.includes(ENDPOINTS.auth.login) ||
        url.includes(ENDPOINTS.auth.refresh) ||
        url.includes(ENDPOINTS.auth.register));
};
const isPublicStatsEndpoint = (url: string | undefined): boolean => Boolean(url && url.includes(`${API_BASE}/public/`));
const isNotificationsEndpoint = (url: string | undefined): boolean => Boolean(url && url.includes(ENDPOINTS.notifications.root));
const isUsersDirectoryListRequest = (url: string | undefined): boolean => {
    if (!url)
        return false;
    const pathOnly = url.split('?')[0];
    return pathOnly === ENDPOINTS.users.root;
};
http.interceptors.request.use((config) => {
    if (isAuthEndpoint(config.url))
        return config;
    const token = authStorage.getAccessToken();
    if (token) {
        config.headers.set('Authorization', `Bearer ${token}`);
    }
    return config;
});
let refreshPromise: Promise<string> | null = null;
const refreshAccessToken = async (): Promise<string> => {
    const refreshToken = authStorage.getRefreshToken();
    if (!refreshToken) {
        throw new Error('No refresh token');
    }
    const response = await axios.post<AuthTokenResponse>(`${baseURL}${ENDPOINTS.auth.refresh}`, { refreshToken }, { headers: { 'Content-Type': 'application/json' }, timeout: HTTP_TIMEOUT_MS });
    authStorage.setAccessToken(response.data.accessToken);
    authStorage.setRefreshToken(response.data.refreshToken);
    return response.data.accessToken;
};
const acquireRefresh = (): Promise<string> => {
    refreshPromise ??= refreshAccessToken().finally(() => {
        refreshPromise = null;
    });
    return refreshPromise;
};
http.interceptors.response.use((response) => response, async (error: AxiosError) => {
    const original = error.config as RetriableConfig | undefined;
    const status = error.response?.status;
    const canTryRefresh = original &&
        !original._retried &&
        !isAuthEndpoint(original.url) &&
        !isPublicStatsEndpoint(original.url) &&
        authStorage.getRefreshToken() &&
        (status === HTTP_UNAUTHORIZED || status === HTTP_FORBIDDEN);
    if (canTryRefresh) {
        original._retried = true;
        try {
            const newToken = await acquireRefresh();
            original.headers.set('Authorization', `Bearer ${newToken}`);
            return http.request(original);
        }
        catch (refreshError) {
            authStorage.clear();
            onSessionExpired?.();
            return Promise.reject(refreshError);
        }
    }
    if (status === HTTP_UNAUTHORIZED &&
        !isAuthEndpoint(original?.url) &&
        !isPublicStatsEndpoint(original?.url)) {
        authStorage.clear();
        onSessionExpired?.();
    }
    if (status === HTTP_FORBIDDEN &&
        original &&
        !isAuthEndpoint(original.url) &&
        !isNotificationsEndpoint(original.url) &&
        !isUsersDirectoryListRequest(original.url)) {
        onForbidden?.();
    }
    return Promise.reject(error);
});
export const requestRaw = async <T>(config: AxiosRequestConfig): Promise<T> => {
    const response = await http.request<T>(config);
    return response.data;
};
