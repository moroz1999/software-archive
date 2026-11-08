/**
 * One observer for every skeleton bone on the page: a bone outside the viewport is marked
 * `data-offscreen` and not painted, so long skeleton lists cost nothing off screen.
 */
let observer: IntersectionObserver | undefined;

function sharedObserver(): IntersectionObserver {
    observer ??= new IntersectionObserver((entries) => {
        for (const entry of entries) {
            entry.target.toggleAttribute('data-offscreen', !entry.isIntersecting);
        }
    });
    return observer;
}

/** Starts tracking the bone; returns the function that stops it. Without IntersectionObserver bones stay visible. */
export function trackVisibility(element: Element): () => void {
    if (typeof IntersectionObserver === 'undefined') {
        return () => undefined;
    }
    const current = sharedObserver();
    element.setAttribute('data-offscreen', '');
    current.observe(element);
    return () => {
        current.unobserve(element);
    };
}

/** Forgets the shared observer; for tests that replace IntersectionObserver. */
export function resetVisibilityObserver(): void {
    observer?.disconnect();
    observer = undefined;
}
