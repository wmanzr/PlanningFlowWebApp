export type MapMarkerKind = 'event' | 'task' | 'candidate';

export type MapMarkerEmphasis = 'primary' | 'default';

export interface MapMarker {
    id: string;
    lat: number;
    lng: number;
    kind: MapMarkerKind;
    label?: string;
    emphasis?: MapMarkerEmphasis;
}
