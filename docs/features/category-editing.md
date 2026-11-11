# Category Editing

Adding, editing and deleting categories of the tree ([domain/categories.md](../domain/categories.md)).

## Pages

| Page | Path | Content |
|---|---|---|
| Add category | `/categories/new` | Empty form; Cancel and "Add category" both return to the tree ([category-catalog.md](category-catalog.md)) |
| Edit category | `/categories/{id}/edit` | "Delete" next to the page title; the form with the category's values, Cancel and "Save" back to the tree; the tree after deleting |

## Form

- Title (required, at most 255 characters) and Parent, in one narrow column; Cancel and the submit button at its bottom right — the layout of the other forms.
- Parent is a choice of every category in the order of the tree, each level indented, and "None, at the top"; the category being edited and its subcategories are not offered.
- Format rules are checked before sending; the error appears under Title on submit and clears when it is edited. While saving, the fields and the buttons are locked.

## Deleting

"Delete" opens a confirmation naming the category and saying its subcategories go with it while their software stays in the archive; "Delete for good" deletes it, Cancel or Escape closes the dialog. A refusal shows inside the dialog.

## Loading and errors

- The edit page preloads the category and the tree ([data-loading.md](../data-loading.md)); the add page preloads the tree. Within 300 ms the form appears filled, otherwise a form skeleton first.
- A parent that is no longer in the archive is refused under Parent ("The parent category is not in the archive any more. Reload and try again."), and so is a parent inside the category ("A category cannot go under itself or its own subcategory."); a refusal of the whole form shows at its top.
- An unknown category, an id in the URL that cannot be one, and a category deleted in the meantime show the not found page ([error-handling.md](../error-handling.md)).
- System errors go to the error dialog; the form keeps what was typed.

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/categories/{id}` | `200`, `{id, title, parentId}`; `parentId` is `null` at the top | `404 category.not-found` |
| `POST /api/categories` `{title, parentId}` | `201`, `Location: /api/categories/{id}`, `{id}` | `400 title.required` / `400 title.too-long`, `400` for malformed JSON or ids; `422 category.parent-unknown` (field `parentId`) |
| `PUT /api/categories/{id}` (same body) | `204` | `404 category.not-found`; the errors of `POST`; `422 category.parent-inside` (field `parentId`); `409 conflict.concurrent` when it lost to a change of the software it recategorizes |
| `DELETE /api/categories/{id}` | `204` | `404 category.not-found` |

## Storage

- The tree of zxart is part of the schema migration that brings the categories: it is reference data of the archive, not fixtures. Its ids are derived from the zxart ids by UUID v5 under a fixed namespace; the zxart ids themselves are not kept.
- The order of subcategories is a position among the children of the same parent. The repository keeps the position of a category saved under the same parent and puts it after the last child when it lands under another one.
- Subcategories and links to software go with a deleted category by `ON DELETE CASCADE` in the schema; links go with deleted software the same way.
- A change checks the parent against its line up to the top of the tree and locks that line until it commits, besides the row of the category itself. Two moves running at once therefore cannot put two categories under each other: the second waits and then sees the first.
- A move tells the software it touches, in its own transaction: software in the moved category or below it and also in a category now above it is loaded, keeps only the deepest category of each branch, and is stored ([domain/categories.md](../domain/categories.md)). The software is locked in the order of its ids. A change of that software running at the same time locks it the other way round; the database then gives up one of the two, which answers `409 conflict.concurrent`.
