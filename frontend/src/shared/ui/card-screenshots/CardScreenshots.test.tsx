import {describe, expect, it} from 'vitest';
import {fireEvent, render, screen} from '@testing-library/react';
import {CardScreenshots} from './CardScreenshots';

const URLS = ['/api/screenshots/1', '/api/screenshots/2', '/api/screenshots/3'];

/** The strips the pointer goes over, left to right. */
function strips(container: HTMLElement): HTMLElement[] {
    return Array.from(container.querySelectorAll<HTMLElement>('.strip'));
}

/** The strip of the picture at the index. */
function strip(container: HTMLElement, index: number): HTMLElement {
    const found = strips(container)[index];
    if (found === undefined) {
        throw new Error(`No strip ${String(index)}`);
    }
    return found;
}

/** The frame of the pictures. */
function frame(container: HTMLElement): Element {
    const found = container.firstElementChild;
    if (found === null) {
        throw new Error('No frame');
    }
    return found;
}

describe(CardScreenshots, () => {
    it('shows the first picture in a 4:3 frame, loading the pictures lazily', () => {
        const {container} = render(<CardScreenshots urls={URLS} alt="Elite"/>);

        expect(container.firstElementChild).toHaveClass('frame');
        expect(screen.getByRole('img', {name: 'Elite'})).toHaveAttribute('src', '/api/screenshots/1');

        const pictures = container.querySelectorAll('img');

        expect(pictures).toHaveLength(3);

        pictures.forEach((picture) => {
            expect(picture).toHaveAttribute('loading', 'lazy');
        });
    });

    it('shows the picture of the strip the pointer is over and the first once it leaves', () => {
        const {container} = render(<CardScreenshots urls={URLS} alt="Elite"/>);
        fireEvent.pointerEnter(strip(container, 2));

        expect(screen.getByRole('img', {name: 'Elite'})).toHaveAttribute('src', '/api/screenshots/3');

        fireEvent.pointerEnter(strip(container, 1));

        expect(screen.getByRole('img', {name: 'Elite'})).toHaveAttribute('src', '/api/screenshots/2');

        fireEvent.pointerLeave(frame(container));

        expect(screen.getByRole('img', {name: 'Elite'})).toHaveAttribute('src', '/api/screenshots/1');
    });

    it('marks the strip of the shown picture', () => {
        const {container} = render(<CardScreenshots urls={URLS} alt="Elite"/>);

        expect(strips(container).map((strip) => strip.classList.contains('active'))).toStrictEqual([true, false, false]);

        fireEvent.pointerEnter(strip(container, 2));

        expect(strips(container).map((strip) => strip.classList.contains('active'))).toStrictEqual([false, false, true]);
    });

    it('has no strips for a single picture', () => {
        const {container} = render(<CardScreenshots urls={['/api/screenshots/1']} alt="Elite"/>);

        expect(strips(container)).toHaveLength(0);
        expect(screen.getByRole('img', {name: 'Elite'})).toHaveAttribute('src', '/api/screenshots/1');
    });

    it('shows the first picture again when the pictures change', () => {
        const {container, rerender} = render(<CardScreenshots urls={URLS} alt="Elite"/>);
        fireEvent.pointerEnter(strip(container, 2));

        rerender(<CardScreenshots urls={['/api/screenshots/7', '/api/screenshots/8']} alt="Elite"/>);

        expect(screen.getByRole('img', {name: 'Elite'})).toHaveAttribute('src', '/api/screenshots/7');
    });
});
