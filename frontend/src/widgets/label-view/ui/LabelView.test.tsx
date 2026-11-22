import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {breadcrumbSteps, json, noContent, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {toLabelId} from '@/entities/labels';
import {LabelView} from './LabelView';

const ID = '00000000-0000-0000-0004-000000000001';
const CREW = '00000000-0000-0000-0005-000000000001';
const JANE = '00000000-0000-0000-0003-000000000001';
const ELITE = '00000000-0000-0000-0001-000000000001';
const GHOST = {id: ID, name: 'Ghost', owner: null, groups: [{id: CREW, name: 'Crew'}], locations: [], origins: [], canChange: true, canMakeOwner: true, canEdit: false, canChangeOwner: false, canGiveToGroup: true, added: {by: 'Zork', at: '2026-03-04T05:06:07Z'}};

const SOFTWARE_URL = `/api/labels/${ID}/software`;
const NO_SOFTWARE = new Map([[SOFTWARE_URL, json([])]]);

function openLabel() {
    const router = createMemoryRouter([
        {path: '/labels/:id', element: <LabelView id={toLabelId(ID)}/>},
        {path: '/authors/:id', element: <p>Author page</p>},
        {path: '/groups/:id', element: <p>Group page</p>},
    ], {initialEntries: [`/labels/${ID}`]});
    renderWithQueries(<RouterProvider router={router}/>);
}

async function openActions() {
    await userEvent.click(await screen.findByRole('button', {name: 'Actions'}));
}

describe(LabelView, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows the label under its name, not known yet whose it is', async () => {
        stubFetchAround(NO_SOFTWARE, json(GHOST));
        openLabel();

        await expect(screen.findByRole('heading', {level: 1, name: 'Ghost'})).resolves.toBeInTheDocument();
        expect(screen.getByText('Not known yet whose label this is')).toBeInTheDocument();
    });

    it('leads back from the label to the authors', async () => {
        stubFetchAround(NO_SOFTWARE, json(GHOST));
        openLabel();

        await screen.findByRole('heading', {level: 1, name: 'Ghost'});

        expect(breadcrumbSteps()).toStrictEqual([['Authors', '/authors'], ['Ghost', null]]);
    });

    it('leads to the groups the label was a member of', async () => {
        stubFetchAround(NO_SOFTWARE, json(GHOST));
        openLabel();

        const groups = await screen.findByRole('list', {name: 'Member of'});

        expect(within(groups).getByRole('link', {name: 'Crew'})).toHaveAttribute('href', `/groups/${CREW}`);
    });

    it('shows where the label is located, the primary place first', async () => {
        stubFetchAround(NO_SOFTWARE, json({...GHOST, locations: [
            {countryId: 'c2', country: 'Ukraine', cityId: null, city: null, primary: false},
            {countryId: 'c1', country: 'Russia', cityId: 'm1', city: 'Moscow', primary: true},
        ]}));
        openLabel();

        const locations = await screen.findByRole('list', {name: 'Locations'});

        expect(within(locations).getAllByRole('listitem').map((item) => item.textContent)).toStrictEqual(['Moscow, Russia', 'Ukraine']);
    });

    it('shows no locations for a label located nowhere', async () => {
        stubFetchAround(NO_SOFTWARE, json(GHOST));
        openLabel();

        await screen.findByRole('heading', {level: 1, name: 'Ghost'});

        expect(screen.queryByRole('list', {name: 'Locations'})).not.toBeInTheDocument();
    });

    it('lists the software the label is credited on', async () => {
        stubFetchAround(new Map([[SOFTWARE_URL, json([
            {id: ELITE, title: 'Elite', releaseDate: '1985', screenshots: [], madeBy: ['Ghost'], credits: [{as: null, roles: ['code']}]},
        ])]]), json(GHOST));
        openLabel();

        const software = await screen.findByRole('region', {name: 'Software'});

        await expect(within(software).findByRole('article')).resolves.toHaveTextContent('Code');
    });

    it('offers saying whose the label is', async () => {
        stubFetchAround(NO_SOFTWARE, json(GHOST));
        openLabel();

        expect(screen.queryByRole('button', {name: 'Say whose label'})).not.toBeInTheDocument();

        await openActions();

        expect(screen.getByRole('button', {name: 'Say whose label'})).toBeInTheDocument();
    });

    it('offers no attaching to whoever may not do it', async () => {
        stubFetchAround(NO_SOFTWARE, json({...GHOST, canChange: false}));
        openLabel();

        await openActions();

        expect(screen.queryByRole('button', {name: 'Say whose label'})).not.toBeInTheDocument();
    });

    it('shows whose the label is when known, the person by the real name with the main name next to it, leading to their page, and offers no attaching', async () => {
        stubFetchAround(NO_SOFTWARE, json({...GHOST, owner: {type: 'author', id: JANE, name: 'Jane Doe', note: 'JD'}, canChange: false}));
        openLabel();

        await openActions();

        expect(screen.getByText('Whose label')).toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Jane Doe'})).toHaveAttribute('href', `/authors/${JANE}`);
        expect(screen.getByText('JD')).toBeInTheDocument();
        expect(screen.queryByText('Not known yet whose label this is')).not.toBeInTheDocument();
        expect(screen.queryByRole('button', {name: 'Say whose label'})).not.toBeInTheDocument();
    });

    it('leads to the page of a group that owns the label', async () => {
        stubFetchAround(NO_SOFTWARE, json({...GHOST, owner: {type: 'group', id: CREW, name: 'Crew', note: null}, groups: [], canChange: false}));
        openLabel();

        await screen.findByRole('heading', {level: 1, name: 'Ghost'});

        expect(screen.getByRole('link', {name: 'Crew'})).toHaveAttribute('href', `/groups/${CREW}`);
    });

    it('offers making an author or a group of the label', async () => {
        stubFetchAround(NO_SOFTWARE, json(GHOST));
        openLabel();

        await openActions();

        expect(screen.getByRole('button', {name: 'Make author'})).toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Make group'})).toBeInTheDocument();
    });

    it('offers making no owner to whoever may not take the label', async () => {
        stubFetchAround(NO_SOFTWARE, json({...GHOST, canMakeOwner: false}));
        openLabel();

        await openActions();

        expect(screen.queryByRole('button', {name: 'Make author'})).not.toBeInTheDocument();
        expect(screen.queryByRole('button', {name: 'Make group'})).not.toBeInTheDocument();
    });

    it('opens the page of the person made of the label', async () => {
        stubFetchAround(NO_SOFTWARE, json(GHOST), json({id: JANE}, 201));
        openLabel();

        await openActions();
        await userEvent.click(screen.getByRole('button', {name: 'Make author'}));

        expect(screen.queryByRole('dialog', {name: 'Actions'})).not.toBeInTheDocument();

        await userEvent.click(within(screen.getByRole('dialog', {name: 'Make author'})).getByRole('button', {name: 'Make author'}));

        await expect(screen.findByText('Author page')).resolves.toBeInTheDocument();
    });

    it('opens saying whose the label is and making a group in their own windows, out of the closed menu', async () => {
        stubFetchAround(new Map([...NO_SOFTWARE, ['/api/group-types', json([])]]), json(GHOST));
        openLabel();

        await openActions();
        await userEvent.click(screen.getByRole('button', {name: 'Say whose label'}));

        expect(screen.queryByRole('dialog', {name: 'Actions'})).not.toBeInTheDocument();
        expect(screen.getByRole('dialog', {name: 'Say whose label'})).toBeInTheDocument();

        await userEvent.click(within(screen.getByRole('dialog', {name: 'Say whose label'})).getByRole('button', {name: 'Cancel'}));
        await openActions();
        await userEvent.click(screen.getByRole('button', {name: 'Make group'}));

        expect(screen.getByRole('dialog', {name: 'Make group'})).toBeInTheDocument();
    });

    it('offers merging the label into a group and making it a label of a group to whoever may', async () => {
        stubFetchAround(NO_SOFTWARE, json(GHOST));
        openLabel();

        await openActions();

        expect(screen.getByRole('button', {name: 'Merge into group and delete'})).toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Make a label of a group'})).toBeInTheDocument();
    });

    it('offers neither to whoever may not', async () => {
        stubFetchAround(NO_SOFTWARE, json({...GHOST, canGiveToGroup: false}));
        openLabel();

        await openActions();

        expect(screen.queryByRole('button', {name: 'Merge into group and delete'})).not.toBeInTheDocument();
        expect(screen.queryByRole('button', {name: 'Make a label of a group'})).not.toBeInTheDocument();
    });

    it('opens the page of the group the label is merged into', async () => {
        stubFetchAround(
            new Map([...NO_SOFTWARE, ['/api/entity-lookup?types=group&query=crew', json({items: [{id: CREW, type: 'group', title: 'Crew'}]})]]),
            json(GHOST),
            noContent(),
        );
        openLabel();

        await openActions();
        await userEvent.click(screen.getByRole('button', {name: 'Merge into group and delete'}));
        const dialog = screen.getByRole('dialog', {name: 'Merge into group and delete'});
        await userEvent.type(within(dialog).getByRole('combobox', {name: 'Group'}), 'crew');
        await userEvent.click(await screen.findByRole('option', {name: 'Crew'}));
        await userEvent.click(within(dialog).getByRole('button', {name: 'Merge and delete'}));

        await expect(screen.findByText('Group page')).resolves.toBeInTheDocument();
    });

    it('opens the page of the group the label is made a label of', async () => {
        stubFetchAround(
            new Map([...NO_SOFTWARE, ['/api/entity-lookup?types=group&query=crew', json({items: [{id: CREW, type: 'group', title: 'Crew'}]})]]),
            json(GHOST),
            noContent(),
        );
        openLabel();

        await openActions();
        await userEvent.click(screen.getByRole('button', {name: 'Make a label of a group'}));
        const dialog = screen.getByRole('dialog', {name: 'Make a label of a group'});
        await userEvent.type(within(dialog).getByRole('combobox', {name: 'Group'}), 'crew');
        await userEvent.click(await screen.findByRole('option', {name: 'Crew'}));
        await userEvent.click(within(dialog).getByRole('button', {name: 'Make a label'}));

        await expect(screen.findByText('Group page')).resolves.toBeInTheDocument();
    });

    it('offers no actions at all to whoever may do none of them', async () => {
        stubFetchAround(NO_SOFTWARE, json({...GHOST, canChange: false, canMakeOwner: false, canEdit: false, canGiveToGroup: false}));
        openLabel();

        await screen.findByRole('heading', {level: 1, name: 'Ghost'});

        expect(screen.queryByRole('button', {name: 'Actions'})).not.toBeInTheDocument();
    });

    it('hands a system error to the error dialog and shows nothing of its own', async () => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetchAround(NO_SOFTWARE, json({}, 500));
        openLabel();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(screen.queryByRole('heading', {level: 1})).not.toBeInTheDocument();

        unsubscribe();
    });

    it('tells who added the label and when', async () => {
        stubFetchAround(NO_SOFTWARE, json(GHOST));
        openLabel();

        await expect(screen.findByText('Added by Zork on 4 March 2026')).resolves.toBeInTheDocument();
    });

    it('tells only when the label was added once the account of whoever added it is gone', async () => {
        stubFetchAround(NO_SOFTWARE, json({...GHOST, added: {by: null, at: '2026-03-04T05:06:07Z'}}));
        openLabel();

        await expect(screen.findByText('Added on 4 March 2026')).resolves.toBeInTheDocument();
    });
});
