import {afterEach, describe, expect, it, vi} from 'vitest';
import {act, renderHook} from '@testing-library/react';
import {MOBILE_QUERY, useMediaQuery} from './media-query';

/** A screen that matches the query as told, and can be told otherwise. */
function stubScreen(matches: boolean) {
    const listeners = new Set<() => void>();
    const list = {
        matches,
        addEventListener: (_type: string, listener: () => void) => listeners.add(listener),
        removeEventListener: (_type: string, listener: () => void) => listeners.delete(listener),
    };
    vi.stubGlobal('matchMedia', vi.fn<(query: string) => typeof list>(() => list));
    return (now: boolean) => {
        list.matches = now;
        listeners.forEach((listener) => {
            listener();
        });
    };
}

describe(useMediaQuery, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('tells whether the screen matches the query', () => {
        stubScreen(true);

        expect(renderHook(() => useMediaQuery(MOBILE_QUERY)).result.current).toBe(true);
    });

    it('follows the screen when it changes', () => {
        const change = stubScreen(false);
        const {result} = renderHook(() => useMediaQuery(MOBILE_QUERY));

        act(() => {
            change(true);
        });

        expect(result.current).toBe(true);
    });

    it('is the width of a phone below 576px', () => {
        expect(MOBILE_QUERY).toBe('(max-width: 575.98px)');
    });
});
