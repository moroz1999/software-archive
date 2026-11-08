/** Joins CSS class names, skipping empty ones. */
export function classNames(...names: (string | false | null | undefined)[]): string {
    return names.filter((name): name is string => typeof name === 'string' && name !== '').join(' ');
}
