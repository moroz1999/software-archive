import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {DeleteSoftwareButton} from './DeleteSoftwareButton';

const SOFTWARE = {
    id: '00000000-0000-0000-0001-000000000001',
    title: 'Elite',
    description: null,
    releaseDate: '1985-09-20',
    platform: {code: 'vector', name: 'Vector'},
    hardware: [],
    categories: [],
    origins: [],
    added: {by: 'Zed', at: '2026-01-01T00:00:00Z'},
    canChange: true,
    preservation: 'unknown' as const,
    lineage: {kind: null, reliability: null, rework: [], source: null},
    compilationItems: [],
    compilations: [],
    credits: [],
    instructions: null,
    sources: [], links: [],
};

describe(DeleteSoftwareButton, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('deletes the software after confirmation', async () => {
        const fetchMock = stubFetch(noContent());
        const onDeleted = vi.fn<() => void>();
        renderWithQueries(<DeleteSoftwareButton software={SOFTWARE} onDeleted={onDeleted}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));

        expect(screen.getByRole('alertdialog', {name: 'Delete software'})).toHaveTextContent('Delete “Elite” from the archive?');

        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await vi.waitFor(() => {
            expect(onDeleted).toHaveBeenCalledExactlyOnceWith();
        });

        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'DELETE'});
    });

    it('deletes nothing when cancelled', async () => {
        const fetchMock = stubFetch(noContent());
        renderWithQueries(<DeleteSoftwareButton software={SOFTWARE} onDeleted={vi.fn<() => void>()}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Cancel'}));

        expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
        expect(fetchMock).not.toHaveBeenCalled();
    });


    it('shows a refusal inside the confirmation, not in the error dialog', async () => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetch(json({status: 409, code: 'software.changed'}, 409));
        renderWithQueries(<DeleteSoftwareButton software={SOFTWARE} onDeleted={vi.fn<() => void>()}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('This item was changed in the meantime. Reload and try again.');
        expect(reports).toHaveLength(0);

        unsubscribe();
    });

    it('says who may delete the software when the archive refuses', async () => {
        stubFetch(json({status: 403, code: 'access.denied'}, 403));
        renderWithQueries(<DeleteSoftwareButton software={SOFTWARE} onDeleted={vi.fn<() => void>()}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('Only whoever added this, or an administrator, may change it.');
    });

    it('tells a public visitor to sign in before deleting', async () => {
        stubFetch(json({status: 401, code: 'auth.required'}, 401));
        renderWithQueries(<DeleteSoftwareButton software={SOFTWARE} onDeleted={vi.fn<() => void>()}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('Sign in to change the archive.');
    });
});
