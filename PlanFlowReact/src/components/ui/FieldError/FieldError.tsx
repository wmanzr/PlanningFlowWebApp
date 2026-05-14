import { type ReactNode } from 'react';
import Typography from '@mui/material/Typography';
export interface FieldErrorProps {
    children: ReactNode;
}
export const FieldError = ({ children }: FieldErrorProps) => children ? (<Typography variant="caption" color="error">
      {children}
    </Typography>) : null;
