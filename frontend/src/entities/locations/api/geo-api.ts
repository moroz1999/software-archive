import {getJson, mapResponse, readArray, readObject, readString} from '@/shared/api';
import {type City, type Country, type CountryId, toCityId, toCountryId} from '../model/geo';

/** The countries to choose from, by name. */
export async function fetchCountries(signal: AbortSignal): Promise<readonly Country[]> {
    const json = await getJson('/api/countries', signal);
    return mapResponse(() => readArray(json, 'countries').map((item) => {
        const dto = readObject(item, 'country');
        return {id: toCountryId(readString(dto, 'id')), name: readString(dto, 'name')};
    }));
}

/** The cities of a country to choose from, by name. */
export async function fetchCities(country: CountryId, signal: AbortSignal): Promise<readonly City[]> {
    const json = await getJson(`/api/countries/${country}/cities`, signal);
    return mapResponse(() => readArray(json, 'cities').map((item) => {
        const dto = readObject(item, 'city');
        return {id: toCityId(readString(dto, 'id')), name: readString(dto, 'name')};
    }));
}

