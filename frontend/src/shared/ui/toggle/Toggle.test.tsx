import {describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {Toggle} from './Toggle';

const VIEWS = [{value: 'table', label: 'Table'}, {value: 'cards', label: 'Cards'}];

describe(Toggle, () => {
    it('is a named group of options, the current one pressed', () => {
        render(<Toggle label="View" options={VIEWS} value="table" onChange={vi.fn<(value: string) => void>()}/>);

        expect(screen.getByRole('group', {name: 'View'})).toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Table'})).toHaveAttribute('aria-pressed', 'true');
        expect(screen.getByRole('button', {name: 'Cards'})).toHaveAttribute('aria-pressed', 'false');
    });

    it('hands on the option chosen', async () => {
        const onChange = vi.fn<(value: string) => void>();
        render(<Toggle label="View" options={VIEWS} value="table" onChange={onChange}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Cards'}));

        expect(onChange).toHaveBeenCalledExactlyOnceWith('cards');
    });

    it('does not hand on the option already chosen', async () => {
        const onChange = vi.fn<(value: string) => void>();
        render(<Toggle label="View" options={VIEWS} value="table" onChange={onChange}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Table'}));

        expect(onChange).not.toHaveBeenCalled();
    });
});
