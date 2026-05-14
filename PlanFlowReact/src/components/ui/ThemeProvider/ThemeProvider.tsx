import { useEffect, useMemo, type ReactNode } from 'react';
import { ThemeProvider as MuiThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { buildMuiTheme, type ThemeMode } from '@/theme/muiTheme';
export type { ThemeMode } from '@/theme/muiTheme';
export interface ThemeProviderProps {
    theme: ThemeMode;
    children: ReactNode;
}
export const ThemeProvider = ({ theme, children }: ThemeProviderProps) => {
    useEffect(() => {
        document.documentElement.dataset.theme = theme;
    }, [theme]);
    const muiTheme = useMemo(() => buildMuiTheme(theme), [theme]);
    return (<MuiThemeProvider theme={muiTheme}>
      <CssBaseline enableColorScheme/>
      {children}
    </MuiThemeProvider>);
};
