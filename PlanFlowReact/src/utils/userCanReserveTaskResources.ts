import { UserRole } from '@/types';
const ROLES_THAT_CAN_RESERVE_TASK_RESOURCES: UserRole[] = [
    UserRole.ADMIN,
    UserRole.ORGANIZER,
    UserRole.COORDINATOR,
];
export function userCanReserveTaskResources(roles: UserRole[] | undefined): boolean {
    if (roles === undefined || roles.length === 0) {
        return false;
    }
    return ROLES_THAT_CAN_RESERVE_TASK_RESOURCES.some((r) => roles.includes(r));
}
