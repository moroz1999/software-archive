import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {ImportRun, ImportSource} from './import-run';

export interface SourceImportState {
    readonly sections: UseQueryResult<readonly string[]>;
    /** The report of the import started last; null before the first one. */
    readonly lastRun: UseQueryResult<ImportRun | null>;
    readonly source: ImportSource;
    readonly chooseSource: (source: ImportSource) => void;
    /** The code of the chosen section of vektor-06c.ru; empty for the whole site. */
    readonly section: string;
    readonly choose: (section: string) => void;
    readonly start: () => void;
    readonly starting: boolean;
    /** Whether the last start went through: the import goes on in the background. */
    readonly started: boolean;
    /** Why the import could not be started. */
    readonly error: string | undefined;
}

/** The start of an import of a source archive, for the controls inside a SourceImportProvider. */
export const [SourceImportContext, useSourceImport] = createRequiredContext<SourceImportState>('SourceImport');
