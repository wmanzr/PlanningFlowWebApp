import { type EventCancelRequest, type EventCreateRequest, type EventDashboardResponseDto, type EventPostMortemAiReportResponseDto, type EventId, type EventResponseDto, type EventUpdateRequest, type ListEventsQuery, type PageResult, } from '@/types';
import { http } from '../http';
import { ENDPOINTS } from '../endpoints';
export const eventsApi = {
    list: (query: ListEventsQuery): Promise<PageResult<EventResponseDto>> => http
        .get<PageResult<EventResponseDto>>(ENDPOINTS.events.root, { params: query })
        .then((r) => r.data),
    byId: (id: EventId): Promise<EventResponseDto> => http.get<EventResponseDto>(ENDPOINTS.events.byId(id)).then((r) => r.data),
    dashboard: (id: EventId): Promise<EventDashboardResponseDto> => http
        .get<EventDashboardResponseDto>(ENDPOINTS.events.dashboard(id))
        .then((r) => r.data),
    create: (body: EventCreateRequest): Promise<number> => http.post<number>(ENDPOINTS.events.root, body).then((r) => r.data),
    update: (body: EventUpdateRequest): Promise<number> => http.put<number>(ENDPOINTS.events.byId(body.eventId), body).then((r) => r.data),
    startPlanning: (id: EventId): Promise<number> => http.post<number>(ENDPOINTS.events.startPlanning(id)).then((r) => r.data),
    activate: (id: EventId): Promise<number> => http.post<number>(ENDPOINTS.events.activate(id)).then((r) => r.data),
    complete: (id: EventId): Promise<number> => http.post<number>(ENDPOINTS.events.complete(id)).then((r) => r.data),
    cancel: (id: EventId, body: EventCancelRequest): Promise<number> => http.post<number>(ENDPOINTS.events.cancel(id), body).then((r) => r.data),
    getPostMortemAiReport: (id: EventId): Promise<EventPostMortemAiReportResponseDto> => http
        .get<EventPostMortemAiReportResponseDto>(ENDPOINTS.events.postMortemAi(id))
        .then((r) => r.data),
    postMortemAi: (id: EventId): Promise<void> => http.post<void>(ENDPOINTS.events.postMortemAi(id)).then(() => undefined),
};
