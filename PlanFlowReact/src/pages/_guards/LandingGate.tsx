import { Navigate } from 'react-router-dom';
import { useAppSelector } from '@/store';
import { selectAuthInitialised, selectCurrentUser, selectIsAuthenticated } from '@/store/slices/auth/selectors';
import { getPostLoginPath } from '@/utils/postLoginPath';
import { LoadingArea } from '@/components/ui';
import { LandingPage } from '../LandingPage/LandingPage';

export const LandingGate = () => {
    const initialised = useAppSelector(selectAuthInitialised);
    const isAuthenticated = useAppSelector(selectIsAuthenticated);
    const user = useAppSelector(selectCurrentUser);
    if (!initialised) {
        return <LoadingArea label="Загрузка"/>;
    }
    if (isAuthenticated && user) {
        return <Navigate to={getPostLoginPath(user.roles)} replace/>;
    }
    return <LandingPage />;
};
