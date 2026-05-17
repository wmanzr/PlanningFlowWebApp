import { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { fetchUserByIdThunk, fetchUserSkillsThunk, fetchUserViewerContextThunk, } from '@/store/slices/users/usersSlice';
import { selectUserById, selectUsersSkillsState, selectUsersViewerContextState, } from '@/store/slices/users/selectors';
import { selectCurrentUser } from '@/store/slices/auth/selectors';
import { UserActivityReadOnly } from '@/components/domain/user';
import { Badge, Button, Card, CardHeader, EmailLink, EmptyState, ErrorMessage, LoadingArea, PageLayout, formatDateTime, } from '@/components/ui';
import { SkillTier, UserRole, asEventId, asUserId, type UserRole as UserRoleType } from '@/types';
import { NotFoundPage } from '../NotFoundPage';
import { PATHS } from '../paths';
const ROLE_LABELS: Record<UserRoleType, string> = {
    [UserRole.ORGANIZER]: 'Организатор',
    [UserRole.COORDINATOR]: 'Координатор',
    [UserRole.PARTICIPANT]: 'Исполнитель',
    [UserRole.ADMIN]: 'Администратор',
};
const TIER_LABELS: Record<SkillTier, string> = {
    [SkillTier.NOVICE]: 'Новичок',
    [SkillTier.PRACTITIONER]: 'Практик',
    [SkillTier.EXPERT]: 'Эксперт',
};
export const UserDetailPage = () => {
    const { userId: paramId } = useParams<{
        userId: string;
    }>();
    const navigate = useNavigate();
    const dispatch = useAppDispatch();
    const sessionRoles = useAppSelector(selectCurrentUser)?.roles;
    const sessionIsAdmin = sessionRoles?.includes(UserRole.ADMIN) ?? false;
    const sessionIsOrganizer = sessionRoles?.includes(UserRole.ORGANIZER) ?? false;
    const sessionIsCoordinator = sessionRoles?.includes(UserRole.COORDINATOR) ?? false;
    const plannerLikeSession = sessionIsAdmin || sessionIsOrganizer || sessionIsCoordinator;
    const userIdNum = paramId === undefined ? NaN : Number(paramId);
    const userId = Number.isFinite(userIdNum) ? asUserId(userIdNum) : undefined;
    const user = useAppSelector(selectUserById(userId));
    const skillsState = useAppSelector(selectUsersSkillsState);
    const viewerCtx = useAppSelector(selectUsersViewerContextState);
    useEffect(() => {
        if (userId === undefined)
            return;
        void dispatch(fetchUserByIdThunk(userId));
        void dispatch(fetchUserViewerContextThunk(userId));
    }, [dispatch, userId]);
    const subjectIsParticipant = !!user?.roles.includes(UserRole.PARTICIPANT);
    useEffect(() => {
        if (userId === undefined)
            return;
        if (!subjectIsParticipant)
            return;
        void dispatch(fetchUserSkillsThunk(userId));
    }, [dispatch, userId, subjectIsParticipant]);
    const skillsReady = userId !== undefined && skillsState.loadedFor === userId && skillsState.status !== 'pending';
    if (paramId === undefined || userId === undefined || Number.isNaN(userIdNum)) {
        return <NotFoundPage />;
    }
    const ctxOk = viewerCtx.loadedFor === userId && viewerCtx.status === 'succeeded';
    return (<PageLayout title={user?.fullName ?? 'Пользователь'} description={user ? `@${user.username}` : undefined} actions={sessionIsAdmin && userId !== undefined ? (<Button variant="secondary" type="button" onClick={() => navigate(PATHS.userProfileEdit(userId))}>
            Редактировать профиль
          </Button>) : undefined}>
      {skillsState.error ? <ErrorMessage message={skillsState.error.message}/> : null}
      {viewerCtx.error ? <ErrorMessage message={viewerCtx.error.message}/> : null}

      {!user ? <LoadingArea /> : null}

      <div className="flex flex-col gap-6">
        {user ? (<Card>
            <CardHeader title="Контакт"/>
            <div className="flex flex-col gap-2 text-sm">
              <EmailLink email={user.email} className="text-sm font-normal text-paragraph"/>
              <div className="flex flex-wrap gap-1.5">
                {user.roles.map((r) => (<Badge key={r} tone="neutral" outline>
                    {ROLE_LABELS[r] ?? r}
                  </Badge>))}
              </div>
            </div>
          </Card>) : null}

        {user && subjectIsParticipant ? (<>
            {skillsState.status === 'pending' && skillsState.loadedFor !== userId ? <LoadingArea /> : null}
            {skillsReady && skillsState.data.length > 0 ? (<Card padded={false}>
                <div className="border-b border-secondary/40 px-5 py-4">
                  <CardHeader title="Навыки" subtitle="Уровни для подбора на задачи и учета компетенций."/>
                </div>
                <ul className="divide-y divide-stroke">
                  {skillsState.data.map((s) => (<li key={s.userSkillId} className="flex justify-between gap-4 px-5 py-3 text-sm">
                      <span className="text-headline">{s.skillName ?? 'Навык'}</span>
                      <Badge tone="neutral" outline>
                        {TIER_LABELS[s.tier] ?? s.tier}
                      </Badge>
                    </li>))}
                </ul>
              </Card>) : null}
            {skillsReady && skillsState.data.length === 0 ? (<Card padded={false}>
                <div className="px-5 py-4">
                  <CardHeader title="Навыки" subtitle="Уровни для подбора на задачи и учета компетенций."/>
                </div>
                <div className="px-5 pb-5">
                  <EmptyState title="Навыки не указаны"/>
                </div>
              </Card>) : null}
          </>) : null}

        {user ? <UserActivityReadOnly user={user}/> : null}

        {viewerCtx.status === 'pending' && viewerCtx.loadedFor !== userId ? <LoadingArea /> : null}

      {ctxOk && viewerCtx.data ? (<>
          {plannerLikeSession && subjectIsParticipant ? (<Card>
              <CardHeader title="Участие в задачах"/>
              <div className="grid gap-2 text-sm">
                <div className="flex items-center justify-between gap-3">
                  <span className="text-paragraph">В ваших мероприятиях</span>
                  <span className="font-medium text-headline">
                    {viewerCtx.data.participantEventsUnderViewerCount ?? '—'}
                  </span>
                </div>
                <div className="flex items-center justify-between gap-3">
                  <span className="text-paragraph">Всего</span>
                  <span className="font-medium text-headline">
                    {viewerCtx.data.participantEventsTotalCount ?? '—'}
                  </span>
                </div>
              </div>
            </Card>) : null}

          {(sessionIsOrganizer || sessionIsAdmin) ? (<>
              {viewerCtx.data.organizerCoordinatorEvents.length > 0 ? (<Card padded={false}>
                  <CardHeader title="Координатор ваших мероприятий (активные и в планировании)"/>
                  <ul className="divide-y divide-stroke">
                    {viewerCtx.data.organizerCoordinatorEvents.map((ev) => (<li key={ev.eventId} className="px-5 py-4">
                        <Link className="font-medium text-headline hover:underline" to={PATHS.eventDetail(asEventId(ev.eventId))}>
                          {ev.title}
                        </Link>
                        <div className="mt-1 flex flex-wrap gap-2 text-sm text-paragraph">
                          <Badge tone="neutral" outline>
                            {ev.status}
                          </Badge>
                          <span>{formatDateTime(ev.startDate)}</span>
                          <span>—</span>
                          <span>{formatDateTime(ev.endDate)}</span>
                        </div>
                      </li>))}
                  </ul>
                </Card>) : null}

              {user?.roles.includes(UserRole.COORDINATOR) ? (<Card>
                  <CardHeader title="Мероприятия под управлением"/>
                  <div className="grid gap-2 text-sm">
                    <div className="flex items-center justify-between gap-3">
                      <span className="text-paragraph">В ваших мероприятиях</span>
                      <span className="font-medium text-headline">
                        {viewerCtx.data.coordinatorEventsUnderOrganizerCount ?? '—'}
                      </span>
                    </div>
                    <div className="flex items-center justify-between gap-3">
                      <span className="text-paragraph">Всего</span>
                      <span className="font-medium text-headline">
                        {viewerCtx.data.coordinatorEventsTotalCount ?? '—'}
                      </span>
                    </div>
                  </div>
                </Card>) : null}

              {viewerCtx.data.completedEventsAsOrganizerCount !== null ? (<Card>
                  <CardHeader title="Завершенные мероприятия"/>
                  <p className="text-lg font-medium text-headline">
                    {viewerCtx.data.completedEventsAsOrganizerCount}
                  </p>
                </Card>) : null}
            </>) : null}

          {!sessionIsAdmin && !sessionIsOrganizer && viewerCtx.data.organizerCoordinatorEvents.length > 0 ? (<Card>
              <CardHeader title="Совместные мероприятия"/>
              <ul className="flex flex-col gap-4">
                {viewerCtx.data.organizerCoordinatorEvents.map((ev) => (<li key={ev.eventId}>
                    <Link className="font-medium text-headline hover:underline" to={PATHS.eventDetail(asEventId(ev.eventId))}>
                      {ev.title}
                    </Link>
                    <div className="mt-1 flex flex-wrap gap-2 text-sm text-paragraph">
                      <Badge tone="neutral" outline>
                        {ev.status}
                      </Badge>
                      <span>{formatDateTime(ev.startDate)}</span>
                      <span>—</span>
                      <span>{formatDateTime(ev.endDate)}</span>
                    </div>
                  </li>))}
              </ul>
            </Card>) : null}
        </>) : null}
      </div>
    </PageLayout>);
};
