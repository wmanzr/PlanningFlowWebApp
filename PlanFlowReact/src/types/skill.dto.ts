import { type SkillId } from './common';
export interface SkillResponseDto {
    id: SkillId;
    name: string;
    category: string;
}
export interface SkillCreateRequest {
    name: string;
    category: string;
}
export interface ListSkillsQuery {
    name?: string;
    page?: number;
    size?: number;
}
