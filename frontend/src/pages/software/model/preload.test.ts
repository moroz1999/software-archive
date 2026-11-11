import {describe, expect, it} from 'vitest';
import type {ShouldRevalidateFunctionArgs} from 'react-router';
import {shouldPreloadSoftwarePage} from './preload';

const GAMES = '00000000-0000-0000-00fd-0000000000c1';
const ACTION = '00000000-0000-0000-00fd-0000000000c2';
const ELITE = '00000000-0000-0000-00fd-000000000001';

function moving(from: string, to: string): ShouldRevalidateFunctionArgs {
    const args: ShouldRevalidateFunctionArgs = {
        currentUrl: new URL(from, 'http://archive'),
        currentParams: {},
        nextUrl: new URL(to, 'http://archive'),
        nextParams: {},
        defaultShouldRevalidate: true,
    };
    return args;
}

describe(shouldPreloadSoftwarePage, () => {
    it('does not preload another filter, category or page of the catalog', () => {
        expect(shouldPreloadSoftwarePage(moving('/software', '/software?year=1987&page=2'))).toBe(false);
        expect(shouldPreloadSoftwarePage(moving('/software', `/software/category/${GAMES}`))).toBe(false);
        expect(shouldPreloadSoftwarePage(moving(`/software/category/${GAMES}`, `/software/category/${ACTION}?year=1987`))).toBe(false);
        expect(shouldPreloadSoftwarePage(moving(`/software/category/${GAMES}`, '/software'))).toBe(false);
    });

    it('preloads when the catalog is entered from another page', () => {
        expect(shouldPreloadSoftwarePage(moving('/', '/software?year=1987'))).toBe(true);
        expect(shouldPreloadSoftwarePage(moving(`/software/${ELITE}`, `/software/category/${GAMES}`))).toBe(true);
    });
});
