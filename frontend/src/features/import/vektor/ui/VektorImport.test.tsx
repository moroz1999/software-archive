import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {VektorImport} from './VektorImport';

const SECTIONS = '/api/imports/vektor/sections';
const START = '/api/imports/vektor';

function accepted(): () => Response {
    return () => new Response(null, {status: 202});
}

describe(VektorImport, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('offers the whole site and every section to import', async () => {
        stubFetchRoutes(new Map([[SECTIONS, json(['arkadnye-igry', 'zagruzchiki'])]]));
        renderWithQueries(<VektorImport/>);

        const section = await screen.findByRole('combobox', {name: 'Section'});

        await expect(within(section).findByRole('option', {name: 'zagruzchiki'})).resolves.toBeInTheDocument();
        expect(within(section).getAllByRole('option').map((option) => option.textContent)).toStrictEqual(['Whole site', 'arkadnye-igry', 'zagruzchiki']);
    });

    it('starts the import of the chosen section and says it goes on in the background', async () => {
        const fetchMock = stubFetchRoutes(new Map([[SECTIONS, json(['arkadnye-igry', 'zagruzchiki'])], [`${START}?section=zagruzchiki`, accepted()]]));
        renderWithQueries(<VektorImport/>);

        await userEvent.selectOptions(await screen.findByRole('combobox', {name: 'Section'}), await screen.findByRole('option', {name: 'zagruzchiki'}));
        await userEvent.click(screen.getByRole('button', {name: 'Start'}));

        await expect(screen.findByRole('status')).resolves.toHaveTextContent('The import has started; the backend log tells how it ended.');
        expect(fetchMock).toHaveBeenCalledWith(`${START}?section=zagruzchiki`, expect.objectContaining({method: 'POST'}));
    });

    it('starts the import of the whole site when no section is chosen', async () => {
        const fetchMock = stubFetchRoutes(new Map([[SECTIONS, json(['zagruzchiki'])], [START, accepted()]]));
        renderWithQueries(<VektorImport/>);

        await screen.findByRole('option', {name: 'zagruzchiki'});
        await userEvent.click(screen.getByRole('button', {name: 'Start'}));

        await screen.findByRole('status');

        expect(fetchMock).toHaveBeenCalledWith(START, expect.objectContaining({method: 'POST'}));
    });

    it('says so when an import is already running', async () => {
        stubFetchRoutes(new Map([[SECTIONS, json(['zagruzchiki'])], [START, json({status: 409, code: 'import.running'}, 409)]]));
        renderWithQueries(<VektorImport/>);

        await screen.findByRole('option', {name: 'zagruzchiki'});
        await userEvent.click(screen.getByRole('button', {name: 'Start'}));

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('An import is already running.');
        expect(screen.queryByRole('status')).not.toBeInTheDocument();
    });
});
