import type { RootState } from '../../types';
import type { UserRole } from '@/types';
export const selectAuthState = (state: RootState) => state.auth;
export const selectCurrentUser = (state: RootState) => state.auth.user;
export const selectIsAuthenticated = (state: RootState): boolean => state.auth.user !== null;
export const selectAuthStatus = (state: RootState) => state.auth.status;
export const selectAuthError = (state: RootState) => state.auth.error;
export const selectAuthInitialised = (state: RootState): boolean => state.auth.initialised;
export const selectHasRole = (role: UserRole) => (state: RootState): boolean => state.auth.user?.roles.includes(role) ?? false;
export const selectHasAnyRole = (roles: UserRole[]) => (state: RootState): boolean => {
    if (state.auth.user === null)
        return false;
    return roles.some((role) => state.auth.user?.roles.includes(role));
};
