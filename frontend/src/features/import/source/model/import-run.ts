import {bkCatalogSectionValues, type components} from '@/shared/api';


/** The source archives an import reads, in the order they are offered. */
export const IMPORT_SOURCES = ['vektor', 'basis', 'bkCatalog'] as const;

export type ImportSource = (typeof IMPORT_SOURCES)[number];

const SOURCE_ARCHIVES: Readonly<Record<ImportSource, string>> = {vektor: 'vektor-06c.ru', basis: 'caglrc.cc', bkCatalog: 'bk-catalog'};

/** The source an archive name of a run stands for; undefined for an archive this page does not know. */
export function importSourceOf(archive: string): ImportSource | undefined {
    return IMPORT_SOURCES.find((source) => SOURCE_ARCHIVES[source] === archive);
}

export function isImportSource(raw: string): raw is ImportSource {
    return IMPORT_SOURCES.some((source) => source === raw);
}

type Schemas = components['schemas'];

export type ImportRunState = Schemas['ImportRunState'];
/** A section of bk-catalog: its games, its programs or its demoscene. */
export type BkCatalogSection = Schemas['BkCatalogSection'];

/** The section of bk-catalog a code stands for; undefined for one this page does not know. */
export function bkCatalogSectionOf(code: string): BkCatalogSection | undefined {
    return bkCatalogSectionValues.find((section) => section === code);
}
/** What an import adds to software it found, in the order the report names them. */
export type SoftwareSupplement = Schemas['SoftwareSupplement'];
/** An author a run took for the first time: created by it, or found in the archive; whether it is still there. */
export type ImportRunAuthor = Schemas['ImportRunAuthorDto'];
/** Where a run is: the program it goes to, counted from 1, 0 while it reads the list; of how many when known. */
export type ImportRunProgress = Schemas['ImportRunProgressDto'];
/** A line of the log of a run, told by its kind: a later line has a greater seq; a line carried over from before the log has no time. */
export type ImportLogLine = Schemas['ImportRunLogLineDto'];
/** The report of a run of an import (docs/features/imports.md); its log holds every step the run took, in order. */
export type ImportRun = Schemas['ImportRunDto'];

/** How often the report of a run going on is read again. */
const RUNNING_REFRESH_MS = 5000;

/** How soon the report of a run is read again: every few seconds while the run goes on, never once it ended. */
export function runRefreshInterval(run: ImportRun | null | undefined): number | false {
    return run?.state === 'running' ? RUNNING_REFRESH_MS : false;
}

/**
 * The report read again over the one already there: the new lines of the log come after those it had, as long as it
 * is the same run; a run started since comes whole, its lines being all after those of an earlier one.
 */
export function withNewLines(previous: ImportRun | null | undefined, next: ImportRun | null): ImportRun | null {
    if (next === null || previous === null || previous === undefined || previous.id !== next.id) {
        return next;
    }
    return {...next, log: [...previous.log, ...next.log]};
}

/** The seq of the last line of the log already read, which the report read again starts after; none to read it all. */
export function lastSeq(run: ImportRun | null | undefined): number | undefined {
    return run?.log.at(-1)?.seq;
}
