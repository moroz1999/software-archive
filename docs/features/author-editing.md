# Author Editing

Adding, editing and deleting authors: people known by a real name, their labels or both ([domain/authors.md](../domain/authors.md)). Group membership is changed on the group's side and is not part of this form.

## Pages

| Page | Path | Content |
|---|---|---|
| Authors | `/authors` | "Add author" next to the page title ([author-catalog.md](author-catalog.md)); an author's page ([author-view.md](author-view.md)) has "Edit" in the menu right after the main name |
| Add author | `/authors/new` | Empty form; Cancel returns to the catalog, "Add author" submits and opens the author's page |
| Edit author | `/authors/{id}/edit` | "Delete" next to the page title; the form with the author's values, Cancel and "Save" back to the author's page; the catalog after deleting |

The edit page shows Authors › the main name, leading to the author's page, › "Editing". Adding pages have none.

## Form

- In one narrow column, in sections ([design-system/form.md](../design-system/form.md)): Main (Real name, at most 255 characters; Notes, multi-line), Names (Labels), Geography (Locations), and on editing Sources; Cancel and the submit button at its bottom right.
- Sources, on editing: for every import the entry came from, the source archive and the id there as text — the name it is known by there, when the source gives it no id ([domain/imports.md](../domain/imports.md#origin)) — the id leading to the page there when it has one; "None" when no import brought it. Not editable (`OriginList`).
- An author needs a real name or at least one label: without either, "Enter a real name or add a label." shows under Real name on submit.
- Each label has a name, a "Display name" radio and × to take it away. "Add label" adds an unmarked row. "No display name" clears the selection and keeps all labels. Removing the selected label also clears the selection. A name is required and can be at most 255 characters. Its error appears under its row. Renaming keeps the label with its credits and memberships.
- A label removed from the form is not deleted: it stays in the archive as a label of nobody, with its credits and memberships. Deleting a label is a separate action.
- Locations: one line per location: Country and City side by side (stacked on phones), "Primary" and ×; "Add location" adds an empty line. City offers the cities of the chosen country and is optional; another country forgets the city. A country is required, "Choose a country." under its row — except for a city of no known country ([domain/geo.md](../domain/geo.md)): its line shows "Country not specified" and the city, and is saved as it is, with no country (`country: null`); choosing a country forgets the city. The rules are `shared/lib/form-rules` (`locationProblem`, `toLocationDraft`, `toLocationValue`), the same for every form of locations. Exactly one location is primary, chosen by its "Primary" radio. One place counts once.
- Format rules are checked before sending; errors appear under their fields on submit and clear when the field is edited. Empty optional fields are sent as unknown (`null`).
- While saving, the fields and the buttons are locked.

## Deleting

"Delete" opens a confirmation naming the author and saying their labels stay in the archive as labels of nobody, with their credits; "Delete for good" deletes the person and their locations, Cancel or Escape closes the dialog. A refusal shows inside the dialog.

## Loading and errors

- The edit page preloads the author's details and the countries ([data-loading.md](../data-loading.md)); the add page preloads the countries. Within 300 ms the form appears filled, otherwise a form skeleton first. The cities of each chosen country load on their own.
- A label of somebody else is refused under Labels ("One of the labels belongs to someone else. Reload and try again."), a place that is not in the archive or a city of another country under Locations; a refusal of the whole form shows at its top.
- An unknown or nameless author, an id in the URL that cannot be one, and an author deleted in the meantime show the not found page ([error-handling.md](../error-handling.md)).
- System errors go to the error dialog; the form keeps what was typed.

## REST

| Call | Answer | Errors |
|---|---|---|
| `POST /api/authors` `{realName, notes, labels: [{id, name, display}], locations: [{country, city, primary}]}` | `201`, `Location: /api/authors/{id}`, `{id}` | `400 author.real-name-blank`, `400 author.real-name-too-long`, `400 title.required` / `400 title.too-long` for a label name, `400 location.empty`, `400` for malformed JSON or ids; `422 author.nameless` (field `realName`), `422 label.not-own`, `422 label.display-one` (field `labels`), `422 location.unknown`, `422 location.primary-one` (field `locations`) |
| `PUT /api/authors/{id}` (same body) | `204` | `404 author.not-found`; the errors of `POST` |
| `DELETE /api/authors/{id}` | `204` | `404 author.not-found` |

`labels` is the author's whole list and at most one label may have `display` set. A label without `id` is new, an `id` must be one of the author's own labels, and a label left out loses its owner. `locations` is the whole list as well, exactly one `primary` when not empty: `country` and `city` are ids, a city alone is placed in its country, a city of no known country in none; such a city given with a country is refused with `location.unknown`. Missing lists mean none; `realName` and `notes` are `null` when unknown.

If several labels are marked, the first marked label by name is used. If several locations or none are marked, the first location (the first marked one, if any) is primary on the page, in the form and when the author is saved.
