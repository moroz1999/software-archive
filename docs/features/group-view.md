# Group View

The page of one group at `/groups/{id}` ([domain/authors.md](../domain/authors.md#group-page)); every entry of the group catalog leads here, and so do the groups on an author's page and on another group's page.

## Page

- The group's name is the page heading, with "Edit" next to it leading to the editing ([group-editing.md](group-editing.md)).
- Under it, each when known: Types, Labels (each leading to its page, [label-view.md](label-view.md)), Locations ("City, Country", the primary one first), Members, Member groups, Member of, Notes line by line.
- A member is the label they were a member under, leading to its own page ([label-view.md](label-view.md)), with the person's main name muted next to it, leading to the person, when it is another one; a person recorded by themselves leads to the person. Their role chips follow; an unknown role is not shown, it tells the reader nothing.
- Member groups and Member of lead to the pages of those groups.
- Software, below the details: the software the group is credited on, as the cards of the software list ([software-list.md](software-list.md#card)), by release date, then title; under who made it and the year, each of its credits on it — "as {label}" muted when it was under a label, then the role chips, an unknown role not shown. Left out when there is none.
- Last: who added the group and on which day ("Added by Zork on 4 March 2026"), only the day once their account is gone ([domain/users.md](../domain/users.md#who-added-an-entry)).

## Loading and errors

- Software is asked for once its block comes into view, with skeleton cards until it arrives; a system error goes to the error dialog and leaves the block out.
- The page preloads the group's details ([data-loading.md](../data-loading.md)): within the window it appears filled, otherwise with a skeleton.
- An unknown group, and an id that cannot be one, show the not found page; a malformed id is never sent. System errors go to the error dialog ([error-handling.md](../error-handling.md)).

## REST

`GET /api/groups/{id}` — `{id, name, notes, types: [code], labels: [{id, name}], locations: [{country, city, primary}], members: [{id, type, name, personId, personName}], memberGroups: [{id, name}], groups: [{id, name}]}`; `name` and the names of related groups are the groups' names; a member's `id` is the person, or the label (`type` `author-label`) they were a member under; `personId` is `null` for a label of nobody, `personName` `null` when it is the member's label; `added: {by, at}` as for authors ([author-view.md](author-view.md#rest)); `404 group.not-found`, `400` for a malformed id.

`GET /api/groups/{id}/software` — `[{id, title, releaseDate, screenshots, madeBy, credits: [{as, roles}]}]`, the card fields of the software list; `as` is `null` for a credit by the group itself; an unknown group gets `[]`.
