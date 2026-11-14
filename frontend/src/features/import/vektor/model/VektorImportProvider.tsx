import {type ReactNode, useState} from 'react';
import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {startVektorImport} from '../api/vektor-import-api';
import {lastImportRunQuery, vektorSectionsQuery} from '../api/vektor-import-queries';
import {vektorImportMessages} from '../config/messages';
import {VektorImportContext, type VektorImportState} from './vektor-import-context';

/** Starting an import of vektor-06c.ru, whole or one section of it, and the report of the import started last. */
export function VektorImportProvider({children}: {readonly children: ReactNode}) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const sections = useQuery(vektorSectionsQuery());
    const lastRun = useQuery(lastImportRunQuery());
    const [section, setSection] = useState('');
    const {mutate, isPending, isSuccess, error, reset} = useMutation({
        mutationFn: startVektorImport,
        // The run is written down before the start answers: its report shows at once.
        onSuccess: () => queryClient.invalidateQueries({queryKey: lastImportRunQuery().queryKey}),
    });
    const businessError = businessErrorOf(error);

    const state: VektorImportState = {
        sections,
        lastRun,
        section,
        choose: (code) => {
            setSection(code);
            reset();
        },
        start: () => {
            mutate(section === '' ? null : section);
        },
        starting: isPending,
        started: isSuccess,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, vektorImportMessages(t)),
    };

    return <VektorImportContext value={state}>{children}</VektorImportContext>;
}
