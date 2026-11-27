/// <reference lib="dom" />
import '@testing-library/jest-dom/vitest';
import {afterEach, vi} from 'vitest';
import {cleanup, configure} from '@testing-library/react';
import i18n from 'i18next';
import {initReactI18next} from 'react-i18next';
import {resources} from './src/app/i18n/resources';
import {keepSentForms} from './src/shared/lib/test/fetch';

void i18n.use(initReactI18next).init({
    resources,
    lng: 'en',
    fallbackLng: 'en',
    initAsync: false,
    interpolation: {escapeValue: false},
    react: {useSuspense: false},
});

// A routed page is imported lazily, and its first import compiles all its code: that alone takes about a second, longer
// under a full run. Waiting for what a page shows has to cover it, or a page that is in time fails as late.
configure({asyncUtilTimeout: 3000});

// The API client is made for the page's origin; the Stylelint plugin tests run in node, with no page and no session.
// Every Request keeps its multipart form for the test to read; the API client takes Request as it is made.
if (typeof document !== 'undefined') {
    keepSentForms();
}
const session = typeof document === 'undefined' ? null : await import('./src/shared/api/session');

afterEach(() => {
    cleanup();
    session?.resetSessionForTests();
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

// jsdom has no Popover API, yet hides every popover until it is shown. Showing and hiding one toggle a marker that
// this stylesheet shows, as the browser shows an open popover; tests spy on the calls to see that overlays make them.
// jsdom weighs its own stylesheet against the page's by specificity alone, so the rule needs `!important` to win as an
// author rule does in a browser.
// The Stylelint plugin tests run in node, with no DOM to stand in for.
if (typeof document !== 'undefined') {
    const shownPopover = 'data-popover-open';
    const popoverStyle = document.createElement('style');
    popoverStyle.textContent = `[popover][${shownPopover}] { display: block !important; }`;
    document.head.append(popoverStyle);
    Object.defineProperties(HTMLElement.prototype, {
        showPopover: {writable: true, configurable: true, value(this: HTMLElement) {
            this.setAttribute(shownPopover, '');
        }},
        hidePopover: {writable: true, configurable: true, value(this: HTMLElement) {
            this.removeAttribute(shownPopover);
        }},
    });
}
