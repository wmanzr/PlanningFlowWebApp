import { useEffect, type Dispatch, type SetStateAction } from 'react';
import type { MatchTaskResponseDto } from '@/types';

export function useAutoSelectRankedExecutors(
    matchResult: MatchTaskResponseDto | null,
    requiredSlots: number,
    setSelectedExecutorIds: Dispatch<SetStateAction<Set<number>>>,
    enabled = true,
): void {
    useEffect(() => {
        if (!enabled || !matchResult) {
            return;
        }
        const rejectedIds = new Set(matchResult.rejected.map((r) => Number(r.candidateId)));
        const ranked = [...matchResult.ranked].sort((a, b) => a.rank - b.rank);
        const ids = ranked
            .filter((c) => !rejectedIds.has(Number(c.candidateId)))
            .slice(0, requiredSlots)
            .map((c) => Number(c.candidateId));
        setSelectedExecutorIds(new Set(ids));
    }, [matchResult, requiredSlots, enabled, setSelectedExecutorIds]);
}
