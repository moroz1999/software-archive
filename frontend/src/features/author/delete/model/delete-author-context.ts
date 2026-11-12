import type {AuthorId} from '@/entities/authors';
import {createRequiredContext} from '@/shared/lib/context';

/** The author to delete, under the name they are shown by. */
export interface DeletedAuthor {
    readonly id: AuthorId;
    readonly name: string;
}

export interface DeleteAuthorState {
    readonly author: DeletedAuthor;
    readonly remove: () => void;
    readonly deleting: boolean;
    readonly deleted: boolean;
    /** Why the author could not be deleted. */
    readonly error: string | undefined;
}

/** The deletion, for the controls inside a DeleteAuthorProvider. */
export const [DeleteAuthorContext, useDeleteAuthor] = createRequiredContext<DeleteAuthorState>('DeleteAuthor');
