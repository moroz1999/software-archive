import {describe, expect, it} from 'vitest';
import {type ImportRun, runRefreshInterval} from './import-run';

const RUN: ImportRun = {
    id: '1', section: null, state: 'running', startedAt: '2026-11-14T12:00:00Z', endedAt: null,
    added: 0, known: 0, skipped: 0, failed: 0, addedPrograms: [], failedPrograms: [],
};

describe(runRefreshInterval, () => {
    it('reads the report again every few seconds while the run goes on', () => {
        expect(runRefreshInterval(RUN)).toBe(5000);
    });

    it('leaves the report of an ended run, or of none, as it is', () => {
        expect(runRefreshInterval({...RUN, state: 'done', endedAt: '2026-11-14T12:05:00Z'})).toBe(false);
        expect(runRefreshInterval(null)).toBe(false);
    });
});
