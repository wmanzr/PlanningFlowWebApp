const enabled = import.meta.env.DEV;
export const logger = {
    debug: (...args: unknown[]): void => {
        if (enabled)
            console.warn('[debug]', ...args);
    },
    warn: (...args: unknown[]): void => {
        if (enabled)
            console.warn('[warn]', ...args);
    },
    error: (...args: unknown[]): void => {
        if (enabled)
            console.error('[error]', ...args);
    },
};
