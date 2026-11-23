import type {CityId, CountryId} from '@/entities/locations';
import type {PlatformCode} from '@/entities/platforms';
import type {Software} from '@/entities/software';
import type {Page} from '@/shared/api';

/** A value of a filter with the count of software it leaves. */
export interface FacetCount<T> {
    readonly value: T;
    readonly count: number;
}

/**
 * An item of hardware to filter by: its code, its name and its group's in the interface language, with its count; the
 * archive gives them in the order of their groups.
 */
export interface HardwareFacetCount extends FacetCount<string> {
    readonly name: string;
    readonly group: string;
    readonly groupName: string;
}

/**
 * The values each filter of the catalog offers, counted over the whole result; a chosen filter's values are counted
 * without its own condition (docs/domain/search.md#software-catalog). The category is no filter but where the catalog
 * is: the whole tree is offered, uncounted. The hardware is named by the archive; a chosen item no software has any
 * more stays with none.
 */
export interface SoftwareCatalogFacets {
    readonly years: readonly FacetCount<number>[];
    readonly countries: readonly FacetCount<CountryId>[];
    readonly cities: readonly FacetCount<CityId>[];
    readonly platforms: readonly FacetCount<PlatformCode>[];
    readonly hardware: readonly HardwareFacetCount[];
}

/** A page of the catalog with the values of its filters. */
export interface SoftwareCatalogPage {
    readonly page: Page<Software>;
    readonly facets: SoftwareCatalogFacets;
}
