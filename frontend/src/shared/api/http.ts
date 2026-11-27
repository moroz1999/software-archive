import createClient from 'openapi-fetch';
import type {paths} from './schema';
import {refreshSession, SESSION_CALLS, sessionSettled, withAccessToken} from './session';
import {API_ORIGIN, fetchOrFail} from './transport';

/**
 * Sends a request, once it is known who is signed in. A request refused for want of a signed-in user refreshes the session once and is repeated with
 * the new access token; when that does not help, the refusal is the answer. A failed fetch rejects as a network error, an aborted one with its
 * AbortError.
 */
async function send(request: Request): Promise<Response> {
    await sessionSettled();
    const repeat = request.clone();
    let response = await fetchOrFail(withAccessToken(request));
    if (response.status === 401 && !SESSION_CALLS.includes(new URL(request.url).pathname) && await refreshSession()) {
        response = await fetchOrFail(withAccessToken(repeat));
    }
    return response;
}

/**
 * The client of the archive's REST API, typed by its OpenAPI document (`schema.ts`, generated from `openapi.json`). A failed answer comes back as
 * `error`, which the caller throws as `apiError(response, error)`.
 */
export const api = createClient<paths>({baseUrl: API_ORIGIN, fetch: send});
