# Voting

Signed-in users rate software, as on ZX-Art: the archive shows how well a work is thought of by those who know it.

## Votes
- A vote is one to five stars. Every user has one vote on a piece of software; voting again replaces it.
- A user may withdraw their vote. A withdrawn vote is kept as withdrawn: it counts for nothing and is not shown among the votes.
- Public visitors see the rating and the votes but do not vote.
- Deleting software deletes its votes.

## Rating
The rating of software is not the plain average of its votes, so a work with three enthusiastic votes does not outrank one with two hundred.

1. **Extremes are dropped.** A vote farther than 1.96 standard deviations (of the sample, divided by n − 1) from the mean of the votes counts for nothing. A single vote, and votes that are all equal, are never dropped.
   - Nine fives and a one: the mean is 4.6, the deviation 1.26, the one lies 2.85 deviations away and is dropped.
   - Four fives and a one: the mean is 4.2, the deviation 1.79, the one lies 1.79 deviations away and counts.
2. **The average is weighted.** Ten votes of the archive's average vote, 3.8, are added to the votes left: `(average × n + 3.8 × 10) / (n + 10)`.
   - Three fives: (15 + 38) / 13 = 4.08.
   - Two hundred votes averaging 4.5: (900 + 38) / 210 = 4.47.
3. The rating is told with the number of votes it stands on, those left after the extremes are dropped. Software nobody voted on has no rating.

## Where it is seen
- The page of software shows the rating as stars, the number of votes behind it on pointing at them; a signed-in user gives their stars there, sees their own vote and withdraws it.
- Under the releases, the votes of the software: who voted, the stars and the day, newest first.
- Every card of software — in the catalog, the search, the latest software, the software of a person, a group or a label — shows the rating and is voted on as the page is, and the catalog can be ordered by rating ([search.md](search.md#software-catalog)).
