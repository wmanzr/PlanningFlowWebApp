import { type ReactNode } from 'react';
import { useAppSelector } from '@/store';
import { selectTheme } from '@/store/slices/ui/selectors';
import { ThemeProvider } from '@/components/ui';
export const AppThemeBridge = ({ children }: {
    children: ReactNode;
}) => {
    const theme = useAppSelector(selectTheme);
    return <ThemeProvider theme={theme}>{children}</ThemeProvider>;
};
