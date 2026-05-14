import { describe, expect, it } from 'vitest';
import { toastsActions, toastsReducer } from '@/store/slices/toasts/toastsSlice';
describe('toastsSlice', () => {
    it('pushes and dismisses toasts', () => {
        let state = toastsReducer(undefined, { type: '@@INIT' });
        state = toastsReducer(state, toastsActions.push({ level: 'info', message: 'hello' }));
        expect(state.queue).toHaveLength(1);
        const id = state.queue[0]?.id ?? '';
        state = toastsReducer(state, toastsActions.dismiss(id));
        expect(state.queue).toHaveLength(0);
    });
    it('clears the queue', () => {
        let state = toastsReducer(undefined, { type: '@@INIT' });
        state = toastsReducer(state, toastsActions.push({ level: 'error', message: 'x' }));
        state = toastsReducer(state, toastsActions.push({ level: 'success', message: 'y' }));
        state = toastsReducer(state, toastsActions.clear());
        expect(state.queue).toHaveLength(0);
    });
});
