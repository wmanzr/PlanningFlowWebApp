import { describe, expect, it } from 'vitest';
import { AxiosError, AxiosHeaders } from 'axios';
import { ERROR_CODE, isNetworkError, parseApiError } from '@/api/errors';
describe('parseApiError', () => {
    it('preserves API error code from response payload', () => {
        const error = new AxiosError('Request failed', 'ERR_BAD_REQUEST', {
            url: '/api/v1/events',
            method: 'get',
            headers: new AxiosHeaders(),
        }, null, {
            status: 422,
            statusText: 'Unprocessable Entity',
            headers: {},
            config: { headers: new AxiosHeaders() },
            data: {
                message: 'invalid',
                errorCode: 'EVENT_VALIDATION_FAILED',
                timestamp: 1700000000000,
            },
        });
        const parsed = parseApiError(error);
        expect(parsed.errorCode).toBe('EVENT_VALIDATION_FAILED');
        expect(parsed.httpStatus).toBe(422);
    });
    it('marks network errors when no response', () => {
        const error = new AxiosError('connect refused', 'ECONNREFUSED');
        const parsed = parseApiError(error);
        expect(parsed.errorCode).toBe(ERROR_CODE.NETWORK);
        expect(isNetworkError(parsed)).toBe(true);
    });
    it('maps HTTP 403 without structured body to a readable message', () => {
        const error = new AxiosError('Request failed with status code 403', 'ERR_BAD_REQUEST', { url: '/api/v1/notifications', method: 'get', headers: new AxiosHeaders() }, null, {
            status: 403,
            statusText: 'Forbidden',
            headers: {},
            config: { headers: new AxiosHeaders() },
            data: {},
        });
        const parsed = parseApiError(error);
        expect(parsed.httpStatus).toBe(403);
        expect(parsed.errorCode).toBe(ERROR_CODE.FORBIDDEN);
        expect(parsed.message).toContain('Доступ запрещен');
    });
    it('maps failed login 403 without body to invalid credentials message', () => {
        const error = new AxiosError('Request failed with status code 403', 'ERR_BAD_REQUEST', { url: '/api/v1/auth/login', method: 'post', headers: new AxiosHeaders() }, null, {
            status: 403,
            statusText: 'Forbidden',
            headers: {},
            config: { headers: new AxiosHeaders() },
            data: {},
        });
        const parsed = parseApiError(error);
        expect(parsed.errorCode).toBe(ERROR_CODE.INVALID_CREDENTIALS);
        expect(parsed.message).toBe('Неверный логин или пароль');
    });
    it('uses API message for INVALID_CREDENTIALS on login', () => {
        const error = new AxiosError('Request failed', 'ERR_BAD_REQUEST', { url: '/api/v1/auth/login', method: 'post', headers: new AxiosHeaders() }, null, {
            status: 401,
            statusText: 'Unauthorized',
            headers: {},
            config: { headers: new AxiosHeaders() },
            data: {
                message: 'Неверный логин или пароль',
                errorCode: 'INVALID_CREDENTIALS',
                timestamp: 1700000000000,
            },
        });
        const parsed = parseApiError(error);
        expect(parsed.errorCode).toBe(ERROR_CODE.INVALID_CREDENTIALS);
        expect(parsed.message).toBe('Неверный логин или пароль');
    });
});
