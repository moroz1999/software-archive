import '@testing-library/jest-dom/vitest';
import {afterEach, vi} from 'vitest';
import {cleanup} from '@testing-library/react';

afterEach(() => {
    cleanup();
});

// Skeletons are not held in tests unless a test unmocks the timing to check exactly that (docs/testing.md).
vi.mock('@/shared/lib/skeleton/skeleton-timing', () => ({SKELETON_MIN_MS: 0}));

// jsdom has no media queries: every test screen is wider than a phone unless a test stubs `matchMedia` itself.
Object.defineProperty(globalThis, 'matchMedia', {
    writable: true,
    configurable: true,
    value: (query: string) => ({matches: false, media: query, addEventListener: () => undefined, removeEventListener: () => undefined}),
});
