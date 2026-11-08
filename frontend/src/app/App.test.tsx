import {StrictMode} from 'react';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {json, pageResponse, stubFetch} from '../shared/lib/test';
import {App} from './App';
import {createAppRouter} from './router';

const ALPHA = {id: '00000000-0000-0000-0001-000000000001', title: 'Fixture Software Alpha', releaseDate: '1985-03-02'};

describe(App, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('loads the first page once, even rendered twice in StrictMode', async () => {
        const fetchMock = stubFetch(json(pageResponse([ALPHA])));

        render(<StrictMode><App router={createAppRouter()}/></StrictMode>);

        await expect(screen.findByText('Fixture Software Alpha')).resolves.toBeInTheDocument();
        expect(fetchMock).toHaveBeenCalledExactlyOnceWith('/api/software?page=0&size=20', expect.anything());
    });

    it('recovers from a system error through the dialog retry', async () => {
        stubFetch(json({status: 500}, 500), json(pageResponse([ALPHA])));

        render(<App router={createAppRouter()}/>);

        await expect(screen.findByRole('alertdialog', {name: 'Something went wrong'})).resolves.toBeInTheDocument();

        await userEvent.click(screen.getByRole('button', {name: 'Retry'}));

        await expect(screen.findByText('Fixture Software Alpha')).resolves.toBeInTheDocument();
        expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
    });
});
