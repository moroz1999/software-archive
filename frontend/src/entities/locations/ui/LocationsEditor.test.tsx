import {useState} from 'react';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import type {LocationValue} from '../model/location-value';
import {LocationsEditor, type LocationsEditorProps} from './LocationsEditor';

const FINLAND = '00000000-0000-0000-0002-000000000001';
const NORWAY = '00000000-0000-0000-0002-000000000002';
const HELSINKI = '00000000-0000-0000-0002-000000000011';
const TAMPERE = '00000000-0000-0000-0002-000000000012';

function answer() {
    return stubFetchRoutes(new Map([
        ['/api/countries', json([{id: FINLAND, name: 'Finland'}, {id: NORWAY, name: 'Norway'}])],
        [`/api/countries/${FINLAND}/cities`, json([{id: HELSINKI, name: 'Helsinki'}, {id: TAMPERE, name: 'Tampere'}])],
        [`/api/countries/${NORWAY}/cities`, json([])],
    ]));
}

function Harness({initial, ...props}: Omit<LocationsEditorProps, 'value' | 'onChange'> & {readonly initial: readonly LocationValue[]}) {
    const [value, setValue] = useState(initial);
    return (
        <>
            <LocationsEditor {...props} value={value} onChange={setValue}/>
            <output aria-label="Value">{JSON.stringify(value)}</output>
        </>
    );
}

function value(): unknown {
    return JSON.parse(screen.getByRole('status', {name: 'Value'}).textContent);
}

/** Whether each of the radios of that name is chosen, in their order. */
function checked(name: string) {
    return screen.getAllByRole('radio', {name}).map((radio) => radio.matches(':checked'));
}

describe(LocationsEditor, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows every location with its country, its city and whether it is the primary one', async () => {
        answer();
        renderWithQueries(<Harness initial={[{country: FINLAND, city: HELSINKI, primary: true}, {country: NORWAY, city: '', primary: false}]}/>);

        expect(screen.getByRole('group', {name: 'Locations'})).toBeInTheDocument();

        await screen.findByRole('option', {name: 'Helsinki'});

        expect(screen.getByRole('combobox', {name: 'Country 1'})).toHaveValue(FINLAND);
        expect(screen.getByRole('combobox', {name: 'City 1'})).toHaveValue(HELSINKI);
        expect(screen.getByRole('combobox', {name: 'Country 2'})).toHaveValue(NORWAY);
        expect(checked('Primary')).toStrictEqual([true, false]);
    });

    it('adds a location and chooses a city of its country', async () => {
        answer();
        renderWithQueries(<Harness initial={[]}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Add location'}));
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Country 1'}), await screen.findByRole('option', {name: 'Finland'}));
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'City 1'}), await screen.findByRole('option', {name: 'Tampere'}));

        expect(value()).toStrictEqual([{country: FINLAND, city: TAMPERE, primary: true}]);
    });

    it('forgets the city when another country is chosen', async () => {
        answer();
        renderWithQueries(<Harness initial={[{country: FINLAND, city: HELSINKI, primary: false}]}/>);

        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Country 1'}), await screen.findByRole('option', {name: 'Norway'}));

        expect(value()).toStrictEqual([{country: NORWAY, city: '', primary: false}]);
    });

    it('passes the primary location to the first location left when it is taken away', async () => {
        answer();
        renderWithQueries(<Harness initial={[{country: FINLAND, city: '', primary: true}, {country: NORWAY, city: '', primary: false}]}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Remove location 1'}));

        expect(value()).toStrictEqual([{country: NORWAY, city: '', primary: true}]);
    });

    it('takes a location away and chooses one primary location', async () => {
        answer();
        renderWithQueries(<Harness initial={[{country: FINLAND, city: '', primary: true}, {country: NORWAY, city: '', primary: false}, {country: FINLAND, city: HELSINKI, primary: false}]}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Remove location 1'}));
        await userEvent.click(screen.getAllByRole('radio', {name: 'Primary'})[1] ?? document.body);

        expect(value()).toStrictEqual([{country: NORWAY, city: '', primary: false}, {country: FINLAND, city: HELSINKI, primary: true}]);
    });

    it('says what is wrong with a location under it, and why the locations were refused under them all', async () => {
        answer();
        renderWithQueries(<Harness initial={[{country: '', city: '', primary: false}]} rowErrors={['Choose a country.']} error="Choose a city of the country."/>);

        await screen.findByRole('option', {name: 'Finland'});

        expect(screen.getByRole('combobox', {name: 'Country 1'})).toHaveAccessibleDescription('Choose a country.');
        expect(screen.getByRole('group', {name: 'Locations'})).toHaveAccessibleDescription('Choose a city of the country.');
    });
});
