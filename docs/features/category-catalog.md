# Category Catalog

The page of the tree of categories ([domain/categories.md](../domain/categories.md)).

## Page

| Page | Path | Content |
|---|---|---|
| Categories | `/categories` | "Add category" next to the page title; the tree, folded to its top |

- Every category is a link to its editing ([category-editing.md](category-editing.md)). A category with subcategories has a toggle before it ("Expand …" / "Collapse …") that unfolds them under it; the tree is the `Tree` of the design system.
- An empty tree says "No categories yet."

## Loading and errors

- The route preloads the whole tree, one call ([data-loading.md](../data-loading.md)): within 300 ms the tree appears at once, otherwise a skeleton of rows first.
- System errors go to the error dialog.

## REST

| Call | Answer |
|---|---|
| `GET /api/categories` | `200`, the categories at the top of the tree, each `{id, title, children}` with its subcategories in their order |

The tree is small (a few hundred categories), so every view that needs it loads it whole.
