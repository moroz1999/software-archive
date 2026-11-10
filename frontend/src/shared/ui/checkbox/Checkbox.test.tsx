import {describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {Checkbox} from './Checkbox';

describe(Checkbox, () => {
    it('is named by its label and tells whether it is checked now', async () => {
        const onChange = vi.fn<(checked: boolean) => void>();
        render(<Checkbox label="AY-3-8910" checked={false} onChange={onChange}/>);

        await userEvent.click(screen.getByText('AY-3-8910'));

        expect(screen.getByRole('checkbox', {name: 'AY-3-8910'})).not.toBeChecked();
        expect(onChange).toHaveBeenCalledExactlyOnceWith(true);
    });

    it('unchecks a checked one', async () => {
        const onChange = vi.fn<(checked: boolean) => void>();
        render(<Checkbox label="AY-3-8910" checked onChange={onChange}/>);

        await userEvent.click(screen.getByRole('checkbox', {name: 'AY-3-8910'}));

        expect(onChange).toHaveBeenCalledExactlyOnceWith(false);
    });

    it('can be locked', () => {
        render(<Checkbox label="AY-3-8910" checked={false} onChange={vi.fn<(checked: boolean) => void>()} disabled/>);

        expect(screen.getByRole('checkbox', {name: 'AY-3-8910'})).toBeDisabled();
    });
});
