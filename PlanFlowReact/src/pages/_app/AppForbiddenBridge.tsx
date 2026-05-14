import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { setForbiddenHandler } from '@/api';
import { PATHS } from '../paths';
export const AppForbiddenBridge = () => {
    const navigate = useNavigate();
    useEffect(() => {
        setForbiddenHandler(() => {
            if (typeof window !== 'undefined' && window.location.pathname === PATHS.forbidden) {
                return;
            }
            navigate(PATHS.forbidden, { replace: true });
        });
        return () => setForbiddenHandler(null);
    }, [navigate]);
    return null;
};
