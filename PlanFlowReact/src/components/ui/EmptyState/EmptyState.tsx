import { type ReactNode } from 'react';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
export interface EmptyStateProps {
    title: ReactNode;
    description?: ReactNode;
    action?: ReactNode;
}
export const EmptyState = ({ title, description, action }: EmptyStateProps) => (<Paper variant="outlined" sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 1.5,
        px: 3,
        py: 6,
        textAlign: 'center',
        borderStyle: 'dashed',
    }}>
    <Typography variant="h6" sx={{ fontWeight: 600 }} color="text.primary">
      {title}
    </Typography>
    {description ? (<Typography variant="body2" color="text.secondary" sx={{ maxWidth: 400 }}>
        {description}
      </Typography>) : null}
    {action ? <Box sx={{ mt: 1 }}>{action}</Box> : null}
  </Paper>);
