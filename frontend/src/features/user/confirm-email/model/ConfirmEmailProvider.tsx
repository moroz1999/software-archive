import {type ReactNode, useEffect, useRef} from 'react';
import {useMutation} from '@tanstack/react-query';
import {businessErrorOf} from '@/shared/api';
import {confirmEmail} from '../api/confirm-api';
import {ConfirmEmailContext, type ConfirmationOutcome} from './confirm-email-context';

export interface ConfirmEmailProviderProps {
    /** The token of the link; null when the link has none. */
    readonly token: string | null;
    readonly children: ReactNode;
}

/**
 * Confirms the account of a link once, as soon as it opens, which signs its user in. A link without a token sends
 * nothing and does not work.
 */
export function ConfirmEmailProvider({token, children}: ConfirmEmailProviderProps) {
    const {mutate, isSuccess, isError, error} = useMutation({
        mutationFn: confirmEmail,
    });
    // A link is used once: the effect running again (StrictMode) must not send it twice.
    const sent = useRef(false);
    useEffect(() => {
        if (token !== null && !sent.current) {
            sent.current = true;
            mutate(token);
        }
    }, [token, mutate]);

    return <ConfirmEmailContext value={outcome(token, isSuccess, isError, error)}>{children}</ConfirmEmailContext>;
}

function outcome(token: string | null, confirmed: boolean, failed: boolean, error: unknown): ConfirmationOutcome {
    if (token === null) {
        return 'invalid';
    }
    if (confirmed) {
        return 'confirmed';
    }
    if (!failed) {
        return 'confirming';
    }
    const refusal = businessErrorOf(error);
    if (refusal === undefined) {
        return 'failed';
    }
    return refusal.code === 'user.confirmation-expired' ? 'expired' : 'invalid';
}
