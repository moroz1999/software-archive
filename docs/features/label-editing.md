# Label Editing

Editing and deleting a label on a page of its own: its name, whose it is, its locations, and where an import took it from ([domain/authors.md](../domain/authors.md#editing-a-label)).

## Pages

| Page | Path | Content |
|---|---|---|
| Label | `/labels/{id}` | "Edit" next to the heading, for whoever may edit the label ([label-view.md](label-view.md)) |
| Edit label | `/labels/{id}/edit` | "Delete" next to the page title; the form with the label's values, Cancel and "Save" back to the label's page; the author catalog after deleting |

## Form

- In one narrow column, in sections, the same layout as the author form ([author-editing.md](author-editing.md)): Main (Name, required, at most 255 characters), Whose label, Geography (Locations), Sources; Cancel and "Save" at its bottom right.
- Whose label: for administrators, "Person or group" searching people and groups by name, filled with the current owner; emptying it leaves the label to nobody. "Display name" is offered once another person is chosen; a group has none, and the display name among the labels a person already has is chosen in the person's form. Anybody else sees whose the label is, or "Not known yet whose label this is", without changing it.
- Locations: the editor of the author form — Country and City, "Primary" and ×; exactly one location is primary.
- Sources: for every import the label came from, the source archive and the id there, the id leading to the source page when the source has one; "None" when it was not imported. Not editable.
- Format rules are checked before sending; errors appear under their fields on submit and clear when the field is edited.
- While saving, the fields and the buttons are locked.

## Deleting

"Delete" opens a confirmation naming the label and saying its credits and memberships go with it; "Delete for good" deletes the label, Cancel or Escape closes the dialog. A refusal shows inside the dialog.

## Loading and errors

- The page preloads the label's details and the countries ([data-loading.md](../data-loading.md)). Within 300 ms the form appears filled, otherwise a form skeleton first.
- An owner not in the archive, the label being the display name of a person with other labels, or the only name of a person without a real name, are refused under Whose label; a place that is not in the archive under Locations; a missing sign-in and no right to change the label at the top of the form. The same refusals of deleting show inside its dialog.
- An unknown label, an id in the URL that cannot be one, and a label deleted in the meantime show the not found page ([error-handling.md](../error-handling.md)).
- System errors go to the error dialog; the form keeps what was typed.

## Backend

- A label remembers who added it (`added_by`, `V24`): the user who typed it in the form of a person or a group, the importer for an imported one. Labels from before were given the importer when an import remembers them, else whoever added their owner.
- The label aggregate holds the name, the locations and the adder of any label, of nobody or not; whose the label is stays with its owner's aggregate, which only attaches and releases it. A change of the owner is a release by the old owner and an adoption by the new one, in the same transaction.
- Locations of a label are rows of `entity_location` of the type `LABEL`, written by the label's repository only: the owners' mappings never touch them.
- Deleting removes the label row: its credits and memberships go with it by the foreign keys; the repository deletes its locations, and the importer forgets its origin. The search documents it reached are enqueued before the rows go.

## Frontend

- `pages/label/label-edit` — the page; its loader preloads the label's details and the countries. Saving returns to the label's page, deleting opens the author catalog.
- `widgets/label-editor` — the page title with Delete, the form skeleton while the label loads, the form with Cancel.
- `features/label/label-edit` — `EditLabelProvider` holds the form and saves it, `EditLabelForm` shows it; a refusal naming `owner` or `locations` shows under it until edited, the others at the top of the form.
- `features/label/delete` — `DeleteLabelButton`, the button and its confirmation.
- The label pages are grouped under `pages/label`.

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/labels/{id}` | as on the page ([label-view.md](label-view.md#rest)), with `locations: [{countryId, country, cityId, city, primary}]` as for authors, `origins: [{origin, externalId, url}]` (`url` `null` without a source page), `canEdit` for whoever added the label and administrators, `canChangeOwner` for administrators | as on the page |
| `PUT /api/labels/{id}` `{name, owner: {type, id, display} \| null, locations: [{country, city, primary}]}` | `204` | `404 label.not-found`; `400 title.required` / `400 title.too-long`, `400 location.empty`, `400` for malformed JSON, ids or another `type`; `422 label.owner-unknown`, `422 label.display-one`, `422 author.nameless` (field `owner`); `422 location.unknown`, `422 location.primary-one` (field `locations`); `401`, `403` |
| `DELETE /api/labels/{id}` | `204` | `404 label.not-found`; `422 label.display-one`, `422 author.nameless`; `401`, `403` |

`owner` is whose the label is after saving: the same owner as now changes nothing, and its `display` is ignored; another owner, or `null` for a label that has one, is a change only administrators make (`403` for anybody else). `locations` is the whole list, exactly one `primary` when not empty.
