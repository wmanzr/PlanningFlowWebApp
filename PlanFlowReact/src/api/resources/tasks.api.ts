import { type AssignmentFilter, type EventId, type ListTasksForEventQuery, type MatchTaskResponseDto, type PageResult, type ReserveResourcesResponseDto, type TaskAllocateResourcesRequest, type TaskAssignRequest, type TaskCreateRequest, type TaskId, type TaskMatchRequest, type TaskResponseDto, type TaskUpdateRequest, type UserId, } from '@/types';
import { http } from '../http';
import { ENDPOINTS } from '../endpoints';
export interface ListTasksForUserQuery extends ListTasksForEventQuery {
    filter: AssignmentFilter;
    title?: string;
}
export const tasksApi = {
    forEvent: (eventId: EventId, query: ListTasksForEventQuery): Promise<PageResult<TaskResponseDto>> => http
        .get<PageResult<TaskResponseDto>>(ENDPOINTS.tasks.forEvent(eventId), { params: query })
        .then((r) => r.data),
    forUser: (userId: UserId, query: ListTasksForUserQuery): Promise<PageResult<TaskResponseDto>> => http
        .get<PageResult<TaskResponseDto>>(ENDPOINTS.tasks.forUser(userId), { params: query })
        .then((r) => r.data),
    byId: (id: TaskId): Promise<TaskResponseDto> => http.get<TaskResponseDto>(ENDPOINTS.tasks.byId(id)).then((r) => r.data),
    create: (body: TaskCreateRequest): Promise<number> => http.post<number>(ENDPOINTS.tasks.root, body).then((r) => r.data),
    update: (id: TaskId, body: TaskUpdateRequest): Promise<number> => http.put<number>(ENDPOINTS.tasks.byId(id), body).then((r) => r.data),
    startExecution: (id: TaskId): Promise<number> => http.post<number>(ENDPOINTS.tasks.startExecution(id)).then((r) => r.data),
    done: (id: TaskId): Promise<number> => http.post<number>(ENDPOINTS.tasks.done(id)).then((r) => r.data),
    cancel: (id: TaskId): Promise<number> => http.post<number>(ENDPOINTS.tasks.cancel(id)).then((r) => r.data),
    assign: (id: TaskId, body: TaskAssignRequest): Promise<number> => http.post<number>(ENDPOINTS.tasks.assignments(id), body).then((r) => r.data),
    unassign: (id: TaskId, userId: UserId): Promise<void> => http.delete<void>(ENDPOINTS.tasks.assignmentByUser(id, userId)).then(() => undefined),
    match: (id: TaskId, body: TaskMatchRequest): Promise<MatchTaskResponseDto> => http.post<MatchTaskResponseDto>(ENDPOINTS.tasks.matching(id), body).then((r) => r.data),
    allocateResources: (id: TaskId, body: TaskAllocateResourcesRequest): Promise<ReserveResourcesResponseDto> => http
        .post<ReserveResourcesResponseDto>(ENDPOINTS.tasks.allocateResources(id), body)
        .then((r) => r.data),
};
