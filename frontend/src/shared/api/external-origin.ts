/**
 * An import an entry came from (docs/domain/imports.md#origin): the source archive, the id the entry has there — or
 * the name it is known by there, when the source gives it no id — and its page there, null when it has none.
 */
export interface ExternalOrigin {
    readonly origin: string;
    readonly externalId: string;
    readonly url: string | null;
}
