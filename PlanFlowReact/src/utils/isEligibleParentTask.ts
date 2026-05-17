import { coerceApiDateTimeToIso } from '@/components/ui';
import type { TaskResponseDto } from '@/types';

function toMs(isoLike: unknown): number {
    const iso = coerceApiDateTimeToIso(isoLike) ?? (typeof isoLike === 'string' ? isoLike : '');
    const ms = new Date(iso).getTime();
    return Number.isFinite(ms) ? ms : Number.NaN;
}

export function isEligibleParentTask(
    candidate: TaskResponseDto,
    child: Pick<TaskResponseDto, 'id' | 'startTime' | 'endTime'>,
): boolean {
    if (child.id !== undefined && candidate.id === child.id) {
        return false;
    }

    const childStartMs = toMs(child.startTime);
    const childEndMs = toMs(child.endTime);
    const parentEndMs = toMs(candidate.endTime);

    if (!Number.isFinite(childStartMs) || !Number.isFinite(parentEndMs)) {
        return false;
    }

    if (childStartMs < parentEndMs) {
        return false;
    }

    if (Number.isFinite(childEndMs) && parentEndMs >= childEndMs) {
        return false;
    }

    return true;
}
