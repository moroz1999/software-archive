import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {subscribeSystemErrors, type SystemErrorReport} from '../../../shared/api';
import {json, stubFetch} from '../../../shared/lib/test';
import {CreateSoftwareForm} from './CreateSoftwareForm';

const ID = '00000000-0000-0000-0001-000000000001';

describe(CreateSoftwareForm, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('adds the software and hands its id on', async () => {
        const fetchMock = stubFetch(json({id: ID}, 201));
        const onCreated = vi.fn<(id: string) => void>();
        render(<CreateSoftwareForm onCreated={onCreated}/>);

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Elite');
        await userEvent.click(screen.getByRole('button', {name: 'Add software'}));

        await vi.waitFor(() => {
            expect(onCreated).toHaveBeenCalledExactlyOnceWith(ID);
        });

        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'POST', body: '{"title":"Elite","description":null,"releaseDate":null}'});
    });

    it('sends nothing until the title is filled', async () => {
        const fetchMock = stubFetch(json({id: ID}, 201));
        render(<CreateSoftwareForm onCreated={vi.fn<(id: string) => void>()}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Add software'}));

        expect(fetchMock).not.toHaveBeenCalled();
    });

    it('hands a system error to the error dialog and keeps the form', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetch(json({status: 500}, 500));
        render(<CreateSoftwareForm onCreated={vi.fn<(id: string) => void>()}/>);

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Elite');
        await userEvent.click(screen.getByRole('button', {name: 'Add software'}));

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveValue('Elite');
        expect(screen.getByRole('textbox', {name: 'Title'})).toBeEnabled();
    });
});
