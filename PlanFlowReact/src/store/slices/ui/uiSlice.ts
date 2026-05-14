import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
export type ThemeMode = 'light' | 'dark';
const THEME_STORAGE_KEY = 'pf.theme';
const readInitialTheme = (): ThemeMode => {
    try {
        const stored = window.localStorage.getItem(THEME_STORAGE_KEY);
        if (stored === 'light' || stored === 'dark')
            return stored;
    }
    catch {
    }
    if (typeof window !== 'undefined' && window.matchMedia) {
        return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }
    return 'light';
};
interface UiState {
    theme: ThemeMode;
    bootstrapped: boolean;
}
const initialState: UiState = {
    theme: readInitialTheme(),
    bootstrapped: false,
};
export const uiSlice = createSlice({
    name: 'ui',
    initialState,
    reducers: {
        setTheme(state, action: PayloadAction<ThemeMode>) {
            state.theme = action.payload;
            try {
                window.localStorage.setItem(THEME_STORAGE_KEY, action.payload);
            }
            catch {
            }
        },
        toggleTheme(state) {
            state.theme = state.theme === 'light' ? 'dark' : 'light';
            try {
                window.localStorage.setItem(THEME_STORAGE_KEY, state.theme);
            }
            catch {
            }
        },
        bootstrapped(state) {
            state.bootstrapped = true;
        },
    },
});
export const uiActions = uiSlice.actions;
export const uiReducer = uiSlice.reducer;
