import type {SoftwareId} from '@/entities/software';

const IMPORT_RUN_STATES = ['running', 'done', 'stopped'] as const;

export type ImportRunState = (typeof IMPORT_RUN_STATES)[number];

const IMPORT_RUN_STATE_SET: ReadonlySet<string> = new Set(IMPORT_RUN_STATES);

export function isImportRunState(raw: string): raw is ImportRunState {
    return IMPORT_RUN_STATE_SET.has(raw);
}

/** How often the report of a run going on is read again. */
const RUNNING_REFRESH_MS = 5000;

/** How soon the report of a run is read again: every few seconds while the run goes on, never once it ended. */
export function runRefreshInterval(run: ImportRun | null | undefined): number | false {
    return run?.state === 'running' ? RUNNING_REFRESH_MS : false;
}

/** A program a run added, and whether its software is still in the archive. */
export interface ImportRunAdded {
    readonly externalId: string;
    readonly softwareId: SoftwareId;
    readonly title: string;
    readonly exists: boolean;
}

/** A program a run could not add: its page in the source archive and why. */
export interface ImportRunFailed {
    readonly externalId: string;
    readonly url: string;
    readonly reason: string;
}

/** The report of a run of an import (docs/features/vektor-import.md). */
export interface ImportRun {
    readonly id: string;
    /** The section imported; the whole site without one. */
    readonly section: string | null;
    readonly state: ImportRunState;
    readonly startedAt: string;
    readonly endedAt: string | null;
    readonly added: number;
    readonly known: number;
    readonly skipped: number;
    readonly failed: number;
    readonly addedPrograms: readonly ImportRunAdded[];
    readonly failedPrograms: readonly ImportRunFailed[];
}
