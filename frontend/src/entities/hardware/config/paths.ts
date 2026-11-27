
/** Where the hardware reference book is kept in the app. */
export const HARDWARE_PATH = '/management/hardware';
export const HARDWARE_NEW_PATH = '/management/hardware/new';
export const HARDWARE_GROUP_NEW_PATH = '/management/hardware/groups/new';
/** The route patterns of the edit pages; their parameter is `code`. */
export const HARDWARE_EDIT_ROUTE = '/management/hardware/:code/edit';
export const HARDWARE_GROUP_EDIT_ROUTE = '/management/hardware/groups/:code/edit';

export function hardwareEditPath(code: string): string {
    return `/management/hardware/${code}/edit`;
}

export function hardwareGroupEditPath(code: string): string {
    return `/management/hardware/groups/${code}/edit`;
}
