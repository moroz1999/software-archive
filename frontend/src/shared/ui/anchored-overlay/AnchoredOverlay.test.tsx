import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import {useRef} from 'react';
import {AnchoredOverlay} from './AnchoredOverlay';

function Anchored({open, align}: {readonly open: boolean; readonly align?: 'stretch' | 'end' | 'start'}) {
    const anchor = useRef<HTMLButtonElement>(null);
    return (
        <>
            <button ref={anchor} type="button">Anchor</button>
            <AnchoredOverlay anchor={anchor} open={open} {...(align === undefined ? {} : {align})}>
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

    it('is as wide as its content from the left edge of the anchor when aligned to its start', () => {
        render(<Anchored open align="start"/>);

        expect(screen.getByText('Floating').parentElement).toHaveClass('start');
    });
});
