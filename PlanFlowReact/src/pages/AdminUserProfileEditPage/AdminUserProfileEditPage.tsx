import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useNavigate, useParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { fetchUserByIdThunk, updateUserProfileThunk, usersActions, } from '@/store/slices/users/usersSlice';
import { selectUserById, selectUsersActionMeta } from '@/store/slices/users/selectors';
import { toastsActions } from '@/store/slices/toasts/toastsSlice';
import type { AppApiError } from '@/types';
import { asUserId } from '@/types';
import { Button, Card, CardHeader, ErrorMessage, Input, LoadingArea, PageLayout, formatDate, } from '@/components/ui';
import { PATHS } from '../paths';
import { NotFoundPage } from '../NotFoundPage';
import { validationErrorsToToastMessage } from '@/utils/validationErrorsToToastMessage';
const FULLNAME_MIN_LENGTH = 2;
const FULLNAME_MAX_LENGTH = 200;
const schema = z.object({
    fullName: z
        .string()
        .trim()
        .min(FULLNAME_MIN_LENGTH, `Минимум ${FULLNAME_MIN_LENGTH} символа`)
        .max(FULLNAME_MAX_LENGTH),
});
type FormValues = z.infer<typeof schema>;
export const AdminUserProfileEditPage = () => {
    const { userId: paramId } = useParams<{
        userId: string;
    }>();
    const navigate = useNavigate();
    const dispatch = useAppDispatch();
    const action = useAppSelector(selectUsersActionMeta);
    const userIdNum = paramId === undefined ? NaN : Number(paramId);
    const userId = Number.isFinite(userIdNum) ? asUserId(userIdNum) : undefined;
    const user = useAppSelector(selectUserById(userId));
    const { register, reset, trigger, getValues, formState } = useForm<FormValues>({
        defaultValues: { fullName: '' },
        resolver: zodResolver(schema),
    });
    useEffect(() => {
        if (userId === undefined)
            return;
        void dispatch(fetchUserByIdThunk(userId));
    }, [dispatch, userId]);
    useEffect(() => {
        if (user) {
            reset({ fullName: user.fullName });
        }
    }, [user, reset]);
    if (paramId === undefined || userId === undefined || Number.isNaN(userIdNum)) {
        return <NotFoundPage />;
    }
    const onSave = async () => {
        const ok = await trigger();
        if (!ok || userId === undefined)
            return;
        const fullName = getValues('fullName').trim();
        dispatch(usersActions.clearActionError());
        try {
            await dispatch(updateUserProfileThunk({ id: userId, body: { fullName } })).unwrap();
            void dispatch(fetchUserByIdThunk(userId));
            dispatch(toastsActions.push({
                level: 'success',
                message: 'Изменения сохранены',
                ttlMs: 4000,
            }));
            navigate(PATHS.userDetail(userId));
        }
        catch (raw) {
            dispatch(toastsActions.push({
                level: 'error',
                message: validationErrorsToToastMessage(raw as AppApiError),
                ttlMs: 6000,
            }));
        }
    };
    return (<PageLayout title="Редактирование профиля" description={user ? `@${user.username}` : undefined} actions={<Button variant="secondary" type="button" onClick={() => navigate(PATHS.userDetail(userId))}>
          К профилю
        </Button>}>
      {action.error ? (<ErrorMessage message={action.error.message} onShown={() => dispatch(usersActions.clearActionError())}/>) : null}

      {!user ? (<LoadingArea />) : (<Card>
          <CardHeader title="Личные данные"/>
          <div className="flex max-w-md flex-col gap-4">
            <Input label="Email" value={user.email} disabled readOnly/>
            <Input label="Дата рождения" value={formatDate(user.birthDate)} disabled readOnly/>
            <Input label="Полное имя" error={formState.errors.fullName?.message} {...register('fullName')}/>
            <div className="flex justify-end gap-2 pt-2">
              <Button variant="secondary" type="button" onClick={() => navigate(PATHS.userDetail(userId))}>
                Отмена
              </Button>
              <Button type="button" onClick={() => void onSave()}>
                Сохранить
              </Button>
            </div>
          </div>
        </Card>)}
    </PageLayout>);
};
