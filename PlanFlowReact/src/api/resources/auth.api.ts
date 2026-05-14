import { type AuthTokenResponse, type LoginRequest, type RefreshTokenRequest, type RegisterRequest, } from '@/types';
import { http } from '../http';
import { ENDPOINTS } from '../endpoints';
export const authApi = {
    login: (body: LoginRequest): Promise<AuthTokenResponse> => http.post<AuthTokenResponse>(ENDPOINTS.auth.login, body).then((r) => r.data),
    refresh: (body: RefreshTokenRequest): Promise<AuthTokenResponse> => http.post<AuthTokenResponse>(ENDPOINTS.auth.refresh, body).then((r) => r.data),
    register: (body: RegisterRequest): Promise<AuthTokenResponse> => http.post<AuthTokenResponse>(ENDPOINTS.auth.register, body).then((r) => r.data),
};
