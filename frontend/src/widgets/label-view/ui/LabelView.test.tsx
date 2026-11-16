import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {toLabelId} from '@/entities/labels';
import {LabelView} from './LabelView';

const ID = '00000000-0000-0000-0004-000000000001';
const CREW = '00000000-0000-0000-0005-000000000001';
const JANE = '00000000-0000-0000-0003-000000000001';
const ELITE = '00000000-0000-0000-0001-000000000001';
const GHOST = {id: ID, name: 'Ghost', owner: null, groups: [{id: CREW, name: 'Crew'}], canChange: true, canMakeOwner: true};

const SOFTWARE_URL = `/api/labels/${ID}/software`;
const NO_SOFTWARE = new Map([[SOFTWARE_URL, json([])]]);

function openLabel() {
    const router = createMemoryRouter([
        {path: '/labels/:id', element: <LabelView id={toLabelId(ID)}/>},
        {path: '/authors/:id', element: <p>Author page</p>},
    ], {initialEntries: [`/labels/${ID}`]});
    renderWithQueries(<RouterProvider router={router}/>);
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

    it('leads to the groups the label was a member of', async () => {
        stubFetchAround(NO_SOFTWARE, json(GHOST));
        openLabel();

        const groups = await screen.findByRole('list', {name: 'Member of'});

        expect(within(groups).getByRole('link', {name: 'Crew'})).toHaveAttribute('href', `/groups/${CREW}`);
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

        await expect(screen.findByRole('button', {name: 'Say whose label'})).resolves.toBeInTheDocument();
    });

    it('offers no attaching to whoever may not do it', async () => {
        stubFetchAround(NO_SOFTWARE, json({...GHOST, canChange: false}));
        openLabel();

        await screen.findByRole('heading', {level: 1, name: 'Ghost'});

        expect(screen.queryByRole('button', {name: 'Say whose label'})).not.toBeInTheDocument();
    });

    it('shows whose the label is when known, the person by the real name with the main name next to it, leading to their page, and offers no attaching', async () => {
        stubFetchAround(NO_SOFTWARE, json({...GHOST, owner: {type: 'author', id: JANE, name: 'Jane Doe', note: 'JD'}, canChange: false}));
        openLabel();

        await screen.findByRole('heading', {level: 1, name: 'Ghost'});

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

        await expect(screen.findByRole('button', {name: 'Make author'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Make group'})).toBeInTheDocument();
    });

    it('offers making no owner to whoever may not take the label', async () => {
        stubFetchAround(NO_SOFTWARE, json({...GHOST, canMakeOwner: false}));
        openLabel();

        await screen.findByRole('heading', {level: 1, name: 'Ghost'});

        expect(screen.queryByRole('button', {name: 'Make author'})).not.toBeInTheDocument();
        expect(screen.queryByRole('button', {name: 'Make group'})).not.toBeInTheDocument();
    });

    it('opens the page of the person made of the label', async () => {
        stubFetchAround(NO_SOFTWARE, json(GHOST), json({id: JANE}, 201));
        openLabel();

        await userEvent.click(await screen.findByRole('button', {name: 'Make author'}));
        await userEvent.click(within(screen.getByRole('dialog', {name: 'Make author'})).getByRole('button', {name: 'Make author'}));

        await expect(screen.findByText('Author page')).resolves.toBeInTheDocument();
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
});
