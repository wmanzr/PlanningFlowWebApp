export function buildStatusFilterOptions<T extends string>(
    labels: Record<T, string>,
    allLabel = 'Все статусы',
): { value: string; label: string }[] {
    return [
        { value: '', label: allLabel },
        ...(Object.keys(labels) as T[]).map((value) => ({
            value,
            label: labels[value],
        })),
    ];
}
