import {describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {Radio} from './Radio';

describe(Radio, () => {
    it('is named by its label and tells when it gets chosen', async () => {
        const onChoose = vi.fn<() => void>();
        render(<Radio label="Primary" name="primary" checked={false} onChoose={onChoose}/>);

        await userEvent.click(screen.getByText('Primary'));

        expect(screen.getByRole('radio', {name: 'Primary'})).not.toBeChecked();
        expect(onChoose).toHaveBeenCalledExactlyOnceWith();
    });

    it('can be locked', () => {
        render(<Radio label="Primary" name="primary" checked={false} onChoose={vi.fn<() => void>()} disabled/>);

        expect(screen.getByRole('radio', {name: 'Primary'})).toBeDisabled();
    });
});
