import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {SoftwareForm, useSoftwareForm} from '@/entities/software';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {EditSoftwareProvider} from './EditSoftwareProvider';

const SOFTWARE = {
    id: '00000000-0000-0000-0001-000000000001',
    title: 'Elite',
    description: 'Space trading',
    releaseDate: '1985-09-20',
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
};
const PLATFORMS = json([{code: 'bk', name: 'BK'}, {code: 'vector', name: 'Vector'}]);
const ORIGINAL_ITEM = {id: '00000000-0000-0000-0001-000000000002', title: 'Jetpac'};
const NEW_ITEM = {id: '00000000-0000-0000-0001-000000000003', title: 'Galactic Bowling'};

function answer(software: () => Response) {
    return stubFetchRoutes(new Map([
        ['/api/platforms', PLATFORMS],
        ['/api/hardware?platform=vector', json([{code: 'vector06c', name: 'Vector-06C', group: 'computers', groupName: 'Computers'}])],
        ['/api/hardware?platform=bk', json([])],
        [`/api/software/${SOFTWARE.id}`, software],
        ['/api/software/lookup?query=Bowling', json({items: [NEW_ITEM]})],
    ]));
}

describe(EditSoftwareProvider, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('saves the edited software', async () => {
        const fetchMock = answer(noContent());
        const onSaved = vi.fn<() => void>();
        renderWithQueries(<EditSoftwareProvider software={SOFTWARE} onSaved={onSaved}><SoftwareForm submitLabel="Save"/></EditSoftwareProvider>);

        const title = screen.getByRole('textbox', {name: 'Title'});
        await userEvent.clear(title);
        await userEvent.type(title, 'Elite Plus');
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(onSaved).toHaveBeenCalledExactlyOnceWith();
        });

        expect(fetchMock.mock.calls.find(([url]) => url === `/api/software/${SOFTWARE.id}`)?.[1]).toMatchObject({
            method: 'PUT',
            body: '{"title":"Elite Plus","description":"Space trading","releaseDate":"1985-09-20","platform":"vector","hardware":[],"categories":[],"credits":[],"preservation":"unknown","instructions":null,"compilationItemIds":null}',
        });
    });


    it('replaces a compilation program with an existing program found by title', async () => {
        const fetchMock = answer(noContent());
        renderWithQueries(
            <EditSoftwareProvider software={{...SOFTWARE, compilationItems: [ORIGINAL_ITEM]}} onSaved={vi.fn<() => void>()}>
                <SoftwareForm submitLabel="Save"/>
            </EditSoftwareProvider>,
        );

        await userEvent.click(screen.getByRole('button', {name: 'Remove Jetpac'}));
        await userEvent.type(screen.getByRole('combobox', {name: 'Compilation programs'}), 'Bowling');
        await userEvent.click(await screen.findByRole('option', {name: 'Galactic Bowling'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            const request = fetchMock.mock.calls.find(([url]) => url === `/api/software/${SOFTWARE.id}`)?.[1];

            expect(request?.method).toBe('PUT');
            expect(JSON.parse(typeof request?.body === 'string' ? request.body : 'null'))
                .toMatchObject({compilationItemIds: [NEW_ITEM.id]});
        });
    });

    it.each([
        ['software.compilation-item-unknown', 'A program is no longer in the archive. Choose another program.'],
        ['software.compilation-cycle', 'This selection would create a cycle of compilations.'],
    ])('shows a compilation refusal under the field and clears it when the selection changes (%s)', async (code, message) => {
        answer(json({status: 422, code, errors: [{field: 'compilationItemIds', code}]}, 422));
        renderWithQueries(
            <EditSoftwareProvider software={{...SOFTWARE, compilationItems: [ORIGINAL_ITEM]}} onSaved={vi.fn<() => void>()}>
                <SoftwareForm submitLabel="Save"/>
            </EditSoftwareProvider>,
        );

        const field = screen.getByRole('combobox', {name: 'Compilation programs'});
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(field).toHaveAccessibleDescription(message);
        });
        await userEvent.click(screen.getByRole('button', {name: 'Remove Jetpac'}));

        expect(field).not.toHaveAccessibleDescription(message);
    });

    it('shows a refusal of a field under that field, not in the error dialog', async () => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        answer(json({status: 422, code: 'software.title-taken', errors: [{field: 'title', code: 'software.title-taken'}]}, 422));
        renderWithQueries(<EditSoftwareProvider software={SOFTWARE} onSaved={vi.fn<() => void>()}><SoftwareForm submitLabel="Save"/></EditSoftwareProvider>);

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(screen.getByRole('textbox', {name: 'Title'})).toHaveAccessibleDescription('The request was refused.');
        });

        expect(reports).toHaveLength(0);

        unsubscribe();
    });

    it('says why the platform cannot change under the platform', async () => {
        answer(json({status: 422, code: 'software.platform-locked', errors: [{field: 'platform', code: 'software.platform-locked'}]}, 422));
        renderWithQueries(<EditSoftwareProvider software={SOFTWARE} onSaved={vi.fn<() => void>()}><SoftwareForm submitLabel="Save"/></EditSoftwareProvider>);

        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Platform'}), await screen.findByRole('option', {name: 'BK'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(screen.getByRole('combobox', {name: 'Platform'})).toHaveAccessibleDescription('Remove the hardware of the software and its releases first.');
        });
    });

    it('says why the hardware was refused under the hardware', async () => {
        answer(json({status: 422, code: 'hardware.unknown', errors: [{field: 'hardware', code: 'hardware.unknown'}]}, 422));
        renderWithQueries(<EditSoftwareProvider software={SOFTWARE} onSaved={vi.fn<() => void>()}><SoftwareForm submitLabel="Save"/></EditSoftwareProvider>);

        await userEvent.click(await screen.findByRole('checkbox', {name: 'Vector-06C'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(screen.getByRole('group', {name: 'Hardware'})).toHaveAccessibleDescription('Choose hardware of the platform of the software.');
        });
    });

    it('says why the categories were refused under the categories', async () => {
        answer(json({status: 422, code: 'category.unknown', errors: [{field: 'categories', code: 'category.unknown'}]}, 422));
        renderWithQueries(<EditSoftwareProvider software={SOFTWARE} onSaved={vi.fn<() => void>()}><SoftwareForm submitLabel="Save" categories={<CategoriesRefusal/>}/></EditSoftwareProvider>);

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await expect(screen.findByTestId('categories-refusal')).resolves.toHaveTextContent('A category is no longer in the archive: choose again.');
    });
});

/** Stands in for the field of the categories, showing only their refusal. */
function CategoriesRefusal() {
    const {refusals} = useSoftwareForm();
    return <span data-testid="categories-refusal">{refusals.categories}</span>;
}
