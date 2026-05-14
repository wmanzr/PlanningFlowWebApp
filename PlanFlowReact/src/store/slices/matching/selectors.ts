import type { RootState } from '../../types';
export const selectMatchingState = (state: RootState) => state.matching;
export const selectMatchingResult = (state: RootState) => state.matching.data;
export const selectMatchingStatus = (state: RootState) => state.matching.status;
export const selectMatchingError = (state: RootState) => state.matching.error;
