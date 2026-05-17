import type {
    MatchTaskResponseDto,
    RankedCandidateResponseDto,
    RejectedCandidateResponseDto,
    UserResponseDto,
} from '@/types';

export type LeftMatchingListItem =
    | { kind: 'overflow-ranked'; candidate: RankedCandidateResponseDto }
    | { kind: 'executor'; user: UserResponseDto }
    | { kind: 'rejected'; rejection: RejectedCandidateResponseDto };

export interface ExecutorMatchingLists {
    recommended: RankedCandidateResponseDto[];
    leftItems: LeftMatchingListItem[];
    rejectedOrphans: RejectedCandidateResponseDto[];
}

function matchesExecutorSearch(fullName: string, username: string, query: string): boolean {
    const q = query.trim().toLowerCase();
    if (!q) {
        return true;
    }
    return `${fullName} ${username}`.toLowerCase().includes(q);
}

export function buildExecutorMatchingLists(params: {
    matchResult: MatchTaskResponseDto;
    requiredSlots: number;
    executors: UserResponseDto[];
    searchQuery: string;
}): ExecutorMatchingLists {
    const { matchResult, requiredSlots, executors, searchQuery } = params;
    const rankedSorted = [...matchResult.ranked].sort((a, b) => a.rank - b.rank);
    const recommended = rankedSorted.slice(0, requiredSlots);
    const recommendedIds = new Set(recommended.map((c) => Number(c.candidateId)));
    const overflowRanked = rankedSorted.slice(requiredSlots);
    const overflowIds = new Set(overflowRanked.map((c) => Number(c.candidateId)));
    const rankedIds = new Set(rankedSorted.map((c) => Number(c.candidateId)));

    const rejectedById = new Map<number, RejectedCandidateResponseDto>();
    for (const r of matchResult.rejected) {
        rejectedById.set(Number(r.candidateId), r);
    }

    const leftItems: LeftMatchingListItem[] = [];

    for (const candidate of overflowRanked) {
        if (!matchesExecutorSearch(candidate.candidateFullName, candidate.candidateUsername, searchQuery)) {
            continue;
        }
        leftItems.push({ kind: 'overflow-ranked', candidate });
    }

    const directoryRejected: UserResponseDto[] = [];
    const directoryNeutral: UserResponseDto[] = [];

    for (const user of executors) {
        const id = Number(user.id);
        if (recommendedIds.has(id) || overflowIds.has(id) || rankedIds.has(id)) {
            continue;
        }
        if (!matchesExecutorSearch(user.fullName ?? '', user.username, searchQuery)) {
            continue;
        }
        if (rejectedById.has(id)) {
            directoryRejected.push(user);
            continue;
        }
        directoryNeutral.push(user);
    }

    directoryNeutral.sort((a, b) => (a.fullName ?? '').localeCompare(b.fullName ?? '', 'ru'));
    for (const user of directoryNeutral) {
        leftItems.push({ kind: 'executor', user });
    }

    directoryRejected.sort((a, b) => (a.fullName ?? '').localeCompare(b.fullName ?? '', 'ru'));
    for (const user of directoryRejected) {
        const rejection = rejectedById.get(Number(user.id));
        if (rejection) {
            leftItems.push({ kind: 'rejected', rejection });
        }
    }

    const directoryIds = new Set(executors.map((u) => Number(u.id)));
    const rejectedOrphans = matchResult.rejected.filter((r) => {
        const id = Number(r.candidateId);
        if (directoryIds.has(id)) {
            return false;
        }
        return matchesExecutorSearch(r.candidateFullName, r.candidateUsername, searchQuery);
    });

    return { recommended, leftItems, rejectedOrphans };
}
