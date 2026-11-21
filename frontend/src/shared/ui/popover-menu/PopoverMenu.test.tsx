import {describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {PopoverMenu, PopoverMenuItem} from '.';

function Menu({onMake}: {readonly onMake?: () => void}) {
    return (
        <MemoryRouter>
            <p>Outside</p>
            <PopoverMenu label="Actions">
                <PopoverMenuItem href="/software/1/edit">Edit</PopoverMenuItem>
                <PopoverMenuItem onClick={onMake ?? (() => undefined)}>Make group</PopoverMenuItem>
            </PopoverMenu>
        </MemoryRouter>
    );
}

describe(PopoverMenu, () => {
    it('is a pencil button named by its label, its items hidden until it opens', () => {
        render(<Menu/>);

        const trigger = screen.getByRole('button', {name: 'Actions'});

        expect(trigger).toHaveAttribute('aria-expanded', 'false');
        expect(trigger.querySelector('svg')).toBeInTheDocument();
        expect(screen.queryByRole('link', {name: 'Edit'})).not.toBeInTheDocument();
    });

    it('opens its window of items named like it', async () => {
        render(<Menu/>);

        const trigger = screen.getByRole('button', {name: 'Actions'});
        await userEvent.click(trigger);

        expect(trigger).toHaveAttribute('aria-expanded', 'true');
        expect(screen.getByRole('dialog', {name: 'Actions'})).toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Edit'})).toHaveAttribute('href', '/software/1/edit');
    });

    it('opens its window from the left edge of the button', async () => {
        render(<Menu/>);

        await userEvent.click(screen.getByRole('button', {name: 'Actions'}));

        expect(screen.getByRole('dialog', {name: 'Actions'}).parentElement).toHaveClass('start');
    });

    it('runs an item and closes', async () => {
        const onMake = vi.fn<() => void>();
        render(<Menu onMake={onMake}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Actions'}));
        await userEvent.click(screen.getByRole('button', {name: 'Make group'}));

        expect(onMake).toHaveBeenCalledExactlyOnceWith();
        expect(screen.queryByRole('dialog', {name: 'Actions'})).not.toBeInTheDocument();
    });

    it('closes once a link is followed', async () => {
        render(<Menu/>);

        await userEvent.click(screen.getByRole('button', {name: 'Actions'}));
        await userEvent.click(screen.getByRole('link', {name: 'Edit'}));

        expect(screen.queryByRole('dialog', {name: 'Actions'})).not.toBeInTheDocument();
    });

    it('closes by Escape and by a press outside', async () => {
        render(<Menu/>);

        await userEvent.click(screen.getByRole('button', {name: 'Actions'}));
        await userEvent.keyboard('{Escape}');

        expect(screen.queryByRole('dialog', {name: 'Actions'})).not.toBeInTheDocument();

        await userEvent.click(screen.getByRole('button', {name: 'Actions'}));
        await userEvent.click(screen.getByText('Outside'));

        expect(screen.queryByRole('dialog', {name: 'Actions'})).not.toBeInTheDocument();
    });
});
