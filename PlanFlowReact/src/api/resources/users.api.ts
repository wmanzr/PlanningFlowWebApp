import { type AssignmentId, type AssignmentRejectRequest, type PageQuery, type PageResult, type UserId, type UserProfileUpdateRequest, type UserResponseDto, type UserRole, type UserSkillResponseDto, type UserSkillsUpdateRequest, type UserViewerContextDto, } from '@/types';
import { http } from '../http';
import { ENDPOINTS } from '../endpoints';
export interface ListUsersQuery extends PageQuery {
    username?: string;
    role?: UserRole;
}
export const usersApi = {
    list: (query: ListUsersQuery): Promise<PageResult<UserResponseDto>> => http
        .get<PageResult<UserResponseDto>>(ENDPOINTS.users.root, { params: query })
        .then((r) => r.data),
    byId: (id: UserId): Promise<UserResponseDto> => http.get<UserResponseDto>(ENDPOINTS.users.byId(id)).then((r) => r.data),
    skills: (id: UserId): Promise<UserSkillResponseDto[]> => http
        .get<UserSkillResponseDto[]>(ENDPOINTS.users.skills(id))
        .then((r) => r.data),
    viewerContext: (id: UserId): Promise<UserViewerContextDto> => http.get<UserViewerContextDto>(ENDPOINTS.users.viewerContext(id)).then((r) => r.data),
    updateProfile: async (id: UserId, body: UserProfileUpdateRequest): Promise<UserResponseDto> => {
        await http.put<number>(ENDPOINTS.users.profile(id), body);
        return http.get<UserResponseDto>(ENDPOINTS.users.byId(id)).then((r) => r.data);
    },
    updateSkills: (id: UserId, body: UserSkillsUpdateRequest): Promise<number[]> => http.put<number[]>(ENDPOINTS.users.skills(id), body).then((r) => r.data),
    acceptAssignment: (assignmentId: AssignmentId): Promise<void> => http
        .post<void>(ENDPOINTS.users.acceptAssignment(assignmentId))
        .then(() => undefined),
    rejectAssignment: (assignmentId: AssignmentId, body: AssignmentRejectRequest): Promise<void> => http
        .post<void>(ENDPOINTS.users.rejectAssignment(assignmentId), body)
        .then(() => undefined),
};
