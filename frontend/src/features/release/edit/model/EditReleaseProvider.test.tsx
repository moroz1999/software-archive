import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {type Hardware, ReleaseForm} from '@/entities/software';
import {json, noContent, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {EditReleaseProvider} from './EditReleaseProvider';

const SOFTWARE = {
    id: '00000000-0000-0000-0001-000000000001',
    title: 'Elite',
    description: null,
    releaseDate: null,
    platform: {code: 'vector', name: 'Vector'},
    hardware: [],
    categories: [],
    origins: [],
    added: {by: 'Zed', at: '2026-01-01T00:00:00Z'},
    canChange: true,
    preservation: 'unknown' as const,
    compilationItems: [],
    compilations: [],
    credits: [],
    instructions: null,
    sources: [],
    links: [],
};
const AY: Hardware = {code: 'ay8910', name: 'AY-3-8910', group: 'sound', groupName: 'Sound'};
const BEEPER: Hardware = {code: 'beeper', name: 'Beeper', group: 'sound', groupName: 'Sound'};
const RELEASE = {
    id: '00000000-0000-0000-0007-000000000002',
    title: 'Elite AY',
    version: '1.1',
    type: 'mod' as const,
    releaseDate: null,
    description: null,
    hardware: [AY],
    archive: null,
    credits: [{id: '00000000-0000-0000-0004-000000000002', ownerId: null, type: 'group' as const, title: 'Crew', roles: ['release' as const]}],
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
        }));
    });

    it('saves the credits of the release with it', async () => {
        const fetchMock = stubFetch(noContent());
        const onSaved = showEditForm();

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(onSaved).toHaveBeenCalledExactlyOnceWith();
        });

        const body = fetchMock.mock.calls[0]?.[1]?.body;
        const part = body instanceof FormData ? body.get('release') : null;

        expect(part).toBeInstanceOf(Blob);

        const release: unknown = part instanceof Blob ? JSON.parse(await part.text()) : null;

        expect(release).toStrictEqual(expect.objectContaining({
            credits: [{id: '00000000-0000-0000-0004-000000000002', type: 'group', roles: ['release']}],
        }));
    });

    it('shows a credited one no longer in the archive under the credits', async () => {
        stubFetch(json({status: 422, code: 'software.credit-unknown', errors: [{field: 'credits', code: 'software.credit-unknown'}]}, 422));
        showEditForm();

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(screen.getByRole('combobox', {name: 'Person or group'})).toHaveAccessibleDescription('One of those credited is no longer in the archive: choose again.');
        });
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
