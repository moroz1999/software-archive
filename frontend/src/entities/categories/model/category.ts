import {type components, NotFoundError} from '@/shared/api';
import type {UiLanguage} from '@/shared/lib/i18n';
import {isUuid} from '@/shared/lib/uuid';

/** A category of the tree with its subcategories, in their order. */
export type CategoryNode = components['schemas']['CategoryNodeDto'];

/** The titles of a category by language: the Russian one is always there (docs/domain/categories.md). */
export type CategoryTitles = Readonly<Partial<Record<UiLanguage, string>> & {readonly ru: string}>;

/** One category as its form shows it, with its titles in every language; no parent at the top of the tree. */
export interface CategoryDetails {
    readonly id: string;
    readonly titles: CategoryTitles;
    readonly parentId: string | null;
}

/** The title of a category in the language, else the Russian one. */
export function categoryTitle(titles: CategoryTitles, language: UiLanguage): string {
    return titles[language] ?? titles.ru;
}

/** The id in a raw value from outside (a URL), or null when it cannot be one. */
export function parseCategoryId(raw: string | undefined): string | null {
    return raw !== undefined && isUuid(raw) ? raw : null;
}

/** The id of a page's category from its URL; one that cannot be an id means the page does not exist. */
export function requireCategoryId(raw: string | undefined): string {
    const id = parseCategoryId(raw);
    if (id === null) {
        throw new NotFoundError('category.not-found');
    }
    return id;
}
