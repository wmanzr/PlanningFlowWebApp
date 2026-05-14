import { createSelector } from '@reduxjs/toolkit';
import type { RootState } from '../../types';
import { incidentsAdapterSelectors } from './incidentsSlice';
import type { EventId, IncidentId } from '@/types';
export const selectIncidentsListMeta = (state: RootState) => state.incidents.list;
export const selectIncidentsActionMeta = (state: RootState) => state.incidents.action;
export const selectAllIncidents = (state: RootState) => incidentsAdapterSelectors.selectAll(state.incidents);
export const makeSelectIncidentsForEvent = (eventId: EventId | undefined) => createSelector(selectAllIncidents, (incidents) => eventId === undefined ? [] : incidents.filter((incident) => incident.eventId === eventId));
export const makeSelectIncidentById = (id: IncidentId | undefined) => createSelector([(state: RootState) => state.incidents], (incidentsState) => id === undefined ? undefined : incidentsAdapterSelectors.selectById(incidentsState, id));
