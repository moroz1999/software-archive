import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {act, renderHook} from '@testing-library/react';
import {useSkeleton, type SkeletonQuery} from './skeleton';

vi.unmock('./skeleton-timing');

const LOADING: SkeletonQuery = {isPending: true, isFetching: true};
const WAITING: SkeletonQuery = {isPending: true, isFetching: false};
const LOADED: SkeletonQuery = {isPending: false, isFetching: false};

function renderSkeleton(initial: SkeletonQuery) {
    return renderHook((query: SkeletonQuery) => useSkeleton(query), {initialProps: initial});
}

function advance(ms: number) {
    act(() => {
        vi.advanceTimersByTime(ms);
    });
}

describe(useSkeleton, () => {
    beforeEach(() => {
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it('keeps the skeleton for half a second since the call started when the data comes sooner', () => {
        const {result, rerender} = renderSkeleton(LOADING);
        advance(100);

        rerender(LOADED);

        expect(result.current).toBe(true);

        advance(399);

        expect(result.current).toBe(true);

        advance(1);

        expect(result.current).toBe(false);
    });

    it('shows the data at once when it comes after half a second', () => {
        const {result, rerender} = renderSkeleton(LOADING);
        advance(700);

        rerender(LOADED);

        expect(result.current).toBe(false);
    });

    it('never shows a skeleton for data that is already there', () => {
        const {result, rerender} = renderSkeleton(LOADED);

        expect(result.current).toBe(false);

        rerender({isPending: false, isFetching: true});

        expect(result.current).toBe(false);
    });

    it('counts the half second from the start of the call, not from a skeleton waiting for it', () => {
        const {result, rerender} = renderSkeleton(WAITING);
        advance(1000);
        rerender(LOADING);
        advance(50);

        rerender(LOADED);

        expect(result.current).toBe(true);

        advance(450);

        expect(result.current).toBe(false);
    });
});
