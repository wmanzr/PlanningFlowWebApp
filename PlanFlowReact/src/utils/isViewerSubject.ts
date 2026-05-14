import { authStorage } from '@/api';
import { decodeAccessClaims } from '@/store/slices/auth/jwt';
import type { UserId } from '@/types';
import { userIdsEqual } from '@/utils/userIdsEqual';
export function isViewerSubject(viewerUserId: UserId | undefined, subjectUserId: UserId | undefined): boolean {
    if (subjectUserId === undefined)
        return false;
    if (viewerUserId !== undefined && userIdsEqual(viewerUserId, subjectUserId))
        return true;
    const token = authStorage.getAccessToken();
    if (!token)
        return false;
    const claims = decodeAccessClaims(token);
    if (claims == null)
        return false;
    const rawSub = claims.sub as unknown;
    if (rawSub === undefined || rawSub === null)
        return false;
    const subNum = Number.parseInt(String(rawSub), 10);
    if (!Number.isFinite(subNum))
        return false;
    return userIdsEqual(subNum, subjectUserId);
}
