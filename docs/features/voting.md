# Voting — implementation

Domain rules: [../domain/voting.md](../domain/voting.md).

## Backend

- The `vote` module. The aggregate `SoftwareVotes` holds every vote of one software and its rating; `RatingRule` makes the rating, its average vote from `voting.average-vote` (3.8). The aggregate is stored in `software_vote` (a withdrawn vote keeps the value 0) and `software_rating` (no row for software nobody voted on); loading it locks the rating row, so votes on one software are counted one after another.
- A vote tells `SoftwareRated`: the search reindexes the software, whose document holds `rating` and `votes_amount` for the card and the order of the catalog ([search.md](search.md#collections)).
- A voter's account cannot be deleted while their votes stand: votes belong to confirmed accounts, which the archive does not delete.

## REST

| Call | Answer | Errors |
|---|---|---|
| `PUT /api/software/{id}/vote` `{value}` | `{rating, votesAmount, myVote}` after the vote; `value` 1–5, or 0 to withdraw; `myVote` is `null` once withdrawn | `401 auth.required`; `404 software.not-found`; `400 vote.value-invalid` for another value or none |
| `GET /api/software/{id}/rating` | `{rating, votesAmount, myVote}`, `myVote` of the reader, `null` for a public visitor or without a vote | `404 software.not-found` |
| `GET /api/my-votes?software={ids}` | `[{software, value}]`, the reader's standing votes on the software asked (ids joined by commas), for the cards of a list; `[]` for a public visitor; software not voted on, or unknown, left out | `400 vote.too-much-software` for more than 100 software |
| `GET /api/software/{id}/votes` | `[{voter, value, castAt}]`, the user name, newest first, without withdrawn votes | `404 software.not-found` |

## Frontend

- `features/software/vote`: `SoftwareRatingProvider` + `SoftwareRating` (the stars with the number of votes as their tooltip, the reader's vote as a number and a cross withdrawing it; a refusal shown under them), `SoftwareVotesProvider` + `SoftwareVotes` (the lazy block of the votes). A vote puts the answer into the rating query and invalidates the votes, which load again.
- The software page ([software-view.md](software-view.md)) preloads the rating with the details.
- Every card of software holds `SoftwareCardVote`, the same `VoteStars` as the page, of the same size, in the card's footer outside its link ([software-list.md](software-list.md#card)). The rating comes with the list: the catalog and the full search from the search index, the latest software and the software of a person, a group or a label from `rating` and `votesAmount` of their lists. The reader's own votes are asked for by the cards of a list together, in one `GET /api/my-votes` gathered over one tick, and only for a signed-in reader. The answer to a vote stands on its card until the list brings another rating, since the search index takes the vote a moment later.
- Pages and widgets supply the same `SoftwareCard` to every software list. It includes voting in its footer. List components require the whole card so sibling features can share it through their parent.
