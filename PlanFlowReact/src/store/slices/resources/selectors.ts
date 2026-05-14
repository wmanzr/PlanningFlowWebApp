import type { RootState } from '../../types';
import { resourcesAdapterSelectors } from './resourcesSlice';
import type { ResourceId } from '@/types';
export const selectResourcesListMeta = (state: RootState) => state.resources.list;
export const selectResourcesActionMeta = (state: RootState) => state.resources.action;
export const selectAllResources = (state: RootState) => resourcesAdapterSelectors.selectAll(state.resources);
export const selectResourceById = (id: ResourceId | undefined) => (state: RootState) => id === undefined ? undefined : resourcesAdapterSelectors.selectById(state.resources, id);
