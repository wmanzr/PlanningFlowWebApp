import { type ReactNode } from 'react';
import Container from '@mui/material/Container';
import type { Breakpoint } from '@mui/material/styles';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import { cn } from '../cn';
export interface PageLayoutProps {
    title?: ReactNode;
    description?: ReactNode;
    actions?: ReactNode;
    children: ReactNode;
    className?: string;
    containerMaxWidth?: Breakpoint | false;
}
export const PageLayout = ({ title, description, actions, children, className, containerMaxWidth = 'lg', }: PageLayoutProps) => (<Container maxWidth={containerMaxWidth === false ? false : containerMaxWidth} className={cn(className)} sx={{
        display: 'flex',
        flexDirection: 'column',
        gap: { xs: 2.5, sm: 3 },
        py: { xs: 3, sm: 4 },
        px: { xs: 1.5, sm: 2.5, lg: 4 },
        minWidth: 0,
    }}>
    {(title ?? actions ?? description) ? (<Box component="header" sx={{
            display: 'flex',
            flexDirection: { xs: 'column', sm: 'row' },
            gap: 1.5,
            alignItems: { sm: 'flex-end' },
            justifyContent: 'space-between',
        }}>
        <div>
          {title ? (<Typography variant="h5" sx={{ fontWeight: 700 }} color="text.primary">
              {title}
            </Typography>) : null}
          {description ? (<Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              {description}
            </Typography>) : null}
        </div>
        {actions ? <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>{actions}</Box> : null}
      </Box>) : null}
    <Box sx={{ display: 'flex', flexDirection: 'column', flex: 1, gap: 3, minWidth: 0 }}>
      {children}
    </Box>
  </Container>);
