import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {ScreenshotsProvider} from '../model/ScreenshotsProvider';
import {SoftwareScreenshots} from './SoftwareScreenshots';

const ID = '00000000-0000-0000-0001-000000000001';
const SHOTS = [
    {id: '00000000-0000-0000-0055-000000000001', url: '/api/screenshots/00000000-0000-0000-0055-000000000001'},
    {id: '00000000-0000-0000-0055-000000000002', url: '/api/screenshots/00000000-0000-0000-0055-000000000002'},
];

function renderScreenshots({enabled = true}: {readonly enabled?: boolean} = {}) {
    renderWithQueries(
        <ScreenshotsProvider softwareId={ID} enabled={enabled}>
            <SoftwareScreenshots title="Elite"/>
        </ScreenshotsProvider>,
    );
}

describe(SoftwareScreenshots, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows the screenshots of the software under their heading', async () => {
        const fetchMock = stubFetch(json(SHOTS));
        renderScreenshots();

        const section = await screen.findByRole('region', {name: 'Screenshots'});

        await expect(within(section).findAllByRole('button', {name: /^Screenshot \d of 2$/u})).resolves.toHaveLength(2);
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/software/${ID}/screenshots`);
    });

    it('shows nothing when there are none', async () => {
        const fetchMock = stubFetch(json([]));
        renderScreenshots();

        await vi.waitFor(() => {
            expect(fetchMock).toHaveBeenCalledTimes(1);
        });
        await vi.waitFor(() => {
            expect(screen.queryByTestId('software-screenshots-skeleton')).not.toBeInTheDocument();
        });

        expect(screen.queryByRole('region', {name: 'Screenshots'})).not.toBeInTheDocument();
    });

    it('holds their place while they load and asks for nothing until in view', () => {
        const fetchMock = stubFetch(json(SHOTS));
        renderScreenshots({enabled: false});

        expect(screen.getByTestId('software-screenshots-skeleton')).toBeInTheDocument();
        expect(fetchMock).not.toHaveBeenCalled();
    });

    it('hands a system error to the error dialog', async () => {
        stubFetch(json({status: 500}, 500));
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => reports.push(report));
        renderScreenshots();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(screen.queryByRole('region', {name: 'Screenshots'})).not.toBeInTheDocument();

        unsubscribe();
    });
});
