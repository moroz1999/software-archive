import {NotFoundError} from '@/shared/api';
import {type Brand} from '@/shared/lib/brand';
import {isUuid} from '@/shared/lib/uuid';

/** The id of a category of software. */
export type CategoryId = Brand<string, 'CategoryId'>;

/** A category of the tree with its subcategories, in their order. */
export interface CategoryNode {
    readonly id: CategoryId;
    readonly title: string;
    readonly children: readonly CategoryNode[];
}

/** One category as its form shows it; no parent at the top of the tree. */
export interface CategoryDetails {
    readonly id: CategoryId;
    readonly title: string;
    readonly parentId: CategoryId | null;
}

export function toCategoryId(raw: string): CategoryId {
    if (!isUuid(raw)) {
        throw new TypeError(`Invalid category id: ${raw}`);
    }
    return raw as CategoryId;
}

/** The id in a raw value from outside (a URL), or null when it cannot be one. */
export function parseCategoryId(raw: string | undefined): CategoryId | null {
    return raw !== undefined && isUuid(raw) ? toCategoryId(raw) : null;
}

/** The id of a page's category from its URL; one that cannot be an id means the page does not exist. */
export function requireCategoryId(raw: string | undefined): CategoryId {
    const id = parseCategoryId(raw);
    if (id === null) {
        throw new NotFoundError('category.not-found');
    }
    return id;
}
