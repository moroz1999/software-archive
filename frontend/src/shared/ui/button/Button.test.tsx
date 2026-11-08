import {afterEach, describe, expect, it, vi} from 'vitest';
import {act, fireEvent, render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter, Route, Routes} from 'react-router';
import {Button} from './Button';

describe(Button, () => {
    afterEach(() => {
        vi.useRealTimers();
    });

    it('renders a medium primary button that reacts to clicks', async () => {
        const onClick = vi.fn<() => void>();
        render(<Button onClick={onClick}>Save</Button>);

        const button = screen.getByRole('button', {name: 'Save'});
        await userEvent.click(button);

        expect(onClick).toHaveBeenCalledExactlyOnceWith();
        expect(button).toHaveClass('button', 'md', 'primary');
    });

    it('is a plain button, never a form submit, unless asked', () => {
        render(<Button>Save</Button>);

        expect(screen.getByRole('button', {name: 'Save'})).toHaveAttribute('type', 'button');
    });

    it('applies size, color and shape', () => {
        render(<Button size="xs" color="outlined" round block>Save</Button>);

        expect(screen.getByRole('button', {name: 'Save'})).toHaveClass('xs', 'outlined', 'round', 'block');
    });

    it('takes its accessible name from ariaLabel for icon-only buttons', () => {
        render(<Button square ariaLabel="Close">×</Button>);

        expect(screen.getByRole('button', {name: 'Close'})).toHaveClass('square');
    });

    it('does not react to clicks when disabled', async () => {
        const onClick = vi.fn<() => void>();
        render(<Button disabled onClick={onClick}>Save</Button>);

        await userEvent.click(screen.getByRole('button', {name: 'Save'}), {pointerEventsCheck: 0});

        expect(onClick).not.toHaveBeenCalled();
    });

    it('navigates inside the app for an internal href', async () => {
        render(
            <MemoryRouter initialEntries={['/']}>
                <Routes>
                    <Route path="/" element={<Button href="/software">Software</Button>}/>
                    <Route path="/software" element={<p>Software page</p>}/>
                </Routes>
            </MemoryRouter>,
        );

        await userEvent.click(screen.getByRole('link', {name: 'Software'}));

        expect(screen.getByText('Software page')).toBeInTheDocument();
    });

    it('opens an external href in a new tab safely', () => {
        render(<Button href="https://example.com" target="_blank" rel="external">Site</Button>);

        const link = screen.getByRole('link', {name: 'Site'});

        expect(link).toHaveAttribute('href', 'https://example.com');
        expect(link).toHaveAttribute('rel', 'external noopener noreferrer');
    });

    it('keeps a disabled link out of the tab order and inert', () => {
        render(<Button href="https://example.com" disabled>Site</Button>);

        const link = screen.getByRole('link', {name: 'Site'});
        const click = new MouseEvent('click', {bubbles: true, cancelable: true});
        link.dispatchEvent(click);

        expect(link).toHaveAttribute('aria-disabled', 'true');
        expect(link).toHaveAttribute('tabindex', '-1');
        expect(click.defaultPrevented).toBe(true);
    });

    it('plays a ripple from the pressed point and stops it afterwards', () => {
        vi.useFakeTimers();
        render(<Button>Save</Button>);
        const button = screen.getByRole('button', {name: 'Save'});

        fireEvent.pointerDown(button, {clientX: 10, clientY: 5});
        act(() => {
            vi.advanceTimersToNextFrame();
        });

        expect(button).toHaveClass('rippling');

        act(() => {
            vi.advanceTimersByTime(560);
        });

        expect(button).not.toHaveClass('rippling');
    });

    it('is locked and busy while its action runs', () => {
        render(<Button loading>Save</Button>);

        const button = screen.getByRole('button', {name: 'Save'});

        expect(button).toBeDisabled();
        expect(button).toHaveAttribute('aria-busy', 'true');
    });
});
