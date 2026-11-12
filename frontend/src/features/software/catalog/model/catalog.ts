import type {CountryId} from '@/entities/locations';
import type {Software} from '@/entities/software';
import type {Page} from '@/shared/api';

/** A value of a filter with the count of software it leaves. */
export interface FacetCount<T> {
    readonly value: T;
    readonly count: number;
}

/**
 * The values each filter of the catalog offers, counted over the whole result; a chosen filter's values are counted
 * without its own condition (docs/domain/search.md#software-catalog). The category is no filter but where the catalog
 * is: the whole tree is offered, uncounted.
 */
export interface SoftwareCatalogFacets {
    readonly years: readonly FacetCount<number>[];
    readonly countries: readonly FacetCount<CountryId>[];
}

/** A page of the catalog with the values of its filters. */
export interface SoftwareCatalogPage {
    readonly page: Page<Software>;
    readonly facets: SoftwareCatalogFacets;
}
