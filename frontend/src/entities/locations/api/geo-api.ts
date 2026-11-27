import {api, apiError} from '@/shared/api';
import {type City, type Country} from '../model/geo';

/** The countries to choose from, by name. */
export async function fetchCountries(signal: AbortSignal): Promise<readonly Country[]> {
    const {data, error, response} = await api.GET('/api/countries', {signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** The cities of a country to choose from, by name. */
export async function fetchCities(country: string, signal: AbortSignal): Promise<readonly City[]> {
    const {data, error, response} = await api.GET('/api/countries/{id}/cities', {params: {path: {id: country}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}
