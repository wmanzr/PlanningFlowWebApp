import { type ReactNode } from 'react';
import { useAppSelector } from '@/store';
import { selectHasAnyRole } from '@/store/slices/auth/selectors';
import { EmptyState } from '@/components/ui';
import type { UserRole } from '@/types';
export interface RequireRoleProps {
    roles: UserRole[];
    children: ReactNode;
    fallback?: ReactNode;
}
export const RequireRole = ({ roles, children, fallback }: RequireRoleProps) => {
    const hasRole = useAppSelector(selectHasAnyRole(roles));
    if (!hasRole) {
        return (<>
        {fallback ?? (<EmptyState title="Недостаточно прав" description="Ваша роль не позволяет выполнить это действие."/>)}
      </>);
    }
    return <>{children}</>;
};
