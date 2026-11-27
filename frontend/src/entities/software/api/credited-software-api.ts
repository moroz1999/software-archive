import {api, apiError} from '@/shared/api';
import type {CreditedRelease} from '../model/credited-release';
import type {CreditedOwner, CreditedSoftware} from '../model/credited-software';

/** The software a person, a group or a label is credited on: `GET /api/{authors|groups|labels}/{id}/software`. */
export async function fetchCreditedSoftware(owner: CreditedOwner, signal: AbortSignal): Promise<readonly CreditedSoftware[]> {
    const {data, error, response} = await api.GET(`/api/${owner.kind}/{id}/software`, {params: {path: {id: owner.id}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** The releases a person, a group or a label is credited on: `GET /api/{authors|groups|labels}/{id}/releases`. */
export async function fetchCreditedReleases(owner: CreditedOwner, signal: AbortSignal): Promise<readonly CreditedRelease[]> {
    const {data, error, response} = await api.GET(`/api/${owner.kind}/{id}/releases`, {params: {path: {id: owner.id}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}
