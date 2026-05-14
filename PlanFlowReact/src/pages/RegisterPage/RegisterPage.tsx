import { useState } from 'react';
import { Link, Navigate, useLocation } from 'react-router-dom';
import { Controller, useForm, type FieldPath } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import { useAppDispatch, useAppSelector } from '@/store';
import { registerThunk } from '@/store/slices/auth/authSlice';
import { selectCurrentUser, selectIsAuthenticated } from '@/store/slices/auth/selectors';
import { Button, Card, ErrorMessage, Input, PageLayout, Select, type SelectOption, } from '@/components/ui';
import { type AppApiError, UserRole } from '@/types';
import { getPostLoginPath } from '@/utils/postLoginPath';
import { PATHS } from '../paths';
function ageCompletedYears(birthIso: string): number {
    const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(birthIso);
    if (!m)
        return NaN;
    const y = Number(m[1]);
    const mo = Number(m[2]);
    const d = Number(m[3]);
    const birth = new Date(y, mo - 1, d);
    if (Number.isNaN(birth.getTime()))
        return NaN;
    const today = new Date();
    let age = today.getFullYear() - birth.getFullYear();
    const dm = today.getMonth() - birth.getMonth();
    if (dm < 0 || (dm === 0 && today.getDate() < birth.getDate())) {
        age -= 1;
    }
    return age;
}
const registerSchema = z.object({
    username: z.string().trim().min(1, 'Введите логин'),
    password: z
        .string()
        .min(8, 'Пароль не короче 8 символов')
        .max(128, 'Пароль не длиннее 128 символов'),
    email: z.string().trim().email('Укажите корректный email'),
    fullName: z.string().trim().min(1, 'Введите ФИО'),
    birthDate: z
        .string()
        .min(1, 'Укажите дату рождения')
        .refine((s) => /^\d{4}-\d{2}-\d{2}$/.test(s), 'Формат ГГГГ-ММ-ДД')
        .refine((s) => {
        const a = ageCompletedYears(s);
        return Number.isFinite(a);
    }, 'Некорректная дата')
        .refine((s) => ageCompletedYears(s) >= 14, 'Регистрация с 14 лет')
        .refine((s) => ageCompletedYears(s) <= 120, 'Проверьте дату рождения'),
    role: z.preprocess((val) => (val === '' || val === undefined ? undefined : val), z.enum([UserRole.ORGANIZER, UserRole.COORDINATOR, UserRole.PARTICIPANT], {
        errorMap: () => ({ message: 'Выберите роль' }),
    })),
});
type RegisterFormInput = z.input<typeof registerSchema>;
type RegisterFormOutput = z.output<typeof registerSchema>;
const ROLE_OPTIONS: SelectOption<UserRole>[] = [
    { value: UserRole.ORGANIZER, label: 'Организатор' },
    { value: UserRole.COORDINATOR, label: 'Координатор' },
    { value: UserRole.PARTICIPANT, label: 'Исполнитель' },
];
interface FromState {
    from?: string;
}
function applyServerFieldErrors(setError: (name: FieldPath<RegisterFormInput>, error: {
    message: string;
}) => void, fieldErrors: Record<string, string>) {
    for (const [key, message] of Object.entries(fieldErrors)) {
        if (key === '_global')
            continue;
        const k = key as FieldPath<RegisterFormInput>;
        setError(k, { message });
    }
}
export const RegisterPage = () => {
    const dispatch = useAppDispatch();
    const location = useLocation();
    const isAuthenticated = useAppSelector(selectIsAuthenticated);
    const user = useAppSelector(selectCurrentUser);
    const [bannerMessage, setBannerMessage] = useState<string | undefined>();
    const { register, control, handleSubmit, setError, clearErrors, formState: { errors, isSubmitting }, } = useForm<RegisterFormInput, unknown, RegisterFormOutput>({
        defaultValues: {
            username: '',
            password: '',
            email: '',
            fullName: '',
            birthDate: '',
            role: UserRole.PARTICIPANT,
        },
        mode: 'onSubmit',
        resolver: zodResolver(registerSchema),
    });
    const fromState = (location.state as FromState | null) ?? null;
    if (isAuthenticated && user) {
        const to = fromState?.from && fromState.from !== PATHS.register
            ? fromState.from
            : getPostLoginPath(user.roles);
        return <Navigate to={to} replace/>;
    }
    const onSubmit = handleSubmit(async (values) => {
        clearErrors();
        setBannerMessage(undefined);
        try {
            await dispatch(registerThunk({
                username: values.username,
                password: values.password,
                email: values.email,
                fullName: values.fullName,
                birthDate: values.birthDate,
                role: values.role,
            })).unwrap();
        }
        catch (raw) {
            const err = raw as AppApiError;
            if (err.fieldErrors) {
                applyServerFieldErrors(setError, err.fieldErrors);
                const globalMsg = err.fieldErrors._global;
                if (globalMsg)
                    setBannerMessage(globalMsg);
            }
            else {
                setBannerMessage(err.message);
            }
        }
    });
    return (<PageLayout>
      <Box sx={{ mx: 'auto', display: 'flex', width: '100%', maxWidth: 448, flexDirection: 'column', gap: 3, py: 6 }}>
        <Box sx={{ textAlign: 'center' }}>
          <Typography variant="h4" sx={{ fontWeight: 700 }} color="text.primary">
            Регистрация
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            Создайте аккаунт и выберите роль в системе.
          </Typography>
        </Box>
        <Card>
          <form className="flex flex-col gap-4" onSubmit={onSubmit} noValidate>
            <Input label="Логин" autoComplete="username" placeholder="Введите логин" error={errors.username?.message} {...register('username')}/>
            <Input label="Пароль" type="password" autoComplete="new-password" placeholder="Введите пароль" error={errors.password?.message} {...register('password')}/>
            <Input label="Email" type="email" autoComplete="email" placeholder="you@example.com" error={errors.email?.message} {...register('email')}/>
            <Input label="ФИО" autoComplete="name" placeholder="Иванов Иван Иванович" error={errors.fullName?.message} {...register('fullName')}/>
            <Input label="Дата рождения" type="date" autoComplete="bday" error={errors.birthDate?.message} {...register('birthDate')}/>
            <Controller control={control} name="role" render={({ field }) => (<Select<UserRole> label="Роль" options={ROLE_OPTIONS} error={errors.role?.message} value={field.value as UserRole} onChange={field.onChange} onBlur={field.onBlur} name={field.name} ref={field.ref}/>)}/>
            {bannerMessage ? <ErrorMessage message={bannerMessage}/> : null}
            <Button type="submit" loading={isSubmitting} block>
              Зарегистрироваться
            </Button>
            <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center' }}>
              Уже есть аккаунт?{' '}
              <Link to={PATHS.login} className="font-medium text-highlight underline-offset-4 hover:underline">
                Войти
              </Link>
            </Typography>
          </form>
        </Card>
      </Box>
    </PageLayout>);
};
