import type { RootState } from '../../types';
import { usersAdapterSelectors } from './usersSlice';
import type { UserId } from '@/types';
export const selectUsersListMeta = (state: RootState) => state.users.list;
export const selectUsersSkillsState = (state: RootState) => state.users.skills;
export const selectUsersActionMeta = (state: RootState) => state.users.action;
export const selectUsersViewerContextState = (state: RootState) => state.users.viewerContext;
export const selectAllUsers = (state: RootState) => usersAdapterSelectors.selectAll(state.users);
export const selectUserById = (id: UserId | undefined) => (state: RootState) => id === undefined ? undefined : usersAdapterSelectors.selectById(state.users, id);
