# Label View

The page of a label at `/labels/{id}` ([domain/authors.md](../domain/authors.md#label-page)); a label of nobody's entry in the author catalog, its credits on software and its memberships on group pages lead here, and so do the labels on the pages of people and groups.

## Page

- Over the heading, the breadcrumbs: Authors, leading to the catalog, then the label.
- The label's name is the page heading; under it "Whose label": a person by the real name with the main name muted next to it (by the main name without a real name), a group by its name, leading to their page, or "Not known yet whose label this is" for a label of nobody; then Locations ("City, Country", the primary one first); then Member of: the groups it was a member of, each leading to its page ([group-view.md](group-view.md)).
- Software below: the software it is credited on, then the releases credited under it, as on an author's page ([author-view.md](author-view.md)).
- Last: who added the label and on which day ("Added by Zork on 4 March 2026"), only the day once their account is gone ([domain/users.md](../domain/users.md#who-added-an-entry)).

## Editing

- What can be done with the label is in the menu right after the heading ([PopoverMenu](../design-system/popover-menu.md)), each item only for whoever may do it; without any, there is no menu. An item that asks something opens its dialog, and the menu closes.
- "Edit" in the menu, for whoever added the label and administrators, opens its editing ([label-editing.md](label-editing.md)).

## Attaching to the owner

- "Say whose label" in the menu, for administrators only, opens a dialog: "Whose label is “{name}”?", "Person or group" searching people and groups by name, and "Display name", unchecked, which is not offered once a group is chosen; "Save" is locked until an owner is chosen. The wording never calls anyone an owner: it asks whose the label is.
- Attaching takes in the owner's own credits: the person or the group tells the label is theirs now (`LabelAdoptedByAuthor`, `LabelAdoptedByGroup`), and the software module joins, in the same transaction, the owner's credit on any software or release that credits the label too into the label's credit, with the roles of both. Making a label of a group does the same. Replacing and merging a label move its release credits as they move its software credits.
- After attaching, the page of the owner opens; the owner's names, the catalogs and the lists of software read again.
- Refusals show inside the dialog: the label got an owner meanwhile, the owner is gone, a person without labels and the label not marked as the display name, a missing sign-in, no right to change the owner. A label deleted meanwhile shows the not found page; system errors go to the error dialog ([error-handling.md](../error-handling.md)).

## Making a person or a group

- "Make author" and "Make group" in the menu, for whoever added the label and administrators; on a label with an owner, only when they may change that owner too.
- "Make author" asks "Make a person known as “{name}”?"; "Make group" asks "Make a group named “{name}”?" with the group types to choose, its button locked until one is chosen. The name is the label's, not editable. A label with an owner adds "It will no longer be a label of {owner}."
- Making a person replaces the label: the person gets its name, locations, external origin, software credits and group memberships directly, and the label no longer exists. The new person tells the label is replaced (`LabelReplacedByAuthor`); the software, group and importer modules move the credits, memberships and origin in answer, in the same transaction, then the label's repository deletes it with its locations. Making a group replaces the label the same way (`LabelReplacedByGroup`): the group gets its name, locations, external origin and software credits directly; the label's memberships become group memberships (`group_member_group`), their roles dropped; the label is deleted. The group is changed last (`GroupChanged`), once the credits are its own, so the search reads the software again with the group.
- After making, the page of the new person or group opens; the old owner, the catalogs and the lists of software read again.
- Refusals show inside the dialog: the label being the display name of a person with other labels, or the only name of a person without a real name; a type gone from the archive; no right to change the owner; a missing sign-in. System errors go to the error dialog.

## Handing over

- "Make a label of a group", "Merge into group and delete", "Merge into author and delete" and "Merge into label and delete" in the menu, for administrators, on a label of nobody or of a group. Each opens a dialog of its name with one field, its button locked until something is chosen: "Group" searching groups only, "Author" people only, "Label" every label but this one (labels of nobody, of people and of groups). "Make a label" tells "“{name}” becomes another name of the group chosen; its credits and memberships stay under this name."; each "Merge and delete" tells "Its credits, memberships, locations and sources go to the group / the person / the label chosen, and “{name}” is deleted."
- After it, the page of the group, the person or the other label opens; the catalogs and the lists of software read again.
- Refusals show inside the dialog: whoever was chosen gone, the label of a person, a label merged into itself, no right to do it. System errors go to the error dialog.
- Making a label of a group releases it from another group that has it and the group adopts it, as attaching does; nothing changes for a label already of the group.
- Merging releases the label from a group that has it first. The one taking it in tells so — the group `LabelReplacedByGroup`, the person `LabelReplacedByAuthor` (as when either is made of it), the other label `LabelMergedIntoLabel` — and its locations join theirs; the software, group and importer modules move the credits, memberships and origin in the same transaction, then the label is deleted. The moves keep one row where the one taking it in already has one: one credit with the joined roles, one membership with the joined roles (one member group, and none of the group in itself, for a group). The one taking it in is changed last (`GroupChanged`, `AuthorChanged`, `LabelChanged`), once the credits are its own, so the search reads the software again.

## Loading and errors

- The page preloads the label's details ([data-loading.md](../data-loading.md)): within the window it appears filled, otherwise with a skeleton. The software is asked for once its block comes into view.
- An unknown label, and an id that cannot be one, show the not found page; a malformed id is never sent.

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/labels/{id}` | `{id, name, owner: {type, id, name, note} \| null, groups: [{id, name}], canChange, canMakeOwner}`; `type` `author` or `group`; `owner.name` the person's real name, else their main name, or the group's name; `owner.note` the person's main name next to a real name, else `null`; group names are main names; `canChange` for an administrator on a label of nobody; `canMakeOwner` for whoever added the label and administrators, on a label with an owner only when they may change that owner; `canHandOver` for administrators on a label that is not a person's; the fields of editing ([label-editing.md](label-editing.md#rest)); `added: {by, at}` as for authors ([author-view.md](author-view.md#rest)) | `404 label.not-found`; `400` for a malformed id |
| `GET /api/labels/{id}/software` | as `GET /api/authors/{id}/software`, every credit by the label itself | `400` for a malformed id |
| `GET /api/labels/{id}/releases` | as `GET /api/authors/{id}/releases`, every credit by the label itself | `400` for a malformed id |
| `POST /api/labels/{id}/owner` `{type, id, display}` | `204` | `404 label.not-found`; `409 label.owned`; `422 label.owner-unknown` (field `owner`), `422 label.display-one` (a person only; `display` is ignored for a group); `401`, `403`; `400` for another `type` or a missing `id` |
| `POST /api/labels/{id}/author` | `201 {id}` of the new person | `404 label.not-found`; `422 label.display-one`, `422 author.nameless` (taking it from a person); `401`, `403`; `400` for a malformed id |
| `POST /api/labels/{id}/group` `{types}` | `201 {id}` of the new group | as `/author`; `422 group.type-unknown` |
| `POST /api/labels/{id}/attach-to-group` `{group}` | `204`; the label is the group's | as `/merge-into-group` |
| `POST /api/labels/{id}/merge-into-group` `{group}` | `204`; the label is deleted | `404 label.not-found`; `422 label.of-person`, `422 label.owner-unknown` (field `group`); `401`, `403` for anyone but an administrator; `400 label.group-required` without `group` |
| `POST /api/labels/{id}/merge-into-author` `{author}` | `204`; the label is deleted | as `/merge-into-group`, `422 label.owner-unknown` with field `author`; `400 label.author-required` without `author` |
| `POST /api/labels/{id}/merge-into-label` `{label}` | `204`; the label is deleted | as `/merge-into-group`, `422 label.owner-unknown` with field `label`; `422 label.merge-into-itself` (field `label`); `400 label.label-required` without `label` |
