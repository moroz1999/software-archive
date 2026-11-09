import {describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {Select} from './Select';

const OPTIONS = [{value: 'fi', label: 'Finland'}, {value: 'no', label: 'Norway'}];

describe(Select, () => {
    it('offers the placeholder and the options, showing the value', () => {
        render(<Select id="country" ariaLabel="Country" value="no" onChange={vi.fn()} options={OPTIONS} placeholder="Any country"/>);

        const select = screen.getByRole('combobox', {name: 'Country'});

        expect(screen.getAllByRole('option').map((option) => option.textContent)).toStrictEqual(['Any country', 'Finland', 'Norway']);
        expect(select).toHaveValue('no');
        expect(select).toHaveClass('select');
    });

    it('hands over the chosen value, empty for the placeholder', async () => {
        const onChange = vi.fn<(value: string) => void>();
        render(<Select id="country" ariaLabel="Country" value="no" onChange={onChange} options={OPTIONS} placeholder="Any country"/>);

        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Country'}), 'Finland');
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Country'}), 'Any country');

        expect(onChange.mock.calls).toStrictEqual([['fi'], ['']]);
    });

    it('marks an invalid choice and points to what describes it', () => {
        render(<><Select id="country" ariaLabel="Country" value="" onChange={vi.fn()} options={OPTIONS} placeholder="Any country" invalid describedBy="country-error"/><span id="country-error">Choose one.</span></>);

        expect(screen.getByRole('combobox', {name: 'Country'})).toBeInvalid();
        expect(screen.getByRole('combobox', {name: 'Country'})).toHaveAccessibleDescription('Choose one.');
    });

    it('can be disabled', () => {
        render(<Select id="city" ariaLabel="City" value="" onChange={vi.fn()} options={[]} placeholder="Any city" disabled/>);

        expect(screen.getByRole('combobox', {name: 'City'})).toBeDisabled();
    });
});
