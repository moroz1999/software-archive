import {getJson, mapResponse, readArray, readNullableString, readObject, readStringArray} from '@/shared/api';
import type {CreditedOwner, CreditedSoftware} from '../model/credited-software';
import {parseSoftware} from './fetch-software-page';

/** The software a person or a group is credited on: `GET /api/authors/{id}/software`, `GET /api/groups/{id}/software`. */
export async function fetchCreditedSoftware(owner: CreditedOwner, signal: AbortSignal): Promise<readonly CreditedSoftware[]> {
    const json = await getJson(`/api/${owner.kind}/${owner.id}/software`, signal);
    return mapResponse(() => readArray(json, 'credited software').map((item) => ({
        ...parseSoftware(item),
        credits: readArray(readObject(item, 'credited software')['credits'], 'credits').map((raw) => {
            const credit = readObject(raw, 'credit');
            return {as: readNullableString(credit, 'as'), roles: readStringArray(credit['roles'], 'roles')};
        }),
    })));
}
