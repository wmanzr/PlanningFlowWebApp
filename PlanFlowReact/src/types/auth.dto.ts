import type { UserRole } from './enums';
export interface LoginRequest {
    username: string;
    password: string;
}
export interface RegisterRequest {
    username: string;
    password: string;
    email: string;
    fullName: string;
    birthDate: string;
    role: UserRole;
}
export interface RefreshTokenRequest {
    refreshToken: string;
}
export interface AuthTokenResponse {
    accessToken: string;
    refreshToken: string;
}
export interface JwtAccessClaims {
    sub: string;
    typ: 'access';
    username: string;
    roles: string[] | string;
    exp: number;
    iat: number;
}
