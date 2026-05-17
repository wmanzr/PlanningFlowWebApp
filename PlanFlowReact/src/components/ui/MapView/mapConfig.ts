import type { GeoPoint } from '@/types';

export const MAP_DEFAULT_CENTER: GeoPoint = {
    latitude: 55.751244,
    longitude: 37.618423,
};

export const MAP_ZOOM_OVERVIEW = 10;

export const MAP_ZOOM_DETAIL = 12;

export const MAP_ZOOM_TASK_LABELS_MIN = 14;

export function geoPointFromLatLng(
    latitude: number | undefined,
    longitude: number | undefined,
): GeoPoint | undefined {
    if (
        latitude === undefined ||
        longitude === undefined ||
        !Number.isFinite(latitude) ||
        !Number.isFinite(longitude)
    ) {
        return undefined;
    }
    return { latitude, longitude };
}

export function resolveMapViewportCenter(
    ...candidates: Array<GeoPoint | undefined>
): GeoPoint {
    for (const point of candidates) {
        if (point !== undefined) {
            return point;
        }
    }
    return MAP_DEFAULT_CENTER;
}
