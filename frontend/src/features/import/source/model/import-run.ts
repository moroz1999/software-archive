import type {SoftwareId} from '@/entities/software';

/** The source archives an import reads, in the order they are offered. */
export const IMPORT_SOURCES = ['vektor', 'basis'] as const;

export type ImportSource = (typeof IMPORT_SOURCES)[number];

const SOURCE_ARCHIVES: Readonly<Record<ImportSource, string>> = {vektor: 'vektor-06c.ru', basis: 'caglrc.cc'};

/** The source an archive name of a run stands for; undefined for an archive this page does not know. */
export function importSourceOf(archive: string): ImportSource | undefined {
    return IMPORT_SOURCES.find((source) => SOURCE_ARCHIVES[source] === archive);
}

export function isImportSource(raw: string): raw is ImportSource {
    return (IMPORT_SOURCES as readonly string[]).includes(raw);
}

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

/** A code of a fixed list as the server sends it: a guard for each list, so a code it does not know is refused. */
function isOneOf<T extends string>(codes: readonly T[]): (raw: string) => raw is T {
    const set: ReadonlySet<string> = new Set(codes);
    return (raw: string): raw is T => set.has(raw);
}

/** What an import adds to software it found, in the order the report names them. */
export const SOFTWARE_SUPPLEMENTS = ['description', 'release-date', 'preservation', 'categories', 'credits', 'screenshots'] as const;

export type SoftwareSupplement = (typeof SOFTWARE_SUPPLEMENTS)[number];

export const isSoftwareSupplement = isOneOf(SOFTWARE_SUPPLEMENTS);

/** A program a run found in the archive, whether its software is still there, and what the run added to it. */
export interface ImportRunKnown {
    readonly externalId: string;
    readonly softwareId: SoftwareId;
    readonly title: string;
    readonly exists: boolean;
    readonly supplements: readonly SoftwareSupplement[];
}

const SKIP_REASONS = ['section', 'category'] as const;

export type SkipReason = (typeof SKIP_REASONS)[number];

export const isSkipReason = isOneOf(SKIP_REASONS);

/** A program a run left out: its page and title in the source, and the section or the category it was left out by. */
export interface ImportRunSkipped {
    readonly externalId: string;
    readonly url: string;
    readonly title: string;
    readonly reason: SkipReason;
    readonly detail: string;
}

const TAKEN_AUTHOR_KINDS = ['author', 'group', 'label'] as const;

export type TakenAuthorKind = (typeof TAKEN_AUTHOR_KINDS)[number];

export const isTakenAuthorKind = isOneOf(TAKEN_AUTHOR_KINDS);

/** An author a run took for the first time: created by it, or found in the archive; whether it is still there. */
export interface ImportRunAuthor {
    readonly kind: TakenAuthorKind;
    readonly id: string;
    readonly name: string;
    readonly created: boolean;
    readonly exists: boolean;
}

const PLACE_KINDS = ['city', 'country'] as const;

export type PlaceKind = (typeof PLACE_KINDS)[number];

export const isPlaceKind = isOneOf(PLACE_KINDS);

/** A city or a country a run added, under the name the source gave. */
export interface ImportRunPlace {
    readonly kind: PlaceKind;
    readonly name: string;
}

/** Where a run is: the program it goes to, counted from 1, 0 while it reads the list; of how many when known. */
export interface ImportRunProgress {
    readonly position: number;
    readonly total: number | null;
    readonly url: string | null;
}

/** The report of a run of an import (docs/features/imports.md). */
export interface ImportRun {
    readonly id: string;
    /** The name of the source archive the run read, such as vektor-06c.ru. */
    readonly archive: string;
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
    readonly knownPrograms: readonly ImportRunKnown[];
    readonly skippedPrograms: readonly ImportRunSkipped[];
    readonly authors: readonly ImportRunAuthor[];
    readonly places: readonly ImportRunPlace[];
    readonly progress: ImportRunProgress;
}
