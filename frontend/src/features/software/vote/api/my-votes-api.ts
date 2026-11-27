import {api, apiError} from '@/shared/api';

/** More than the largest page of software any list shows, as the archive allows. */
const MOST_SOFTWARE = 100;

interface Waiting {
    readonly resolve: (stars: number | null) => void;
    readonly reject: (error: unknown) => void;
}

let waiting = new Map<string, Waiting[]>();

/** The reader's votes on the software asked for in one go: `GET /api/my-votes?software={ids}`. */
async function fetchMyVotes(ids: readonly string[]): Promise<ReadonlyMap<string, number>> {
    const {data, error, response} = await api.GET('/api/my-votes', {params: {query: {software: ids}}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return new Map(data.map((vote) => [vote.software, vote.value]));
}

async function flush(): Promise<void> {
    const asked = waiting;
    waiting = new Map();
    const ids = [...asked.keys()];
    for (let start = 0; start < ids.length; start += MOST_SOFTWARE) {
        const chunk = ids.slice(start, start + MOST_SOFTWARE);
        try {
            const votes = await fetchMyVotes(chunk);
            chunk.forEach((id) => {
                asked.get(id)?.forEach(({resolve}) => {
                    resolve(votes.get(id) ?? null);
                });
            });
        } catch (error) {
            chunk.forEach((id) => {
                asked.get(id)?.forEach(({reject}) => {
                    reject(error);
                });
            });
        }
    }
}

/**
 * The stars the reader gave the software, `null` for none. The cards of one list ask at once, so their votes are asked
 * for together, in one request.
 */
export function fetchMyVote(id: string): Promise<number | null> {
    return new Promise((resolve, reject) => {
        if (waiting.size === 0) {
            setTimeout(() => {
                void flush();
            }, 0);
        }
        waiting.set(id, [...(waiting.get(id) ?? []), {resolve, reject}]);
    });
}
