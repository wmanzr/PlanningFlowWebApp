export const PREVIEW_LIST_MAX_ITEMS = 3;

export function slicePreviewList<T>(items: readonly T[], max = PREVIEW_LIST_MAX_ITEMS): {
    preview: T[];
    moreCount: number;
} {
    const preview = items.slice(0, max);
    return { preview, moreCount: Math.max(0, items.length - preview.length) };
}

export const SUMMARY_PREVIEW_PANEL_BODY = 'flex flex-col gap-3 p-5';

export const SUMMARY_PREVIEW_PANEL_HEADER =
    'flex flex-col gap-3 border-b border-secondary/60 px-5 py-4 sm:flex-row sm:items-center sm:justify-between';
