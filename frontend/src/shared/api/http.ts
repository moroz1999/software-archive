import {refreshSession, SESSION_CALLS, sessionSettled, withAccessToken} from './session';
import {fetchOrFail, readJson, toError} from './transport';

/**
 * Sends a request, once it is known who is signed in. A request refused for want of a signed-in user refreshes the session once and is repeated; when
 * that does not help, it fails with AuthenticationRequiredError.
 */
async function send(path: string, init: RequestInit): Promise<Response> {
    await sessionSettled();
    let response = await fetchOrFail(path, withAccessToken(init));
    if (response.status === 401 && !SESSION_CALLS.includes(path)) {
        const refused = await toError(response);
        if (!await refreshSession()) {
            throw refused;
        }
        response = await fetchOrFail(path, withAccessToken(init));
    }
    if (!response.ok) {
        throw await toError(response);
    }
    return response;
}

function withJson(method: 'POST' | 'PUT', body: unknown): RequestInit {
    return {method, headers: {Accept: 'application/json', 'Content-Type': 'application/json'}, body: JSON.stringify(body)};
}

/**
 * GETs JSON from the backend. Like every call here, fails with NotFoundError, BusinessError, AuthenticationRequiredError
 * or SystemError only (docs/error-handling.md); an aborted request rejects with the original AbortError.
 */
export async function getJson(path: string, signal?: AbortSignal): Promise<unknown> {
    return readJson(await send(path, {headers: {Accept: 'application/json'}, ...(signal ? {signal} : {})}));
}

/** POSTs a JSON body and returns the JSON answer. */
export async function postJson(path: string, body: unknown): Promise<unknown> {
    return readJson(await send(path, withJson('POST', body)));
}

/** POSTs a JSON body, or none; the backend answers with no content. */
export async function postNoContent(path: string, body?: unknown): Promise<void> {
    await send(path, body === undefined ? {method: 'POST', headers: {Accept: 'application/json'}} : withJson('POST', body));
}

/** PUTs a JSON body; the backend answers with no content. */
export async function putJson(path: string, body: unknown): Promise<void> {
    await send(path, withJson('PUT', body));
}

/** DELETEs a resource; the backend answers with no content. */
export async function deleteResource(path: string): Promise<void> {
    await send(path, {method: 'DELETE', headers: {Accept: 'application/json'}});
}

/** POSTs a multipart form, such as files, and returns the JSON answer. */
export async function postForm(path: string, form: FormData): Promise<unknown> {
    return readJson(await send(path, {method: 'POST', headers: {Accept: 'application/json'}, body: form}));
}

/** PUTs a multipart form and expects no content. */
export async function putForm(path: string, form: FormData): Promise<void> {
    await send(path, {method: 'PUT', headers: {Accept: 'application/json'}, body: form});
}
