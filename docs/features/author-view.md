# Author View

The page of one person at `/authors/{id}` ([domain/authors.md](../domain/authors.md#author-page)); every entry of the catalog leads here.

## Page

- Over the heading, the breadcrumbs: Authors, leading to the catalog, then the main name.
- The main name is the page heading, with the menu right after its text ([PopoverMenu](../design-system/popover-menu.md)) holding "Edit", leading to the editing ([author-editing.md](author-editing.md)).
- Under it, each when known: Real name, Labels (each leading to its page, [label-view.md](label-view.md)), Locations ("City, Country", the primary one first), Groups, Notes line by line.
- A group leads to its page ([group-view.md](group-view.md)), with "as {label}" muted next to it when the person was a member under another label than the main name; a membership under two labels is two lines.
- Software, below the details: the software the person is credited on, as the cards of the software list ([software-list.md](software-list.md#card)), by release date, then title; under who made it and the year, each of their credits on it — "as {label}" muted when it was under another label than the main name, then the role chips, an unknown role not shown. Left out when there is none.
- Releases, below the software: the releases the person is credited on, as cards: the release title leading to its page, its type, "Software: {title}" leading to the software, the date and version, then each credit as on the software cards; by release date, then title. Asked for once it comes into view; left out when there is none.
- Last: who added the person and on which day ("Added by Zork on 4 March 2026"), only the day once their account is gone ([domain/users.md](../domain/users.md#who-added-an-entry)).

## Loading and errors

- Software is asked for once its block comes into view, with skeleton cards until it arrives; a system error goes to the error dialog and leaves the block out.
- The page preloads the author's details ([data-loading.md](../data-loading.md)): within the window it appears filled, otherwise with a skeleton.
- An unknown or nameless person, and an id that cannot be one, show the not found page; a malformed id is never sent. System errors go to the error dialog ([error-handling.md](../error-handling.md)).

## REST

`GET /api/authors/{id}` — `{id, name, realName, notes, labels: [{id, name, display}], locations: [{countryId, country, cityId, city, primary}], groups: [{id, name, as}]}`; `display` marks the label chosen as the display name, `countryId` of a city given alone is its country's; `groups[].name` is the group's main name, `as` is `null` for a membership under the main name; `added: {by, at}` is who added the person (their user name, `null` once the account is gone) and when; `404 author.not-found`, `400` for a malformed id.

`GET /api/authors/{id}/software` — `[{id, title, releaseDate, screenshots, madeBy, credits: [{as, roles}]}]`, the card fields of the software list; `as` is `null` for a credit by the person or under the main name; an unknown person gets `[]`.

`GET /api/authors/{id}/releases` — `[{id, softwareId, softwareTitle, title, version, type, releaseDate, screenshots, credits: [{as, roles}]}]`; `title` falls back to the software's title; `as` as for software; an unknown person gets `[]`.
