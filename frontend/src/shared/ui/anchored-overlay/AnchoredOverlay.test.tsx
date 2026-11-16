import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import {useRef} from 'react';
import {AnchoredOverlay} from './AnchoredOverlay';

function Anchored({open}: {readonly open: boolean}) {
    const anchor = useRef<HTMLButtonElement>(null);
    return (
        <>
            <button ref={anchor} type="button">Anchor</button>
            <AnchoredOverlay anchor={anchor} open={open}>
                <p>Floating</p>
            </AnchoredOverlay>
        </>
    );
}

describe(AnchoredOverlay, () => {
    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('shows its content in the top layer as a popover, above any window on the page', () => {
        const showPopover = vi.spyOn(HTMLElement.prototype, 'showPopover');
        render(<Anchored open/>);

        const overlay = screen.getByText('Floating').parentElement;

        expect(overlay).toHaveAttribute('popover', 'manual');
        expect(showPopover.mock.contexts).toStrictEqual([overlay]);
    });

    it('renders nothing while closed', () => {
        const showPopover = vi.spyOn(HTMLElement.prototype, 'showPopover');
        render(<Anchored open={false}/>);

        expect(screen.queryByText('Floating')).not.toBeInTheDocument();
        expect(showPopover).not.toHaveBeenCalled();
    });
});
