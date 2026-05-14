const REFRESH_KEY = 'pf.refresh';
let accessTokenInMemory: string | null = null;
export const authStorage = {
    getAccessToken(): string | null {
        return accessTokenInMemory;
    },
    setAccessToken(token: string | null): void {
        accessTokenInMemory = token;
    },
    getRefreshToken(): string | null {
        try {
            return window.localStorage.getItem(REFRESH_KEY);
        }
        catch {
            return null;
        }
    },
    setRefreshToken(token: string | null): void {
        try {
            if (token) {
                window.localStorage.setItem(REFRESH_KEY, token);
            }
            else {
                window.localStorage.removeItem(REFRESH_KEY);
            }
        }
        catch {
        }
    },
    clear(): void {
        accessTokenInMemory = null;
        this.setRefreshToken(null);
    },
};
