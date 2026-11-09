import type {BusinessError, BusinessStatus} from './errors';

/** A slice's texts for the codes of the business errors it shows (its `config/messages.ts`). */
export type BusinessMessages = Readonly<Partial<Record<string, string>>>;

const FALLBACK: Readonly<Record<BusinessStatus, string>> = {
    409: 'This item was changed in the meantime. Reload and try again.',
    422: 'The request was refused.',
};

/**
 * The user-facing text of a business error: the slice's own text for its `code`,
 * or a generic one for the error's status (docs/error-handling.md).
 */
export function businessErrorMessage(error: BusinessError, messages: BusinessMessages = {}): string {
    return messages[error.code] ?? FALLBACK[error.status];
}
