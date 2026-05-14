import { type ExternalResourceResponseDto, type InternalResourceCreateRequest, type InternalResourceResponseDto, type InternalResourceUpdateRequest, type PageQuery, type PageResult, type ResourceId, } from '@/types';
import { http } from '../http';
import { ENDPOINTS } from '../endpoints';
export interface ListInternalResourcesQuery extends PageQuery {
    name?: string;
}
export const internalResourcesApi = {
    list: (query: ListInternalResourcesQuery): Promise<PageResult<InternalResourceResponseDto>> => http
        .get<PageResult<InternalResourceResponseDto>>(ENDPOINTS.resources.internal, {
        params: query,
    })
        .then((r) => r.data),
    byId: (id: ResourceId): Promise<InternalResourceResponseDto> => http
        .get<InternalResourceResponseDto>(ENDPOINTS.resources.internalById(id))
        .then((r) => r.data),
    create: (body: InternalResourceCreateRequest): Promise<number> => http.post<number>(ENDPOINTS.resources.internal, body).then((r) => r.data),
    update: (id: ResourceId, body: InternalResourceUpdateRequest): Promise<number> => http
        .put<number>(ENDPOINTS.resources.internalById(id), body)
        .then((r) => r.data),
    delete: (id: ResourceId): Promise<void> => http.delete<void>(ENDPOINTS.resources.internalById(id)).then(() => undefined),
    markOperational: (id: ResourceId): Promise<number> => http
        .post<number>(ENDPOINTS.resources.internalOperational(id))
        .then((r) => r.data),
    markBroken: (id: ResourceId): Promise<number> => http
        .post<number>(ENDPOINTS.resources.internalBroken(id))
        .then((r) => r.data),
    externalById: (id: ResourceId): Promise<ExternalResourceResponseDto> => http
        .get<ExternalResourceResponseDto>(ENDPOINTS.resources.externalById(id))
        .then((r) => r.data),
};
