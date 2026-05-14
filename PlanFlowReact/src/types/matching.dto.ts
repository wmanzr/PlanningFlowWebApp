import { type RejectionReason } from './enums';
import { type SkillId, type TaskId, type UserId } from './common';
export interface ScoreBreakdownResponseDto {
    totalScore: number;
    skillScore: number;
    geoScore: number;
    loadScore: number;
}
export interface RankedCandidateResponseDto {
    candidateId: UserId;
    candidateUsername: string;
    candidateFullName: string;
    score: ScoreBreakdownResponseDto;
    rank: number;
    distanceMeters?: number;
    workedTodayMinutes: number;
    maxDailyLoadMinutes: number;
    matchedRequiredSkillIds: SkillId[];
}
export interface RejectedCandidateResponseDto {
    candidateId: UserId;
    candidateUsername: string;
    candidateFullName: string;
    reason: RejectionReason;
    details: string;
    distanceMeters?: number;
    workedTodayMinutes: number;
    maxDailyLoadMinutes: number;
    matchedRequiredSkillIds: SkillId[];
}
export interface MatchTaskResponseDto {
    taskId?: TaskId;
    requiredCount: number;
    ranked: RankedCandidateResponseDto[];
    rejected: RejectedCandidateResponseDto[];
    shortageCount: number;
}
