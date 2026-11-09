import {BusinessError, type BusinessStatus, type FieldError, NotFoundError, SystemError} from './errors';
import {isObject, type JsonObject} from './parse';

const BUSINESS_STATUSES: readonly number[] = [409, 422] satisfies BusinessStatus[];

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

async function toError(response: Response): Promise<Error> {
    const {status} = response;
    if (status === 404 || isBusinessStatus(status)) {
        const problem = await readBody(response);
        if (isObject(problem) && typeof problem['code'] === 'string') {
            return status === 404
                ? new NotFoundError(problem['code'])
                : new BusinessError(status, problem['code'], readFieldErrors(problem));
        }
    }
    return new SystemError('server', `HTTP ${String(status)}`);
}

async function send(path: string, init: RequestInit): Promise<Response> {
    let response: Response;
    try {
        response = await fetch(path, init);
    } catch (error) {
        if (error instanceof DOMException && error.name === 'AbortError') {
            throw error;
        }
        throw new SystemError('network', 'Network failure', {cause: error});
    }
    if (!response.ok) {
        throw await toError(response);
    }
    return response;
}

async function readJson(response: Response): Promise<unknown> {
    try {
        return await response.json();
    } catch (error) {
        throw new SystemError('response', 'Unreadable response body', {cause: error});
    }
}

function withJson(method: 'POST' | 'PUT', body: unknown): RequestInit {
    return {method, headers: {Accept: 'application/json', 'Content-Type': 'application/json'}, body: JSON.stringify(body)};
}

/**
 * GETs JSON from the backend. Like every call here, fails with NotFoundError, BusinessError or SystemError only
 * (docs/error-handling.md); an aborted request rejects with the original AbortError.
 */
export async function getJson(path: string, signal?: AbortSignal): Promise<unknown> {
    return readJson(await send(path, {headers: {Accept: 'application/json'}, ...(signal ? {signal} : {})}));
}

/** POSTs a JSON body and returns the JSON answer. */
export async function postJson(path: string, body: unknown): Promise<unknown> {
    return readJson(await send(path, withJson('POST', body)));
}

/** PUTs a JSON body; the backend answers with no content. */
export async function putJson(path: string, body: unknown): Promise<void> {
    await send(path, withJson('PUT', body));
}

/** DELETEs a resource; the backend answers with no content. */
export async function deleteResource(path: string): Promise<void> {
    await send(path, {method: 'DELETE', headers: {Accept: 'application/json'}});
}
