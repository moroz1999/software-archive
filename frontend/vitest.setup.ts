import '@testing-library/jest-dom/vitest';
import {afterEach, vi} from 'vitest';
import {cleanup} from '@testing-library/react';

afterEach(() => {
    cleanup();
});

// Skeletons are not held in tests unless a test unmocks the timing to check exactly that (docs/testing.md).
vi.mock('@/shared/lib/skeleton/skeleton-timing', () => ({SKELETON_MIN_MS: 0}));
