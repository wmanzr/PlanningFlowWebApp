import { useEffect, useLayoutEffect, useState, type ReactNode } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import Box from '@mui/material/Box';
import Drawer from '@mui/material/Drawer';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';
import MenuIcon from '@mui/icons-material/Menu';
import PersonIcon from '@mui/icons-material/Person';
import { useTheme } from '@mui/material/styles';
import useMediaQuery from '@mui/material/useMediaQuery';
import { useAppSelector } from '@/store';
import { selectCurrentUser } from '@/store/slices/auth/selectors';
import { getPostLoginPath } from '@/utils/postLoginPath';
import { PATHS } from '../paths';
import { AppSidebar } from './AppSidebar';
const DRAWER_WIDTH = 256;
export const ShellLayout = ({ children }: {
    children: ReactNode;
}) => {
    const [mobileOpen, setMobileOpen] = useState(false);
    const user = useAppSelector(selectCurrentUser);
    const location = useLocation();
    const brandTo = user?.roles?.length ? getPostLoginPath(user.roles) : PATHS.home;
    const theme = useTheme();
    const isDesktop = useMediaQuery(theme.breakpoints.up('lg'));
    useEffect(() => {
        setMobileOpen(false);
    }, [location.pathname]);
    useLayoutEffect(() => {
        window.scrollTo(0, 0);
    }, [location.pathname, location.search]);
    return (<Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      {isDesktop && (<Drawer variant="permanent" sx={{
                width: DRAWER_WIDTH,
                flexShrink: 0,
                '& .MuiDrawer-paper': {
                    width: DRAWER_WIDTH,
                    boxSizing: 'border-box',
                    borderRight: 1,
                    borderColor: 'divider',
                },
            }}>
          <AppSidebar onNavigate={() => setMobileOpen(false)}/>
        </Drawer>)}

      {!isDesktop && (<Drawer variant="temporary" open={mobileOpen} onClose={() => setMobileOpen(false)} ModalProps={{ keepMounted: true }} sx={{
                '& .MuiDrawer-paper': {
                    width: 'min(19rem, 92vw)',
                    boxSizing: 'border-box',
                },
            }}>
          <AppSidebar onNavigate={() => setMobileOpen(false)}/>
        </Drawer>)}

      <Box sx={{ display: 'flex', flexDirection: 'column', flex: 1, minWidth: 0, minHeight: '100vh' }}>
        {!isDesktop && (<AppBar position="sticky" elevation={0} sx={{
                borderBottom: 1,
                borderColor: 'divider',
                backdropFilter: 'blur(8px)',
            }}>
            <Toolbar variant="dense" sx={{ gap: 1 }}>
              <IconButton edge="start" onClick={() => setMobileOpen(true)} aria-label="Открыть меню" sx={{ color: 'text.primary' }}>
                <MenuIcon />
              </IconButton>
              <Typography variant="h6" sx={{
                flex: 1,
                fontWeight: 700,
                color: 'primary.main',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                textDecoration: 'none',
            }} component={NavLink} to={brandTo}>
                PlanFlow
              </Typography>
              <IconButton component={NavLink} to={PATHS.profile} aria-label="Профиль" sx={{ color: 'text.primary' }}>
                <PersonIcon />
              </IconButton>
            </Toolbar>
          </AppBar>)}

        <Box component="main" sx={{ flex: 1, overflowX: 'hidden' }}>
          {children}
        </Box>
      </Box>
    </Box>);
};
