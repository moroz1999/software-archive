import {type ReactNode, useState} from 'react';
import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {startBasisImport, startVektorImport} from '../api/import-api';
import {basisCategoriesQuery, lastImportRunQuery, vektorSectionsQuery} from '../api/import-queries';
import {importMessages} from '../config/messages';
import type {ImportSource} from './import-run';
import {SourceImportContext, type SourceImportState} from './source-import-context';

/**
 * Starting an import of a source archive — whole or one section or category — and the report of the import started
 * last.
 */
export function SourceImportProvider({children}: {readonly children: ReactNode}) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const [source, setSource] = useState<ImportSource>('vektor');
    const [section, setSection] = useState('');
    const sections = useQuery(vektorSectionsQuery());
    const basisCategories = useQuery({...basisCategoriesQuery(), enabled: source === 'basis'});
    const lastRun = useQuery(lastImportRunQuery());
    const {mutate, isPending, isSuccess, error, reset} = useMutation({
        mutationFn: ({source: chosen, section: code}: {readonly source: ImportSource; readonly section: string | null}) =>
            chosen === 'basis' ? startBasisImport(code) : startVektorImport(code),
        // The run is written down before the start answers: its report shows at once.
        onSuccess: () => queryClient.invalidateQueries({queryKey: lastImportRunQuery().queryKey}),
    });
    const businessError = businessErrorOf(error);

    const state: SourceImportState = {
        sections,
        basisCategories,
        lastRun,
        source,
        chooseSource: (chosen) => {
            setSource(chosen);
            setSection('');
            reset();
        },
        section,
        choose: (code) => {
            setSection(code);
            reset();
        },
        start: () => {
            mutate({source, section: section === '' ? null : section});
        },
        starting: isPending,
        started: isSuccess,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, importMessages(t)),
    };

    return <SourceImportContext value={state}>{children}</SourceImportContext>;
}
