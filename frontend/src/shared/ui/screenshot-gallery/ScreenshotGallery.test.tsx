import {describe, expect, it} from 'vitest';
import {render, screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {ScreenshotGallery} from './ScreenshotGallery';

const URLS = ['/api/screenshots/1', '/api/screenshots/2', '/api/screenshots/3'];

describe(ScreenshotGallery, () => {
    it('shows every screenshot as a thumbnail button in its order', () => {
        render(<ScreenshotGallery urls={URLS} title="Elite"/>);

        const thumbnails = screen.getAllByRole('button', {name: /^Screenshot \d of 3$/u});

        expect(thumbnails.map((thumbnail) => within(thumbnail).getByRole('img', {hidden: true}).getAttribute('src'))).toStrictEqual(URLS);
    });

    it('opens the picture of a thumbnail whole in a viewer named by the title', async () => {
        render(<ScreenshotGallery urls={URLS} title="Elite"/>);

        await userEvent.click(screen.getByRole('button', {name: 'Screenshot 2 of 3'}));

        const viewer = screen.getByRole('dialog', {name: 'Elite'});

        expect(within(viewer).getByRole('img', {name: 'Screenshot 2 of 3'})).toHaveAttribute('src', '/api/screenshots/2');
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

    it('has nothing to go through for a single screenshot', async () => {
        render(<ScreenshotGallery urls={['/api/screenshots/1']} title="Elite"/>);
        await userEvent.click(screen.getByRole('button', {name: 'Screenshot 1 of 1'}));

        expect(screen.queryByRole('button', {name: 'Next screenshot'})).not.toBeInTheDocument();
        expect(screen.queryByRole('button', {name: 'Previous screenshot'})).not.toBeInTheDocument();
    });
});
