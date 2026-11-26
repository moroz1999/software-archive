import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import type {SoftwareId} from '@/entities/software';
import {softwareRatingQuery} from '../api/vote-queries';
import {SoftwareRatingContext, type SoftwareRatingState} from './software-rating-context';
import {useCastVote} from './use-cast-vote';

export interface SoftwareRatingProviderProps {
    readonly id: SoftwareId;
    readonly children: ReactNode;
}

/** The rating of one software and the reader's vote on it, given and withdrawn (useCastVote). */
export function SoftwareRatingProvider({id, children}: SoftwareRatingProviderProps) {
    const query = useQuery(softwareRatingQuery(id));
    const state: SoftwareRatingState = {query, ...useCastVote(id)};
    return <SoftwareRatingContext value={state}>{children}</SoftwareRatingContext>;
}
