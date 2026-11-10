import {useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {subscribeSystemErrors, type SystemErrorKind, type SystemErrorReport} from '@/shared/api';
import {ErrorDialog} from '@/shared/ui/dialog';

const MESSAGE_KEYS: Readonly<Record<SystemErrorKind, string>> = {
    network: 'errors.system.network',
    server: 'errors.system.server',
    response: 'errors.system.response',
};

/** The single system error modal, mounted once by the app (docs/error-handling.md). */
export function SystemErrorDialog() {
    const {t} = useTranslation();
    const [report, setReport] = useState<SystemErrorReport | null>(null);

    useEffect(() => subscribeSystemErrors(setReport), []);

    const close = () => {
        setReport(null);
    };

    if (report === null) {
        return null;
    }

    const {retry} = report;

    return (
        <ErrorDialog
            open
            title={t('errors.system.title')}
            message={t(MESSAGE_KEYS[report.error.kind])}
            onClose={close}
            {...(retry === undefined ? {} : {
                onRetry: () => {
                    setReport(null);
                    retry();
                },
            })}
        />
    );
}
