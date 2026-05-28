import { type ReactNode } from 'react';
import Box from '@mui/material/Box';
import { Spinner } from '../Spinner';
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
    {children ?? <Spinner size="lg" label={label}/>}
  </Box>);
