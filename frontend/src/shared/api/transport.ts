import {AuthenticationRequiredError, BusinessError, type BusinessStatus, type FieldError, NotFoundError, SystemError} from './errors';
import {contentLanguage} from './content-language';
import {isObject, type JsonObject} from './parse';

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

async function readBody(response: Response): Promise<unknown> {
    try {
        return await response.json();
    } catch {
        return null;
    }
}

/** The error of a failed answer (docs/error-handling.md). */
export async function toError(response: Response): Promise<Error> {
    const {status} = response;
    if (status === 401 || status === 404 || isBusinessStatus(status)) {
        const problem = await readBody(response);
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

function withContentLanguage(headers: HeadersInit | undefined): HeadersInit {
    if (headers instanceof Headers || Array.isArray(headers)) {
        const all = new Headers(headers);
        all.set('Accept-Language', contentLanguage());
        return all;
    }
    return {...headers, 'Accept-Language': contentLanguage()};
}

/**
 * Fetches, asking for the archive content in the content language; a fetch that cannot reach the server fails as a
 * network error, an aborted one with its AbortError.
 */
export async function fetchOrFail(path: string, init: RequestInit): Promise<Response> {
    try {
        return await fetch(path, {...init, headers: withContentLanguage(init.headers)});
    } catch (error) {
        if (error instanceof DOMException && error.name === 'AbortError') {
            throw error;
        }
        throw new SystemError('network', 'Network failure', {cause: error});
    }
}

export async function readJson(response: Response): Promise<unknown> {
    try {
        return await response.json();
    } catch (error) {
        throw new SystemError('response', 'Unreadable response body', {cause: error});
    }
}
