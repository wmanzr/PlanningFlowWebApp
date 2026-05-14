import type { RootState } from '../../types';
import { skillsAdapterSelectors } from './skillsSlice';
import type { SkillId } from '@/types';
export const selectSkillsListMeta = (state: RootState) => state.skills.list;
export const selectSkillsActionMeta = (state: RootState) => state.skills.action;
export const selectSkillsCategories = (state: RootState) => state.skills.categories;
export const selectAllSkills = (state: RootState) => skillsAdapterSelectors.selectAll(state.skills);
export const selectSkillById = (id: SkillId | undefined) => (state: RootState) => id === undefined ? undefined : skillsAdapterSelectors.selectById(state.skills, id);
