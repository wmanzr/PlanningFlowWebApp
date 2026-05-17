import { Navigate, type RouteObject } from 'react-router-dom';
import { UserRole } from '@/types';
import { AuthPage } from './AuthPage';
import { RegisterPage } from './RegisterPage/RegisterPage';
import { EventsListPage } from './EventsListPage';
import { EventDetailPage } from './EventDetailPage';
import { EventDashboardPage } from './EventDashboardPage';
import { EventTasksPage } from './EventTasksPage/EventTasksPage';
import { TaskDetailPage } from './TaskDetailPage';
import { TaskMatchingPage } from './TaskMatchingPage';
import { TaskBookingsPage } from './TaskBookingsPage';
import { IncidentsPage } from './IncidentsPage';
import { IncidentDetailPage } from './IncidentDetailPage/IncidentDetailPage';
import { ResourcesPage } from './ResourcesPage';
import { SkillsPage } from './SkillsPage';
import { MyTasksPage } from './MyTasksPage';
import { ProfilePage } from './ProfilePage';
import { UsersDirectoryPage } from './UsersDirectoryPage';
import { UserDetailPage } from './UserDetailPage';
import { AdminUserProfileEditPage } from './AdminUserProfileEditPage';
import { NotificationsPage } from './NotificationsPage';
import { ForbiddenPage } from './ForbiddenPage/ForbiddenPage';
import { NotFoundPage } from './NotFoundPage';
import { RequireAuth } from './_guards/RequireAuth';
import { RequireRole } from './_guards/RequireRole';
import { SmartHomeRedirect } from './_guards/SmartHomeRedirect';
import { AuthenticatedLayout } from './_layout/AuthenticatedLayout';
import { LandingGate } from './_guards/LandingGate';
import { PATHS } from './paths';
export const buildRoutes = (): RouteObject[] => [
    {
        path: PATHS.landing,
        element: <LandingGate />,
    },
    {
        path: PATHS.auth,
        element: <AuthPage />,
    },
    {
        path: PATHS.login,
        element: <AuthPage />,
    },
    {
        path: PATHS.register,
        element: <RegisterPage />,
    },
    {
        element: <RequireAuth />,
        children: [
            {
                element: <AuthenticatedLayout />,
                children: [
                    {
                        path: PATHS.home,
                        element: (<RequireRole roles={[UserRole.ADMIN, UserRole.ORGANIZER, UserRole.COORDINATOR]} fallback={<Navigate to={PATHS.myTasks} replace/>}>
                <EventsListPage />
              </RequireRole>),
                    },
                    { path: '/events/:eventId', element: <EventDetailPage /> },
                    { path: '/events/:eventId/dashboard', element: <EventDashboardPage /> },
                    { path: '/events/:eventId/incidents', element: <IncidentsPage /> },
                    { path: '/incidents/:incidentId', element: <IncidentDetailPage /> },
                    { path: '/events/:eventId/tasks', element: <EventTasksPage /> },
                    { path: '/events/:eventId/tasks/:taskId', element: <TaskDetailPage /> },
                    {
                        path: '/events/:eventId/tasks/:taskId/matching',
                        element: <TaskMatchingPage />,
                    },
                    {
                        path: '/events/:eventId/tasks/:taskId/bookings',
                        element: <TaskBookingsPage />,
                    },
                    { path: PATHS.resources, element: <ResourcesPage /> },
                    {
                        path: PATHS.usersDirectory,
                        element: (<RequireRole roles={[UserRole.ORGANIZER, UserRole.COORDINATOR, UserRole.ADMIN]} fallback={<Navigate to={PATHS.forbidden} replace/>}>
                <UsersDirectoryPage />
              </RequireRole>),
                    },
                    { path: '/users/:userId', element: <UserDetailPage /> },
                    {
                        path: '/users/:userId/edit',
                        element: (<RequireRole roles={[UserRole.ADMIN]} fallback={<Navigate to={PATHS.forbidden} replace/>}>
                <AdminUserProfileEditPage />
              </RequireRole>),
                    },
                    {
                        path: PATHS.notifications,
                        element: <NotificationsPage />,
                    },
                    {
                        path: PATHS.skills,
                        element: (<RequireRole roles={[UserRole.ORGANIZER, UserRole.PARTICIPANT, UserRole.ADMIN]} fallback={<Navigate to={PATHS.forbidden} replace/>}>
                <SkillsPage />
              </RequireRole>),
                    },
                    { path: PATHS.myTasks, element: <MyTasksPage /> },
                    { path: PATHS.profile, element: <ProfilePage /> },
                    { path: PATHS.forbidden, element: <ForbiddenPage /> },
                    { path: '/dashboard', element: <SmartHomeRedirect /> },
                    { path: '*', element: <NotFoundPage /> },
                ],
            },
        ],
    },
];
