import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAppSelector } from '@/store';
import { selectAuthInitialised, selectIsAuthenticated } from '@/store/slices/auth/selectors';
import { LoadingArea } from '@/components/ui';
import { PATHS } from '../paths';
export const RequireAuth = () => {
    const location = useLocation();
    const isAuthenticated = useAppSelector(selectIsAuthenticated);
    const initialised = useAppSelector(selectAuthInitialised);
    if (!initialised) {
        return <LoadingArea label="Восстанавливаем сессию"/>;
    }
    if (!isAuthenticated) {
        return <Navigate to={PATHS.login} replace state={{ from: location.pathname }}/>;
    }
    return <Outlet />;
};
