import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import {LoadingState} from './LoadingState';

describe(LoadingState, () => {
    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('keeps the content visible and marks it busy while loading', () => {
        render(<LoadingState loading>Content</LoadingState>);

        expect(screen.getByText('Content')).toHaveAttribute('aria-busy', 'true');
        expect(screen.getByText('Content')).toHaveClass('content', 'loading');
    });

    it('is not busy and shows no spinner when idle', () => {
        render(<LoadingState loading={false}>Content</LoadingState>);

        expect(screen.getByText('Content')).toHaveAttribute('aria-busy', 'false');
        expect(screen.queryByRole('status', {hidden: true})).not.toBeInTheDocument();
    });

    it('centres the spinner on the visible part of the content', () => {
        vi.spyOn(HTMLElement.prototype, 'getBoundingClientRect').mockReturnValue(DOMRect.fromRect({x: -100, y: 200, width: 600, height: 2000}));
        Object.defineProperty(window, 'innerWidth', {configurable: true, value: 400});
        Object.defineProperty(window, 'innerHeight', {configurable: true, value: 1000});

        render(<LoadingState loading>Content</LoadingState>);

        const pane = screen.getByTestId('loading-state-spinner');

        expect(pane).toHaveClass('visible');
        expect(pane.style.getPropertyValue('--loading-state-spinner-x')).toBe('200px');
        expect(pane.style.getPropertyValue('--loading-state-spinner-y')).toBe('600px');
    });

    it('hides the spinner when the content is off screen', () => {
        vi.spyOn(HTMLElement.prototype, 'getBoundingClientRect').mockReturnValue(DOMRect.fromRect({x: 0, y: 2000, width: 400, height: 100}));
        Object.defineProperty(window, 'innerHeight', {configurable: true, value: 1000});

        render(<LoadingState loading>Content</LoadingState>);

        expect(screen.getByTestId('loading-state-spinner')).not.toHaveClass('visible');
    });

    it('fades the spinner out when loading ends', () => {
        vi.spyOn(HTMLElement.prototype, 'getBoundingClientRect').mockReturnValue(DOMRect.fromRect({x: 0, y: 0, width: 400, height: 100}));
        const {rerender} = render(<LoadingState loading>Content</LoadingState>);

        rerender(<LoadingState loading={false}>Content</LoadingState>);

        expect(screen.getByTestId('loading-state-spinner')).not.toHaveClass('visible');
        expect(screen.getByTestId('loading-state-spinner')).toHaveAttribute('aria-hidden', 'true');
    });
});
