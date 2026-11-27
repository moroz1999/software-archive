import {api, apiError} from '@/shared/api';
import type {SoftwareRating, SoftwareVote} from '../model/rating';

/** The rating of software and the reader's vote: `GET /api/software/{id}/rating`. */
export async function fetchSoftwareRating(id: string, signal: AbortSignal): Promise<SoftwareRating> {
    const {data, error, response} = await api.GET('/api/software/{id}/rating', {params: {path: {id}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** Gives software the stars, or withdraws the reader's vote with 0, and answers with the rating they make. */
export async function castVote(id: string, value: number): Promise<SoftwareRating> {
    const {data, error, response} = await api.PUT('/api/software/{id}/vote', {params: {path: {id}}, body: {value}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** The votes of software, newest first: `GET /api/software/{id}/votes`. */
export async function fetchSoftwareVotes(id: string, signal: AbortSignal): Promise<readonly SoftwareVote[]> {
    const {data, error, response} = await api.GET('/api/software/{id}/votes', {params: {path: {id}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}
