import {type SoftwareDetails, softwarePath} from '@/entities/software';
import {toCategoryId} from '@/entities/categories';
import type {Crumb} from '@/shared/ui/breadcrumbs';
import {catalogHrefOf} from './catalog-filter';

/**
 * The way down to software: the catalog, the branch of its first category from the top, each step leading to its
 * catalog, and the software itself (docs/features/software-view.md).
 */
export function softwareBreadcrumbs(software: Pick<SoftwareDetails, 'id' | 'title' | 'categories'>, catalog: string): readonly Crumb[] {
    const branch = software.categories.at(0)?.branch ?? [];
    return [
        {label: catalog, href: catalogHrefOf({})},
        ...branch.map((step) => ({label: step.title, href: catalogHrefOf({category: toCategoryId(step.id)})})),
        {label: software.title, href: softwarePath(software.id)},
    ];
}
