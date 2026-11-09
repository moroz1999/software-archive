/** The page of a list in a raw value from the address, where pages count from 1; the first page, counted from 0, when it cannot be one. */
export function parsePageParam(raw: string | null): number {
    return raw !== null && /^\d+$/u.test(raw) && Number(raw) >= 1 ? Number(raw) - 1 : 0;
}

/** The page counted from 0 as the address writes it, counted from 1; null for the first page, which the address leaves out. */
export function pageParam(page: number): string | null {
    return page > 0 ? String(page + 1) : null;
}
