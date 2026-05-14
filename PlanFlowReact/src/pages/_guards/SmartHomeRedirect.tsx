import { Navigate } from 'react-router-dom';
import { useAppSelector } from '@/store';
import { selectCurrentUser } from '@/store/slices/auth/selectors';
import { getPostLoginPath } from '@/utils/postLoginPath';
import { PATHS } from '../paths';
export const SmartHomeRedirect = () => {
    const user = useAppSelector(selectCurrentUser);
    if (!user) {
        return <Navigate to={PATHS.login} replace/>;
    }
    return <Navigate to={getPostLoginPath(user.roles)} replace/>;
};
