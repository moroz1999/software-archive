import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {PlatformSelectionProvider} from '../model/PlatformSelectionProvider';
import {readSelectedPlatform} from '../model/stored-platform';
import {PlatformSwitch} from './PlatformSwitch';

const PLATFORMS = [{code: 'agat', name: 'Agat'}, {code: 'bk', name: 'BK'}];

describe(PlatformSwitch, () => {
    afterEach(() => {
        localStorage.clear();
        vi.unstubAllGlobals();
    });

    it('offers all platforms and every platform of the archive', async () => {
        stubFetch(json(PLATFORMS));
        renderWithQueries(<PlatformSelectionProvider><PlatformSwitch/></PlatformSelectionProvider>);

        await expect(screen.findByRole('option', {name: 'BK'})).resolves.toBeInTheDocument();
        expect(screen.getAllByRole('option').map((option) => option.textContent)).toStrictEqual(['All platforms', 'Agat', 'BK']);
        expect(screen.getByRole('combobox', {name: 'Platform'})).toHaveValue('');
    });

    it('shows the platform kept in the browser', async () => {
        localStorage.setItem('archive.platform', 'bk');
        stubFetch(json(PLATFORMS));
        renderWithQueries(<PlatformSelectionProvider><PlatformSwitch/></PlatformSelectionProvider>);

        await screen.findByRole('option', {name: 'BK'});

        expect(screen.getByRole('combobox', {name: 'Platform'})).toHaveValue('bk');
    });

    it('chooses a platform for the whole archive and all of them again', async () => {
        stubFetch(json(PLATFORMS));
        renderWithQueries(<PlatformSelectionProvider><PlatformSwitch/></PlatformSelectionProvider>);
        await screen.findByRole('option', {name: 'Agat'});

        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Platform'}), 'Agat');

        expect(readSelectedPlatform()).toBe('agat');

        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Platform'}), 'All platforms');

        expect(readSelectedPlatform()).toBeNull();
    });
});
