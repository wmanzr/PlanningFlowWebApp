import Typography from '@mui/material/Typography';
import { useTheme } from '@mui/material/styles';
import { resolveMapMarkerColor } from './mapMarkerHtml';
import type { MapMarker } from './mapMarker.types';
function isLegendZoomTarget(marker: MapMarker): boolean {
    return marker.kind === 'event' || marker.kind === 'task';
}
export interface MapMarkersLegendProps {
    markers: MapMarker[];
    maxItems?: number;
    className?: string;
    onLegendItemClick?: (marker: MapMarker) => void;
}
export function MapMarkersLegend({ markers, maxItems = 12, className, onLegendItemClick }: MapMarkersLegendProps) {
    const theme = useTheme();
    const eventSwatchColor = theme.palette.primary.main;
    if (markers.length === 0) {
        return null;
    }
    const visible = markers.slice(0, maxItems);
    const hiddenCount = markers.length - visible.length;
    return (<div className={className}>
            <ul className="m-0 grid max-h-36 list-none grid-cols-1 gap-x-3 gap-y-2 overflow-y-auto p-0 sm:grid-cols-2 md:grid-cols-3">
                {visible.map((m) => {
            const titleText = m.label ?? `${m.lat.toFixed(4)}, ${m.lng.toFixed(4)}`;
            const zoomable = onLegendItemClick !== undefined && isLegendZoomTarget(m);
            return (<li key={m.id} className="flex min-w-0 items-center gap-2">
                        <span className="inline-block h-2.5 w-2.5 shrink-0 rounded-full border border-white shadow-sm" style={{ background: m.kind === 'event' ? eventSwatchColor : resolveMapMarkerColor(m.kind) }} aria-hidden/>
                        {zoomable ? (<button type="button" className="min-w-0 max-w-full cursor-pointer truncate border-0 bg-transparent p-0 text-left text-xs leading-snug text-paragraph underline-offset-2 transition-colors hover:text-headline hover:underline focus-visible:rounded-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/30" title={titleText} onClick={() => onLegendItemClick(m)}>
                                {titleText}
                            </button>) : (<Typography variant="caption" component="span" className="min-w-0 truncate text-paragraph" title={titleText}>
                                {titleText}
                            </Typography>)}
                    </li>);
        })}
            </ul>
            {hiddenCount > 0 ? (<Typography variant="caption" color="text.secondary" className="mt-1.5 block">
                    и ещё {hiddenCount}
                </Typography>) : null}
        </div>);
}
