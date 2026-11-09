import {MutationCache, QueryCache, QueryClient} from '@tanstack/react-query';
import {BusinessError, NotFoundError, SystemError} from './errors';
import {reportSystemError} from './system-errors';

function isNotFound(error: Error): boolean {
    return error instanceof NotFoundError;
}

/** How long loaded data counts as fresh: a block mounted right after its route preloaded it does not call again. */
const FRESH_MS = 30_000;

/** A failure the user cannot fix, as the error dialog explains it; null for a business error or a missing entity. */
function systemErrorOf(error: unknown): SystemError | null {
    if (error instanceof BusinessError || error instanceof NotFoundError) {
        return null;
    }
    return error instanceof SystemError ? error : new SystemError('response', 'Unexpected failure', {cause: error});
}

/**
 * The query client of the app, one per page load. System errors of queries and mutations go to the error
 * dialog with a retry, and so do business errors of queries; business errors of mutations stay in the mutation
 * for the form that ran it to show; a missing entity is thrown to the route's error boundary, which shows the
 * not found page (docs/error-handling.md).
 * Nothing is repeated automatically: repeating is the user's choice.
 */
export function createQueryClient(): QueryClient {
    const client: QueryClient = new QueryClient({
        queryCache: new QueryCache({
            onError: (error, query) => {
                // Reading shows no business errors inline: one that reaches a read is reported like a breakage.
                const systemError = error instanceof BusinessError
                    ? new SystemError('server', `Business error of a read: ${error.code}`, {cause: error})
                    : systemErrorOf(error);
                if (systemError !== null) {
                    reportSystemError({
                        error: systemError,
                        retry: () => {
                            void client.refetchQueries({queryKey: query.queryKey, exact: true});
                        },
                    });
                }
            },
        }),
        mutationCache: new MutationCache({
            onError: (error, variables, _result, mutation) => {
                const systemError = systemErrorOf(error);
                if (systemError !== null) {
                    reportSystemError({
                        error: systemError,
                        retry: () => {
                            mutation.execute(variables).catch(() => undefined);
                        },
                    });
                }
            },
        }),
        defaultOptions: {
            // A failed query is called again by its route's preload, not by every block that mounts.
            queries: {retry: false, retryOnMount: false, refetchOnWindowFocus: false, staleTime: FRESH_MS, throwOnError: isNotFound},
            mutations: {retry: false, throwOnError: isNotFound},
        },
    });
    return client;
}
