import { forwardRef, type ButtonHTMLAttributes } from 'react';
import MuiButton from '@mui/material/Button';
import MuiIconButton from '@mui/material/IconButton';
import CircularProgress from '@mui/material/CircularProgress';
import { useTheme } from '@mui/material/styles';
export interface ButtonVariants {
    variant?: 'primary' | 'secondary' | 'ghost' | 'danger' | 'link' | null;
    size?: 'sm' | 'md' | 'lg' | 'icon' | null;
    block?: boolean | null;
}
export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement>, ButtonVariants {
    loading?: boolean | undefined;
}
export const Button = forwardRef<HTMLButtonElement, ButtonProps>(({ className, variant = 'primary', size = 'md', block, loading, disabled, children, type, ...rest }, ref) => {
    const theme = useTheme();
    if (size === 'icon') {
        return (<MuiIconButton ref={ref} disabled={disabled || loading} className={className} type={type ?? 'button'} sx={{
                width: 40,
                height: 40,
                borderRadius: '10px',
                color: theme.app.headline,
                [theme.breakpoints.down('sm')]: {
                    width: 34,
                    height: 34,
                },
                ...(block ? { width: '100%' } : {}),
            }} {...(rest as Record<string, unknown>)}>
          {loading ? <CircularProgress size={18} color="inherit" sx={{ [theme.breakpoints.down('sm')]: { width: 16, height: 16 } }}/> : children}
        </MuiIconButton>);
    }
    const muiVariant = variant === 'primary' || variant === 'danger'
        ? 'contained'
        : variant === 'ghost'
            ? 'outlined'
            : variant === 'link'
                ? 'text'
                : 'contained';
    const muiSize = size === 'sm' ? 'small' : size === 'lg' ? 'large' : 'medium';
    const sxOverrides: Record<string, unknown> = {};
    if (variant === 'secondary') {
        sxOverrides.backgroundColor = theme.app.secondary;
        sxOverrides.color = theme.app.headline;
        sxOverrides['&:hover'] = { backgroundColor: theme.app.secondary, opacity: 0.9 };
    }
    else if (variant === 'danger') {
        sxOverrides.backgroundColor = theme.palette.error.main;
        sxOverrides.color = theme.palette.error.contrastText;
        sxOverrides['&:hover'] = { backgroundColor: theme.palette.error.dark, opacity: 1 };
    }
    else if (variant === 'ghost') {
        sxOverrides.borderColor = theme.app.secondary;
        sxOverrides.color = theme.app.headline;
        sxOverrides['&:hover'] = { backgroundColor: `${theme.app.secondary}66`, borderColor: theme.app.secondary };
    }
    else if (variant === 'link') {
        sxOverrides.color = theme.app.highlight;
        sxOverrides.padding = 0;
        sxOverrides.minWidth = 'auto';
        sxOverrides.height = 'auto';
        sxOverrides['&:hover'] = { textDecoration: 'underline', backgroundColor: 'transparent' };
    }
    if (block) {
        sxOverrides.width = '100%';
    }
    const loadingSpinnerSize = size === 'lg' ? 18 : size === 'sm' ? 14 : 16;
    return (<MuiButton ref={ref} variant={muiVariant} size={muiSize} disabled={disabled || loading} className={className} type={type ?? 'button'} disableRipple={variant === 'link'} sx={sxOverrides} startIcon={loading ? (<CircularProgress size={loadingSpinnerSize} color="inherit" sx={{ [theme.breakpoints.down('sm')]: { width: loadingSpinnerSize - 2, height: loadingSpinnerSize - 2 } }}/>) : undefined} {...(rest as Record<string, unknown>)}>
        {children}
      </MuiButton>);
});
Button.displayName = 'Button';
