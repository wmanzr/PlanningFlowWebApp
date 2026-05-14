import { type ReactNode } from 'react';
import CircularProgress from '@mui/material/CircularProgress';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
export interface LoadingAreaProps {
    children?: ReactNode;
    label?: string;
}
export const LoadingArea = ({ children, label = 'Загрузка' }: LoadingAreaProps) => (<Box sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 1.5,
        py: 8,
        color: 'text.secondary',
    }}>
    <CircularProgress size={40} color="primary"/>
    {children ?? (<Typography variant="body2" color="text.secondary">
        {label}
      </Typography>)}
  </Box>);
