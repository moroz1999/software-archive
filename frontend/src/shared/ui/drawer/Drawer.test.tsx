import {describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {Drawer} from './Drawer';

describe(Drawer, () => {
    it('shows a panel beside the page, labelled by its title, with its header, body and footer', () => {
        render((
            <Drawer open title="Assistant" onClose={vi.fn<() => void>()} header={<span>Header</span>} footer={<span>Footer</span>}>
                Body
            </Drawer>
        ));

        const drawer = screen.getByRole('dialog', {name: 'Assistant'});

        expect(drawer).not.toHaveAttribute('aria-modal', 'true');
        expect(drawer).toHaveTextContent('Header');
        expect(drawer).toHaveTextContent('Body');
        expect(drawer).toHaveTextContent('Footer');
    });

    it('renders nothing when closed', () => {
        render(<Drawer open={false} title="Assistant" onClose={vi.fn<() => void>()}>Body</Drawer>);

        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('closes by its cross', async () => {
        const onClose = vi.fn<() => void>();
        render(<Drawer open title="Assistant" onClose={onClose}>Body</Drawer>);

        await userEvent.click(screen.getByRole('button', {name: 'Close'}));

        expect(onClose).toHaveBeenCalledExactlyOnceWith();
    });

    it('closes on Escape pressed inside it, and leaves Escape elsewhere to the page', async () => {
        const onClose = vi.fn<() => void>();
        render(<><input aria-label="Page field"/><Drawer open title="Assistant" onClose={onClose}><input aria-label="Field"/></Drawer></>);

        await userEvent.click(screen.getByRole('textbox', {name: 'Page field'}));
        await userEvent.keyboard('{Escape}');

        expect(onClose).not.toHaveBeenCalled();

        await userEvent.click(screen.getByRole('textbox', {name: 'Field'}));
        await userEvent.keyboard('{Escape}');

        expect(onClose).toHaveBeenCalledExactlyOnceWith();
    });
});
