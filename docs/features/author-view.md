# Author View

The page of one person at `/authors/{id}` ([domain/authors.md](../domain/authors.md#author-page)); every entry of the catalog leads here.

## Page

- The main name is the page heading.
- Under it, each when known: Real name, Aliases, Locations ("City, Country", the primary one first), Groups, Notes line by line.

## Loading and errors

- The page preloads the author's details ([data-loading.md](../data-loading.md)): within the window it appears filled, otherwise with a skeleton.
- An unknown or nameless person, and an id that cannot be one, show the not found page; a malformed id is never sent. System errors go to the error dialog ([error-handling.md](../error-handling.md)).

## REST

`GET /api/authors/{id}` — `{id, name, realName, notes, aliases: [{id, name}], locations: [{country, city, primary}], groups: [{id, name}]}`; `404 author.not-found`, `400` for a malformed id.
