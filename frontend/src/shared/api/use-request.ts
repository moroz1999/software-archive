import {useCallback, useEffect, useState} from 'react';
import {BusinessError, SystemError} from './errors';
import type {Load, Preloaded, ViewRequest} from './preload';
import {reportSystemError} from './system-errors';

export type RequestState<T> =
    | {readonly status: 'loading'}
    | {readonly status: 'success'; readonly data: T; readonly reloading: boolean}
    | {readonly status: 'business-error'; readonly error: BusinessError}
    | {readonly status: 'system-error'};

export interface Request<T> {
    readonly state: RequestState<T>;
    readonly reload: () => void;
}

export interface RequestOptions {
    /** The call starts only once enabled, e.g. when its block enters the viewport. */
    readonly enabled?: boolean;
}

/** The state a finished preload shows at once; a system error is reported by the effect instead. */
function preloadedState<T>(preloaded: Preloaded<T> | undefined): RequestState<T> | undefined {
    const settled = preloaded?.settled;
    if (settled === undefined) {
        return undefined;
    }
    if (settled.ok) {
        return {status: 'success', data: settled.value, reloading: false};
    }
    return settled.error instanceof BusinessError ? {status: 'business-error', error: settled.error} : undefined;
}

/**
 * Runs a call and tracks its state. Business errors are returned to the caller to show inline;
 * system errors are reported to the error dialog with a retry and never handled by the caller.
 * A view request preloaded by the route is taken over instead of being called again.
 * A bare load function must be stable (module-level or memoized): a new function starts a new call.
 */
export function useRequest<T>(request: ViewRequest<T> | Load<T>, {enabled = true}: RequestOptions = {}): Request<T> {
    const load = typeof request === 'function' ? request : request.load;
    const [preloaded] = useState(() => (typeof request === 'function' ? undefined : request.take()));
    const [shownAtOnce] = useState(() => preloadedState(preloaded));
    const [state, setState] = useState<RequestState<T>>(shownAtOnce ?? {status: 'loading'});
    const [attempt, setAttempt] = useState(0);

    const reload = useCallback(() => {
        setState((current) => (current.status === 'success' ? {...current, reloading: true} : {status: 'loading'}));
        setAttempt((n) => n + 1);
    }, []);

    useEffect(() => {
        const first = attempt === 0;
        if (!enabled || (first && shownAtOnce !== undefined)) {
            return undefined;
        }
        let active = true;
        const controller = new AbortController();
        const call = first && preloaded !== undefined ? preloaded.promise : load(controller.signal);
        call.then(
            (data) => {
                if (active) {
                    setState({status: 'success', data, reloading: false});
                }
            },
            (error: unknown) => {
                if (!active) {
                    return;
                }
                if (error instanceof BusinessError) {
                    setState({status: 'business-error', error});
                    return;
                }
                setState({status: 'system-error'});
                reportSystemError({
                    error: error instanceof SystemError ? error : new SystemError('response', 'Unexpected failure', {cause: error}),
                    retry: reload,
                });
            },
        );
        return () => {
            active = false;
            controller.abort();
        };
    }, [load, attempt, reload, enabled, preloaded, shownAtOnce]);

    return {state, reload};
}
