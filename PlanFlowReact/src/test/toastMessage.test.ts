import { describe, expect, it } from 'vitest';
import { toastDisplayMessage } from '@/components/ui/Toast/toastMessage';
describe('toastDisplayMessage', () => {
    it('returns trimmed error text without leading exclamation', () => {
        expect(toastDisplayMessage('error', 'Сеть недоступна')).toBe('Сеть недоступна');
    });
    it('strips leading exclamation and spaces from error text', () => {
        expect(toastDisplayMessage('error', '! Уже есть')).toBe('Уже есть');
        expect(toastDisplayMessage('error', '  ! с отступом  ')).toBe('с отступом');
    });
    it('leaves non-error levels unchanged', () => {
        expect(toastDisplayMessage('success', 'Сохранено')).toBe('Сохранено');
    });
});
