import { type HTMLAttributes, type ReactNode } from 'react';
import MuiCard from '@mui/material/Card';
import Typography from '@mui/material/Typography';
import { cn } from '../cn';
export interface CardProps extends HTMLAttributes<HTMLDivElement> {
    bordered?: boolean;
    padded?: boolean;
}
export const Card = ({ className, bordered = true, padded = true, children, ...rest }: CardProps) => (<MuiCard variant={bordered ? 'outlined' : 'elevation'} className={cn(className)} sx={{
        ...(padded ? { p: 2.5 } : {}),
        ...(!bordered ? { border: 'none' } : {}),
    }} {...(rest as Record<string, unknown>)}>
    {children}
  </MuiCard>);
export const CardHeader = ({ title, subtitle, actions, }: {
    title: ReactNode;
    subtitle?: ReactNode;
    actions?: ReactNode;
}) => (<div className="mb-4 flex flex-wrap items-start justify-between gap-3">
    <div className="min-w-0 flex-1">
      <Typography variant="h6" sx={{ fontWeight: 600 }} color="text.primary">
        {title}
      </Typography>
      {subtitle ? (<Typography variant="body2" color="text.secondary">
          {subtitle}
        </Typography>) : null}
    </div>
    {actions ? (<div className="ml-auto flex shrink-0 flex-wrap items-center justify-end gap-2">{actions}</div>) : null}
  </div>);
