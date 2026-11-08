import {afterEach, describe, expect, it, vi} from 'vitest';
import {act, render, screen} from '@testing-library/react';
import {useInViewport} from './in-viewport';

type ObserverCallback = (entries: readonly Pick<IntersectionObserverEntry, 'isIntersecting'>[]) => void;

function Probe() {
    const [ref, visible] = useInViewport();
    return <div ref={ref}>{visible ? 'visible' : 'hidden'}</div>;
}

describe(useInViewport, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('turns visible once the element enters the viewport and stays so', () => {
        const callbacks: ObserverCallback[] = [];
        const disconnect = vi.fn<() => void>();
        vi.stubGlobal('IntersectionObserver', class {
            constructor(callback: ObserverCallback) {
                callbacks.push(callback);
            }
            observe = vi.fn<() => void>();
            disconnect = disconnect;
        });
        render(<Probe/>);

        expect(screen.getByText('hidden')).toBeInTheDocument();

        act(() => {
            callbacks.forEach((callback) => {
                callback([{isIntersecting: true}]);
            });
        });

        expect(screen.getByText('visible')).toBeInTheDocument();
        expect(disconnect).toHaveBeenCalledWith();
    });

    it('is visible at once where intersection cannot be observed', () => {
        vi.stubGlobal('IntersectionObserver', undefined);

        render(<Probe/>);

        expect(screen.getByText('visible')).toBeInTheDocument();
    });
});
