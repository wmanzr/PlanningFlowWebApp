import Typography from '@mui/material/Typography';
import { resolveMapMarkerColor } from './mapMarkerHtml';
import type { MapMarker } from './mapMarker.types';

export interface MapMarkersLegendProps {
    markers: MapMarker[];
    maxItems?: number;
    className?: string;
}

export function MapMarkersLegend({ markers, maxItems = 12, className }: MapMarkersLegendProps) {
    if (markers.length === 0) {
        return null;
    }

    const visible = markers.slice(0, maxItems);
    const hiddenCount = markers.length - visible.length;

    return (
        <div className={className}>
            <ul className="m-0 grid max-h-36 list-none grid-cols-1 gap-x-3 gap-y-1.5 overflow-y-auto p-0 sm:grid-cols-2 md:grid-cols-3">
                {visible.map((m) => (
                    <li key={m.id} className="flex min-w-0 items-start gap-2">
                        <span
                            className="mt-1.5 inline-block h-2.5 w-2.5 shrink-0 rounded-full border border-white shadow-sm"
                            style={{ background: resolveMapMarkerColor(m.kind) }}
                            aria-hidden
                        />
                        <Typography
                            variant="caption"
                            component="span"
                            className="min-w-0 truncate text-paragraph"
                            title={m.label ?? `${m.lat.toFixed(4)}, ${m.lng.toFixed(4)}`}
                        >
                            {m.label ?? `${m.lat.toFixed(4)}, ${m.lng.toFixed(4)}`}
                        </Typography>
                    </li>
                ))}
            </ul>
            {hiddenCount > 0 ? (
                <Typography variant="caption" color="text.secondary" className="mt-1.5 block">
                    и ещё {hiddenCount}
                </Typography>
            ) : null}
        </div>
    );
}
