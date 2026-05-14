import type { RootState } from '../../types';
export const selectTheme = (state: RootState) => state.ui.theme;
export const selectBootstrapped = (state: RootState) => state.ui.bootstrapped;
