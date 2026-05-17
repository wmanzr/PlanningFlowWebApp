import type { MapMarkerEmphasis, MapMarkerKind } from './mapMarker.types';

export const MAP_MARKER_HEX: Record<MapMarkerKind, string> = {
    event: '#3d5a80',
    task: '#e45858',
    candidate: '#c9a227',
};

export function resolveMapMarkerColor(kind: MapMarkerKind): string {
    return MAP_MARKER_HEX[kind];
}

export const MAP_PRIMARY_PIN_COLOR = '#3d5a80';

const KIND_LABEL: Record<MapMarkerKind, string> = {
    event: 'Мероприятие',
    task: 'Задача',
    candidate: 'Кандидат',
};

function escapeHtml(text: string): string {
    return text
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;');
}

function buildPrimaryPinHtml(title: string, color: string): string {
    return `<div title="${title}" style="display:flex;flex-direction:column;align-items:center;transform:translate(-50%,-100%);pointer-events:auto;cursor:default;">
  <svg width="34" height="44" viewBox="0 0 32 42" aria-hidden="true" style="display:block;filter:drop-shadow(0 2px 5px rgba(0,0,0,0.35));">
    <path fill="${color}" stroke="#ffffff" stroke-width="2" d="M16 0C9.4 0 4 5.4 4 12c0 9 12 28 12 28s12-19 12-28C28 5.4 22.6 0 16 0z"/>
    <circle cx="16" cy="12" r="4.5" fill="#ffffff"/>
  </svg>
</div>`;
}

function buildDefaultDotHtml(
    kind: MapMarkerKind,
    color: string,
    label: string | undefined,
    showLabel: boolean,
): string {
    const size = kind === 'event' ? 14 : 12;
    const title = label ? escapeHtml(label) : KIND_LABEL[kind];
    const labelHtml =
        showLabel && label
            ? `<span style="max-width:140px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font:600 11px/1.2 system-ui,sans-serif;color:#1a1a1a;text-shadow:0 0 3px #fff,0 0 3px #fff,0 1px 2px #fff;">${escapeHtml(label)}</span>`
            : '';

    return `<div title="${title}" style="display:flex;flex-direction:column;align-items:center;gap:3px;transform:translate(-50%,-100%);pointer-events:auto;cursor:default;">
  <div style="width:${size}px;height:${size}px;border-radius:50%;background:${color};border:2px solid #fff;box-shadow:0 1px 4px rgba(0,0,0,0.35);flex-shrink:0;"></div>
  ${labelHtml}
</div>`;
}

export function buildMapMarkerHtml(
    kind: MapMarkerKind,
    color: string,
    label: string | undefined,
    showLabel: boolean,
    emphasis: MapMarkerEmphasis,
    primaryColor: string = MAP_PRIMARY_PIN_COLOR,
): string {
    const title = escapeHtml(label ?? KIND_LABEL[kind]);
    if (emphasis === 'primary') {
        return buildPrimaryPinHtml(title, primaryColor);
    }
    return buildDefaultDotHtml(kind, color, label, showLabel);
}
