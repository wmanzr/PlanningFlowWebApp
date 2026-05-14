import { PATHS } from '@/pages/paths';
import { UserRole, type UserRole as UserRoleType } from '@/types';
const STAFF_ROLES: readonly UserRoleType[] = [
    UserRole.ADMIN,
    UserRole.ORGANIZER,
    UserRole.COORDINATOR,
];
export function isParticipantOnlyRole(roles: readonly UserRoleType[] | undefined): boolean {
    if (!roles?.length)
        return false;
    return (roles.includes(UserRole.PARTICIPANT) &&
        !roles.includes(UserRole.ADMIN) &&
        !roles.includes(UserRole.ORGANIZER) &&
        !roles.includes(UserRole.COORDINATOR));
}
export function getPostLoginPath(roles: readonly UserRoleType[]): string {
    if (roles.some((r) => STAFF_ROLES.includes(r))) {
        return PATHS.home;
    }
    return PATHS.myTasks;
}
