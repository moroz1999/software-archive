import {describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {useState} from 'react';
import {IconPopover} from './IconPopover';

function Account({onOpenChange}: {readonly onOpenChange?: (open: boolean) => void}) {
    const [open, setOpen] = useState(false);
    return (
        <>
            <p>Outside</p>
            <IconPopover
                label="Account"
                icon={<span>@</span>}
                open={open}
                onOpenChange={(next) => {
                    onOpenChange?.(next);
                    setOpen(next);
                }}
            >
                <p>Inside</p>
            </IconPopover>
        </>
    );
}

describe(IconPopover, () => {
    it('opens its window under the trigger, named like it', async () => {
        render(<Account/>);

        const trigger = screen.getByRole('button', {name: 'Account'});

        expect(trigger).toHaveAttribute('aria-expanded', 'false');
        expect(trigger).toHaveAttribute('aria-haspopup', 'dialog');
        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();

        await userEvent.click(trigger);

        expect(trigger).toHaveAttribute('aria-expanded', 'true');
        expect(screen.getByRole('dialog', {name: 'Account'})).toHaveTextContent('Inside');
    });

    it('closes by the trigger again', async () => {
        render(<Account/>);

        await userEvent.click(screen.getByRole('button', {name: 'Account'}));
        await userEvent.click(screen.getByRole('button', {name: 'Account'}));

        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('closes by Escape and gives the focus back to the trigger', async () => {
        render(<Account/>);

        await userEvent.click(screen.getByRole('button', {name: 'Account'}));
        await userEvent.keyboard('{Escape}');

        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Account'})).toHaveFocus();
    });

    it('closes by a press outside, but not by one inside', async () => {
        const onOpenChange = vi.fn<(open: boolean) => void>();
        render(<Account onOpenChange={onOpenChange}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Account'}));
        await userEvent.click(screen.getByText('Inside'));

        expect(screen.getByRole('dialog')).toBeInTheDocument();

        await userEvent.click(screen.getByText('Outside'));

        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
        expect(onOpenChange.mock.calls).toStrictEqual([[true], [false]]);
    });
});
