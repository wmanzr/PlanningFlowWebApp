import { type ListSkillsQuery, type PageResult, type SkillCreateRequest, type SkillId, type SkillResponseDto, } from '@/types';
import { http } from '../http';
import { ENDPOINTS } from '../endpoints';
export const skillsApi = {
    categories: (): Promise<string[]> => http.get<string[]>(ENDPOINTS.skills.categories).then((r) => r.data),
    list: (query: ListSkillsQuery): Promise<PageResult<SkillResponseDto>> => http
        .get<PageResult<SkillResponseDto>>(ENDPOINTS.skills.root, { params: query })
        .then((r) => r.data),
    byId: (id: SkillId): Promise<SkillResponseDto> => http.get<SkillResponseDto>(ENDPOINTS.skills.byId(id)).then((r) => r.data),
    create: (body: SkillCreateRequest): Promise<number> => http.post<number>(ENDPOINTS.skills.root, body).then((r) => r.data),
    delete: (id: SkillId): Promise<void> => http.delete<void>(ENDPOINTS.skills.byId(id)).then(() => undefined),
};
