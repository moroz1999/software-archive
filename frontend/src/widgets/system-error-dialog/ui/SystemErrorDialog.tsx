import {useEffect, useState} from 'react';
import {subscribeSystemErrors, type SystemErrorKind, type SystemErrorReport} from '@/shared/api';
import {ErrorDialog} from '@/shared/ui/dialog';

const MESSAGES: Readonly<Record<SystemErrorKind, string>> = {
    network: 'The server could not be reached. Check your connection and try again.',
    server: 'The server could not complete the request. Please try again.',
    response: 'The server sent a response this page does not understand. Please try again later.',
};

/** The single system error modal, mounted once by the app (docs/error-handling.md). */
export function SystemErrorDialog() {
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
            title="Something went wrong"
            message={MESSAGES[report.error.kind]}
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
