import {StrictMode} from 'react';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createQueryClient} from '@/shared/api';
import {json, noContent, pageResponse, stubFetch} from '@/shared/lib/test';
import {App} from './App';
import {type AppRouter, createAppRouter} from './router';

const ALPHA = {id: '00000000-0000-0000-0001-000000000001', title: 'Fixture Software Alpha', releaseDate: '1985-03-02'};
const ALPHA_DETAILS = {...ALPHA, description: null};

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
        expect(fetchMock).toHaveBeenCalledExactlyOnceWith('/api/software?page=0&size=20', expect.anything());
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

    it.each(['/software/42', '/software/42/edit'])('shows the not found page for %s without asking the backend', async (path) => {
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
