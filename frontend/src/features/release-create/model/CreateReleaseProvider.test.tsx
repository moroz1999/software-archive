import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {type Hardware, ReleaseForm, type ReleaseId, toSoftwareId, toSoftwarePlatformCode, toSoftwareTitle} from '@/entities/software';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {CreateReleaseProvider} from './CreateReleaseProvider';

const SOFTWARE = {
    id: toSoftwareId('00000000-0000-0000-0001-000000000001'),
    title: toSoftwareTitle('Elite'),
    description: null,
    releaseDate: null,
    platform: {code: toSoftwarePlatformCode('vector'), name: 'Vector'},
    hardware: [],
    categories: [],
};
const RELEASE = '00000000-0000-0000-0007-000000000002';
const HARDWARE: readonly Hardware[] = [{code: 'ay8910', name: 'AY-3-8910', group: 'sound'}];

function showCreateForm(onCreated = vi.fn<(id: ReleaseId) => void>()) {
    renderWithQueries(
        <CreateReleaseProvider software={SOFTWARE} hardware={HARDWARE} onCreated={onCreated}>
            <ReleaseForm submitLabel="Add release"/>
        </CreateReleaseProvider>,
    );
    return onCreated;
}

describe(CreateReleaseProvider, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('adds the release to the software', async () => {
        const fetchMock = stubFetch(json({id: RELEASE}, 201));
        const onCreated = showCreateForm();

        await userEvent.type(screen.getByRole('textbox', {name: 'Version'}), '1.1');
        await userEvent.click(screen.getByRole('checkbox', {name: 'AY-3-8910'}));
        await userEvent.click(screen.getByRole('button', {name: 'Add release'}));

        await vi.waitFor(() => {
            expect(onCreated).toHaveBeenCalledExactlyOnceWith(RELEASE);
        });

        expect(fetchMock).toHaveBeenCalledExactlyOnceWith(`/api/software/${SOFTWARE.id}/releases`, expect.objectContaining({
            method: 'POST',
            body: '{"title":null,"version":"1.1","type":"unknown","releaseDate":null,"description":null,"hardware":["ay8910"]}',
        }));
    });

    it('shows hardware of another platform refused under the hardware, not in the error dialog', async () => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetch(json({status: 422, code: 'hardware.unknown', errors: [{field: 'hardware', code: 'hardware.unknown'}]}, 422));
        showCreateForm();

        await userEvent.click(screen.getByRole('button', {name: 'Add release'}));

        await vi.waitFor(() => {
            expect(screen.getByRole('group', {name: 'Hardware'})).toHaveAccessibleDescription('A group checked here replaces that group of the software. Choose hardware of the platform of the software.');
        });

        expect(reports).toHaveLength(0);

        unsubscribe();
    });
});
