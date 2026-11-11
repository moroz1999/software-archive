import '@testing-library/jest-dom/vitest';
import {afterEach, vi} from 'vitest';
import {cleanup} from '@testing-library/react';
import i18n from 'i18next';
import {initReactI18next} from 'react-i18next';
import {resources} from './src/app/i18n/resources';

void i18n.use(initReactI18next).init({
    resources,
    lng: 'en',
    fallbackLng: 'en',
    initAsync: false,
    interpolation: {escapeValue: false},
    react: {useSuspense: false},
});

afterEach(() => {
    cleanup();
    void i18n.changeLanguage('en');
});

// Skeletons are not held in tests unless a test unmocks the timing to check exactly that (docs/testing.md).
vi.mock('@/shared/lib/loading/skeleton-timing', () => ({SKELETON_MIN_MS: 0}));

// jsdom has no media queries: every test screen is wider than a phone unless a test stubs `matchMedia` itself.
Object.defineProperty(globalThis, 'matchMedia', {
    writable: true,
    configurable: true,
    value: (query: string) => ({matches: false, media: query, addEventListener: () => undefined, removeEventListener: () => undefined}),
});
