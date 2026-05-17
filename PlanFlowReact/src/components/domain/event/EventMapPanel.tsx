import { useMemo, useState } from 'react';
import {
    Button,
    Card,
    MapView,
    Modal,
    MAP_ZOOM_DETAIL,
    MAP_ZOOM_OVERVIEW,
    resolveMapViewportCenter,
    type MapMarker,
} from '@/components/ui';
import type { GeoPoint } from '@/types';

export interface EventMapPanelProps {
    markers: MapMarker[];
    center?: GeoPoint;
}

export function EventMapPanel({ markers, center }: EventMapPanelProps) {
    const [mapExpanded, setMapExpanded] = useState(false);
    const mapViewportCenter = useMemo(() => resolveMapViewportCenter(center), [center]);
    const markerSummary =
        markers.length === 0
            ? 'Нет точек с координатами'
            : `На карте: ${markers.length} ${markers.length === 1 ? 'точка' : markers.length < 5 ? 'точки' : 'точек'}`;

    return (
        <>
            <Card padded={false} className="flex w-full min-w-0 flex-col overflow-hidden">
                <div className="flex flex-wrap items-center justify-between gap-3 border-b border-secondary/60 px-5 py-4">
                    <div>
                        <h2 className="text-lg font-semibold text-headline">Карта</h2>
                        <p className="text-sm text-paragraph">{markerSummary}</p>
                    </div>
                    <Button
                        size="sm"
                        variant="secondary"
                        type="button"
                        onClick={() => setMapExpanded((open) => !open)}
                    >
                        {mapExpanded ? 'Свернуть' : 'Развернуть'}
                    </Button>
                </div>
                <div className="px-3 py-3 sm:px-5 sm:py-4">
                    <MapView
                        height="200px"
                        center={mapViewportCenter}
                        zoom={MAP_ZOOM_OVERVIEW}
                        markers={markers}
                        showLegend={markers.length > 1}
                    />
                </div>
            </Card>

            <Modal
                open={mapExpanded}
                onClose={() => setMapExpanded(false)}
                title="Карта мероприятия"
                description={markerSummary}
                size="lg"
            >
                {mapExpanded ? (
                    <MapView
                        height="min(480px, 55vh)"
                        center={mapViewportCenter}
                        zoom={MAP_ZOOM_DETAIL}
                        markers={markers}
                        showLegend={markers.length > 1}
                    />
                ) : null}
            </Modal>
        </>
    );
}
