const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

/** Whether a raw value is a UUID, the form of every id in the archive. */
export function isUuid(raw: string): boolean {
    return UUID.test(raw);
}
