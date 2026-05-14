import { Link, Navigate, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import { useAppDispatch, useAppSelector } from '@/store';
import { loginThunk } from '@/store/slices/auth/authSlice';
import { selectAuthError, selectAuthStatus, selectCurrentUser, selectIsAuthenticated, } from '@/store/slices/auth/selectors';
import { Button, Card, ErrorMessage, Input, PageLayout, } from '@/components/ui';
import { getPostLoginPath } from '@/utils/postLoginPath';
import { PATHS } from '../paths';
const USERNAME_MIN_LENGTH = 3;
const PASSWORD_MIN_LENGTH = 6;
const loginSchema = z.object({
    username: z
        .string()
        .trim()
        .min(USERNAME_MIN_LENGTH, `Не короче ${USERNAME_MIN_LENGTH} символов`),
    password: z
        .string()
        .min(PASSWORD_MIN_LENGTH, `Не короче ${PASSWORD_MIN_LENGTH} символов`),
});
type LoginFormValues = z.infer<typeof loginSchema>;
interface FromState {
    from?: string;
}
export const AuthPage = () => {
    const dispatch = useAppDispatch();
    const location = useLocation();
    const isAuthenticated = useAppSelector(selectIsAuthenticated);
    const user = useAppSelector(selectCurrentUser);
    const status = useAppSelector(selectAuthStatus);
    const error = useAppSelector(selectAuthError);
    const { register, handleSubmit, formState: { errors, isSubmitting }, } = useForm<LoginFormValues>({
        defaultValues: { username: '', password: '' },
        mode: 'onSubmit',
        resolver: zodResolver(loginSchema),
    });
    const fromState = (location.state as FromState | null) ?? null;
    if (isAuthenticated && user) {
        const to = fromState?.from && fromState.from !== PATHS.login ? fromState.from : getPostLoginPath(user.roles);
        return <Navigate to={to} replace/>;
    }
    const onSubmit = handleSubmit(async (values) => {
        await dispatch(loginThunk(values));
    });
    return (<PageLayout>
      <Box sx={{ mx: 'auto', display: 'flex', width: '100%', maxWidth: 448, flexDirection: 'column', gap: 3, py: 6 }}>
        <Box sx={{ textAlign: 'center' }}>
          <Typography variant="h4" sx={{ fontWeight: 700 }} color="text.primary">
            PlanFlow
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            Войдите, чтобы управлять мероприятиями и задачами.
          </Typography>
        </Box>
        <Card>
          <form className="flex flex-col gap-4" onSubmit={onSubmit} noValidate>
            <Input label="Логин" autoComplete="username" placeholder="Введите логин" error={errors.username?.message} {...register('username')}/>
            <Input label="Пароль" type="password" autoComplete="current-password" placeholder="Введите пароль" error={errors.password?.message} {...register('password')}/>
            {error ? <ErrorMessage message={error.message}/> : null}
            <Button type="submit" loading={status === 'pending' || isSubmitting} block>
              Войти
            </Button>
            <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center' }}>
              Нет аккаунта?{' '}
              <Link to={PATHS.register} className="font-medium text-highlight underline-offset-4 hover:underline">
                Зарегистрироваться
              </Link>
            </Typography>
          </form>
        </Card>
      </Box>
    </PageLayout>);
};
