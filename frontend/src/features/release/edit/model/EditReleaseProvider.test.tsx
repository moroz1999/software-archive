import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {type Hardware, ReleaseForm, toReleaseId, toSoftwareId, toSoftwarePlatformCode, toSoftwareTitle} from '@/entities/software';
import {json, noContent, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {EditReleaseProvider} from './EditReleaseProvider';

const SOFTWARE = {
    id: toSoftwareId('00000000-0000-0000-0001-000000000001'),
    title: toSoftwareTitle('Elite'),
    description: null,
    releaseDate: null,
    platform: {code: toSoftwarePlatformCode('vector'), name: 'Vector'},
    hardware: [],
    categories: [],
};
const AY: Hardware = {code: 'ay8910', name: 'AY-3-8910', group: 'sound'};
const BEEPER: Hardware = {code: 'beeper', name: 'Beeper', group: 'sound'};
const RELEASE = {
    id: toReleaseId('00000000-0000-0000-0007-000000000002'),
    title: 'Elite AY',
    version: '1.1',
    type: 'mod' as const,
    releaseDate: null,
    description: null,
    hardware: [AY],
};

function showEditForm(onSaved = vi.fn<() => void>()) {
    renderWithQueries(
        <EditReleaseProvider software={SOFTWARE} release={RELEASE} hardware={[AY, BEEPER]} onSaved={onSaved}>
            <ReleaseForm submitLabel="Save"/>
        </EditReleaseProvider>,
    );
    return onSaved;
}

describe(EditReleaseProvider, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('starts from what the release states and saves the edited release', async () => {
        const fetchMock = stubFetch(noContent());
        const onSaved = showEditForm();

        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveValue('Elite AY');
        expect(screen.getByRole('checkbox', {name: 'AY-3-8910'})).toBeChecked();

        await userEvent.click(screen.getByRole('checkbox', {name: 'Beeper'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(onSaved).toHaveBeenCalledExactlyOnceWith();
        });

        expect(fetchMock).toHaveBeenCalledExactlyOnceWith(`/api/software/${SOFTWARE.id}/releases/${RELEASE.id}`, expect.objectContaining({
            method: 'PUT',
            body: '{"title":"Elite AY","version":"1.1","type":"mod","releaseDate":null,"description":null,"hardware":["ay8910","beeper"]}',
        }));
    });

    it('shows hardware of another platform refused under the hardware', async () => {
        stubFetch(json({status: 422, code: 'hardware.unknown', errors: [{field: 'hardware', code: 'hardware.unknown'}]}, 422));
        showEditForm();

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(screen.getByRole('group', {name: 'Hardware'})).toHaveAccessibleDescription('A group checked here replaces that group of the software. Choose hardware of the platform of the software.');
        });
    });
});
