import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {act, fireEvent, render, screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {ScreenshotGallery} from './ScreenshotGallery';

const URLS = ['/api/screenshots/1', '/api/screenshots/2', '/api/screenshots/3'];

let nativeSize: {width: number; height: number};
let stageSize: {width: number; height: number};
let resize: () => void;

describe(ScreenshotGallery, () => {
    beforeEach(() => {
        nativeSize = {width: 256, height: 192};
        stageSize = {width: 1000, height: 800};
        resize = () => undefined;
        vi.stubGlobal('ResizeObserver', class {
            constructor(callback: () => void) {
                resize = callback;
            }

            observe(target: Element): void {
                Object.defineProperties(target, {
                    clientWidth: {configurable: true, get: () => stageSize.width},
                    clientHeight: {configurable: true, get: () => stageSize.height},
                });
                resize();
            }

            disconnect(): void {
                resize = () => undefined;
            }
        });
        vi.stubGlobal('Image', class {
            naturalWidth = 0;
            naturalHeight = 0;
            onload: (() => void) | null = null;

            set src(_value: string) {
                this.naturalWidth = nativeSize.width;
                this.naturalHeight = nativeSize.height;
                this.onload?.();
            }
        });
    });

    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows every screenshot as a thumbnail button in its order', () => {
        render(<ScreenshotGallery urls={URLS} title="Elite"/>);

        const thumbnails = screen.getAllByRole('button', {name: /^Screenshot \d of 3$/u});

        expect(thumbnails.map((thumbnail) => within(thumbnail).getByRole('img', {hidden: true}).getAttribute('src'))).toStrictEqual(URLS);
    });

    it('puts the controls given for a thumbnail on it, beside its button', () => {
        render(<ScreenshotGallery urls={URLS} title="Elite" controls={(index) => <span>{`controls of ${String(index + 1)}`}</span>}/>);

        const items = screen.getAllByRole('listitem');

        expect(items.map((item) => item.textContent)).toStrictEqual(['controls of 1', 'controls of 2', 'controls of 3']);
        expect(within(screen.getByRole('button', {name: 'Screenshot 1 of 3'})).queryByText('controls of 1')).not.toBeInTheDocument();
    });

    it('opens the picture of a thumbnail whole in a viewer named by the title', async () => {
        render(<ScreenshotGallery urls={URLS} title="Elite"/>);

        await userEvent.click(screen.getByRole('button', {name: 'Screenshot 2 of 3'}));

        const viewer = screen.getByRole('dialog', {name: 'Elite'});

        expect(within(viewer).getByRole('img', {name: 'Screenshot 2 of 3'})).toHaveAttribute('src', '/api/screenshots/2');
    });


    it('shows the picture at the largest whole multiple of its native size that fits and recalculates it on resize', async () => {
        render(<ScreenshotGallery urls={URLS} title="Elite"/>);
        await userEvent.click(screen.getByRole('button', {name: 'Screenshot 1 of 3'}));
        const picture = within(screen.getByRole('dialog', {name: 'Elite'})).getByRole('img', {name: 'Screenshot 1 of 3'});

        expect(picture).toHaveAttribute('width', '768');
        expect(picture).toHaveAttribute('height', '576');

        stageSize = {width: 544, height: 416};
        act(() => {
            resize();
        });

        expect(picture).toHaveAttribute('width', '512');
        expect(picture).toHaveAttribute('height', '384');
    });

    it('shrinks a picture larger than the viewport just enough to fit', async () => {
        nativeSize = {width: 1000, height: 500};
        stageSize = {width: 532, height: 900};
        render(<ScreenshotGallery urls={URLS} title="Elite"/>);

        await userEvent.click(screen.getByRole('button', {name: 'Screenshot 1 of 3'}));

        const picture = within(screen.getByRole('dialog', {name: 'Elite'})).getByRole('img', {name: 'Screenshot 1 of 3'});

        expect(picture).toHaveAttribute('width', '500');
        expect(picture).toHaveAttribute('height', '250');
    });

    it('goes through the pictures by its buttons and the arrow keys, round from the last to the first', async () => {
        render(<ScreenshotGallery urls={URLS} title="Elite"/>);
        await userEvent.click(screen.getByRole('button', {name: 'Screenshot 3 of 3'}));
        const viewer = screen.getByRole('dialog', {name: 'Elite'});

        await userEvent.click(within(viewer).getByRole('button', {name: 'Next screenshot'}));

        expect(within(viewer).getByRole('img', {name: 'Screenshot 1 of 3'})).toBeInTheDocument();

        await userEvent.click(within(viewer).getByRole('button', {name: 'Previous screenshot'}));

        expect(within(viewer).getByRole('img', {name: 'Screenshot 3 of 3'})).toBeInTheDocument();

        await userEvent.keyboard('{ArrowRight}{ArrowRight}');

        expect(within(viewer).getByRole('img', {name: 'Screenshot 2 of 3'})).toBeInTheDocument();

        await userEvent.keyboard('{ArrowLeft}');

        expect(within(viewer).getByRole('img', {name: 'Screenshot 1 of 3'})).toBeInTheDocument();
    });

    it('closes the viewer by Escape', async () => {
        render(<ScreenshotGallery urls={URLS} title="Elite"/>);
        await userEvent.click(screen.getByRole('button', {name: 'Screenshot 1 of 3'}));

        await userEvent.keyboard('{Escape}');

        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('closes the viewer by a click on its backdrop around the picture', async () => {
        render(<ScreenshotGallery urls={URLS} title="Elite"/>);
        await userEvent.click(screen.getByRole('button', {name: 'Screenshot 1 of 3'}));

        await userEvent.click(screen.getByRole('dialog', {name: 'Elite'}));

        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('closes the viewer by its cross', async () => {
        render(<ScreenshotGallery urls={URLS} title="Elite"/>);
        await userEvent.click(screen.getByRole('button', {name: 'Screenshot 1 of 3'}));

        await userEvent.click(within(screen.getByRole('dialog', {name: 'Elite'})).getByRole('button', {name: 'Close'}));

        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('keeps the viewer open on a click on the picture and on its buttons', async () => {
        render(<ScreenshotGallery urls={URLS} title="Elite"/>);
        await userEvent.click(screen.getByRole('button', {name: 'Screenshot 1 of 3'}));
        const viewer = screen.getByRole('dialog', {name: 'Elite'});

        await userEvent.click(within(viewer).getByRole('img', {name: 'Screenshot 1 of 3'}));
        await userEvent.click(within(viewer).getByRole('button', {name: 'Next screenshot'}));
        await userEvent.click(within(viewer).getByRole('button', {name: 'Previous screenshot'}));

        expect(screen.getByRole('dialog', {name: 'Elite'})).toBeInTheDocument();
    });

    it('keeps the viewer open when a press on the picture is released on the backdrop', async () => {
        render(<ScreenshotGallery urls={URLS} title="Elite"/>);
        await userEvent.click(screen.getByRole('button', {name: 'Screenshot 1 of 3'}));
        const viewer = screen.getByRole('dialog', {name: 'Elite'});

        fireEvent.pointerDown(within(viewer).getByRole('img', {name: 'Screenshot 1 of 3'}));
        fireEvent.pointerUp(viewer);
        fireEvent.click(viewer);

        expect(screen.getByRole('dialog', {name: 'Elite'})).toBeInTheDocument();
    });

    it('has nothing to go through for a single screenshot', async () => {
        render(<ScreenshotGallery urls={['/api/screenshots/1']} title="Elite"/>);
        await userEvent.click(screen.getByRole('button', {name: 'Screenshot 1 of 1'}));

        expect(screen.queryByRole('button', {name: 'Next screenshot'})).not.toBeInTheDocument();
        expect(screen.queryByRole('button', {name: 'Previous screenshot'})).not.toBeInTheDocument();
    });
});
