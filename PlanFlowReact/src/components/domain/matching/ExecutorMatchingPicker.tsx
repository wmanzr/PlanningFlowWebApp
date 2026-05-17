import { type ReactNode, useMemo } from 'react';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import { Badge } from '@/components/ui';
import { surnameWithInitials } from '@/components/domain/event/EventCard';
import { buildExecutorMatchingLists } from './buildExecutorMatchingLists';
import { ExecutorMatchingCard } from './ExecutorMatchingCard';
import { MATCHING_REJECTION_LABELS } from './RejectedRow';
import type {
    AppApiError,
    MatchTaskResponseDto,
    RejectedCandidateResponseDto,
    SkillId,
    UserId,
    UserResponseDto,
} from '@/types';

const MATCHING_LIST_HEIGHT_CLASS = 'h-[min(360px,42vh)] min-h-[280px]';
const MATCHING_LIST_SCROLL_MAX_CLASS = 'max-h-[min(360px,42vh)]';

function recommendedListContainerClass(count: number): string {
    const base = 'flex flex-col gap-2 pr-1';
    if (count <= 1) {
        return base;
    }
    if (count === 2) {
        return `${base} max-h-[min(220px,30vh)] overflow-y-auto sm:max-h-[min(360px,42vh)]`;
    }
    return `${base} ${MATCHING_LIST_SCROLL_MAX_CLASS} min-h-0 overflow-y-auto lg:h-[min(360px,42vh)] lg:min-h-[280px]`;
}

function formatRejectionTooltip(r: RejectedCandidateResponseDto): string {
    const label = MATCHING_REJECTION_LABELS[r.reason];
    const d = r.details?.trim();
    return d ? `${label}: ${d}` : label;
}

function MatchingListEmptyState({ compact = false }: { compact?: boolean }) {
    const sizeClass = compact
        ? 'h-[min(120px,18vh)] min-h-[88px]'
        : MATCHING_LIST_HEIGHT_CLASS;
    return (
        <div className={`flex ${sizeClass} items-center justify-center rounded-lg border border-dashed border-secondary/50 px-4`}>
            <Typography variant="body2" color="text.secondary" className="text-center">
                Исполнителей нет
            </Typography>
        </div>
    );
}

export interface ExecutorMatchingPickerProps {
    matchResult: MatchTaskResponseDto | null;
    matchStatus: string;
    matchError: AppApiError | null;
    requiredSlots: number;
    executors: UserResponseDto[];
    executorSearch: string;
    onExecutorSearchChange: (value: string) => void;
    selectedExecutorIds: Set<number>;
    onToggleExecutor: (userId: UserId) => void;
}

export function ExecutorMatchingPicker({
    matchResult,
    matchStatus,
    matchError,
    requiredSlots,
    executors,
    executorSearch,
    onExecutorSearchChange,
    selectedExecutorIds,
    onToggleExecutor,
}: ExecutorMatchingPickerProps) {
    const metricsByUserId = useMemo(() => {
        const m = new Map<
            number,
            {
                distanceMeters?: number;
                workedTodayMinutes: number;
                maxDailyLoadMinutes: number;
                matchedRequiredSkillIds: SkillId[];
            }
        >();
        if (!matchResult) {
            return m;
        }
        for (const r of matchResult.ranked) {
            m.set(Number(r.candidateId), {
                workedTodayMinutes: r.workedTodayMinutes,
                maxDailyLoadMinutes: r.maxDailyLoadMinutes,
                matchedRequiredSkillIds: r.matchedRequiredSkillIds,
                ...(r.distanceMeters !== undefined ? { distanceMeters: r.distanceMeters } : {}),
            });
        }
        for (const r of matchResult.rejected) {
            const id = Number(r.candidateId);
            if (m.has(id)) {
                continue;
            }
            m.set(id, {
                workedTodayMinutes: r.workedTodayMinutes,
                maxDailyLoadMinutes: r.maxDailyLoadMinutes,
                matchedRequiredSkillIds: r.matchedRequiredSkillIds,
                ...(r.distanceMeters !== undefined ? { distanceMeters: r.distanceMeters } : {}),
            });
        }
        return m;
    }, [matchResult]);

    const lists = useMemo(() => {
        if (!matchResult) {
            return null;
        }
        return buildExecutorMatchingLists({
            matchResult,
            requiredSlots,
            executors,
            searchQuery: executorSearch,
        });
    }, [matchResult, requiredSlots, executors, executorSearch]);

    const renderSelectableCard = (
        userId: UserId,
        fullName: string,
        username: string,
        matchedSkillIds: SkillId[],
        distanceMeters: number | undefined,
        workedTodayMinutes: number | undefined,
        maxDailyLoadMinutes: number | undefined,
        rankBadge?: ReactNode,
        algorithmMiss = false,
        blockedTip?: string,
    ) => {
        const uid = Number(userId);
        const sel = !blockedTip && selectedExecutorIds.has(uid);
        const addLocked = !blockedTip && !sel && selectedExecutorIds.size >= requiredSlots;
        return (
            <ExecutorMatchingCard
                fullName={surnameWithInitials(fullName)}
                username={username}
                matchedSkillIds={matchedSkillIds}
                {...(distanceMeters !== undefined ? { distanceMeters } : {})}
                {...(workedTodayMinutes !== undefined && maxDailyLoadMinutes !== undefined
                    ? { workedTodayMinutes, maxDailyLoadMinutes }
                    : {})}
                algorithmMiss={algorithmMiss && !blockedTip}
                rankBadge={rankBadge}
                {...(blockedTip !== undefined
                    ? { blockedSelectionTooltip: blockedTip, selectable: false }
                    : {
                          selectable: true,
                          selected: sel,
                          selectionAddDisabled: addLocked,
                          onToggleSelect: () => {
                              if (addLocked) {
                                  return;
                              }
                              onToggleExecutor(userId);
                          },
                      })}
            />
        );
    };

    return (
        <div className="flex min-h-0 flex-col gap-4">
            {matchStatus === 'pending' ? (
                <Typography variant="body2" color="text.secondary">
                    Выполняется подбор кандидатов…
                </Typography>
            ) : null}
            {matchError ? (
                <Typography variant="body2" color="error">
                    {matchError.message}
                </Typography>
            ) : null}

            {matchResult ? (
                <>
                    <div className="rounded-lg border border-secondary/45 bg-surface-muted/70 px-3 py-2.5">
                        <Typography variant="body2" className="font-semibold text-headline">
                            Выделено: {selectedExecutorIds.size} из {requiredSlots}
                        </Typography>
                        <Typography variant="caption" color="text.secondary" className="mt-0.5 block">
                            Нажмите на карточку, чтобы выбрать исполнителя.
                        </Typography>
                    </div>

                    <div className="grid w-full min-w-0 flex-1 grid-cols-1 gap-4 lg:min-h-[min(520px,62vh)] lg:grid-cols-2 lg:items-stretch">
                        <div className="flex min-h-0 min-w-0 flex-col gap-3">
                            <Typography variant="subtitle2">Все исполнители</Typography>
                            <TextField
                                size="small"
                                label="Поиск по ФИО"
                                value={executorSearch}
                                onChange={(e) => onExecutorSearchChange(e.target.value)}
                                placeholder="Имя или фамилия"
                            />
                            {lists && lists.leftItems.length === 0 && lists.rejectedOrphans.length === 0 ? (
                                <MatchingListEmptyState />
                            ) : (
                                <div
                                    className={`flex ${MATCHING_LIST_HEIGHT_CLASS} flex-col gap-2 overflow-y-auto pr-1`}
                                >
                                    {lists?.leftItems.map((item) => {
                                        if (item.kind === 'overflow-ranked') {
                                            const c = item.candidate;
                                            return (
                                                <div key={`overflow-${c.candidateId}`}>
                                                    {renderSelectableCard(
                                                        c.candidateId,
                                                        c.candidateFullName,
                                                        c.candidateUsername,
                                                        c.matchedRequiredSkillIds,
                                                        c.distanceMeters,
                                                        c.workedTodayMinutes,
                                                        c.maxDailyLoadMinutes,
                                                        <Badge tone="neutral" outline>
                                                            #{c.rank}
                                                        </Badge>,
                                                    )}
                                                </div>
                                            );
                                        }
                                        if (item.kind === 'executor') {
                                            const u = item.user;
                                            const metrics = metricsByUserId.get(Number(u.id));
                                            const algorithmMiss = metrics === undefined;
                                            return (
                                                <div key={u.id}>
                                                    {renderSelectableCard(
                                                        u.id,
                                                        u.fullName,
                                                        u.username,
                                                        metrics?.matchedRequiredSkillIds ?? [],
                                                        metrics?.distanceMeters,
                                                        metrics?.workedTodayMinutes,
                                                        metrics?.maxDailyLoadMinutes,
                                                        undefined,
                                                        algorithmMiss,
                                                    )}
                                                </div>
                                            );
                                        }
                                        const r = item.rejection;
                                        return (
                                            <ExecutorMatchingCard
                                                key={`rej-${r.candidateId}`}
                                                fullName={surnameWithInitials(r.candidateFullName)}
                                                username={r.candidateUsername}
                                                matchedSkillIds={r.matchedRequiredSkillIds}
                                                workedTodayMinutes={r.workedTodayMinutes}
                                                maxDailyLoadMinutes={r.maxDailyLoadMinutes}
                                                {...(r.distanceMeters !== undefined
                                                    ? { distanceMeters: r.distanceMeters }
                                                    : {})}
                                                blockedSelectionTooltip={formatRejectionTooltip(r)}
                                                selectable={false}
                                            />
                                        );
                                    })}
                                    {lists?.rejectedOrphans.map((r) => (
                                        <ExecutorMatchingCard
                                            key={`rej-orphan-${r.candidateId}`}
                                            fullName={surnameWithInitials(r.candidateFullName)}
                                            username={r.candidateUsername}
                                            matchedSkillIds={r.matchedRequiredSkillIds}
                                            workedTodayMinutes={r.workedTodayMinutes}
                                            maxDailyLoadMinutes={r.maxDailyLoadMinutes}
                                            {...(r.distanceMeters !== undefined
                                                ? { distanceMeters: r.distanceMeters }
                                                : {})}
                                            blockedSelectionTooltip={formatRejectionTooltip(r)}
                                            selectable={false}
                                        />
                                    ))}
                                </div>
                            )}
                        </div>

                        <div className="flex min-h-0 min-w-0 flex-col gap-3 self-start">
                            <Typography variant="subtitle2">Рекомендованные кандидаты</Typography>
                            {lists && lists.recommended.length === 0 ? (
                                <MatchingListEmptyState compact />
                            ) : (
                                <div
                                    className={recommendedListContainerClass(
                                        lists?.recommended.length ?? 0,
                                    )}
                                >
                                    {lists?.recommended.map((c) => (
                                        <div key={c.candidateId}>
                                            {renderSelectableCard(
                                                c.candidateId,
                                                c.candidateFullName,
                                                c.candidateUsername,
                                                c.matchedRequiredSkillIds,
                                                c.distanceMeters,
                                                c.workedTodayMinutes,
                                                c.maxDailyLoadMinutes,
                                                <Badge tone="success">#{c.rank}</Badge>,
                                            )}
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                </>
            ) : null}
        </div>
    );
}
