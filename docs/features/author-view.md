# Author View

The page of one person at `/authors/{id}` ([domain/authors.md](../domain/authors.md#author-page)); every entry of the catalog leads here.

## Page

- The main name is the page heading.
- Under it, each when known: Real name, Labels, Locations ("City, Country", the primary one first), Groups, Notes line by line.
- A group leads to its page ([group-view.md](group-view.md)), with "as {label}" muted next to it when the person was a member under another label than the main name; a membership under two labels is two lines.

## Loading and errors

- The page preloads the author's details ([data-loading.md](../data-loading.md)): within the window it appears filled, otherwise with a skeleton.
- An unknown or nameless person, and an id that cannot be one, show the not found page; a malformed id is never sent. System errors go to the error dialog ([error-handling.md](../error-handling.md)).

## REST

`GET /api/authors/{id}` — `{id, name, realName, notes, labels: [{id, name}], locations: [{country, city, primary}], groups: [{id, name, as}]}`; `groups[].name` is the group's main name, `as` is `null` for a membership under the main name; `404 author.not-found`, `400` for a malformed id.
