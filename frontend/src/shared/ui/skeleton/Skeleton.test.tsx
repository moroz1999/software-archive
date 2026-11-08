import {afterEach, describe, expect, it, vi} from 'vitest';
import {act, render} from '@testing-library/react';
import {Skeleton} from './Skeleton';
import {resetVisibilityObserver} from './visibility';

type ObserverCallback = (entries: readonly Pick<IntersectionObserverEntry, 'target' | 'isIntersecting'>[]) => void;

function stubIntersectionObserver() {
    const callbacks: ObserverCallback[] = [];
    vi.stubGlobal('IntersectionObserver', class {
        constructor(callback: ObserverCallback) {
            callbacks.push(callback);
        }
        observe = vi.fn<() => void>();
        unobserve = vi.fn<() => void>();
        disconnect = vi.fn<() => void>();
    });
    return (target: Element, isIntersecting: boolean) => {
        act(() => {
            for (const callback of callbacks) {
                callback([{target, isIntersecting}]);
            }
        });
    };
}

function boneOf(container: HTMLElement): Element {
    const bone = container.firstElementChild;
    if (bone === null) {
        throw new Error('No bone rendered');
    }
    return bone;
}

describe(Skeleton, () => {
    afterEach(() => {
        resetVisibilityObserver();
        vi.unstubAllGlobals();
    });

    it('is a hidden, animated block placeholder by default', () => {
        const {container} = render(<Skeleton/>);

        expect(container.firstElementChild).toHaveAttribute('aria-hidden', 'true');
        expect(container.firstElementChild).toHaveClass('bone', 'animated');
        expect(container.firstElementChild).not.toHaveClass('inline');
    });

    it('can be static and inline', () => {
        const {container} = render(<Skeleton animated={false} inline/>);

        expect(container.firstElementChild).toHaveClass('inline');
        expect(container.firstElementChild).not.toHaveClass('animated');
    });

    it('is not painted while off screen', () => {
        const intersect = stubIntersectionObserver();
        const {container} = render(<Skeleton/>);
        const bone = boneOf(container);

        expect(bone).toHaveAttribute('data-offscreen');

        intersect(bone, true);

        expect(bone).not.toHaveAttribute('data-offscreen');

        intersect(bone, false);

        expect(bone).toHaveAttribute('data-offscreen');
    });
});
