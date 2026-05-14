import { authStorage } from '../authStorage';
import { ENDPOINTS } from '../endpoints';
import { logger } from '../_internal/logger';
export interface IncomingWsEvent {
    type: string;
    payload: unknown;
}
type Listener = (event: IncomingWsEvent) => void;
let socket: WebSocket | null = null;
const listeners = new Set<Listener>();
const buildWsUrl = (): string | null => {
    const token = authStorage.getAccessToken();
    if (!token)
        return null;
    const httpBase = (import.meta.env.VITE_API_BASE_URL ?? '').toString();
    const wsBase = httpBase
        ? httpBase.replace(/^http/, 'ws')
        : `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}`;
    return `${wsBase}${ENDPOINTS.ws.events}?access_token=${encodeURIComponent(token)}`;
};
export const eventsSocket = {
    connect(): void {
        if (socket && socket.readyState !== WebSocket.CLOSED)
            return;
        const url = buildWsUrl();
        if (!url)
            return;
        try {
            socket = new WebSocket(url);
            socket.addEventListener('message', (msg) => {
                try {
                    const parsed = JSON.parse(msg.data as string) as IncomingWsEvent;
                    listeners.forEach((listener) => listener(parsed));
                }
                catch (err) {
                    logger.warn('ws message parse failed', err);
                }
            });
            socket.addEventListener('error', (err) => logger.warn('ws error', err));
        }
        catch (err) {
            logger.warn('ws connect failed', err);
        }
    },
    disconnect(): void {
        socket?.close();
        socket = null;
    },
    subscribe(listener: Listener): () => void {
        listeners.add(listener);
        return () => listeners.delete(listener);
    },
};
