import {useCallback, useEffect, useRef, useState} from 'react';
import {BusinessError, SystemError} from './errors';
import {reportSystemError} from './system-errors';

export type ActionState =
    | {readonly status: 'idle'}
    | {readonly status: 'running'}
    | {readonly status: 'business-error'; readonly error: BusinessError};

export interface Action<A extends unknown[]> {
    readonly state: ActionState;
    readonly run: (...args: A) => void;
}

/**
 * Runs a user action (a save, a delete) and tracks its state. Business errors are returned to show
 * inline; system errors are reported to the error dialog with a retry of the same input
 * (docs/error-handling.md). The result goes to `onSuccess`.
 */
export function useAction<A extends unknown[], R>(action: (...args: A) => Promise<R>, onSuccess: (result: R) => void): Action<A> {
    const [state, setState] = useState<ActionState>({status: 'idle'});
    const latest = useRef({action, onSuccess});
    const mounted = useRef(true);

    useEffect(() => {
        latest.current = {action, onSuccess};
    });

    useEffect(() => {
        mounted.current = true;
        return () => {
            mounted.current = false;
        };
    }, []);

    const run = useCallback((...args: A) => {
        const attempt = () => {
            setState({status: 'running'});
            latest.current.action(...args).then(
                (result) => {
                    if (mounted.current) {
                        setState({status: 'idle'});
                    }
                    latest.current.onSuccess(result);
                },
                (error: unknown) => {
                    if (!mounted.current) {
                        return;
                    }
                    if (error instanceof BusinessError) {
                        setState({status: 'business-error', error});
                        return;
                    }
                    setState({status: 'idle'});
                    reportSystemError({
                        error: error instanceof SystemError ? error : new SystemError('response', 'Unexpected failure', {cause: error}),
                        retry: attempt,
                    });
                },
            );
        };
        attempt();
    }, []);

    return {state, run};
}
