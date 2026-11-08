import type {SystemError} from './errors';

export interface SystemErrorReport {
    readonly error: SystemError;
    /** Repeats the failed action, when it can be repeated. */
    readonly retry?: () => void;
}

type Listener = (report: SystemErrorReport) => void;

const listeners = new Set<Listener>();

/** Hands a system error to the single error dialog mounted by the app. */
export function reportSystemError(report: SystemErrorReport): void {
    listeners.forEach((listener) => {
        listener(report);
    });
}

export function subscribeSystemErrors(listener: Listener): () => void {
    listeners.add(listener);
    return () => {
        listeners.delete(listener);
    };
}
