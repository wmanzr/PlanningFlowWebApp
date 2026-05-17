import type { MapMarker } from './mapMarker.types';

const LOCATION_BUCKET = 1e-5;

export interface PlacedMapMarker extends MapMarker {
    displayLat: number;
    displayLng: number;
    showLabelOnMap: boolean;
}

function locationKey(lat: number, lng: number): string {
    const latBucket = Math.round(lat / LOCATION_BUCKET);
    const lngBucket = Math.round(lng / LOCATION_BUCKET);
    return `${latBucket}:${lngBucket}`;
}

function markerSortWeight(marker: MapMarker): number {
    if (marker.emphasis === 'primary') {
        return 0;
    }
    if (marker.kind === 'event') {
        return 1;
    }
    if (marker.kind === 'task') {
        return 2;
    }
    return 3;
}

function ringOffset(index: number, total: number, centerLat: number): { dLat: number; dLng: number } {
    if (total <= 1) {
        return { dLat: 0, dLng: 0 };
    }
    const angle = (2 * Math.PI * index) / total - Math.PI / 2;
    const radius = 0.0001 + total * 0.00002;
    const cosLat = Math.cos((centerLat * Math.PI) / 180);
    return {
        dLat: radius * Math.sin(angle),
        dLng: (radius * Math.cos(angle)) / (cosLat || 1),
    };
}

export function layoutMapMarkers(markers: MapMarker[]): PlacedMapMarker[] {
    const groups = new Map<string, MapMarker[]>();

    for (const marker of markers) {
        const key = locationKey(marker.lat, marker.lng);
        const bucket = groups.get(key);
        if (bucket) {
            bucket.push(marker);
        } else {
            groups.set(key, [marker]);
        }
    }

    const placed: PlacedMapMarker[] = [];

    for (const group of groups.values()) {
        const sorted = [...group].sort((a, b) => markerSortWeight(a) - markerSortWeight(b));

        if (sorted.length === 1) {
            const only = sorted[0];
            if (!only) {
                continue;
            }
            placed.push({
                ...only,
                emphasis: only.emphasis ?? 'default',
                displayLat: only.lat,
                displayLng: only.lng,
                showLabelOnMap: only.emphasis !== 'primary',
            });
            continue;
        }

        const anchor = sorted[0];
        if (!anchor) {
            continue;
        }

        placed.push({
            ...anchor,
            emphasis: anchor.emphasis ?? 'default',
            displayLat: anchor.lat,
            displayLng: anchor.lng,
            showLabelOnMap: false,
        });

        const ring = sorted.slice(1);
        ring.forEach((marker, index) => {
            const { dLat, dLng } = ringOffset(index, ring.length, anchor.lat);
            placed.push({
                ...marker,
                emphasis: marker.emphasis ?? 'default',
                displayLat: anchor.lat + dLat,
                displayLng: anchor.lng + dLng,
                showLabelOnMap: false,
            });
        });
    }

    return placed;
}
