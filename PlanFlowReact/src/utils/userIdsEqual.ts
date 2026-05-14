export function userIdsEqual(a: number | undefined, b: unknown): boolean {
    if (a === undefined || b === undefined || b === null)
        return false;
    return Number(a) === Number(b);
}
