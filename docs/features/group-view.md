# Group View

The page of one group at `/groups/{id}` ([domain/authors.md](../domain/authors.md#group-page)); every entry of the group catalog leads here, and so do the groups on an author's page and on another group's page.

## Page

- The main name is the page heading.
- Under it: Name, then each when known: Types, Labels, Locations ("City, Country", the primary one first), Members, Member groups, Member of, Notes line by line.
- A member is the label they were a member under, leading to the person when known, with the person's main name muted next to it when it is another one; a label of nobody known is plain text.
- Member groups and Member of lead to the pages of those groups.

## Loading and errors

- The page preloads the group's details ([data-loading.md](../data-loading.md)): within the window it appears filled, otherwise with a skeleton.
- An unknown group, and an id that cannot be one, show the not found page; a malformed id is never sent. System errors go to the error dialog ([error-handling.md](../error-handling.md)).

## REST

`GET /api/groups/{id}` — `{id, name, groupName, notes, types: [code], labels: [{id, name}], locations: [{country, city, primary}], members: [{name, personId, personName}], memberGroups: [{id, name}], groups: [{id, name}]}`; `name` and the names of related groups are main names; `personId` is `null` for a label of nobody known, `personName` `null` when it is the member's label; `404 group.not-found`, `400` for a malformed id.
