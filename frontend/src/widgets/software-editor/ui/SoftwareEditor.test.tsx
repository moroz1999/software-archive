import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {json, noContent, renderWithQueries, stubFetchAround, stubFetchRoutes} from '@/shared/lib/test';
import {type SoftwareId, toSoftwareId} from '@/entities/software';
import {SoftwareEditor} from './SoftwareEditor';

const ID = '00000000-0000-0000-0001-000000000001';
const ELITE = {id: ID, title: 'Elite', description: 'Space trading', releaseDate: '1985-09-20', platform: {code: 'vector', name: 'Vector'}, hardware: [],
    categories: [{
        id: '00000000-0000-0000-0006-000000000002',
        title: 'Action',
        branch: [{id: '00000000-0000-0000-0006-000000000001', title: 'Games'}, {id: '00000000-0000-0000-0006-000000000002', title: 'Action'}],
    }]};
const GAMES = '00000000-0000-0000-0006-000000000001';
const ACTION = '00000000-0000-0000-0006-000000000002';
const TREE = [{id: GAMES, title: 'Games', children: [{id: ACTION, title: 'Action', children: []}]}];
/** What the editor loads besides the software itself. */
const AROUND = new Map([
    ['/api/categories', json(TREE)],
    ['/api/platforms', json([{code: 'vector', name: 'Vector'}])],
    ['/api/hardware?platform=vector', json([])],
]);

describe(SoftwareEditor, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows a skeleton, then the form with the software and a delete button', async () => {
        stubFetchAround(AROUND, json(ELITE));
        renderWithQueries(<SoftwareEditor id={toSoftwareId(ID)} onSaved={vi.fn<(id: SoftwareId) => void>()} onDeleted={vi.fn<() => void>()}/>);

        expect(screen.getByTestId('software-editor-skeleton')).toBeInTheDocument();
        await expect(screen.findByRole('textbox', {name: 'Title'})).resolves.toHaveValue('Elite');
        expect(screen.getByRole('button', {name: 'Delete'})).toBeInTheDocument();
    });

    it('reports the software saved', async () => {
        stubFetchAround(AROUND, json(ELITE), noContent());
        const onSaved = vi.fn<(id: SoftwareId) => void>();
        renderWithQueries(<SoftwareEditor id={toSoftwareId(ID)} onSaved={onSaved} onDeleted={vi.fn<() => void>()}/>);

        await screen.findByRole('textbox', {name: 'Title'});
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(onSaved).toHaveBeenCalledExactlyOnceWith(toSoftwareId(ID));
        });
    });

    it('reports the software deleted', async () => {
        stubFetchAround(AROUND, json(ELITE), noContent());
        const onDeleted = vi.fn<() => void>();
        renderWithQueries(<SoftwareEditor id={toSoftwareId(ID)} onSaved={vi.fn<(id: SoftwareId) => void>()} onDeleted={onDeleted}/>);

        await screen.findByRole('textbox', {name: 'Title'});
        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await vi.waitFor(() => {
            expect(onDeleted).toHaveBeenCalledExactlyOnceWith();
        });
    });

    it('shows the categories of the software checked and saves the categories chosen', async () => {
        const fetchMock = stubFetchRoutes(new Map([...AROUND, [`/api/software/${ID}`, json(ELITE)]]));
        renderWithQueries(<SoftwareEditor id={toSoftwareId(ID)} onSaved={vi.fn<(id: SoftwareId) => void>()} onDeleted={vi.fn<() => void>()}/>);

        await expect(screen.findByRole('checkbox', {name: 'Action'})).resolves.toBeChecked();

        await userEvent.click(screen.getByRole('checkbox', {name: 'Games'}));
        await userEvent.click(screen.getByRole('button', {name: 'Remove Action'}));
        fetchMock.mockClear();
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(fetchMock.mock.calls.find((call) => call[1]?.method === 'PUT')?.[1]?.body).toContain(`"categories":["${GAMES}"]`);
        });
    });
});
