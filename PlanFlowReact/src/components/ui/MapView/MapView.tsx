import { useEffect, useMemo, useRef, useState } from 'react';
import { load } from '@2gis/mapgl';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import { useTheme } from '@mui/material/styles';
import { cn } from '../cn';
import type { GeoPoint } from '@/types';
import { MAP_DEFAULT_CENTER, MAP_ZOOM_OVERVIEW, MAP_ZOOM_TASK_LABELS_MIN } from './mapConfig';
import { layoutMapMarkers } from './mapMarkerLayout';
import { buildMapMarkerHtml, MAP_PRIMARY_PIN_COLOR, resolveMapMarkerColor } from './mapMarkerHtml';
import type { MapMarker, MapMarkerKind } from './mapMarker.types';
import { MapMarkersLegend } from './MapMarkersLegend';

export type { MapMarker, MapMarkerKind };

export interface MapViewProps {
    center?: GeoPoint;
    zoom?: number;
    viewResetKey?: number;
    markers?: MapMarker[];
    showLegend?: boolean;
    onMarkerClick?: (id: string) => void;
    onMapClick?: (point: GeoPoint) => void;
    height?: string;
    className?: string;
}

interface MapInstance {
    destroy: () => void;
    setCenter: (lngLat: [number, number]) => void;
    setZoom: (zoom: number) => void;
    getZoom: () => number;
    on: (event: string, handler: (e?: { lngLat?: [number, number] }) => void) => void;
}

interface HtmlMarkerInstance {
    destroy: () => void;
    on?: (event: string, handler: () => void) => void;
}

interface MapglApi {
    Map: new (
        container: HTMLElement,
        options: {
            center: [number, number];
            zoom: number;
            key: string;
        },
    ) => MapInstance;
    HtmlMarker: new (
        map: MapInstance,
        options: {
            coordinates: [number, number];
            html: string;
        },
    ) => HtmlMarkerInstance;
}

function toLngLat(point: GeoPoint): [number, number] {
    return [point.longitude, point.latitude];
}

export const MapView = ({
    center = MAP_DEFAULT_CENTER,
    zoom = MAP_ZOOM_OVERVIEW,
    viewResetKey = 0,
    markers = [],
    showLegend = false,
    onMarkerClick,
    onMapClick,
    height = '400px',
    className,
}: MapViewProps) => {
    const theme = useTheme();
    const containerRef = useRef<HTMLDivElement | null>(null);
    const mapRef = useRef<MapInstance | null>(null);
    const markerRefs = useRef<Map<string, HtmlMarkerInstance>>(new Map());
    const onMapClickRef = useRef(onMapClick);
    const onMarkerClickRef = useRef(onMarkerClick);
    const [mapReady, setMapReady] = useState(false);
    const [mapZoom, setMapZoom] = useState(zoom);

    const apiKey = (import.meta.env.VITE_2GIS_API_KEY ?? '').toString().trim();

    const primaryPinColor =
        theme.app?.button?.startsWith('#') === true ? theme.app.button : MAP_PRIMARY_PIN_COLOR;

    const placedMarkers = useMemo(() => layoutMapMarkers(markers), [markers]);
    const legendVisible = showLegend && markers.length > 0;

    onMapClickRef.current = onMapClick;
    onMarkerClickRef.current = onMarkerClick;

    useEffect(() => {
        if (!apiKey || !containerRef.current) {
            return undefined;
        }

        let disposed = false;

        void load().then((api) => {
            if (disposed || !containerRef.current) {
                return;
            }

            const mapglApi = api as unknown as MapglApi;
            const map = new mapglApi.Map(containerRef.current, {
                center: toLngLat(center),
                zoom,
                key: apiKey,
            });

            map.on('click', (e) => {
                if (!e?.lngLat) {
                    return;
                }
                onMapClickRef.current?.({
                    latitude: e.lngLat[1],
                    longitude: e.lngLat[0],
                });
            });

            const syncZoom = () => setMapZoom(map.getZoom());
            map.on('zoom', syncZoom);
            map.on('zoomend', syncZoom);
            syncZoom();

            mapRef.current = map;
            setMapReady(true);
        });

        return () => {
            disposed = true;
            setMapReady(false);
            markerRefs.current.forEach((marker) => marker.destroy());
            markerRefs.current.clear();
            mapRef.current?.destroy();
            mapRef.current = null;
        };
    }, [apiKey]);

    useEffect(() => {
        if (!mapReady || !mapRef.current) {
            return;
        }
        mapRef.current.setCenter(toLngLat(center));
        mapRef.current.setZoom(zoom);
        setMapZoom(mapRef.current.getZoom());
    }, [viewResetKey, mapReady, center, zoom]);

    useEffect(() => {
        if (!mapReady || !mapRef.current || !apiKey) {
            return;
        }

        void load().then((api) => {
            const map = mapRef.current;
            if (!map) {
                return;
            }

            const mapglApi = api as unknown as MapglApi;
            markerRefs.current.forEach((marker) => marker.destroy());
            markerRefs.current.clear();

            placedMarkers.forEach((marker) => {
                const emphasis = marker.emphasis ?? 'default';
                const color = resolveMapMarkerColor(marker.kind);
                const showLabelOnMap =
                    marker.showLabelOnMap &&
                    (marker.kind !== 'task' || mapZoom >= MAP_ZOOM_TASK_LABELS_MIN);
                const html = buildMapMarkerHtml(
                    marker.kind,
                    color,
                    marker.label,
                    showLabelOnMap,
                    emphasis,
                    primaryPinColor,
                );
                const created = new mapglApi.HtmlMarker(map, {
                    coordinates: [marker.displayLng, marker.displayLat],
                    html,
                });
                if (created.on) {
                    created.on('click', () => onMarkerClickRef.current?.(marker.id));
                }
                markerRefs.current.set(marker.id, created);
            });
        });
    }, [apiKey, mapReady, placedMarkers, primaryPinColor, mapZoom]);

    if (!apiKey) {
        return (
            <Paper
                variant="outlined"
                className={cn(className)}
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    p: 3,
                    textAlign: 'center',
                    borderStyle: 'dashed',
                    height,
                }}
            >
                <Typography variant="body2" sx={{ fontWeight: 500 }} color="text.primary">
                    Карта не настроена
                </Typography>
                <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5 }}>
                    Укажите <code>VITE_2GIS_API_KEY</code> в <code>.env</code>.
                </Typography>
                <MapMarkersLegend markers={markers} className="mt-3 w-full max-w-md text-left" />
            </Paper>
        );
    }

    return (
        <div className={cn('flex min-w-0 flex-col', className)}>
            <Paper
                variant="outlined"
                sx={{ height, width: '100%', overflow: 'hidden', position: 'relative', flexShrink: 0 }}
            >
                <Box ref={containerRef} sx={{ position: 'absolute', inset: 0 }} />
            </Paper>
            {legendVisible ? (
                <MapMarkersLegend
                    markers={markers}
                    className="mt-2 rounded-md border border-secondary/40 bg-surface-muted/60 px-3 py-2"
                />
            ) : null}
        </div>
    );
};

