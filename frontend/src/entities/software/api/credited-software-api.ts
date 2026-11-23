import {getJson, mapResponse, readArray, readNullableString, readObject, readString, readStringArray} from '@/shared/api';
import type {CreditedRelease} from '../model/credited-release';
import type {CreditedOwner, CreditedSoftware, OwnCredit} from '../model/credited-software';
import {toReleaseId, toReleaseType} from '../model/release';
import {toReleaseDate, toSoftwareId} from '../model/software';
import {parseSoftware} from './fetch-software-page';
import {parseScreenshots} from './screenshot-api';

function parseOwnCredits(raw: unknown): readonly OwnCredit[] {
    return readArray(raw, 'credits').map((item) => {
        const credit = readObject(item, 'credit');
        return {as: readNullableString(credit, 'as'), roles: readStringArray(credit['roles'], 'roles')};
    });
}

/** The software a person or a group is credited on: `GET /api/authors/{id}/software`, `GET /api/groups/{id}/software`. */
export async function fetchCreditedSoftware(owner: CreditedOwner, signal: AbortSignal): Promise<readonly CreditedSoftware[]> {
    const json = await getJson(`/api/${owner.kind}/${owner.id}/software`, signal);
    return mapResponse(() => readArray(json, 'credited software').map((item) => ({
        ...parseSoftware(item),
        credits: parseOwnCredits(readObject(item, 'credited software')['credits']),
    })));
}

/** The releases a person, a group or a label is credited on: `GET /api/{authors|groups|labels}/{id}/releases`. */
export async function fetchCreditedReleases(owner: CreditedOwner, signal: AbortSignal): Promise<readonly CreditedRelease[]> {
    const json = await getJson(`/api/${owner.kind}/${owner.id}/releases`, signal);
    return mapResponse(() => readArray(json, 'credited releases').map((item) => {
        const dto = readObject(item, 'credited release');
        const releaseDate = readNullableString(dto, 'releaseDate');
        return {
            id: toReleaseId(readString(dto, 'id')),
            softwareId: toSoftwareId(readString(dto, 'softwareId')),
            softwareTitle: readString(dto, 'softwareTitle'),
            title: readString(dto, 'title'),
            version: readNullableString(dto, 'version'),
            type: toReleaseType(readString(dto, 'type')),
            releaseDate: releaseDate === null ? null : toReleaseDate(releaseDate),
            screenshots: parseScreenshots(dto['screenshots']),
            credits: parseOwnCredits(dto['credits']),
        };
    }));
}
