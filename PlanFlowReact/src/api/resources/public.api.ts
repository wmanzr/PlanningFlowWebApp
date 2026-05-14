import { http } from '../http';
import { ENDPOINTS } from '../endpoints';

export interface PublicLandingStatsDto {
    totalEventsCount: number;
    completedEventsCount: number;
    tasksDoneCount: number;
    registeredUsersCount: number;
    resolvedIncidentsCount: number;
    acceptedAssignmentsCount: number;
}

export const publicApi = {
    landingStats: (): Promise<PublicLandingStatsDto> => http
        .get<PublicLandingStatsDto>(ENDPOINTS.public.landingStats)
        .then((r) => r.data),
};
