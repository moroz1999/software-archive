import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {act, renderHook} from '@testing-library/react';
import {useLoadingLock} from './loading-lock';

function renderLock(initial: boolean) {
    return renderHook((loading: boolean) => useLoadingLock(loading), {initialProps: initial});
}

function advance(ms: number) {
    act(() => {
        vi.advanceTimersByTime(ms);
    });
}

describe(useLoadingLock, () => {
    beforeEach(() => {
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it('does not lock without a load', () => {
        const {result} = renderLock(false);

        advance(500);

        expect(result.current).toBe(false);
    });

    it('never locks for a load shorter than 100 ms', () => {
        const {result, rerender} = renderLock(true);

        expect(result.current).toBe(false);

        advance(99);

        expect(result.current).toBe(false);

        rerender(false);
        advance(500);

        expect(result.current).toBe(false);
    });

    it('locks once the load lasts 100 ms', () => {
        const {result} = renderLock(true);

        advance(100);

        expect(result.current).toBe(true);
    });

    it('unlocks as soon as the load ends', () => {
        const {result, rerender} = renderLock(true);
        advance(100);

        rerender(false);

        expect(result.current).toBe(false);
    });

    it('waits the 100 ms anew for the next load', () => {
        const {result, rerender} = renderLock(true);
        advance(60);
        rerender(false);

        rerender(true);
        advance(60);

        expect(result.current).toBe(false);

        advance(40);

        expect(result.current).toBe(true);
    });
});
