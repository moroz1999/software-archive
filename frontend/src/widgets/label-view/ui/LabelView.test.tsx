import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {toLabelId} from '@/entities/labels';
import {LabelView} from './LabelView';

const ID = '00000000-0000-0000-0004-000000000001';
const CREW = '00000000-0000-0000-0005-000000000001';
const JANE = '00000000-0000-0000-0003-000000000001';
const ELITE = '00000000-0000-0000-0001-000000000001';
const GHOST = {id: ID, name: 'Ghost', owner: null, groups: [{id: CREW, name: 'Crew'}], canChange: true};

const SOFTWARE_URL = `/api/labels/${ID}/software`;
const NO_SOFTWARE = new Map([[SOFTWARE_URL, json([])]]);

function openLabel() {
    const router = createMemoryRouter([
        {path: '/labels/:id', element: <LabelView id={toLabelId(ID)}/>},
        {path: '/authors/:id', element: <p>The page of the person</p>},
    ], {initialEntries: [`/labels/${ID}`]});
    renderWithQueries(<RouterProvider router={router}/>);
    return router;
}

describe(LabelView, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows the label under its name, its owner unknown', async () => {
        stubFetchAround(NO_SOFTWARE, json(GHOST));
        openLabel();

        await expect(screen.findByRole('heading', {level: 1, name: 'Ghost'})).resolves.toBeInTheDocument();
        expect(screen.getByText('Owner unknown')).toBeInTheDocument();
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

    it('offers attaching the label to its owner', async () => {
        stubFetchAround(NO_SOFTWARE, json(GHOST));
        openLabel();

        await expect(screen.findByRole('button', {name: 'Attach to owner'})).resolves.toBeInTheDocument();
    });

    it('offers no attaching to whoever may not do it', async () => {
        stubFetchAround(NO_SOFTWARE, json({...GHOST, canChange: false}));
        openLabel();

        await screen.findByRole('heading', {level: 1, name: 'Ghost'});

        expect(screen.queryByRole('button', {name: 'Attach to owner'})).not.toBeInTheDocument();
    });

    it('goes over to the page of the owner of a label that has one', async () => {
        stubFetchAround(NO_SOFTWARE, json({...GHOST, owner: {type: 'author', id: JANE}}));
        const router = openLabel();

        await expect(screen.findByText('The page of the person')).resolves.toBeInTheDocument();
        expect(router.state.location.pathname).toBe(`/authors/${JANE}`);
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
