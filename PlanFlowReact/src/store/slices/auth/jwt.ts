import type { JwtAccessClaims, UserRole } from '@/types';
const isUserRole = (value: string): value is UserRole => value === 'ORGANIZER' ||
    value === 'COORDINATOR' ||
    value === 'PARTICIPANT' ||
    value === 'ADMIN';
const ROLE_PREFIX = 'ROLE_';
const decodeBase64Url = (input: string): string => {
    const padded = input.replace(/-/g, '+').replace(/_/g, '/');
    const padding = padded.length % 4 === 0 ? '' : '='.repeat(4 - (padded.length % 4));
    return atob(padded + padding);
};
export const decodeAccessClaims = (token: string): JwtAccessClaims | null => {
    const parts = token.split('.');
    if (parts.length !== 3)
        return null;
    try {
        const decoded = decodeBase64Url(parts[1] ?? '');
        const json = JSON.parse(decoded) as JwtAccessClaims;
        return json;
    }
    catch {
        return null;
    }
};
const normalizeRolesRaw = (raw: unknown): unknown[] => {
    if (raw == null)
        return [];
    if (Array.isArray(raw))
        return raw;
    if (typeof raw === 'string') {
        const s = raw.trim();
        if (!s)
            return [];
        if (s.startsWith('[')) {
            try {
                const parsed = JSON.parse(s) as unknown;
                return Array.isArray(parsed) ? parsed : [parsed];
            }
            catch {
                return s.split(',').map((x) => x.trim()).filter(Boolean);
            }
        }
        return [s];
    }
    return [raw];
};
export const extractRoles = (claims: JwtAccessClaims): UserRole[] => {
    const list = normalizeRolesRaw(claims.roles as unknown);
    return list
        .map((role) => String(role).trim())
        .map((role) => (role.startsWith(ROLE_PREFIX) ? role.slice(ROLE_PREFIX.length) : role))
        .map((role) => role.toUpperCase())
        .filter((role): role is UserRole => isUserRole(role));
};
