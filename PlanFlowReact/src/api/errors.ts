import axios, { type AxiosError } from 'axios';
import { type ApiErrorResponse, type AppApiError } from '@/types';
export const ERROR_CODE = {
    NETWORK: 'NETWORK_ERROR',
    TIMEOUT: 'TIMEOUT',
    UNAUTHORIZED: 'AUTH_FAILED',
    FORBIDDEN: 'ACCESS_DENIED',
    VALIDATION_FAILED: 'VALIDATION_FAILED',
    EXTERNAL_UNAVAILABLE: 'EXTERNAL_SUPPLIER_UNAVAILABLE',
    EXTERNAL_TIMEOUT: 'EXTERNAL_SUPPLIER_TIMEOUT',
    UNKNOWN: 'UNKNOWN',
    CANCELLED: 'CANCELLED',
} as const;
const parseFieldErrors = (raw: unknown): Record<string, string> | undefined => {
    if (typeof raw !== 'object' || raw === null || Array.isArray(raw))
        return undefined;
    const out: Record<string, string> = {};
    for (const [k, v] of Object.entries(raw)) {
        if (typeof v === 'string')
            out[k] = v;
    }
    return Object.keys(out).length > 0 ? out : undefined;
};
const isApiErrorResponse = (data: unknown): data is ApiErrorResponse => {
    if (typeof data !== 'object' || data === null)
        return false;
    const candidate = data as Record<string, unknown>;
    return (typeof candidate.message === 'string' &&
        typeof candidate.errorCode === 'string' &&
        typeof candidate.timestamp === 'number');
};
export const parseApiError = (error: unknown): AppApiError => {
    if (axios.isCancel(error)) {
        return {
            message: 'Запрос отменен',
            errorCode: ERROR_CODE.CANCELLED,
            httpStatus: null,
            timestamp: Date.now(),
        };
    }
    if (axios.isAxiosError(error)) {
        const axiosError = error as AxiosError<unknown>;
        const status = axiosError.response?.status ?? null;
        const data = axiosError.response?.data;
        if (isApiErrorResponse(data)) {
            const raw = data as unknown as Record<string, unknown>;
            const fieldErrors = parseFieldErrors(raw.fieldErrors);
            const base: AppApiError = {
                message: data.message,
                errorCode: data.errorCode,
                httpStatus: status,
                timestamp: data.timestamp,
            };
            if (fieldErrors !== undefined) {
                base.fieldErrors = fieldErrors;
            }
            return base;
        }
        if (axiosError.code === 'ECONNABORTED') {
            return {
                message: 'Превышено время ожидания запроса',
                errorCode: ERROR_CODE.TIMEOUT,
                httpStatus: status,
                timestamp: Date.now(),
            };
        }
        if (axiosError.response === undefined) {
            return {
                message: 'Нет соединения с сервером',
                errorCode: ERROR_CODE.NETWORK,
                httpStatus: null,
                timestamp: Date.now(),
            };
        }
        if (status === 403) {
            return {
                message: 'Доступ запрещен. Выйдите из аккаунта и войдите снова или обратитесь к администратору.',
                errorCode: ERROR_CODE.FORBIDDEN,
                httpStatus: status,
                timestamp: Date.now(),
            };
        }
        if (status === 401) {
            return {
                message: 'Требуется повторный вход.',
                errorCode: ERROR_CODE.UNAUTHORIZED,
                httpStatus: status,
                timestamp: Date.now(),
            };
        }
        return {
            message: axiosError.message || 'Ошибка запроса',
            errorCode: ERROR_CODE.UNKNOWN,
            httpStatus: status,
            timestamp: Date.now(),
        };
    }
    if (error instanceof Error) {
        return {
            message: error.message,
            errorCode: ERROR_CODE.UNKNOWN,
            httpStatus: null,
            timestamp: Date.now(),
        };
    }
    return {
        message: 'Неизвестная ошибка',
        errorCode: ERROR_CODE.UNKNOWN,
        httpStatus: null,
        timestamp: Date.now(),
    };
};
export const isExternalSupplierError = (err: AppApiError): boolean => err.errorCode === ERROR_CODE.EXTERNAL_UNAVAILABLE ||
    err.errorCode === ERROR_CODE.EXTERNAL_TIMEOUT;
export const isNetworkError = (err: AppApiError): boolean => err.errorCode === ERROR_CODE.NETWORK || err.errorCode === ERROR_CODE.TIMEOUT;
