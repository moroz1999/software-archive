import type {SoftwareId} from '@/entities/software';
import {getJson, type JsonObject, mapResponse, putJsonForAnswer, readArray, readNumber, readObject, readString} from '@/shared/api';
import type {SoftwareRating, SoftwareVote} from '../model/rating';

function parseRating(json: unknown): SoftwareRating {
    const dto: JsonObject = readObject(json, 'rating');
    const myVote = dto['myVote'];
    return {
        rating: readNumber(dto, 'rating'),
        votesAmount: readNumber(dto, 'votesAmount'),
        myVote: myVote === null || myVote === undefined ? null : readNumber(dto, 'myVote'),
    };
}

/** The rating of software and the reader's vote: `GET /api/software/{id}/rating`. */
export async function fetchSoftwareRating(id: SoftwareId, signal: AbortSignal): Promise<SoftwareRating> {
    const json = await getJson(`/api/software/${id}/rating`, signal);
    return mapResponse(() => parseRating(json));
}

/** Gives software the stars, or withdraws the reader's vote with 0, and answers with the rating they make. */
export async function castVote(id: SoftwareId, value: number): Promise<SoftwareRating> {
    const json = await putJsonForAnswer(`/api/software/${id}/vote`, {value});
    return mapResponse(() => parseRating(json));
}

/** The votes of software, newest first: `GET /api/software/{id}/votes`. */
export async function fetchSoftwareVotes(id: SoftwareId, signal: AbortSignal): Promise<readonly SoftwareVote[]> {
    const json = await getJson(`/api/software/${id}/votes`, signal);
    return mapResponse(() => readArray(json, 'votes').map((item) => {
        const dto = readObject(item, 'vote');
        return {voter: readString(dto, 'voter'), value: readNumber(dto, 'value'), castAt: readString(dto, 'castAt')};
    }));
}
