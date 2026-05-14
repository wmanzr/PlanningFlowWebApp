import { describe, expect, it } from 'vitest';
import { authActions, authReducer } from '@/store/slices/auth/authSlice';
import { asUserId } from '@/types';
describe('authSlice', () => {
    it('resets user on logout', () => {
        const populated = authReducer(undefined, { type: '@@INIT' });
        const next = authReducer({ ...populated, user: { id: asUserId(1), username: 'u', roles: ['ADMIN'] } }, authActions.logout());
        expect(next.user).toBeNull();
        expect(next.status).toBe('idle');
    });
    it('clears user when session expires', () => {
        const populated = {
            user: { id: asUserId(2), username: 'a', roles: ['ORGANIZER' as const] },
            status: 'succeeded' as const,
            error: null,
            initialised: true,
        };
        const next = authReducer(populated, authActions.sessionExpired());
        expect(next.user).toBeNull();
        expect(next.error).toBeNull();
    });
});
