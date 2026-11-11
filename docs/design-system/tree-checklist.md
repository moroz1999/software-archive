# TreeChecklist

`TreeChecklist` — many items chosen from a tree, such as the categories of software.

- The chosen items stand on top as removable chips, "N selected" under the list; the order is the order of choosing.
- Every item is a checkbox under its parent, each level standing in from the one above; the items at the top stand apart. Checking a parent does not check its children.
- A filter finds items by name and keeps the parents that lead to them; "Nothing found." when nothing is, and chosen items stay chosen while hidden.
- The tree scrolls inside its own box so a long one keeps the form short.
- Controlled: `value` (ids) and `onChange(ids)`. `label` names the whole choice; `error` is shown under it and describes it for assistive technology; `disabled` locks it all.
- An item with children is a group named by the item, so assistive technology tells where an item stands.
- Theme: `app/styles/components/tree-checklist.css`.
