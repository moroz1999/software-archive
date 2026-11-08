/** Statuses that carry a business error when the body has a problem `code` (docs/error-handling.md). */
export type BusinessStatus = 404 | 409 | 422;

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
