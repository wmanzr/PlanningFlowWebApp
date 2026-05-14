import { type ReactNode } from 'react';
import Box from '@mui/material/Box';
export interface AppShellProps {
    topbar: ReactNode;
    sidebar?: ReactNode;
    children: ReactNode;
}
export const AppShell = ({ topbar, sidebar, children }: AppShellProps) => (<Box sx={{ display: 'flex', minHeight: '100vh', flexDirection: 'column', bgcolor: 'background.default', color: 'text.secondary' }}>
    <Box sx={{ position: 'sticky', top: 0, zIndex: 30, borderBottom: 1, borderColor: 'divider', bgcolor: 'background.default', backdropFilter: 'blur(8px)' }}>
      {topbar}
    </Box>
    <Box sx={{
        display: 'flex',
        flex: 1,
        flexDirection: sidebar ? { xs: 'column', md: 'row' } : 'column',
    }}>
      {sidebar ? (<Box component="aside" sx={{
            borderBottom: { xs: 1, md: 0 },
            borderRight: { md: 1 },
            borderColor: 'divider',
            bgcolor: 'background.default',
            width: { md: 256 },
            flexShrink: 0,
        }}>
          {sidebar}
        </Box>) : null}
      <Box component="main" sx={{ flex: 1 }}>
        {children}
      </Box>
    </Box>
  </Box>);
