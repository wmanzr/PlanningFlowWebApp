import { useEffect, useRef } from 'react';
import { load } from '@2gis/mapgl';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import { cn } from '../cn';
import type { GeoPoint } from '@/types';
const DEFAULT_CENTER: GeoPoint = { latitude: 55.751244, longitude: 37.618423 };
const DEFAULT_ZOOM = 11;
const MARKER_PALETTE: Record<MapMarkerKind, string> = {
    event: 'var(--color-button)',
    task: 'var(--color-tertiary)',
    candidate: 'var(--color-highlight)',
};
export type MapMarkerKind = 'event' | 'task' | 'candidate';
export interface MapMarker {
    id: string;
    lat: number;
    lng: number;
    kind: MapMarkerKind;
    label?: string;
}
export interface MapViewProps {
    center?: GeoPoint;
    zoom?: number;
    markers?: MapMarker[];
    onMarkerClick?: (id: string) => void;
    onMapClick?: (point: GeoPoint) => void;
    height?: string;
    className?: string;
}
interface MapInstance {
    destroy: () => void;
    setCenter: (lngLat: [
        number,
        number
    ]) => void;
    setZoom: (zoom: number) => void;
    on: (event: string, handler: (e: {
        lngLat: [
            number,
            number
        ];
    }) => void) => void;
}
interface MarkerInstance {
    destroy: () => void;
    on?: (event: string, handler: () => void) => void;
}
interface MapglApi {
    Map: new (container: HTMLElement, options: {
        center: [
            number,
            number
        ];
        zoom: number;
        key: string;
        style?: string;
    }) => MapInstance;
    Marker: new (map: MapInstance, options: {
        coordinates: [
            number,
            number
        ];
        label?: {
            text: string;
        };
        icon?: string;
    }) => MarkerInstance;
    HtmlMarker?: new (map: MapInstance, options: {
        coordinates: [
            number,
            number
        ];
        html: string;
    }) => MarkerInstance;
}
export const MapView = ({ center, zoom = DEFAULT_ZOOM, markers = [], onMarkerClick, onMapClick, height = '400px', className, }: MapViewProps) => {
    const containerRef = useRef<HTMLDivElement | null>(null);
    const mapRef = useRef<MapInstance | null>(null);
    const markerRefs = useRef<Map<string, MarkerInstance>>(new Map());
    const apiKey = import.meta.env.VITE_2GIS_API_KEY;
    useEffect(() => {
        if (!containerRef.current)
            return undefined;
        if (!apiKey)
            return undefined;
        let disposed = false;
        let createdMap: MapInstance | null = null;
        void load().then((api) => {
            if (disposed || !containerRef.current)
                return;
            const mapglApi = api as unknown as MapglApi;
            const initialCenter: [
                number,
                number
            ] = center
                ? [center.longitude, center.latitude]
                : [DEFAULT_CENTER.longitude, DEFAULT_CENTER.latitude];
            createdMap = new mapglApi.Map(containerRef.current, {
                center: initialCenter,
                zoom,
                key: apiKey,
            });
            mapRef.current = createdMap;
            if (onMapClick) {
                createdMap.on('click', (e) => {
                    if (!e.lngLat)
                        return;
                    onMapClick({ latitude: e.lngLat[1], longitude: e.lngLat[0] });
                });
            }
        });
        const markersSnapshot = markerRefs.current;
        return () => {
            disposed = true;
            markersSnapshot.forEach((marker) => marker.destroy());
            markersSnapshot.clear();
            mapRef.current?.destroy();
            mapRef.current = null;
            createdMap?.destroy();
        };
    }, [apiKey]);
    useEffect(() => {
        if (!mapRef.current)
            return;
        if (center)
            mapRef.current.setCenter([center.longitude, center.latitude]);
        mapRef.current.setZoom(zoom);
    }, [center, zoom]);
    useEffect(() => {
        if (!mapRef.current)
            return;
        void load().then((api) => {
            const mapglApi = api as unknown as MapglApi;
            const map = mapRef.current;
            if (!map)
                return;
            markerRefs.current.forEach((marker) => marker.destroy());
            markerRefs.current.clear();
            markers.forEach((marker) => {
                const created = new mapglApi.Marker(map, {
                    coordinates: [marker.lng, marker.lat],
                    ...(marker.label !== undefined ? { label: { text: marker.label } } : {}),
                });
                if (onMarkerClick && created.on) {
                    created.on('click', () => onMarkerClick(marker.id));
                }
                markerRefs.current.set(marker.id, created);
            });
        });
    }, [markers, onMarkerClick]);
    if (!apiKey) {
        return (<Paper variant="outlined" className={cn(className)} sx={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                p: 3,
                textAlign: 'center',
                borderStyle: 'dashed',
                height,
            }}>
        <Typography variant="body2" sx={{ fontWeight: 500 }} color="text.primary">
          Карта не настроена
        </Typography>
        <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5 }}>
          Укажите <code>VITE_2GIS_API_KEY</code> в <code>.env.local</code>, чтобы включить MapGL.
        </Typography>
        <Box component="ul" sx={{ mt: 1.5, display: 'grid', gap: 0.5, textAlign: 'left' }}>
          {markers.map((m) => (<li key={m.id}>
              <span className="mr-2 inline-block h-2 w-2 rounded-full" style={{ background: MARKER_PALETTE[m.kind] }}/>
              <Typography variant="caption" component="span">
                {m.label ?? `${m.lat.toFixed(4)}, ${m.lng.toFixed(4)}`}
              </Typography>
            </li>))}
        </Box>
      </Paper>);
    }
    return (<Paper ref={containerRef} variant="outlined" className={cn(className)} sx={{ height, width: '100%', overflow: 'hidden' }}/>);
};
