import { createTheme, type ThemeOptions } from '@mui/material/styles';
export type ThemeMode = 'light' | 'dark';
const lightPalette = {
    bg: '#fffffe',
    headline: '#2b2c34',
    paragraph: '#2b2c34',
    button: '#6246ea',
    buttonText: '#fffffe',
    stroke: '#2b2c34',
    highlight: '#6246ea',
    secondary: '#d1d1e9',
    tertiary: '#e45858',
    surface: '#f4f4fc',
    surfaceMuted: '#ebebf8',
    accent: '#8b5cf6',
    accentSoft: 'rgba(98, 70, 234, 0.14)',
    accentWarm: '#d97706',
} as const;
const darkPalette = {
    bg: '#16161a',
    headline: '#fffffe',
    paragraph: '#94a1b2',
    button: '#7f5af0',
    buttonText: '#fffffe',
    stroke: '#010101',
    highlight: '#7f5af0',
    secondary: '#72757e',
    tertiary: '#2cb67d',
    surface: '#1f2128',
    surfaceMuted: '#242830',
    accent: '#a78bfa',
    accentSoft: 'rgba(167, 139, 250, 0.15)',
    accentWarm: '#fbbf24',
} as const;
export type AppPalette = {
    bg: string;
    headline: string;
    paragraph: string;
    button: string;
    buttonText: string;
    stroke: string;
    highlight: string;
    secondary: string;
    tertiary: string;
    surface: string;
    surfaceMuted: string;
    accent: string;
    accentSoft: string;
    accentWarm: string;
};
declare module '@mui/material/styles' {
    interface Theme {
        app: AppPalette;
    }
    interface ThemeOptions {
        app?: AppPalette;
    }
}
export function buildMuiTheme(mode: ThemeMode) {
    const p = mode === 'light' ? lightPalette : darkPalette;
    const options: ThemeOptions = {
        palette: {
            mode,
            primary: { main: p.button, contrastText: p.buttonText },
            secondary: { main: p.secondary },
            error: { main: mode === 'light' ? '#e45858' : '#ef4444' },
            warning: { main: p.accentWarm },
            success: { main: mode === 'light' ? '#22c55e' : '#2cb67d' },
            background: { default: p.bg, paper: p.surface },
            text: { primary: p.headline, secondary: p.paragraph },
            divider: mode === 'light' ? 'rgba(209,209,233,0.6)' : 'rgba(114,117,126,0.6)',
        },
        app: p,
        shape: { borderRadius: 10 },
        typography: {
            fontFamily: [
                'Inter',
                'system-ui',
                '-apple-system',
                'Segoe UI',
                'Roboto',
                'sans-serif',
            ].join(','),
            button: { textTransform: 'none' as const, fontWeight: 500 },
        },
        components: {
            MuiCssBaseline: {
                styleOverrides: {
                    body: { backgroundColor: p.bg, color: p.paragraph },
                },
            },
            MuiButton: {
                defaultProps: { disableElevation: true },
                styleOverrides: {
                    root: ({ theme }) => ({
                        borderRadius: 10,
                        '&.Mui-disabled': { opacity: 0.5 },
                        [theme.breakpoints.down('sm')]: {
                            minHeight: 'unset',
                        },
                    }),
                    sizeMedium: ({ theme }) => ({
                        height: 40,
                        padding: '0 16px',
                        fontSize: '0.875rem',
                        [theme.breakpoints.down('sm')]: {
                            height: 34,
                            padding: '0 12px',
                            fontSize: '0.8125rem',
                        },
                    }),
                    sizeSmall: ({ theme }) => ({
                        height: 32,
                        padding: '0 12px',
                        fontSize: '0.875rem',
                        [theme.breakpoints.down('sm')]: {
                            height: 28,
                            padding: '0 10px',
                            fontSize: '0.75rem',
                        },
                    }),
                    sizeLarge: ({ theme }) => ({
                        height: 48,
                        padding: '0 20px',
                        fontSize: '1rem',
                        [theme.breakpoints.down('sm')]: {
                            height: 38,
                            padding: '0 14px',
                            fontSize: '0.875rem',
                        },
                    }),
                },
            },
            MuiIconButton: {
                styleOverrides: {
                    root: ({ theme }) => ({
                        [theme.breakpoints.down('sm')]: {
                            padding: 6,
                        },
                    }),
                    sizeSmall: ({ theme }) => ({
                        [theme.breakpoints.down('sm')]: {
                            padding: 4,
                        },
                    }),
                },
            },
            MuiTextField: {
                defaultProps: { variant: 'outlined', size: 'small' },
                styleOverrides: {
                    root: {
                        '& .MuiOutlinedInput-root': {
                            borderRadius: 10,
                            backgroundColor: p.bg,
                            color: p.headline,
                            '& fieldset': { borderColor: p.secondary },
                            '&:hover fieldset': { borderColor: p.highlight },
                            '&.Mui-focused fieldset': {
                                borderColor: p.highlight,
                                borderWidth: 2,
                            },
                        },
                        '& .MuiInputLabel-root': { color: p.paragraph },
                        '& .MuiInputLabel-root.Mui-focused': { color: p.highlight },
                    },
                },
            },
            MuiSelect: {
                defaultProps: { variant: 'outlined', size: 'small' },
                styleOverrides: {
                    root: { borderRadius: 10 },
                },
            },
            MuiDialog: {
                styleOverrides: {
                    paper: {
                        borderRadius: 14,
                        border: `1px solid ${mode === 'light' ? 'rgba(209,209,233,0.6)' : 'rgba(114,117,126,0.6)'}`,
                        backgroundColor: p.bg,
                        backgroundImage: 'none',
                    },
                },
            },
            MuiCard: {
                styleOverrides: {
                    root: {
                        borderRadius: 14,
                        backgroundColor: p.bg,
                        backgroundImage: 'none',
                        boxShadow: '0 1px 2px rgba(0, 0, 0, 0.04), 0 4px 16px rgba(0, 0, 0, 0.06)',
                    },
                },
            },
            MuiChip: {
                styleOverrides: {
                    root: { fontWeight: 500, fontSize: '0.75rem' },
                },
            },
            MuiTab: {
                styleOverrides: {
                    root: {
                        textTransform: 'none' as const,
                        fontWeight: 500,
                        fontSize: '0.875rem',
                        minHeight: 36,
                    },
                },
            },
            MuiTabs: {
                styleOverrides: {
                    indicator: { backgroundColor: p.highlight, height: 3, borderRadius: 2 },
                },
            },
            MuiPaper: {
                styleOverrides: {
                    root: { backgroundImage: 'none' },
                },
            },
            MuiDrawer: {
                styleOverrides: {
                    paper: {
                        backgroundColor: p.surface,
                        backgroundImage: 'none',
                    },
                },
            },
            MuiAppBar: {
                styleOverrides: {
                    root: {
                        backgroundColor: p.surface,
                        backgroundImage: 'none',
                        color: p.headline,
                    },
                },
            },
            MuiTooltip: {
                styleOverrides: {
                    tooltip: {
                        backgroundColor: p.headline,
                        color: p.bg,
                        fontSize: '0.75rem',
                    },
                },
            },
            MuiPagination: {
                styleOverrides: {
                    root: {
                        '& .MuiPaginationItem-root': {
                            color: p.paragraph,
                            '&.Mui-selected': {
                                backgroundColor: p.button,
                                color: p.buttonText,
                                '&:hover': { backgroundColor: p.highlight },
                            },
                        },
                    },
                },
            },
        },
    };
    return createTheme(options);
}
