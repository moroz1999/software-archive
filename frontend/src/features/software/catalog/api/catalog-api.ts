import {api, apiError} from '@/shared/api';
import type {SoftwareCatalogPage} from '../model/catalog';
import type {SoftwareCatalogFilter} from '../model/catalog-filter';

/** A page of the catalog with the values of its filters: `GET /api/software`, of the platform when one is chosen. */
export async function fetchSoftwareCatalog(
    filter: SoftwareCatalogFilter,
    platform: string | null,
    signal: AbortSignal,
): Promise<SoftwareCatalogPage> {
    const {data, error, response} = await api.GET('/api/software', {
        params: {
            query: {
                page: filter.page,
                q: filter.keyword,
                year: filter.year,
                category: filter.category,
                country: filter.country,
                city: filter.city,
                platform,
                // The platforms of the filter are a choice among all of them: with one chosen in the switch they do not count.
                platforms: platform === null && filter.platforms.length > 0 ? filter.platforms : null,
                hardware: filter.hardware.length > 0 ? filter.hardware : null,
                // The order by title is the archive's own: it is not asked for.
                ...(filter.order === 'rating' ? {order: filter.order} : {}),
            },
        },
        signal,
    });
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}
