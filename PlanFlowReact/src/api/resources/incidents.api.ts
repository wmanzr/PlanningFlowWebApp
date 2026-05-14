import { type EventId, type IncidentCreateRequest, type IncidentId, type IncidentResolveRequest, type IncidentResponseDto, type PageQuery, type PageResult, } from '@/types';
import { http } from '../http';
import { ENDPOINTS } from '../endpoints';
export const incidentsApi = {
    forEvent: (eventId: EventId, query: PageQuery): Promise<PageResult<IncidentResponseDto>> => http
        .get<PageResult<IncidentResponseDto>>(ENDPOINTS.incidents.forEvent(eventId), {
        params: query,
    })
        .then((r) => r.data),
    byId: (id: IncidentId): Promise<IncidentResponseDto> => http.get<IncidentResponseDto>(ENDPOINTS.incidents.byId(id)).then((r) => r.data),
    create: (body: IncidentCreateRequest): Promise<number> => http.post<number>(ENDPOINTS.incidents.root, body).then((r) => r.data),
    accept: (id: IncidentId): Promise<void> => http.post<void>(ENDPOINTS.incidents.accept(id)).then(() => undefined),
    resolve: (id: IncidentId, body: IncidentResolveRequest): Promise<void> => http.post<void>(ENDPOINTS.incidents.resolve(id), body).then(() => undefined),
};
