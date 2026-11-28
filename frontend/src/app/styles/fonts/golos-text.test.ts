import {describe, expect, it} from 'vitest';
import entryHtml from '/index.html?raw';

describe('Golos Text', () => {
    it.each(['cyrillic', 'latin'])('The initial page preloads the %s font', (subset) => {
        const page = new DOMParser().parseFromString(entryHtml, 'text/html');
        const fontUrl = `/src/app/styles/fonts/golos-text-${subset}.woff2`;
        const preload = page.head.querySelector(`link[rel="preload"][as="font"][href="${fontUrl}"]`);

        expect(preload?.getAttribute('type')).toBe('font/woff2');
        expect(preload?.getAttribute('crossorigin')).toBe('anonymous');
    });
});
