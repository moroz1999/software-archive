import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {SoftwareForm, toReleaseDate, toSoftwareId, toSoftwareTitle} from '@/entities/software';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {EditSoftwareProvider} from './EditSoftwareProvider';

const SOFTWARE = {
    id: toSoftwareId('00000000-0000-0000-0001-000000000001'),
    title: toSoftwareTitle('Elite'),
    description: 'Space trading',
    releaseDate: toReleaseDate('1985-09-20'),
};

describe(EditSoftwareProvider, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('saves the edited software', async () => {
        const fetchMock = stubFetch(noContent());
        const onSaved = vi.fn<() => void>();
        renderWithQueries(<EditSoftwareProvider software={SOFTWARE} onSaved={onSaved}><SoftwareForm submitLabel="Save"/></EditSoftwareProvider>);

        const title = screen.getByRole('textbox', {name: 'Title'});
        await userEvent.clear(title);
        await userEvent.type(title, 'Elite Plus');
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(onSaved).toHaveBeenCalledExactlyOnceWith();
        });

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/software/${SOFTWARE.id}`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({
            method: 'PUT',
            body: '{"title":"Elite Plus","description":"Space trading","releaseDate":"1985-09-20"}',
        });
    });


    it('shows a refusal of a field under that field, not in the error dialog', async () => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetch(json({status: 422, code: 'software.title-taken', errors: [{field: 'title', code: 'software.title-taken'}]}, 422));
        renderWithQueries(<EditSoftwareProvider software={SOFTWARE} onSaved={vi.fn<() => void>()}><SoftwareForm submitLabel="Save"/></EditSoftwareProvider>);

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(screen.getByRole('textbox', {name: 'Title'})).toHaveAccessibleDescription('The request was refused.');
        });

        expect(reports).toHaveLength(0);

        unsubscribe();
    });
});
