/** The page a message is written on: its path and title, and the entry it shows, if any. */
export interface PageContext {
    readonly path: string;
    readonly title: string;
    readonly entityType: string | null;
    readonly entityId: string | null;
}

const UUID = '[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}';

/** The pages that show an entry, by their path; the entry's id is the last group. */
const ENTRY_PAGES: readonly (readonly [RegExp, string])[] = [
    [new RegExp(`^/software/${UUID}/releases/(${UUID})(?:/edit)?$`, 'u'), 'release'],
    [new RegExp(`^/software/(${UUID})(?:/edit)?$`, 'u'), 'software'],
    [new RegExp(`^/authors/(${UUID})(?:/edit)?$`, 'u'), 'author'],
    [new RegExp(`^/groups/(${UUID})(?:/edit)?$`, 'u'), 'group'],
    [new RegExp(`^/labels/(${UUID})(?:/edit)?$`, 'u'), 'label'],
    [new RegExp(`^/management/geography/countries/(${UUID})/edit$`, 'u'), 'country'],
    [new RegExp(`^/management/geography/cities/(${UUID})/edit$`, 'u'), 'city'],
    [/^\/management\/hardware\/(?!groups\/)([a-z0-9-]+)\/edit$/u, 'hardware'],
];

/**
 * The page at the path, with its query — the filters and page of a list, which the assistant reads as the editor sees
 * them — and the entry it shows when it is the page of one.
 */
export function pageContextOf(path: string, title: string): PageContext {
    const [pathname = path] = path.split('?', 1);
    for (const [pattern, entityType] of ENTRY_PAGES) {
        const entityId = pattern.exec(pathname)?.[1];
        if (entityId !== undefined) {
            return {path, title, entityType, entityId};
        }
    }
    return {path, title, entityType: null, entityId: null};
}
