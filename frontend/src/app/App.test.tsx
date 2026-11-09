import {StrictMode} from 'react';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createQueryClient} from '@/shared/api';
import {json, noContent, pageResponse, stubFetchAround, stubFetchRoutes} from '@/shared/lib/test';
import {App} from './App';
import {type AppRouter, createAppRouter} from './router';

const ALPHA = {id: '00000000-0000-0000-0001-000000000001', title: 'Fixture Software Alpha', releaseDate: '1985-03-02'};
const ALPHA_DETAILS = {...ALPHA, description: null, platform: {code: 'vector', name: 'Vector'}, hardware: []};
const ZED = '00000000-0000-0000-0003-000000000001';
const CREW = '00000000-0000-0000-0005-000000000001';
const ZED_DETAILS = {id: ZED, name: 'Zed', realName: null, notes: null, labels: [], locations: [], groups: [{id: CREW, name: 'Crew', as: null}]};
const CREW_DETAILS = {
    id: CREW, name: 'Crew', groupName: 'Crew Ltd', notes: null, types: [], labels: [], locations: [],
    members: [{name: 'Zed', personId: ZED, personName: null}], memberGroups: [], groups: [],
};

/** What every page or the software page loads besides its own data: the platforms of the switch, the releases. */
const AROUND = new Map([
    ['/api/platforms', json([{code: 'vector', name: 'Vector'}])],
    [`/api/software/${ALPHA.id}/releases`, json([])],
]);

/** Answers what every page loads, every other call with the responses in order. */
function stubFetch(...responses: (() => Response)[]) {
    return stubFetchAround(AROUND, ...responses);
}

/** Answers what every page loads and the given routes. */
function stubRoutes(routes: [string, () => Response][]) {
    return stubFetchRoutes(new Map([...AROUND, ...routes]));
}

describe(App, () => {
    const routers: AppRouter[] = [];

    const queryClient = createQueryClient();

    /** A router follows the browser history until disposed; one left behind would react to the next test. */
    function appRouter(): AppRouter {
        const router = createAppRouter(queryClient);
        routers.push(router);
        return router;
    }

    afterEach(() => {
        routers.forEach((router) => {
            router.dispose();
        });
        routers.length = 0;
        queryClient.clear();
        window.history.replaceState(null, '', '/');
        vi.unstubAllGlobals();
    });

    it('loads the first page once, even rendered twice in StrictMode', async () => {
        const fetchMock = stubFetch(json(pageResponse([ALPHA])));

        render(<StrictMode><App router={appRouter()} queryClient={queryClient}/></StrictMode>);

        await expect(screen.findByText('Fixture Software Alpha')).resolves.toBeInTheDocument();
        expect(fetchMock).toHaveBeenCalledExactlyOnceWith('/api/software/latest?size=12', expect.anything());
    });

    it('opens the sections of the archive from the main menu, which stays over every page', async () => {
        stubRoutes([
            ['/api/software/latest?size=12', json(pageResponse([ALPHA], {size: 12}))],
            ['/api/software?page=0&size=20', json(pageResponse([ALPHA]))],
            ['/api/authors?page=0&size=100', json(pageResponse([{personId: ZED, name: 'Zed', labelOf: null}], {size: 100}))],
            ['/api/countries', json([])],
            [`/api/authors/${ZED}`, json(ZED_DETAILS)],
        ]);

        render(<App router={appRouter()} queryClient={queryClient}/>);

        await expect(screen.findByRole('heading', {level: 1, name: 'Recently added'})).resolves.toBeInTheDocument();

        const menu = screen.getByRole('navigation', {name: 'Main menu'});

        await userEvent.click(within(menu).getByRole('link', {name: 'Software'}));

        await expect(screen.findByRole('heading', {level: 1, name: 'Software'})).resolves.toBeInTheDocument();

        await userEvent.click(within(menu).getByRole('link', {name: 'Authors'}));

        await expect(screen.findByRole('heading', {level: 1, name: 'Authors'})).resolves.toBeInTheDocument();

        await userEvent.click(await screen.findByRole('link', {name: 'Zed'}));

        await expect(screen.findByRole('heading', {level: 1, name: 'Zed'})).resolves.toBeInTheDocument();
        expect(within(screen.getByRole('navigation', {name: 'Main menu'})).getByRole('link', {name: 'Authors'})).toHaveAttribute('aria-current', 'page');
    });

    it('opens the groups from the main menu, a group from the catalog and its members from the group', async () => {
        stubRoutes([
            ['/api/software/latest?size=12', json(pageResponse([ALPHA], {size: 12}))],
            ['/api/groups?page=0&size=100', json(pageResponse([{groupId: CREW, name: 'Crew', groupName: 'Crew Ltd'}], {size: 100}))],
            ['/api/countries', json([])],
            [`/api/groups/${CREW}`, json(CREW_DETAILS)],
            [`/api/authors/${ZED}`, json(ZED_DETAILS)],
        ]);

        render(<App router={appRouter()} queryClient={queryClient}/>);

        await screen.findByRole('heading', {level: 1, name: 'Recently added'});
        await userEvent.click(within(screen.getByRole('navigation', {name: 'Main menu'})).getByRole('link', {name: 'Groups'}));

        await expect(screen.findByRole('heading', {level: 1, name: 'Groups'})).resolves.toBeInTheDocument();

        await userEvent.click(await screen.findByRole('link', {name: 'Crew'}));

        await expect(screen.findByRole('heading', {level: 1, name: 'Crew'})).resolves.toBeInTheDocument();
        expect(within(screen.getByRole('navigation', {name: 'Main menu'})).getByRole('link', {name: 'Groups'})).toHaveAttribute('aria-current', 'page');

        await userEvent.click(within(screen.getByRole('list', {name: 'Members'})).getByRole('link', {name: 'Zed'}));

        await expect(screen.findByRole('heading', {level: 1, name: 'Zed'})).resolves.toBeInTheDocument();

        await userEvent.click(within(screen.getByRole('list', {name: 'Groups'})).getByRole('link', {name: 'Crew'}));

        await expect(screen.findByRole('heading', {level: 1, name: 'Crew'})).resolves.toBeInTheDocument();
    });

    it('shows the not found page for a group that does not exist', async () => {
        stubFetch(json({status: 404, code: 'group.not-found'}, 404));
        window.history.replaceState(null, '', `/groups/${CREW}`);

        render(<App router={appRouter()} queryClient={queryClient}/>);

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
        expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
    });

    it('shows the not found page for an author who does not exist', async () => {
        stubFetch(json({status: 404, code: 'author.not-found'}, 404));
        window.history.replaceState(null, '', `/authors/${ZED}`);

        render(<App router={appRouter()} queryClient={queryClient}/>);

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
        expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
    });

    it('recovers from a system error through the dialog retry', async () => {
        stubFetch(json({status: 500}, 500), json(pageResponse([ALPHA])));

        render(<App router={appRouter()} queryClient={queryClient}/>);

        await expect(screen.findByRole('alertdialog', {name: 'Something went wrong'})).resolves.toBeInTheDocument();

        await userEvent.click(screen.getByRole('button', {name: 'Retry'}));

        await expect(screen.findByText('Fixture Software Alpha')).resolves.toBeInTheDocument();
        expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
    });

    it('shows the saved values on the software page after editing', async () => {
        stubFetch(json(ALPHA_DETAILS), noContent(), json({...ALPHA_DETAILS, title: 'Fixture Software Alpha Plus'}));
        window.history.replaceState(null, '', `/software/${ALPHA.id}`);

        render(<App router={appRouter()} queryClient={queryClient}/>);

        await userEvent.click(await screen.findByRole('link', {name: 'Edit'}));
        const title = await screen.findByRole('textbox', {name: 'Title'});
        await userEvent.clear(title);
        await userEvent.type(title, 'Fixture Software Alpha Plus');
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await expect(screen.findByRole('heading', {level: 1, name: 'Fixture Software Alpha Plus'})).resolves.toBeInTheDocument();
    });

    it('shows the not found page for software that does not exist', async () => {
        stubFetch(json({status: 404, code: 'software.not-found'}, 404));
        window.history.replaceState(null, '', `/software/${ALPHA.id}`);

        render(<App router={appRouter()} queryClient={queryClient}/>);

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
        expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
    });

    it.each(['/software/42', '/software/42/edit', '/authors/42', '/groups/42'])('shows the not found page for %s without asking the backend', async (path) => {
        const fetchMock = stubFetch(json(ALPHA_DETAILS));
        window.history.replaceState(null, '', path);

        render(<App router={appRouter()} queryClient={queryClient}/>);

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
        expect(fetchMock).not.toHaveBeenCalled();
    });

    it('shows the not found page for an address the app does not know', async () => {
        stubFetch(json(ALPHA_DETAILS));
        window.history.replaceState(null, '', '/nowhere');

        render(<App router={appRouter()} queryClient={queryClient}/>);

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('navigation', {name: 'Main menu'})).toBeInTheDocument();
    });

    it('shows the not found page when the software is deleted meanwhile and then saved', async () => {
        stubFetch(json(ALPHA_DETAILS), json({status: 404, code: 'software.not-found'}, 404));
        window.history.replaceState(null, '', `/software/${ALPHA.id}/edit`);

        render(<App router={appRouter()} queryClient={queryClient}/>);

        await userEvent.click(await screen.findByRole('button', {name: 'Save'}));

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
        expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
    });

    it('shows the not found page when the software is deleted meanwhile and then deleted', async () => {
        stubFetch(json(ALPHA_DETAILS), json({status: 404, code: 'software.not-found'}, 404));
        window.history.replaceState(null, '', `/software/${ALPHA.id}/edit`);

        render(<App router={appRouter()} queryClient={queryClient}/>);

        await userEvent.click(await screen.findByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
    });
});
