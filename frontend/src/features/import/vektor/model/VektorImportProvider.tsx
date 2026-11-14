import {type ReactNode, useState} from 'react';
import {useMutation, useQuery} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {startVektorImport} from '../api/vektor-import-api';
import {vektorSectionsQuery} from '../api/vektor-import-queries';
import {vektorImportMessages} from '../config/messages';
import {VektorImportContext, type VektorImportState} from './vektor-import-context';

/** Starting an import of vektor-06c.ru, whole or one section of it. */
export function VektorImportProvider({children}: {readonly children: ReactNode}) {
    const {t} = useTranslation();
    const sections = useQuery(vektorSectionsQuery());
    const [section, setSection] = useState('');
    const {mutate, isPending, isSuccess, error, reset} = useMutation({mutationFn: startVektorImport});
    const businessError = businessErrorOf(error);

    const state: VektorImportState = {
        sections,
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
