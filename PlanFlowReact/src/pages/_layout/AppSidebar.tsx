import type { ReactNode } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import List from '@mui/material/List';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import Typography from '@mui/material/Typography';
import Divider from '@mui/material/Divider';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import MuiBadge from '@mui/material/Badge';
import { useTheme } from '@mui/material/styles';
import useMediaQuery from '@mui/material/useMediaQuery';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import LightModeIcon from '@mui/icons-material/LightMode';
import LogoutIcon from '@mui/icons-material/Logout';
import PersonIcon from '@mui/icons-material/Person';
import EventIcon from '@mui/icons-material/Event';
import AssignmentIcon from '@mui/icons-material/Assignment';
import InventoryIcon from '@mui/icons-material/Inventory';
import GroupIcon from '@mui/icons-material/Group';
import SchoolIcon from '@mui/icons-material/School';
import NotificationsNoneIcon from '@mui/icons-material/NotificationsNone';
import { useAppDispatch, useAppSelector } from '@/store';
import { authActions } from '@/store/slices/auth/authSlice';
import { selectCurrentUser } from '@/store/slices/auth/selectors';
import { selectTheme } from '@/store/slices/ui/selectors';
import { uiActions } from '@/store/slices/ui/uiSlice';
import { Button } from '@/components/ui';
import { UserRole } from '@/types';
import { getPostLoginPath, isParticipantOnlyRole } from '@/utils/postLoginPath';
import { PATHS } from '../paths';
import { selectUnreadNotificationsCount } from '@/store/slices/notifications/selectors';
type Item = {
    to: string;
    label: string;
    icon: ReactNode;
    end?: boolean;
};
const ROLE_LABEL: Record<UserRole, string> = {
    [UserRole.ORGANIZER]: 'Организатор',
    [UserRole.COORDINATOR]: 'Координатор',
    [UserRole.PARTICIPANT]: 'Участник',
    [UserRole.ADMIN]: 'Администратор',
};
function formatRolesShort(roles: UserRole[] | undefined): string | null {
    if (!roles?.length)
        return null;
    const labels = [...new Set(roles)].map((r) => ROLE_LABEL[r] ?? r);
    return labels.join(' · ');
}
function buildItems(roles: UserRole[] | undefined): Item[] {
    if (!roles?.length)
        return [];
    const isAdmin = roles.includes(UserRole.ADMIN);
    const canListUsers = roles.includes(UserRole.ORGANIZER) ||
        roles.includes(UserRole.COORDINATOR) ||
        roles.includes(UserRole.ADMIN);
    const organizerOnly = roles.includes(UserRole.ORGANIZER) &&
        !roles.includes(UserRole.COORDINATOR) &&
        !roles.includes(UserRole.ADMIN);
    const coordinatorOnly = roles.includes(UserRole.COORDINATOR) &&
        !roles.includes(UserRole.ORGANIZER) &&
        !roles.includes(UserRole.ADMIN);
    const participantOnly = isParticipantOnlyRole(roles);
    const core: Item[] = [
        { to: PATHS.profile, label: 'Профиль', icon: <PersonIcon fontSize="small"/>, end: true },
        { to: PATHS.notifications, label: 'Уведомления', icon: <NotificationsNoneIcon fontSize="small"/> },
    ];
    if (participantOnly) {
        core.push({
            to: PATHS.myTasks,
            label: 'Задачи',
            icon: <AssignmentIcon fontSize="small"/>,
            end: true,
        });
    }
    else {
        core.push({
            to: PATHS.home,
            label: coordinatorOnly ? 'Управление' : 'Мероприятия',
            icon: <EventIcon fontSize="small"/>,
            end: true,
        });
    }
    if (!isAdmin && !organizerOnly && !coordinatorOnly && !participantOnly) {
        core.push({ to: PATHS.myTasks, label: 'Задачи', icon: <AssignmentIcon fontSize="small"/> });
    }
    if (!participantOnly) {
        core.push({ to: PATHS.resources, label: 'Ресурсы', icon: <InventoryIcon fontSize="small"/> });
    }
    if (canListUsers) {
        core.push({ to: PATHS.usersDirectory, label: 'Участники', icon: <GroupIcon fontSize="small"/> });
    }
    if (!organizerOnly && !coordinatorOnly && !participantOnly) {
        core.push({ to: PATHS.skills, label: 'Навыки', icon: <SchoolIcon fontSize="small"/> });
    }
    return core;
}
export interface AppSidebarProps {
    onNavigate?: () => void;
}
export const AppSidebar = ({ onNavigate }: AppSidebarProps) => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const user = useAppSelector(selectCurrentUser);
    const unreadCount = useAppSelector(selectUnreadNotificationsCount);
    const themeMode = useAppSelector(selectTheme);
    const muiTheme = useTheme();
    const isDesktop = useMediaQuery(muiTheme.breakpoints.up('lg'));
    const items = buildItems(user?.roles);
    const rolesLine = formatRolesShort(user?.roles);
    const brandTo = user?.roles?.length ? getPostLoginPath(user.roles) : PATHS.home;
    const handleLogout = () => {
        dispatch(authActions.logout());
        navigate(PATHS.login, { replace: true });
        onNavigate?.();
    };
    return (<Box sx={{ display: 'flex', flexDirection: 'column', height: '100%', minHeight: 0, bgcolor: 'background.paper' }}>
      {isDesktop && (<>
          <Box sx={{ px: 2.5, py: 2.5, borderBottom: 1, borderColor: 'divider' }}>
            <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main', textDecoration: 'none', display: 'block' }} component={NavLink} to={brandTo} onClick={onNavigate}>
              PlanFlow
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block' }}>
              Планирование мероприятий
            </Typography>
          </Box>
        </>)}

      <List component="nav" aria-label="Основное меню" sx={{ flex: 1, minHeight: 0, overflowY: 'auto', py: 1, px: 1 }}>
        {items.map((item) => (<ListItemButton key={item.to} component={NavLink} to={item.to} end={item.end ?? false} onClick={onNavigate} sx={{
                borderRadius: 2,
                mb: 0.5,
                '&.active': {
                    bgcolor: `${muiTheme.app.highlight}26`,
                    color: 'text.primary',
                    boxShadow: `inset 0 0 0 1px ${muiTheme.app.highlight}40`,
                },
                '&:not(.active)': {
                    color: 'text.secondary',
                    '&:hover': { bgcolor: `${muiTheme.app.secondary}80`, color: 'text.primary' },
                },
            }}>
            <ListItemIcon sx={{ minWidth: 36, color: 'inherit' }}>
              {item.to === PATHS.notifications ? (<MuiBadge color="error" overlap="circular" badgeContent={unreadCount > 9 ? '9+' : unreadCount} invisible={unreadCount <= 0}>
                  {item.icon}
                </MuiBadge>) : (item.icon)}
            </ListItemIcon>
            <ListItemText primary={item.label} slotProps={{ primary: { sx: { fontSize: '0.875rem', fontWeight: 500 } } }}/>
            {item.to === PATHS.profile && rolesLine ? (<Typography variant="caption" color="text.secondary" noWrap sx={{ maxWidth: 120, textAlign: 'right' }}>
                {rolesLine}
              </Typography>) : null}
          </ListItemButton>))}
      </List>

      <Divider />
      <Box sx={{ p: 1.5, bgcolor: 'action.hover', display: 'flex', alignItems: 'center', gap: 1 }}>
        <Tooltip title={themeMode === 'light' ? 'Темная тема' : 'Светлая тема'}>
          <IconButton onClick={() => dispatch(uiActions.toggleTheme())} size="small" sx={{ color: 'warning.main' }}>
            {themeMode === 'light' ? <DarkModeIcon /> : <LightModeIcon />}
          </IconButton>
        </Tooltip>
        <Button variant="secondary" size="sm" block onClick={handleLogout} className="flex-1">
          <LogoutIcon sx={{ fontSize: 20, mr: 0.5 }}/>
          Выйти
        </Button>
      </Box>
    </Box>);
};
