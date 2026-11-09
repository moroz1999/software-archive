import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {authorDetailsQuery} from '../api/author-queries';
import type {AuthorId} from './author';
import {AuthorDetailsContext} from './author-details-context';

export interface AuthorDetailsProviderProps {
    readonly id: AuthorId;
    readonly children: ReactNode;
}

/** Loads one author's details for the blocks inside; takes over the call its route preloaded. */
export function AuthorDetailsProvider({id, children}: AuthorDetailsProviderProps) {
    const query = useQuery(authorDetailsQuery(id));
    return <AuthorDetailsContext value={query}>{children}</AuthorDetailsContext>;
}
