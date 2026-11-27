import type {components} from '@/shared/api';

/**
 * A page of the catalog with the values each filter offers, counted over the whole result; a chosen filter's values are
 * counted without its own condition (docs/domain/search.md#software-catalog). The category is no filter but where the
 * catalog is: the whole tree is offered, uncounted. The hardware is named by the archive, in the order of its groups;
 * a chosen item no software has any more stays with none.
 */
export type SoftwareCatalogPage = components['schemas']['SoftwareCatalogDto'];
export type SoftwareCatalogFacets = components['schemas']['SoftwareCatalogFacetsDto'];
/** A value of a filter with the count of software it leaves. */
export type FacetCount = components['schemas']['FacetValueDtoString'];
export type HardwareFacetCount = components['schemas']['HardwareFacetValueDto'];
