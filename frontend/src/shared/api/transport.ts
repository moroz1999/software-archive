import {AuthenticationRequiredError, BusinessError, type BusinessStatus, type FieldError, NotFoundError, SystemError} from './errors';
import {contentLanguage} from './content-language';

type JsonObject = Readonly<Record<string, unknown>>;

/** The origin the API is called on: the page's own; Vite proxies `/api` in development (docs/local-dev.md). */
export const API_ORIGIN = globalThis.location.origin;

/** A problem body is read as it comes: its `code` and `errors` are extensions the OpenAPI document does not describe. */
function isObject(value: unknown): value is JsonObject {
    return typeof value === 'object' && value !== null && !Array.isArray(value);
}

const BUSINESS_STATUSES: readonly number[] = [403, 409, 422] satisfies BusinessStatus[];

function isBusinessStatus(status: number): status is BusinessStatus {
    return BUSINESS_STATUSES.includes(status);
}

function readFieldErrors(problem: JsonObject): FieldError[] {
    const errors = problem['errors'];
    if (!Array.isArray(errors)) {
        return [];
    }
    return errors.flatMap((entry: unknown) => {
        if (isObject(entry) && typeof entry['field'] === 'string' && typeof entry['code'] === 'string') {
            return [{field: entry['field'], code: entry['code']}];
        }
        return [];
    });
}

/**
 * The error of a failed answer, with the problem body `openapi-fetch` read from it: NotFoundError, BusinessError (AuthenticationRequiredError for a
 * `401`) or SystemError (docs/error-handling.md).
 */
export function apiError(response: Response, problem: unknown): Error {
    const {status} = response;
    if (status === 401 || status === 404 || isBusinessStatus(status)) {
        if (isObject(problem) && typeof problem['code'] === 'string') {
            if (status === 401) {
                return new AuthenticationRequiredError(problem['code']);
            }
            return status === 404
                ? new NotFoundError(problem['code'])
                : new BusinessError(status, problem['code'], readFieldErrors(problem));
        }
        if (status === 401) {
            return new AuthenticationRequiredError('auth.required');
        }
    }
    return new SystemError('server', `HTTP ${String(status)}`);
}

/**
 * Fetches, asking for the archive content in the content language; a fetch that cannot reach the server fails as a
 * network error, an aborted one with its AbortError.
 */
export async function fetchOrFail(request: Request): Promise<Response> {
    const headers = new Headers(request.headers);
    headers.set('Accept-Language', contentLanguage());
    try {
        return await fetch(new Request(request, {headers}));
    } catch (error) {
        if (error instanceof DOMException && error.name === 'AbortError') {
            throw error;
        }
        throw new SystemError('network', 'Network failure', {cause: error});
    }
}
