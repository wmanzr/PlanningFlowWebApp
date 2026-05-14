import { useCallback, useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useAppDispatch, useAppSelector } from '@/store';
import { usersActions, fetchUserByIdThunk, fetchUserSkillsThunk, updateUserProfileThunk, updateUserSkillsThunk, } from '@/store/slices/users/usersSlice';
import { selectUserById, selectUsersActionMeta, selectUsersSkillsState, } from '@/store/slices/users/selectors';
import { selectCurrentUser } from '@/store/slices/auth/selectors';
import { fetchSkillsThunk } from '@/store/slices/skills/skillsSlice';
import { selectAllSkills } from '@/store/slices/skills/selectors';
import { toastsActions } from '@/store/slices/toasts/toastsSlice';
import { UserRole, type AppApiError, type SkillId, type UserSkillResponseDto } from '@/types';
import { Button, Card, CardHeader, ErrorMessage, Input, LoadingArea, PageLayout, formatDate, } from '@/components/ui';
import { UserActivityReadOnly, UserSkillEditor, type SkillRowDraft } from '@/components/domain/user';
import { validationErrorsToToastMessage } from '@/utils/validationErrorsToToastMessage';
const FULLNAME_MIN_LENGTH = 2;
const FULLNAME_MAX_LENGTH = 200;
const profileSchema = z.object({
    fullName: z
        .string()
        .trim()
        .min(FULLNAME_MIN_LENGTH, `Минимум ${FULLNAME_MIN_LENGTH} символа`)
        .max(FULLNAME_MAX_LENGTH),
});
type ProfileValues = z.infer<typeof profileSchema>;
function mapSkillsToDraft(skills: UserSkillResponseDto[]): SkillRowDraft[] {
    return skills
        .filter((s): s is UserSkillResponseDto & {
        skillId: SkillId;
    } => s.skillId !== undefined)
        .map((s) => ({ skillId: s.skillId, tier: s.tier }));
}
function dedupeSkillTiers(rows: SkillRowDraft[]) {
    const map = new Map<SkillId, NonNullable<SkillRowDraft['tier']>>();
    for (const row of rows) {
        if (row.skillId === undefined || row.tier === undefined)
            continue;
        map.set(row.skillId, row.tier);
    }
    return [...map.entries()].map(([skillId, tier]) => ({ skillId, tier }));
}
function validateSkillDraft(rows: SkillRowDraft[]): Record<number, {
    skill?: string;
    tier?: string;
}> {
    const out: Record<number, {
        skill?: string;
        tier?: string;
    }> = {};
    rows.forEach((row, index) => {
        const entry: {
            skill?: string;
            tier?: string;
        } = {};
        if (row.skillId === undefined)
            entry.skill = 'Выберите навык';
        if (row.tier === undefined)
            entry.tier = 'Выберите уровень';
        if (Object.keys(entry).length > 0)
            out[index] = entry;
    });
    return out;
}
export const ProfilePage = () => {
    const dispatch = useAppDispatch();
    const currentUser = useAppSelector(selectCurrentUser);
    const detailedUser = useAppSelector(selectUserById(currentUser?.id));
    const skillsState = useAppSelector(selectUsersSkillsState);
    const action = useAppSelector(selectUsersActionMeta);
    const allSkills = useAppSelector(selectAllSkills);
    const canEditSkills = !!currentUser &&
        currentUser.roles.includes(UserRole.PARTICIPANT) &&
        !currentUser.roles.includes(UserRole.COORDINATOR) &&
        !currentUser.roles.includes(UserRole.ORGANIZER);
    const [skillDraft, setSkillDraft] = useState<SkillRowDraft[]>([]);
    const [skillRowErrors, setSkillRowErrors] = useState<Record<number, {
        skill?: string;
        tier?: string;
    }>>({});
    const [savingAll, setSavingAll] = useState(false);
    const handleSkillDraftChange = useCallback((next: SkillRowDraft[]) => {
        setSkillDraft(next);
        setSkillRowErrors({});
    }, []);
    const { register, reset, trigger, getValues, formState } = useForm<ProfileValues>({
        defaultValues: { fullName: '' },
        resolver: zodResolver(profileSchema),
    });
    useEffect(() => {
        if (currentUser) {
            void dispatch(fetchUserByIdThunk(currentUser.id));
            if (canEditSkills) {
                void dispatch(fetchUserSkillsThunk(currentUser.id));
                void dispatch(fetchSkillsThunk({ page: 1, size: 500 }));
            }
        }
    }, [dispatch, currentUser, canEditSkills]);
    useEffect(() => {
        if (detailedUser) {
            reset({ fullName: detailedUser.fullName });
        }
    }, [detailedUser, reset]);
    useEffect(() => {
        if (!currentUser ||
            !canEditSkills ||
            skillsState.status !== 'succeeded' ||
            skillsState.loadedFor !== currentUser.id) {
            return;
        }
        setSkillDraft(mapSkillsToDraft(skillsState.data));
        setSkillRowErrors({});
    }, [currentUser, canEditSkills, skillsState.status, skillsState.loadedFor, skillsState.data]);
    const onSaveAll = useCallback(async () => {
        if (!currentUser)
            return;
        const nameOk = await trigger();
        let skillsOk = true;
        if (canEditSkills) {
            const skillErr = validateSkillDraft(skillDraft);
            setSkillRowErrors(skillErr);
            skillsOk = Object.keys(skillErr).length === 0;
        }
        if (!nameOk || !skillsOk)
            return;
        const fullName = getValues('fullName').trim();
        setSavingAll(true);
        dispatch(usersActions.clearActionError());
        try {
            await dispatch(updateUserProfileThunk({ id: currentUser.id, body: { fullName } })).unwrap();
            if (canEditSkills) {
                await dispatch(updateUserSkillsThunk({
                    id: currentUser.id,
                    body: { skillTiers: dedupeSkillTiers(skillDraft) },
                })).unwrap();
                void dispatch(fetchUserSkillsThunk(currentUser.id));
            }
            void dispatch(fetchUserByIdThunk(currentUser.id));
            dispatch(toastsActions.push({
                level: 'success',
                message: 'Изменения сохранены',
                ttlMs: 4000,
            }));
        }
        catch (raw) {
            dispatch(toastsActions.push({
                level: 'error',
                message: validationErrorsToToastMessage(raw as AppApiError),
                ttlMs: 6000,
            }));
        }
        finally {
            setSavingAll(false);
        }
    }, [
        canEditSkills,
        currentUser,
        dispatch,
        getValues,
        skillDraft,
        trigger,
    ]);
    if (!currentUser) {
        return (<PageLayout title="Профиль">
        <ErrorMessage message="Сессия не активна"/>
      </PageLayout>);
    }
    return (<PageLayout title="Профиль" description={`@${currentUser.username}`}>
      {action.error ? (<ErrorMessage message={action.error.message} onShown={() => dispatch(usersActions.clearActionError())}/>) : null}

      {!detailedUser ? (<LoadingArea />) : (<>
          <div className={canEditSkills ? 'grid gap-6 lg:grid-cols-2 lg:items-start' : 'grid gap-6 lg:items-start'}>
            <Card>
              <CardHeader title="Личные данные"/>
              <div className="flex flex-col gap-4">
                <Input label="Email" value={detailedUser.email} disabled readOnly/>
                <Input label="Дата рождения" value={formatDate(detailedUser.birthDate)} disabled readOnly/>
                <Input label="Полное имя" error={formState.errors.fullName?.message} {...register('fullName')}/>
              </div>
            </Card>

            {canEditSkills ? (skillsState.status === 'pending' && skillsState.data.length === 0 ? (<LoadingArea />) : (<UserSkillEditor rows={skillDraft} onRowsChange={handleSkillDraftChange} available={allSkills} rowErrors={skillRowErrors} showFooterSave={false}/>)) : null}
          </div>

          <UserActivityReadOnly user={detailedUser} className="mt-6"/>

          <div className="mt-6 flex justify-end">
            <Button type="button" loading={savingAll} onClick={() => void onSaveAll()}>
              Сохранить
            </Button>
          </div>
        </>)}
    </PageLayout>);
};
