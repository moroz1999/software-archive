declare const brand: unique symbol;

/** A scalar branded with the name of a value object: `Brand<string, 'SoftwareId'>` (docs/value-objects.md). */
export type Brand<T, Name extends string> = T & {readonly [brand]: Name};
