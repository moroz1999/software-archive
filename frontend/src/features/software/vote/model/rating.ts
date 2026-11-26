/** The rating of software, the number of votes behind it and the stars the reader gave it, if any (docs/domain/voting.md). */
export interface SoftwareRating {
    readonly rating: number;
    readonly votesAmount: number;
    readonly myVote: number | null;
}

/** A vote of the history of software: who gave how many stars, and when. */
export interface SoftwareVote {
    readonly voter: string;
    readonly value: number;
    /** An ISO moment. */
    readonly castAt: string;
}
