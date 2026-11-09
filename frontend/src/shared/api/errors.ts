/** Statuses that carry a business error when the body has a problem `code` (docs/error-handling.md). */
export type BusinessStatus = 409 | 422;

export interface FieldError {
    readonly field: string;
    readonly code: string;
}

/** The domain refused the request: shown inline by the slice that made it. */
export class BusinessError extends Error {
    override readonly name = 'BusinessError';
    readonly status: BusinessStatus;
    readonly code: string;
    readonly fieldErrors: readonly FieldError[];

    constructor(status: BusinessStatus, code: string, fieldErrors: readonly FieldError[]) {
        super(`${String(status)} ${code}`);
        this.status = status;
        this.code = code;
        this.fieldErrors = fieldErrors;
    }
}

/**
 * The entity asked for does not exist (a `404` with a problem `code`): neither a business rule nor a breakage.
 * Never handled by slices: queries and mutations throw it to the route's error boundary, which shows the
 * not found page.
 */
export class NotFoundError extends Error {
    override readonly name = 'NotFoundError';
    readonly code: string;

    constructor(code: string) {
        super(code);
        this.code = code;
    }
}

/** What broke: the network, the server, or the shape of a response. */
export type SystemErrorKind = 'network' | 'server' | 'response';

/** Something is broken: never handled by slices, shown in the system error dialog. */
export class SystemError extends Error {
    override readonly name = 'SystemError';
    readonly kind: SystemErrorKind;

    constructor(kind: SystemErrorKind, message: string, options?: ErrorOptions) {
        super(message, options);
        this.kind = kind;
    }
}

/** The business error of a query or a mutation, shown inline by the slice; undefined for none or a system error. */
export function businessErrorOf(error: unknown): BusinessError | undefined {
    return error instanceof BusinessError ? error : undefined;
}
